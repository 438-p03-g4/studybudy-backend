# StudyBuddy app

Expo / React Native application for web and Android. This repository contains the StudyBuddy client.

## Prerequisites

Git, Node.js 22.13.0 (the version used in current CI), npm, and Internet access
for dependency installation. Start with web; Android requires a configured
emulator/device and is not a replacement for the local API onboarding check.

## Fresh clone and web startup

```powershell
git clone https://github.com/438-p03-g4/studybuddy-app.git
cd studybuddy-app
git switch main
git rev-parse HEAD
npm ci
npm run web
```

Open the URL printed by Expo, normally http://localhost:8081. Stop with Ctrl+C.
Expected current behavior: home screen, menu, local Todo creation and storage,
study-group pages, and group discovery through the authenticated API.

## Checks

In another terminal in the repository root:

```powershell
npm run typecheck
npx --no-install expo export --platform web
```

These are type/build checks, not a client unit or e2e test suite. There is no
`npm test` script on the audited main commit.

## API and local stack

The backend lives in [studybudy-backend](https://github.com/438-p03-g4/studybudy-backend).
Use its README and `docs/runbook.md` for the Docker API/database setup
and the backend test suite. 

The group-search page calls `GET /groups` through the `EXPO_PUBLIC_API_URL`
environment variable. If it is not set, web uses `http://localhost:8080` and
the Android emulator uses `http://10.0.2.2:8080`. The endpoint requires an
authenticated StudyBuddy session, so OAuth must be configured before protected
group results can load.

## Onboarding issues

File client blockers here with label `onboarding`, one issue per problem. Include
the README step, exact command and text output, expected result, OS and Docker
versions. If the complete attempt succeeds, file one clean-run issue instead.
