# StudyBuddy Backend

## Local onboarding (API + PostgreSQL)

This repository is the Java 25 / Spring Boot API. The companion client is in
[studybuddy-app](https://github.com/438-p03-g4/studybuddy-app).

### Prerequisites

- Git and a running Docker engine with Docker Compose v2.
- Internet access on first build to download images and Gradle dependencies.
- Free local port 8080. Java is included in the images; no host JDK is needed
  for this Docker path. Native testing requires JDK 25.

### Clone, configure and start

PowerShell on Windows:

```powershell
git clone https://github.com/438-p03-g4/studybudy-backend.git
cd studybudy-backend
git switch main
git rev-parse HEAD
Copy-Item .env.example .env
docker --version
docker compose version
docker info
docker compose config --quiet
docker compose up --build -d
docker compose ps
curl.exe --fail http://localhost:8080/actuator/health
```

macOS/Linux: replace `Copy-Item .env.example .env` with `cp .env.example .env`
and `curl.exe` with `curl`. Other commands are the same.

The build may take several minutes. If the health request is too early, inspect
`docker compose logs --tail=100 api` and retry once startup has completed.
Expected: HTTP 200 and a JSON body with `"status":"UP"`.

The Compose database is local PostgreSQL; no Neon account or real secret is
required for this startup check. The sample OAuth values are deliberately fake:
Google/GitHub sign-in will not work with them. Protected endpoints such as
`/groups` require an authenticated session in the current implementation and
may redirect to sign-in. Health success alone is not proof of full product login.

### Run tests

```powershell
docker compose --profile test run --build --rm tests
```

Expected: `BUILD SUCCESSFUL`, exit code 0. Current tests use isolated H2 databases;
they do not test the running PostgreSQL container or a complete OAuth flow.
The audited main commit had 15 tests. Record the actual count for the tested commit.

### Demo data and troubleshooting

See [the runbook](docs/runbook.md) for local seed data, logs, ports, stopping the
stack, native test commands, and known limitations.

Current client checks (`npm run typecheck` and web export) are build checks,
not a client unit/e2e test suite. Group joining is not a completed client flow yet.

File each onboarding blocker as a separate issue with label `onboarding`, exact
command/output, expected result, documentation step, and OS/Docker versions.


## Optional hosted deployment: Neon and Render

The local Compose path above does not need these credentials. The following
section preserves the existing hosted-deployment instructions.

## Connect to Neon

1. Create or select a Neon project and database.
2. In Neon, open **Connect** and copy the connection details. Use the pooled connection host for serverless deployments if your deployment platform starts many short-lived instances.
3. Configure these environment variables in your local shell or deployment platform:

   - `DATABASE_URL`: JDBC URL in the form `jdbc:postgresql://<host>/<database>?sslmode=require&channel_binding=require`
   - `DATABASE_USERNAME`: Neon role name
   - `DATABASE_PASSWORD`: Neon role password

   Convert the `postgresql://` prefix in a Neon URI to `jdbc:postgresql://`. Keep the host, database, and query parameters from Neon. Do not commit real credentials. Add the three values as deployment secrets in the hosting platform.

4. Run the service with `./gradlew bootRun`.

During development, Hibernate updates the Neon schema from the JPA entities (`ddl-auto: update`). For production, manage schema changes with versioned SQL migrations rather than automatic schema updates.

## Deploy on Render

Create a **Web Service** for this repository and choose **Docker** as the runtime
Keep the Dockerfile path as `./Dockerfile`; the container listens on Render's `PORT` environment variable (10000 by default).

Set these environment variables in the Render service settings using the Neon connection details:

- `DATABASE_URL`: `jdbc:postgresql://<neon-host>/<database>?sslmode=require&channel_binding=require`
- `DATABASE_USERNAME`: Neon role name
- `DATABASE_PASSWORD`: Neon role password

Don't commit or put the database credentials on dockerfile
