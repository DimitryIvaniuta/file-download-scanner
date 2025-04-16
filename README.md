# Expert Spring Boot File Download Service

This project is a Spring Boot application that demonstrates an expert approach to logging into a third–party web portal using actual username/password authentication (via HTTP form login), fetching a protected resource page, scraping links to files, and downloading them to disk, storing records in a PostgreSQL database while avoiding duplicate downloads. Configuration is fully externalized, using `application.yml`, environment variables, and Flyway for database migrations.

## Features

- **Form-Based Portal Login**: Authenticate with a remote portal by submitting credentials to a login endpoint and managing session cookies.
- **Protected Resource Fetching**: Use the authenticated session to request a protected page and parse its HTML for file links.
- **File Download & Duplication Check**: Download only new files; store metadata (`fileUrl`, `localPath`, `downloadedAt`) in PostgreSQL.
- **Configuration Classes**: Bind `application.yml` properties to `@ConfigurationProperties` beans (`PortalProperties`, `DownloadProperties`).
- **Environment Variables**: Load sensitive settings and custom flags from a `.env` file via java-dotenv.
- **Database Migrations**: Manage schema evolution using Flyway migration scripts.
- **Spring Data JPA**: Use a JPA repository for easy persistence and lookups.
- **Gradle Build**: Leverage Gradle wrapper for reproducible builds.

## Prerequisites

- Java 21 SDK
- PostgreSQL database
- Gradle (or use the bundled wrapper)

## Getting Started

1. **Clone the repository**
    ```bash
    git clone https://github.com/your-org/file-download-project.git
    cd file-download-project
    ```

2. **Create a `.env`** in the project root alongside `build.gradle`:
    ```bash
    # .env
    PORTAL_LOGIN_URL=https://third-party-portal.com/login
    PORTAL_RESOURCE_URL=https://third-party-portal.com/protected-page
    PORTAL_USERNAME=portalUser
    PORTAL_PASSWORD=portalPass

    DOWNLOAD_DIR=./downloads

    DB_NAME=mydb
    DB_USERNAME=myuser
    DB_PASSWORD=mypassword
    ```

3. **Configure application.yml** in `src/main/resources` (defaults provided):
    ```yaml
    spring:
      config:
        import:
          - "optional:dotenv:./.env"

      datasource:
        url: jdbc:postgresql://localhost:5432/${DB_NAME:mydb}
        username: ${DB_USERNAME:user}
        password: ${DB_PASSWORD:pass}

      jpa:
        hibernate:
          ddl-auto: update

      flyway:
        baseline-on-migrate: true

    server:
      port: 8080

    app:
      portal:
        loginUrl:    ${PORTAL_LOGIN_URL}
        resourceUrl: ${PORTAL_RESOURCE_URL}
        username:    ${PORTAL_USERNAME}
        password:    ${PORTAL_PASSWORD}

    download:
      dir: ${DOWNLOAD_DIR:./downloads}
    ```

4. **Initialize the database**
    - Ensure PostgreSQL is running and the database `mydb` exists (or adjust names).
    - Flyway will apply `V1__create_downloaded_files_table.sql` on startup.

5. **Build & Run**
    ```bash
    ./gradlew bootRun
    ```

6. **Use the API**
    - **Trigger downloads**: `POST http://localhost:8080/api/download-files`
    - **List downloaded files**: `GET  http://localhost:8080/api/downloaded-files`

## Project Structure

```
├── build.gradle             # Gradle configuration
├── .env                     # Environment variables (not committed)
├── src/main/java
│   └── com/importservice
│       ├── FileDownloadApplication.java
│       ├── config
│       │   ├── PortalProperties.java
│       │   └── DownloadProperties.java
│       ├── controller
│       │   └── DownloadController.java
│       ├── model
│       │   └── DownloadedFile.java
│       ├── repository
│       │   └── DownloadedFileRepository.java
│       ├── service
│       │   ├── PortalAuthService.java
│       │   └── FileDownloadService.java
│       └── util
│           └── HtmlContentParsingService.java
└── src/main/resources
    ├── application.yml      # Application configuration
    └── db/migration
        └── V1__create_downloaded_files_table.sql
```

## Customization

- **Session Handling**: Adjust `PortalAuthService` if your portal uses different form fields or cookie names.
- **File Filtering**: Modify `FileDownloadService.isDownloadable(...)` to support other file types.
- **Error Handling**: Enhance logging, retry logic, or exception management as needed.

## License

This project is licensed under the [MIT License](LICENSE).

---

## Contact

**Dzmitry Ivaniuta** — [diafter@gmail.com](mailto:diafter@gmail.com) — [GitHub](https://github.com/DimitryIvaniuta)