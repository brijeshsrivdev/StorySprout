# Local development

## Prerequisites

- Node.js 24 LTS
- npm 11
- Java 21
- Docker / Docker Compose
- curl for Maven Wrapper bootstrap

## Start infrastructure

`docker compose up -d`

PostgreSQL: `localhost:5432`
MinIO API: `localhost:9000`
MinIO Console: `localhost:9001`

## Start web

`npm install`
`npm run dev`

## Start API

`cd apps/api && ./mvnw spring-boot:run`

Health: `GET http://localhost:8080/api/v1/health`

## Start renderer

`npm install`
`npm --workspace services/renderer run dev`

Health: `GET http://localhost:4000/health`
