# Fees and Receipt Service

A Spring Boot API for student invoices, idempotent payments, balances, PDF receipts, reconciliation, and mock payment gateway webhooks.

## Agreed Technology

| Area | Choice |
| --- | --- |
| Core | Spring Boot 3, Spring Web |
| Language | Java 21 target |
| Build | Maven |
| Database | Local PostgreSQL |
| Persistence | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| PDF receipts | Thymeleaf HTML templates + Flying Saucer/OpenPDF-compatible renderer |
| Tests | JUnit 5, Mockito, Spring MockMvc |
| API docs | Springdoc OpenAPI / Swagger UI |
| Containers | Not used during the initial build |
| Source control | Normal Git workflow |

Java 26 is currently installed on my development machine. The project will target Java 21 for LTS compatibility; the Java 26 JDK can compile and run the Java 21 target unless a dependency exposes a compatibility issue.

## First Working Milestone

Complete this flow first:

1. Create a student.
2. Create and issue an invoice.
3. Record a payment with an idempotency key.
4. Repeat the same request and receive the original result without a duplicate payment.
5. Retrieve the correct invoice balance.

## Planned API Areas

- Students
- Invoices and invoice line items
- Payments and idempotency keys
- Invoice and student balances
- Downloadable PDF receipts
- Payment reconciliation reports
- Mock gateway webhooks with HMAC signature verification

## Finance Rules

- Used `BigDecimal` for all monetary calculations.
- Stored and return an explicit currency.
- Rejected duplicate payment references.
- Rejected payments greater than the outstanding invoice balance.
- Treated an idempotency key reused with a different request as a conflict.
- Returned HTTP `422 Unprocessable Entity` for validation failures.
- Kept payment creation and balance updates transactional.

## Local PostgreSQL

PostgreSQL is installed and usually managed through pgAdmin/SQLTools, but the terminal client `psql` is now currently available on PATH in my vscode. You can set your db path and connect to the db by using the command:

$psqlBin = 'D:\PostgreSQL\18\bin'; $userPath = [Environment]::GetEnvironmentVariable('Path','User'); 
if (($userPath -split ';') -notcontains $psqlBin) { [Environment]::SetEnvironmentVariable('Path', (($userPath.TrimEnd(';')
 + ';' + $psqlBin).Trim(';')), 'User') }; $env:Path = $psqlBin + ';' + $env:Path; psql --version

You can find this path (D:\PostgreSQL\18\bin') by looking for where postgres is installed on your pc.


After adding it to the User or System PATH, open a new terminal and verify:

```powershell
psql --version
```

Then connect to postgres using:

```powershell
psql -U postgres -h localhost -d postgres
```

The application database and credentials are configured outside my committed source code. The main configuration imports the project-root `.env` file, so keep credentials in this format:

```env
DB_USERNAME=postgres
DB_PASSWORD=your-local-password
DB_URL=your-db-url
```

The `.env` file is ignored by Git. Start the application normally and Spring Boot will resolve the `${DB_*}` placeholders from it:

```powershell
mvn spring-boot:run
```

Alternatively, this project supports the native `application-local.yml` profile file.

Copy `application-local.example.yml` to `src/main/resources/application-local.yml`, enter your local PostgreSQL password, and run with the `local` profile:

```powershell
Copy-Item .\src\main\resources\application-local.example.yml .\src\main\resources\application-local.yml
# Edit application-local.yml and replace CHANGE_ME with your password
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The local file is ignored by Git. For CI or another machine, environment variables can be supplied instead:

```powershell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-password"
mvn spring-boot:run
```

## Git Workflow

This project uses the normal Git commands:

```powershell
git init
git status
git add .
git commit -m "Initial project setup"
git rm "FILE NAME"
git push -u origin branch name
```

No special Git configuration is required.

## Build and Run

Once the Maven project is generated:

```powershell
mvn clean test
mvn spring-boot:run
```

Swagger UI will be available at:

```text
http://localhost:8081/swagger-ui.html
```

## Implementation Order

1. Bootstrap the Maven Spring Boot project.
2. Add configuration, Flyway, and the initial database schema.
3. Implement students and invoices.
4. Implement transactional payments and idempotency.
5. Implement balance summaries.
6. Add Thymeleaf receipt templates and PDF generation.
7. Add reconciliation endpoints.
8. Add mock webhooks and HMAC verification.
9. Add integration coverage and finish API documentation.

# Image so you don't have necessarily run it to see things work:
![alt text](<Screenshot (2001).png>)