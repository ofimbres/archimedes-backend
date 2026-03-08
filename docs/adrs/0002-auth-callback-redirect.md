## ADR 0002 – OAuth callback redirects to frontend with tokens in fragment

- **Status**: Accepted  
- **Date**: 2026-03-02  
- **Owner**: backend-team  

---

## Context

We use AWS Cognito for authentication (Google OAuth and optionally email/password).  
Originally, Cognito redirected back to the backend, which exchanged the authorization code for tokens and then returned **HTML or JSON** directly from the backend domain.

We want a **fully custom auth UI** in the React frontend so users never land on Cognito’s Hosted UI or a backend-only page. The frontend already has an `/auth/callback` route that can parse tokens from the URL.

Constraints:

- Keep the backend as the **OAuth redirect_uri** for Cognito (simpler and works for non-browser clients).  
- Avoid exposing tokens in query params sent to servers.  
- Keep the flow compatible with SPAs and the existing frontend routing.

---

## Decision

After exchanging the OAuth code for tokens, the backend **redirects (302) the browser to the frontend**, putting tokens in the URL **fragment**, not in the query string.

- Cognito redirect_uri remains the backend callback:  
  - `GET /api/v1/auth/callback?code=...&state=...`
- Backend exchanges `code` for tokens with Cognito.
- If `FRONTEND_URL` is configured, backend returns:

```text
302 Location: {FRONTEND_URL}/auth/callback#access_token={access_token}&refresh_token={refresh_token}&token_type=Bearer&expires_in={expires_in}
```

- When `FRONTEND_URL` is **not** set, backend falls back to returning HTML/JSON (legacy / non-SPA clients).

Backend responsibilities:

- Define and use `FRONTEND_URL` env var for redirect base.  
- Ensure error cases also redirect with an `error` fragment instead of leaking details in query.  
- Keep the contract documented in `AUTH_AND_PROFILE_CONTRACT.md`.

---

## Consequences

### Positive

- Users land on the **custom frontend** after login; they never see Cognito or a backend-only page.  
- Tokens stay in the **fragment**, which is **not sent to servers**, matching standard SPA OAuth patterns.  
- Backend still controls the OAuth exchange, which works for both browser and non-browser clients.

### Negative / risks

- Frontend must reliably handle `/auth/callback` and parse tokens from the fragment.  
- If `FRONTEND_URL` is misconfigured, users can land on a broken URL after login.  
- Debugging redirect chains across Cognito → backend → frontend can be harder.

### Follow-ups

- Add monitoring/logs around `/api/v1/auth/callback` to debug redirect issues.  
- Consider a short-lived intermediate page on the frontend to show loading/errors during token processing.

---

## Alternatives considered

- **Return JSON from backend callback**  
  - Pros: Simple API-style response; easy to test with tools like `httpie`.  
  - Cons: Frontend would need a separate flow to receive the code and call the backend; worse UX for browser-based login.

- **Make frontend the Cognito redirect_uri**  
  - Pros: Tokens go directly to the SPA, which then calls backend.  
  - Cons: More complex setup in Cognito; backend would need new endpoints to exchange codes and handle refresh; harder to support non-browser clients.

- **Keep Cognito Hosted UI as the primary user experience**  
  - Pros: Minimal changes to backend and frontend.  
  - Cons: Inconsistent, non-branded UX; does not meet the product goal of a custom auth UI.

---

## References

- PRD: `docs/prd.md` (Goals: custom auth UX, safe auth integration).  
- Contracts: `docs/auth-and-profile-contract.md` (OAuth callback behavior), `docs/infra-backend-contract.md` (env vars like `FRONTEND_URL`, `OAUTH_CALLBACK_URI`).  
- Plan doc: `docs/custom-auth-ui-plan.md`.  

