"""Authentication router for user registration and management."""

import html
from typing import Any, Optional
from urllib.parse import urlencode

from fastapi import APIRouter, Depends, HTTPException, Query, Request, status
from fastapi.responses import HTMLResponse, JSONResponse, RedirectResponse, Response
from sqlalchemy.ext.asyncio import AsyncSession

from sqlalchemy import select

from app.config import settings
from app.database import get_db
from app.dependencies.auth import get_current_user_claims, CurrentUserClaims
from app.models.student import Student
from app.models.teacher import Teacher
from app.schemas.auth import (
    UserRegistrationRequest,
    UserRegistrationResponse,
    UserLoginRequest,
    UserLoginResponse,
    TokenRefreshRequest,
    LogoutRequest,
    MeResponse,
    CompleteProfileRequest,
    MeUpdateRequest,
)
from app.schemas.enrollment import EnrollmentCreate
from app.schemas.student import StudentCreate, StudentResponse, StudentUpdate
from app.schemas.teacher import TeacherCreate, TeacherResponse, TeacherUpdate
from app.services.course_service import CourseService
from app.services.enrollment_service import EnrollmentService
from app.services.student_service import StudentService
from app.services.teacher_service import TeacherService
from app.services.user_registration_service import UserRegistrationService
from app.services.cognito_service import CognitoService

router = APIRouter(
    prefix="/auth",
    tags=["Authentication"]
)

# OAuth callback URL (must match Cognito app client redirect URIs)


def _oauth_callback_uri() -> str:
    return settings.oauth_callback_uri.strip().rstrip("/")


def _frontend_callback_url() -> Optional[str]:
    """Frontend base URL for OAuth callback redirect (custom auth UI). None if not configured."""
    url = (settings.frontend_url or "").strip().rstrip("/")
    return url if url else None


@router.get(
    "/oauth/url",
    summary="Get Cognito Hosted UI login URL (e.g. Sign in with Google)",
    description="Returns the URL to redirect the user to for Cognito Hosted UI sign-in (e.g. Google).",
)
async def get_oauth_login_url(
    state: Optional[str] = Query(
        None, description="Optional state for CSRF protection"),
    identity_provider: Optional[str] = Query(
        "Google", description="Cognito IdP name (e.g. Google)"),
) -> Any:
    """Return the Cognito Hosted UI authorize URL. Frontend redirects the user to this URL."""
    cognito = CognitoService()
    redirect_uri = _oauth_callback_uri()
    url = cognito.get_authorization_url(
        redirect_uri=redirect_uri,
        state=state,
        identity_provider=identity_provider,
    )
    return {"url": url, "redirect_uri": redirect_uri}


@router.get(
    "/oauth/redirect",
    summary="Redirect to Cognito Hosted UI (Sign in with Google)",
    description="302 redirect to Cognito Hosted UI. Use from a login page link/button.",
)
async def oauth_redirect(
    state: Optional[str] = Query(None, description="Optional state for CSRF"),
    identity_provider: Optional[str] = Query(
        "Google", description="IdP name in Cognito"),
) -> RedirectResponse:
    """Redirect the user to Cognito Hosted UI (e.g. Google sign-in)."""
    cognito = CognitoService()
    redirect_uri = _oauth_callback_uri()
    url = cognito.get_authorization_url(
        redirect_uri=redirect_uri,
        state=state,
        identity_provider=identity_provider,
    )
    return RedirectResponse(url=url, status_code=302)


@router.get(
    "/callback",
    summary="OAuth callback (Cognito Hosted UI / Google)",
    description="Exchanges the authorization code for tokens. Returns HTML in browser, JSON for API.",
)
async def oauth_callback(
    request: Request,
    code: Optional[str] = Query(
        None, description="Authorization code from Cognito"),
    state: Optional[str] = Query(
        None, description="State passed to authorize URL"),
) -> Response:
    """Exchange authorization code for tokens, or return error when Cognito redirects with error=."""
    frontend_base = _frontend_callback_url()

    if not code:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Missing code; Cognito may have redirected with an error. Check error and error_description.",
        )
    cognito = CognitoService()
    redirect_uri = _oauth_callback_uri()
    auth_result = await cognito.exchange_code_for_tokens(code=code, redirect_uri=redirect_uri)
    access_token = auth_result["access_token"]
    user_info = auth_result.get("user_info") or {}

    if frontend_base:
        fragment_params = {
            "access_token": access_token,
            "token_type": auth_result.get("token_type", "Bearer"),
            "expires_in": str(auth_result.get("expires_in", 3600)),
        }
        if auth_result.get("refresh_token"):
            fragment_params["refresh_token"] = auth_result["refresh_token"]
        fragment = urlencode(fragment_params)
        return RedirectResponse(
            url=f"{frontend_base}/auth/callback#{fragment}",
            status_code=302,
        )

    resp = UserLoginResponse(
        access_token=access_token,
        token_type=auth_result["token_type"],
        expires_in=auth_result["expires_in"],
        refresh_token=auth_result.get("refresh_token"),
        id_token=auth_result.get("id_token"),
        user=user_info,
    )
    return JSONResponse(content=resp.model_dump(mode="json"))


