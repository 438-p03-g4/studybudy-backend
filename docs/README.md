# StudyBuddy app

This repository is now an Expo React Native application that targets Android and the web through React Native Web. The original Kotlin/Gradle project is retained locally in `android-legacy/` for reference and is excluded from source control.

## Run the app

From the repository root:

```bash
npm install
npm start
```

Then choose a target from the Expo terminal, or use one of these commands:

```bash
npm run android
npm run web
```

The project can also be checked without launching a device:

```bash
npm run typecheck
```

## Current functionality

- Home screen matching the existing StudyBuddy layout.
- Menu popup with the existing menu labels.
- Local todo creation and persistence using AsyncStorage.
- Android and web-compatible Expo Router navigation.
- OAuth provider configuration prepared for Google and GitHub, but no sign-in request is made yet because the backend authentication endpoints are not implemented.


# StudyBuddy backend

Java and Spring Boot backend. PostgreSQL is provided by Neon.

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