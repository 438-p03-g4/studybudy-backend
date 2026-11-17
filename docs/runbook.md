# StudyBuddy local onboarding runbook

**Preparation draft: Compose has not been executed by the preparer because
Docker was unavailable. A teammate must validate it before this is merged.**
After a real run, replace this paragraph with the date, commit, platform and
observed startup/test results. Never claim a test that was not performed.

## 1. Scope and prerequisites

This runbook starts only the API and a local PostgreSQL instance. The existing
Render/Neon deployment remains separate. Use Docker Compose v2 and an available
port 8080. See the root README for clone and startup commands.

Keep `.env` out of Git and Docker context using the provided ignore additions.
The values in `.env.example` are local-only examples. A Docker `.env` is consumed
by Compose; it is not automatically loaded by a native `gradlew bootRun` command.

## 2. Check startup

```powershell
docker compose config --quiet
docker compose up --build -d
docker compose ps
docker compose logs --tail=100 db
docker compose logs --tail=100 api
curl.exe --fail http://localhost:8080/actuator/health
```

Expected: database healthy, API running, and health status `UP`. On macOS/Linux
use `curl`. If health is not ready, read the logs and retry after startup.

## 3. Run the test suite

```powershell
docker compose --profile test run --build --rm tests
```

Expected: `BUILD SUCCESSFUL` and exit code 0. Capture the full output. Current
tests use H2, not the live PostgreSQL container. GitHub passing does not prove
that a visitor's run succeeded; visitors must run and record their own result.

Without Docker, a diagnostic alternative is JDK 25 plus:

```powershell
.\gradlew.bat test --no-daemon --console=plain
```

macOS/Linux: `./gradlew test --no-daemon --console=plain`.
This fallback does not satisfy the required Docker-stack attempt by itself.

## 4. Local demo data (after API health is UP)

The API creates the `study_group` table through Hibernate at startup. To add
one repeatable local example (never run this against Neon/production):

```powershell
docker compose exec -T db psql -U studybuddy -d studybuddy -v ON_ERROR_STOP=1 -c "INSERT INTO study_group (name, course) SELECT 'CST438 Onboarding Group', 'CST438' WHERE NOT EXISTS (SELECT 1 FROM study_group WHERE name = 'CST438 Onboarding Group' AND course = 'CST438');"
docker compose exec -T db psql -U studybuddy -d studybuddy -c "SELECT id, name, course FROM study_group ORDER BY id;"
```

Expected: the example group appears once. This creates no user or login session.
There is currently no documented, working test-only auth mechanism. Do not
publish real provider secrets or turn off authentication to make the demo pass.

## 5. Client startup

In another directory/terminal:

```powershell
git clone https://github.com/438-p03-g4/studybuddy-app.git
cd studybuddy-app
git switch main
npm ci
npm run typecheck
npm run web
```

Use the URL printed by Expo (normally port 8081). The current app has a home
screen and local Todo creation/storage. Group/account menu actions and OAuth
integration are not complete. It does not yet demonstrate authenticated API
group search/joining from the UI. The separate `studybuddy-website` repository
contains another Expo starter and is not assumed to be this same client.

## 6. Troubleshooting

| Symptom | Check / action |
|---|---|
| `docker` not found | Install Docker Desktop/Engine through the normal team setup; verify Compose v2 before class. |
| Cannot connect to Docker daemon | Start Docker and wait for the engine, then run `docker info`. |
| Port 8080 occupied | Stop the conflicting local service, or change only the host side of the port mapping and record the new URL. |
| Database connection refused | Read database health and API logs; confirm Compose is being run from the repo root. |
| Password mismatch after editing `.env` | Existing PostgreSQL volumes keep their original password. Restore the original local password or back up and explicitly reset only disposable local data. |
| Missing OAuth placeholder | Re-copy `.env.example` to a new local `.env` and check the required variable names. Real sign-in requires real development-client configuration. |
| Redirect at `/groups` | Current server requires OAuth login; a redirect is not successful group retrieval. Record the auth gap if the README could not get you signed in. |
| Test failure | Record exact test command, exit code, first failing test and output; include the XML/HTML test report if available. |
| Expo menu closes without navigating | Known placeholder in current client. Record actual behavior rather than claiming group search works. |

Do not paste private environment values into shared command logs. This runbook
does not instruct visitors to request access to the production Neon database.

## 7. Stop the local stack

```powershell
docker compose down
```

This keeps the local database volume. Volume deletion is intentionally not part
of normal onboarding; use it only for explicitly disposable data after checking
what will be lost.

## 8. Report onboarding problems

One issue per problem, label `onboarding`. Include the doc step, exact command
and output, expected result, OS and Docker versions. Even a completely clean
run gets one issue with commands and results. Hosts assign an owner or reply
with a plan before class ends. Hosts never take over the visitor's keyboard.