@router.get(
    "/me",
    response_model=MeResponse,
    summary="Current user profile (from JWT)",
    description="Returns the DB profile (student or teacher) linked to the Cognito identity, or null if not yet completed.",
)
async def me(
    claims: CurrentUserClaims = Depends(get_current_user_claims),
    db: AsyncSession = Depends(get_db),
) -> MeResponse:
    """Return the current user's DB profile if linked (by cognito_user_id), or admin role, else profile=null."""
    sub = claims.sub
    email = (claims.email or "").strip().lower()
    admin_emails = [e.strip().lower() for e in (
        settings.admin_emails or "").split(",") if e.strip()]
    admin_ids = [s.strip() for s in (
        settings.admin_cognito_ids or "").split(",") if s.strip()]
    if admin_ids and sub in admin_ids:
        return MeResponse(user_type="admin", profile=None)
    if admin_emails and email in admin_emails:
        return MeResponse(user_type="admin", profile=None)
    # Prefer student first; a user could in theory be in both, but we link one cognito user to one role
    result = await db.execute(select(Student).where(Student.cognito_user_id == sub))
    student = result.scalar_one_or_none()
    if student:
        profile = StudentResponse.model_validate(
            student).model_dump(mode="json")
        return MeResponse(user_type="students", profile=profile)
    result = await db.execute(select(Teacher).where(Teacher.cognito_user_id == sub))
    teacher = result.scalar_one_or_none()
    if teacher:
        profile = TeacherResponse.model_validate(
            teacher).model_dump(mode="json")
        return MeResponse(user_type="teachers", profile=profile)
    return MeResponse(user_type=None, profile=None)


@router.patch(
    "/me",
    response_model=MeResponse,
    summary="Update current user profile (personal info)",
    description="Update the linked student or teacher profile with optional first_name, last_name.",
)
async def update_me(
    body: MeUpdateRequest,
    claims: CurrentUserClaims = Depends(get_current_user_claims),
    db: AsyncSession = Depends(get_db),
) -> MeResponse:
    """Update the current user's student or teacher record with provided fields."""
    sub = claims.sub
    result = await db.execute(select(Student).where(Student.cognito_user_id == sub))
    student = result.scalar_one_or_none()
    if student:
        data = body.model_dump(exclude_unset=True, by_alias=False)
        if not data:
            profile = StudentResponse.model_validate(
                student).model_dump(mode="json")
            return MeResponse(user_type="students", profile=profile)
        student_service = StudentService(db)
        update_data = StudentUpdate(**data)
        updated = await student_service.update_student(student.id, update_data)
        profile = StudentResponse.model_validate(
            updated).model_dump(mode="json")
        return MeResponse(user_type="students", profile=profile)
    result = await db.execute(select(Teacher).where(Teacher.cognito_user_id == sub))
    teacher = result.scalar_one_or_none()
    if teacher:
        data = body.model_dump(exclude_unset=True, by_alias=False)
        if not data:
            profile = TeacherResponse.model_validate(
                teacher).model_dump(mode="json")
            return MeResponse(user_type="teachers", profile=profile)
        teacher_service = TeacherService(db)
        update_data = TeacherUpdate(**data)
        updated = await teacher_service.update_teacher(teacher.id, update_data)
        profile = TeacherResponse.model_validate(
            updated).model_dump(mode="json")
        return MeResponse(user_type="teachers", profile=profile)
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail="No profile linked; complete profile first",
    )


