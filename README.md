# SkillBridge — Unified Cross-Platform Career Ecosystem

SkillBridge is a full-stack **React + TypeScript + Tailwind CSS + Express + Firebase** application delivering **one unified product across Web, Mobile (installable PWA), and Desktop (installable PWA)** backed by one shared central backend, real-time SSE synchronization, Cloud Firestore persistence, and server-side `@google/genai` intelligence.

## Platform Access Architecture

- **Web Application**: Instant browser access with full bilingual (EN/AR) RTL support, Light/Dark themes, and zero installation required.
- **Mobile Application**: Responsive thumb-zone bottom bar + installable PWA (`vite-plugin-pwa`, Android Chrome & iOS Safari Add-to-Home-Screen).
- **Desktop Application**: Expanded sidebar workspace, multi-column analytics & review tables, and standalone installable desktop PWA.

## Core Features & Isolated Portals

- **4 Strict RBAC Roles**:
  - **Student (`STUDENT`)**: Readiness Ring, 6-step Verified Career Loop, AI Skill Gap Analyzer, AI Coach & Mock Interviewer, Company Briefs Marketplace, Teams & Sprints, Community Q&A, Ethical Reverse Engineering Sandbox, Badges, Points Wallet, Leaderboard, and verifiable Experience Passport.
  - **Company Partner (`COMPANY`)**: Company verification tier status, project brief publishing, Company Director assignment scoped to company, applicant Shortlist/Accept/Reject pipeline, and applicant analytics.
  - **Company Director (`COMPANY_DIRECTOR`)**: Isolated execution studio strictly scoped to assigned briefs, milestone task assignment, rubric scoring (40–100) with verification codes, and brief completion.
  - **Platform Admin (`PLATFORM_ADMIN`)**: Company KYC approval & tiers, Project Brief moderation queue, user activation/suspension, safety reports moderation, and platform activity ledger.
- **Real-Time Synchronization**: Server-Sent Events (`/api/events`), central authoritative API (`/api/ecosystem/*`), cross-device session tracking with remote revocation, and offline read resilience.
- **Security**: Salted `scrypt` password hashing, HMAC-SHA256 Bearer tokens, strict `firestore.rules` enforcement, secret-leak scanner, and optimistic concurrency conflict checks.

## Local Development & Scripts

- `npm run dev` — Starts the Express + Vite middleware dev server on `http://0.0.0.0:3000`
- `npm run lint` — Runs ESLint (including `@firebase/eslint-plugin-security-rules`) and `tsc --noEmit`
- `npm run build` — Generates production bundle and PWA service worker (`dist/`)
- `npm run start` — Serves the built application via Node.js (`node server.ts`)
