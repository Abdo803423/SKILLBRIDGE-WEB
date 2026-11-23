# SkillBridge — Career Development Website

SkillBridge is a full-stack **React + TypeScript + Tailwind CSS + Express + Firebase** web application. It runs as a conventional, browser-based website across desktop, tablet, and mobile viewports with clean URL routing, bilingual EN/AR (LTR/RTL) support, Light/Dark themes, Cloud Firestore persistence, real-time Server-Sent Events (`/api/events`), and server-side Gemini AI coaching (`/api/ai/*`).

## Core Features

- **4 Isolated Role Portals**:
  - **Student (`STUDENT`)**: Skill Gap Analyzer, Readiness Ring, Verified Career Loop, AI Coach & Mock Interviewer, Company Briefs Marketplace, Teams & Sprints, Community Q&A, Ethical Reverse Engineering Sandbox, Badges, Points Wallet, Leaderboard, and verifiable Experience Passport.
  - **Company Partner (`COMPANY`)**: Verification tier status, project brief publishing, Director assignment, applicant pipeline (Shortlist/Accept/Reject), and analytics.
  - **Company Director (`COMPANY_DIRECTOR`)**: Scoped execution studio for assigned briefs, milestone tasks, rubric scoring (40–100) with verification codes, and brief completion.
  - **Platform Admin (`PLATFORM_ADMIN`)**: Company KYC approval, brief moderation queue, user access management, safety reports moderation, and activity ledger.
- **Standard Browser Navigation**: Clean History API URLs (`/`, `/discover`, `/contests`, `/labs`, `/community`, `/teams`, `/passport`, `/briefs`, `/platform`, `/architecture`, `/settings`, `/company`, `/director`, `/admin`), working Back/Forward buttons, direct URL navigation, and browser refresh support.

## Project Structure

```text
├── public/                 # Static web assets (favicon.ico, icon.svg, 404.html, images)
├── src/
│   ├── components/         # Shared UI shell, motion effects, charts, AI drawer
│   ├── context/            # Application state, auth/session sync, URL history routing
│   ├── data/               # Catalog and seed datasets
│   ├── hooks/              # Browser network and viewport helpers
│   ├── i18n/               # English & Arabic (RTL) localization dictionaries
│   ├── lib/                # Firebase initialization and URL route mapping
│   ├── screens/            # Feature pages and role portals
│   ├── services/           # Central API client, AI SSE streaming client, Firestore repo
│   └── types/              # TypeScript domain types
├── server/                 # Central authoritative ecosystem store
├── server.ts               # Express API server + production static file server
├── vercel.json             # Vercel SPA routing & build configuration
├── netlify.toml            # Netlify build & SPA redirect configuration
└── .github/workflows/      # GitHub Actions CI build & GitHub Pages deployment
```

## Local Development

1. Install dependencies:
   ```bash
   npm install
   ```
2. Copy `.env.example` to `.env` if setting `GEMINI_API_KEY` or custom ports:
   ```bash
   cp .env.example .env
   ```
3. Start the development server on port `3000`:
   ```bash
   npm run dev
   ```
4. Lint & type-check:
   ```bash
   npm run lint
   ```
5. Build production static assets into `dist/`:
   ```bash
   npm run build
   ```
6. Preview the production static build or run the full-stack Node server:
   ```bash
   npm run preview
   # or full-stack Express + dist/:
   NODE_ENV=production npm run start
   ```

## Hosting & Deployment

### Option A: Vercel (Static Frontend) or Netlify
- **Vercel**: Connect your GitHub repository. Vercel automatically detects `vercel.json` (`npm run build` → `dist/`, SPA rewrite to `/index.html`). If using a separate Express backend URL for `/api/*`, set `VITE_API_BASE_URL` in Vercel Environment Variables.
- **Netlify**: Connect your GitHub repository. Netlify automatically applies `netlify.toml` (`npm run build` → `dist/`, `/* -> /index.html` status `200`). Set `VITE_API_BASE_URL` if pointing to a separate backend host.

### Option B: GitHub Pages
- Enable **Settings → Pages → Source: GitHub Actions** in your GitHub repository.
- Push to `main`. `.github/workflows/deploy.yml` runs `npm ci`, `npm run lint`, builds `dist/` with `VITE_BASE_PATH=/<repo-name>/`, and publishes cleanly with `public/404.html` SPA refresh handling.

### Option C: Full-Stack Node Hosting (Render, Railway, Fly.io, VPS)
- Build Command: `npm install && npm run build`
- Start Command: `NODE_ENV=production npm run start`
- Environment Variables: `PORT=3000`, `GEMINI_API_KEY=<your-key>`, and `ALLOWED_ORIGINS` if serving an external static frontend.
