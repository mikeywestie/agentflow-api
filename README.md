# AgentFlow API

AgentFlow is an AI agent orchestration platform built with Java Spring Boot.

The MVP workflow is:

```text
User Request
  -> Planner Agent
  -> Builder Agent
  -> Reviewer Agent
  -> Final Output
```

## Tech Stack

- Java 21
- Spring Boot 3.3.5
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Swagger / OpenAPI
- JWT
- Gemini API planned for Week 2

## Week 1 Scope

This starter contains:

- Auth endpoints
- Core entities
- Repositories
- Agent endpoints
- Workflow endpoints
- Execution endpoints
- Stub AI provider
- Flyway database schema
- Data seeder for Planner, Builder, Reviewer workflow

## Run locally

Start PostgreSQL:

```bash
docker compose up -d
```

Run the API with Maven:

```bash
mvn spring-boot:run
```

Or open the project in IntelliJ and run `AgentFlowApiApplication`.

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

## First Test Flow

1. Start the app.
2. Open Swagger.
3. Register a user:

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

4. Check seeded agents:

```http
GET /api/agents
```

5. Check seeded workflow:

```http
GET /api/workflows
```

6. Run the default workflow:

```http
POST /api/executions/run
```

```json
{
  "request": "Build a quotation management system for electricians."
}
```

Because Week 1 uses `StubAiProvider`, the response will not call Gemini yet. Week 2 replaces this with the real Gemini provider.

## Week 2 Plan

Add:

- `GeminiAiProvider`
- API client using `RestClient`
- Configurable model name
- Real Planner -> Builder -> Reviewer outputs

## Week 3 Plan

Build `agentflow-ui` with:

- Login
- Dashboard
- Agents
- Workflows
- Run Workflow
- Execution History
- Execution Detail

## Week 4 Plan

Deploy:

- Backend: Render
- Database: Supabase PostgreSQL
- Frontend: GitHub Pages / Netlify
- AI Brain: Gemini API

## Important MVP Notes

For Week 1, the security configuration permits all non-auth endpoints to make learning and Swagger testing easier.

Before production/demo polish, add:

- JWT authentication filter
- Role-based endpoint protection
- Refresh token or safer session strategy
- Better error logging
- Render/Supabase environment configuration