@router.post(
    "/complete-profile",
    response_model=MeResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Complete profile (OAuth users)",
    description="Create student or teacher record for the current Cognito user (e.g. after Google sign-in). Idempotent: returns existing if already linked.",
)
async def complete_profile(
    body: CompleteProfileRequest,
    claims: CurrentUserClaims = Depends(get_current_user_claims),
    db: AsyncSession = Depends(get_db),
) -> MeResponse:
    """Create a student or teacher record linked to the current JWT identity (cognito_user_id)."""
    sub = claims.sub
    # Idempotent: already have a profile?
    result = await db.execute(select(Student).where(Student.cognito_user_id == sub))
    student = result.scalar_one_or_none()
    if student:
        profile = StudentResponse.model_validate(
            student).model_dump(mode="json")
        return JSONResponse(
            content=MeResponse(user_type="students",
                               profile=profile).model_dump(mode="json"),
            status_code=200,
        )
    result = await db.execute(select(Teacher).where(Teacher.cognito_user_id == sub))
    teacher = result.scalar_one_or_none()
    if teacher:
        profile = TeacherResponse.model_validate(
            teacher).model_dump(mode="json")
        return JSONResponse(
            content=MeResponse(user_type="teachers",
                               profile=profile).model_dump(mode="json"),
            status_code=200,
        )

    # Need email from token (ID token has it; access token may not)
    email = (claims.email or "").strip()
    if not email:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email is required to complete profile; use the ID token (not only access token) when calling this endpoint.",
        )
    first_name = (claims.given_name or "User").strip() or "User"
    last_name = (claims.family_name or "").strip()
    # Stable unique username for OAuth users (Cognito sub)
    username = sub

    if body.user_type == "students":
        school_id = body.school_id
        join_code = (body.join_code or "").strip() if body.join_code else None
        if join_code:
            course_service = CourseService(db)
            course = await course_service.get_course_by_join_code(join_code)
            if not course:
                raise HTTPException(
                    status_code=status.HTTP_404_NOT_FOUND,
                    detail="Invalid join code or course not found",
                )
            if not getattr(course, "is_active", True):
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Course is not active",
                )
            school_id = course.school_id
        if not school_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Students must provide either joinCode or schoolId",
            )
        student_service = StudentService(db)
        student_data = StudentCreate(
            school_id=school_id,
            first_name=first_name,
            last_name=last_name,
            email=email,
            username=username,
            cognito_user_id=sub,
        )
        created = await student_service.create_student(student_data)
        if join_code:
            enrollment_service = EnrollmentService(db)
            try:
                await enrollment_service.enroll_with_join_code(
                    student_id=created.id,
                    enrollment_data=EnrollmentCreate(join_code=join_code),
                )
            except ValueError as e:
                if "already enrolled" in str(e).lower():
                    pass
                else:
                    raise HTTPException(
                        status_code=status.HTTP_400_BAD_REQUEST,
                        detail=str(e),
                    )
        profile = created.model_dump(mode="json")
        return MeResponse(user_type="students", profile=profile)
    else:
        teacher_service = TeacherService(db)
        teacher_data = TeacherCreate(
            school_id=body.school_id,
            first_name=first_name,
            last_name=last_name,
            email=email,
            username=username,
            cognito_user_id=sub,
        )
        created = await teacher_service.create_teacher(teacher_data)
        profile = TeacherResponse.model_validate(
            created).model_dump(mode="json")
        return MeResponse(user_type="teachers", profile=profile)


@router.post(
    "/register",
    response_model=UserRegistrationResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Register a new user",
    description="Register a new user in both AWS Cognito and the database"
)
async def register_user(
    request: UserRegistrationRequest,
    db: AsyncSession = Depends(get_db)
) -> Any:
    """
    Register a new user in both AWS Cognito and the database.

    This endpoint creates a user account in AWS Cognito for authentication
    and creates the corresponding user record in the database.

    Args:
        request: User registration data including credentials and profile info

    Returns:
        UserRegistrationResponse: The created user information

    Raises:
        HTTPException: If registration fails
    """
    try:
        registration_service = UserRegistrationService(db)
        return await registration_service.register_user(request)

    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Registration failed: {str(e)}"
        )


@router.post(
    "/login",
    response_model=UserLoginResponse,
    summary="User login",
    description="Authenticate user with username/email and password"
)
async def login(
    request: UserLoginRequest,
    db: AsyncSession = Depends(get_db)
) -> Any:
    """
    Authenticate a user and return access tokens.

    Args:
        request: Login credentials
        db: Database session dependency

    Returns:
        UserLoginResponse: Access tokens and user information

    Raises:
        HTTPException: If authentication fails
    """
    try:
        cognito_service = CognitoService()
        auth_result = await cognito_service.authenticate_user(
            username=request.username,
            password=request.password
        )

        return UserLoginResponse(
            access_token=auth_result['access_token'],
            token_type=auth_result['token_type'],
            expires_in=auth_result['expires_in'],
            refresh_token=auth_result.get('refresh_token'),
            user=auth_result['user_info']
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Login failed: {str(e)}"
        )


@router.post(
    "/refresh",
    response_model=UserLoginResponse,
    summary="Refresh access token",
    description="Get a new access token using a refresh token"
)
async def refresh_token(
    request: TokenRefreshRequest
) -> Any:
    """
    Refresh an access token using a refresh token.

    Args:
        request: Refresh token request

    Returns:
        UserLoginResponse: New access token

    Raises:
        HTTPException: If token refresh fails
    """
    try:
        cognito_service = CognitoService()
        auth_result = await cognito_service.refresh_access_token(
            refresh_token=request.refresh_token
        )

        return UserLoginResponse(
            access_token=auth_result['access_token'],
            token_type=auth_result['token_type'],
            expires_in=auth_result['expires_in'],
            refresh_token=None,  # Refresh tokens are not returned on refresh
            user={}  # User info not available during refresh
        )

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Token refresh failed: {str(e)}"
        )


@router.post(
    "/logout",
    summary="User logout",
    description="Logout user and invalidate access token"
)
async def logout(
    request: LogoutRequest
) -> Any:
    """
    Logout a user by invalidating their access token.

    Args:
        request: Logout request with access token

    Returns:
        Success message

    Raises:
        HTTPException: If logout fails
    """
    try:
        cognito_service = CognitoService()
        await cognito_service.logout_user(request.access_token)

        return {"message": "Successfully logged out"}

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Logout failed: {str(e)}"
        )
