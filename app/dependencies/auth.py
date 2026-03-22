"""
JWT validation for Cognito-issued tokens (access or ID token).
Exposes get_current_user_claims for protected routes.
"""

from typing import Any, Dict, Optional

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel

from app.config import settings


# Bearer token extraction
security = HTTPBearer(auto_error=False)


class CurrentUserClaims(BaseModel):
    """Verified claims from Cognito JWT (sub always present; rest from token)."""

    sub: str
    email: Optional[str] = None
    given_name: Optional[str] = None
    family_name: Optional[str] = None
    token_use: Optional[str] = None  # "id" | "access"

    class Config:
        extra = "ignore"


def _get_jwks_url() -> str:
    """Cognito JWKS URL for the user pool."""
    region = settings.aws_region
    pool_id = settings.cognito_user_pool_id
    if not pool_id:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Cognito is not configured (COGNITO_USER_POOL_ID)",
        )
    return (
        f"https://cognito-idp.{region}.amazonaws.com/{pool_id}/.well-known/jwks.json"
    )


def _verify_cognito_token(token: str) -> Dict[str, Any]:
    """Verify Cognito JWT (access or ID) and return payload."""
    jwks_url = _get_jwks_url()
    try:
        jwks_client = jwt.PyJWKClient(jwks_url)
        signing_key = jwks_client.get_signing_key_from_jwt(token)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token or key error",
        ) from e

    issuer = (
        f"https://cognito-idp.{settings.aws_region}.amazonaws.com/"
        f"{settings.cognito_user_pool_id}"
    )
    options = {
        "verify_signature": True,
        "verify_exp": True,
        "verify_iss": True,
        "verify_aud": False,
        "require": ["exp", "iss", "sub"],
    }
    decode_kwargs = {
        "algorithms": ["RS256"],
        "issuer": issuer,
        "options": {
            **options,
            "verify_aud": False,
        },
    }
    try:
        payload = jwt.decode(
            token,
            signing_key.key,
            **decode_kwargs,
        )
    except jwt.ExpiredSignatureError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token has expired",
        )
    except jwt.InvalidTokenError as e:
        detail = "Invalid token"
        if getattr(settings, "debug", False):
            detail = f"Invalid token: {type(e).__name__}: {str(e)}"
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=detail,
        ) from e

    # ID token: aud must match app client. Access token: client_id must match (no aud).
    if settings.cognito_client_id:
        client_id = settings.cognito_client_id
        token_use = payload.get("token_use")
        if token_use == "access":
            if payload.get("client_id") != client_id:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Invalid access token client_id",
                )
        elif token_use == "id":
            aud = payload.get("aud")
            if isinstance(aud, list):
                ok = client_id in aud
            else:
                ok = aud == client_id
            if not ok:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Invalid ID token audience",
                )
        else:
            aud = payload.get("aud")
            if aud is not None:
                if isinstance(aud, list):
                    aud_ok = client_id in aud
                else:
                    aud_ok = aud == client_id
                if not aud_ok:
                    raise HTTPException(
                        status_code=status.HTTP_401_UNAUTHORIZED,
                        detail="Invalid token audience",
                    )
            elif payload.get("client_id") is not None:
                if payload.get("client_id") != client_id:
                    raise HTTPException(
                        status_code=status.HTTP_401_UNAUTHORIZED,
                        detail="Invalid token client_id",
                    )
            else:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Token missing token_use / aud / client_id",
                )

    return payload


def claims_is_platform_admin(claims: CurrentUserClaims) -> bool:
    """True if Cognito sub or email is configured as platform admin."""
    email = (claims.email or "").strip().lower()
    admin_emails = [
        e.strip().lower()
        for e in (settings.admin_emails or "").split(",")
        if e.strip()
    ]
    admin_ids = [
        s.strip()
        for s in (settings.admin_cognito_ids or "").split(",")
        if s.strip()
    ]
    if admin_ids and claims.sub in admin_ids:
        return True
    if admin_emails and email in admin_emails:
        return True
    return False


async def get_current_user_claims(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(security),
) -> CurrentUserClaims:
    """
    Validate Bearer token (Cognito access or ID token) and return claims.
    Use as a dependency on routes that require authentication.
    """
    if not credentials or credentials.credentials is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing or invalid authorization header",
            headers={"WWW-Authenticate": "Bearer"},
        )
    token = credentials.credentials
    payload = _verify_cognito_token(token)
    sub = payload.get("sub")
    if not sub:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token missing sub",
        )
    return CurrentUserClaims(
        sub=sub,
        email=payload.get("email"),
        given_name=payload.get("given_name"),
        family_name=payload.get("family_name"),
        token_use=payload.get("token_use"),
    )


async def get_optional_current_user_claims(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(security),
) -> Optional[CurrentUserClaims]:
    """
    Return verified JWT claims when a Bearer token is present; else None.
    Invalid or expired tokens still raise 401.
    """
    if not credentials or credentials.credentials is None:
        return None
    token = credentials.credentials
    payload = _verify_cognito_token(token)
    sub = payload.get("sub")
    if not sub:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token missing sub",
        )
    return CurrentUserClaims(
        sub=sub,
        email=payload.get("email"),
        given_name=payload.get("given_name"),
        family_name=payload.get("family_name"),
        token_use=payload.get("token_use"),
    )


async def get_current_admin_claims(
    claims: CurrentUserClaims = Depends(get_current_user_claims),
) -> CurrentUserClaims:
    """
    Require that the current user is an admin (by admin_emails or admin_cognito_ids).
    Use as a dependency on admin-only routes.
    """
    if claims_is_platform_admin(claims):
        return claims
    raise HTTPException(
        status_code=status.HTTP_403_FORBIDDEN,
        detail="Admin access required",
    )
