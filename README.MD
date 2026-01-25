# Devy BigBoard

Fantasy football rookie draft application with database persistence, player verification, and draft sharing capabilities.

## Technology Stack

- **Backend**: Spring Boot 3.4.2, Java 21, Spring Data JPA
- **Frontend**: React 18, TypeScript, Webpack 5
- **Database**: MySQL 8.0
- **Deployment**: Railway with Nixpacks

## Configuration

### Spring Profiles

The application uses Spring profiles to manage different environments:

- **local** (default): For local development with hardcoded Railway MySQL credentials
- **cloud**: For Railway deployment with environment variables

### Local Development

1. The application uses the `local` profile by default
2. Configuration is in `app-server/src/main/resources/application-local.yml`
3. This file is git-ignored and contains hardcoded credentials for the Railway MySQL database

### Railway Deployment

Set the following environment variables in Railway:

#### Required Environment Variables

```
SPRING_PROFILES_ACTIVE=cloud

# MySQL Connection (Railway provides these automatically)
MYSQL_URL=jdbc:mysql://<host>:<port>/<database>
MYSQL_USERNAME=<username>
MYSQL_PASSWORD=<password>

# Application Settings
VERIFICATION_SECRET=<your-secret-verification-code>
```

#### Railway MySQL Variables

If Railway doesn't provide `MYSQL_URL` in the correct format, you can construct it from:

```
MYSQLHOST=<host>
MYSQLPORT=<port>
MYSQLDATABASE=<database>
MYSQLUSER=<username>
MYSQLPASSWORD=<password>

# Then set:
MYSQL_URL=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}
MYSQL_USERNAME=${MYSQLUSER}
MYSQL_PASSWORD=${MYSQLPASSWORD}
```

### Database Configuration

The application uses:
- **Hibernate DDL Auto**: `update` (automatically creates/updates tables)
- **Connection Pool**: HikariCP with optimized settings
- **Dialect**: MySQL 8.0

### Building and Running

#### Quick Start Scripts

For convenience, use the provided scripts to start the server with the local profile:

**Windows:**
```bash
start-local.bat
```

**Mac/Linux:**
```bash
chmod +x start-local.sh  # First time only
./start-local.sh
```

#### Local Development

```bash
# Install pnpm globally (if not already installed)
npm install -g pnpm

# Build the entire project (backend + frontend)
./gradlew clean build

# Run the Spring Boot application with local profile
./gradlew :app-server:bootRun --args="--spring.profiles.active=local"

# Or use the quick start scripts above

# Run frontend dev server (separate terminal)
cd app-client
pnpm install
pnpm dev
```

#### Production Build

```bash
# Build for production
./gradlew clean build -x test

# Run the JAR
java -Dspring.profiles.active=cloud -jar app-server/build/libs/devy-big-board.jar
```

### Frontend Build Performance

The frontend now uses **Vite** instead of Webpack for significantly faster builds:
- ⚡ **Dev server starts in ~200ms** (vs 10-30s with Webpack)
- 🚀 **Hot Module Replacement (HMR)** is instant
- 📦 **Production builds are 5-10x faster**
- 🎯 **Uses pnpm** for faster dependency installation

#### Frontend Scripts

```bash
cd app-client

# Development server (with HMR)
pnpm dev

# Production build
pnpm build

# Preview production build
pnpm preview
```

## Project Structure

```
devy-big-board/
├── app-client/          # React frontend
│   ├── src/
│   │   ├── components/
│   │   └── models/
│   └── build.gradle
├── app-server/          # Spring Boot backend
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-local.yml (git-ignored)
│       │       └── application-cloud.yml
│       └── test/
├── railway.json         # Railway deployment config
├── nixpacks.toml        # Nixpacks build config
└── settings.gradle
```

## Security Notes

- Never commit `application-local.yml` files (they are git-ignored)
- Change the `VERIFICATION_SECRET` in production
- Use strong passwords for database connections
- The verification secret is used to approve player submissions