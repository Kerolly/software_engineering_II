set windows-shell := ["powershell.exe", "-NoLogo", "-Command"]
set dotenv-load

# List all commands
default:
    @just --list

# Install and check everything needed for development (run this first)
setup:
    {{ if os_family() == "windows" { "powershell -NoProfile -ExecutionPolicy Bypass -File scripts/setup.ps1" } else { "bash scripts/setup.sh" } }}

# Start the dev infrastructure (PostgreSQL + Logto)
infra:
    docker compose up -d --wait

# Stop the dev infrastructure
down:
    docker compose down

# Follow logs, optionally for one service
logs service="":
    docker compose logs -f {{service}}

# Show container status
ps:
    docker compose ps

# Open psql in the application database
db-shell:
    docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'

# Delete all local data, including Logto accounts
[confirm("This deletes ALL local data, including Logto accounts. Continue?")]
db-reset:
    docker compose down -v

mvnw := if os_family() == "windows" { "mvnw.cmd" } else { "./mvnw" }

# Start everything for development: infrastructure in Docker, backend and frontend on the host
dev: infra dev-apps

[parallel]
dev-apps: dev-backend dev-frontend

# Run the backend on the host (needs JDK 25)
[working-directory('backend')]
dev-backend:
    {{mvnw}} spring-boot:run

# Run the frontend on the host (needs Node)
[working-directory('frontend')]
dev-frontend:
    npm install
    npm run dev
