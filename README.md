# StudyBudy backend

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
