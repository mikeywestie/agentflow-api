# AgentFlow API

AgentFlow API is the Spring Boot backend for **AgentFlow**, an AI agent orchestration platform that runs user requests through a configurable workflow of specialist agents.

The current MVP focuses on proving the orchestration flow locally before adding production security and hosting polish.

## Current version

Documented state: **v0.4.0**

## MVP workflow

```text
User Request
  -> Planner Agent
  -> Builder Agent
  -> Reviewer Agent
  -> Final Output
```

The system stores workflows, workflow steps, executions, and individual agent runs so each generated result can be reviewed as a trace instead of only returning a single final response.

## Tech stack

- Java 21
- Spring Boot 3.3.5
- Spring Web
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway database migrations
- Swagger / OpenAPI via Springdoc
- JWT generation
- Gemini API integration
- OpenRouter planned
- Stub AI fallback for local MVP testing
- Docker Compose for local PostgreSQL

## Current MVP features

- User registration and login endpoints
- JWT token generation
- Agent management endpoints
- Workflow management endpoints
- Workflow execution endpoint
- Execution history endpoint
- Execution detail endpoint with agent trace data
- Planner, Builder, Reviewer workflow concept
- AI provider abstraction through `AiProvider`
- Gemini provider implementation
- Stub provider for local fallback/testing
- Retry handling for AI provider failures
- Partial success handling when an agent fails after a previous agent completed
- Skipped agent run records when a workflow cannot continue
- Flyway-managed PostgreSQL schema
- Local Docker PostgreSQL setup
- Swagger UI for manual API testing

## AI provider strategy

The intended provider order is:

```text
GeminiAiProvider
  -> OpenRouterAiProvider (planned)
  -> StubAiProvider fallback
```

Current status:

- `GeminiAiProvider` is present for real AI calls.
- `OpenRouterAiProvider` is planned as the secondary provider.
- `StubAiProvider` exists as a safe local fallback for MVP testing.

The goal is to keep the orchestration code independent from any single AI vendor by routing calls through the `AiProvider` contract.

## Local development setup

### Prerequisites

- Java 21+
- Maven
- Docker Desktop
- PostgreSQL client tooling optional

### Start PostgreSQL

```bash
docker compose up -d
```

The local database defaults are:

```text
Database: agentflow_db
Username: agentflow_user
Password: agentflow_pass
Port: 5432
```

### Run the API

```bash
mvn spring-boot:run
```

Or run `AgentFlowApiApplication` from IntelliJ.

### Swagger UI

```text
http://localhost:8080/swagger-ui.html
```

### Health check

```text
http://localhost:8080/actuator/health
```

## Environment variables

The application supports environment-based configuration. For local development, defaults are provided in `application.yml`.

| Variable | Purpose | Local default |
|---|---|---|
| `DATABASE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/agentflow_db` |
| `DATABASE_USERNAME` | PostgreSQL username | `agentflow_user` |
| `DATABASE_PASSWORD` | PostgreSQL password | `agentflow_pass` |
| `JWT_SECRET` | Secret used for signing JWTs | Local placeholder secret |
| `JWT_EXPIRATION_MS` | JWT expiry time in milliseconds | `86400000` |
| `FRONTEND_URL` | Allowed frontend origin for CORS | `http://localhost:5173` |
| `GEMINI_API_KEY` | Gemini API key | empty |
| `GEMINI_MODEL` | Gemini model name | configured in `application.yml` |

Do not commit real secrets. Use local environment variables or a local `.env` file that remains ignored by Git.

## First test flow

1. Start PostgreSQL.
2. Start the API.
3. Open Swagger.
4. Register a user.
5. Check available agents.
6. Check available workflows.
7. Run a workflow execution.
8. View the execution history and execution detail trace.

### Register

```http
POST /api/auth/register
```

```json
{
  "fullName": "Mikey Westman",
  "email": "mikey@example.com",
  "password": "Password123"
}
```

### Login

```http
POST /api/auth/login
```

```json
{
  "email": "mikey@example.com",
  "password": "Password123"
}
```

### List agents

```http
GET /api/agents
```

### List workflows

```http
GET /api/workflows
```

### Run workflow

```http
POST /api/executions/run
```

```json
{
  "request": "Build a quotation management system for electrical contractors."
}
```

When a `workflowId` is supplied, the API runs that workflow. When omitted, the API can resolve an enabled workflow automatically.

## Main API areas

| Area | Base path | Purpose |
|---|---|---|
| Auth | `/api/auth` | Register and login users |
| Agents | `/api/agents` | Create and list AI agents |
| Workflows | `/api/workflows` | Create and list workflows |
| Executions | `/api/executions` | Run workflows and inspect execution history |

## Security status

The current local MVP intentionally keeps non-auth endpoints open to make Postman, Swagger, and frontend testing faster.

Before hosting or public demo usage, the security layer should be tightened with:

- JWT authentication filter
- Role-based endpoint protection
- Safer user role assignment
- Strong production `JWT_SECRET`
- Strict production CORS origin configuration
- Environment-specific configuration

Current approach:

```text
Local development first -> secure before hosting
```

## Current limitations

- OpenRouter provider is planned but not implemented yet.
- Production-grade JWT request filtering is not enabled yet.
- Registered users are currently treated as MVP admin users.
- No refresh token/session hardening yet.
- No hosted environment configuration has been finalized yet.
- Error handling exists, but can be expanded with more detailed logging and structured diagnostics.

## Related project

The frontend dashboard lives in:

```text
mikeywestie/agentflow-ui
```

## Roadmap

### Backend next

- Finalize provider chain: Gemini -> OpenRouter -> Stub fallback
- Add `.env.example`
- Confirm `.env` is ignored
- Add provider selection/fallback tests
- Add JWT filter before hosting
- Add role-based authorization before hosting
- Prepare Render/Supabase configuration notes

### UI next

- Improve early MVP UI layout
- Add Tailwind CSS
- Move API URL to `VITE_API_BASE_URL`
- Add better dashboard sections
- Improve execution trace design
- Add loading, empty, and error states

## Portfolio note

AgentFlow demonstrates backend architecture skills around API design, workflow orchestration, persistence, AI provider abstraction, execution tracing, retry handling, and local-first development using Spring Boot and PostgreSQL.
