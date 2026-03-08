## Docs index

- `prd.md` – Product Requirements for this backend service.  
- `auth-and-profile-contract.md` – Contract between backend and frontend for auth/profile flows.  
- `infra-backend-contract.md` – Contract between infra and backend (env vars, stack outputs).  
- `postgresql-schema.md` – High-level database schema and query patterns.  
- `custom-auth-ui-plan.md` and related setup docs – Implementation plans for specific features.  
- `adrs/` – Architecture Decision Records for **backend-focused** decisions.

---

## Legacy filenames

Earlier versions of this repo used SCREAMING_SNAKE filenames like `AUTH_AND_PROFILE_CONTRACT.md` and `BACKEND_CONTRACT.md`.  
As of 2026-03-02 these have been renamed to kebab-case (e.g. `auth-and-profile-contract.md`, `infra-backend-contract.md`) for consistency. Update any external links accordingly.

---

## What goes where

- **PRD (`prd.md`)**
  - Single, shared description of the product/domain this backend serves.
  - Other repos (frontend, infra) should link to this instead of duplicating product text.

- **Backend ADRs (`docs/adrs/*.md`)**
  - Decisions primarily about **backend behavior or contracts**:
    - API shapes and auth flows.
    - Data model choices.
    - How backend integrates with infra outputs.
  - Example: `0002-auth-callback-redirect.md` describes how the OAuth callback behaves on the backend.

- **Frontend ADRs (in frontend repo)**
  - Decisions that are mostly about UI, routing, or SPA behavior should live in the **frontend repo’s** `docs/adrs/` folder.
  - Backend docs can reference these ADRs, but should not duplicate all of their details.

- **Infra ADRs (in infra repo)**
  - Decisions about AWS resources, networking, cost tradeoffs, etc. belong in **archimedes-infra** (or equivalent) under its own `docs/adrs/`.
  - Backend keeps only the **contract** it depends on (e.g. `BACKEND_CONTRACT.md`), not the full infra rationale.

---

## Rule of thumb for ADRs

- If the decision is **purely backend** → add or update an ADR in `docs/adrs/`.  
- If the decision is **purely frontend** → capture it in a frontend ADR and only reference it here if needed.  
- If the decision is **purely infra** → capture it in infra ADRs; keep backend-doc changes to contracts/env var expectations.  
- If a decision truly spans multiple repos, pick **one “home” ADR** and create small link-only ADRs or references in the other repos instead of duplicating the full content.

