# Copilot Instructions for bx-sqlite BoxLang Module

## Architecture Overview

This is a **BoxLang JDBC driver module** that extends the BoxLang runtime with SQLite database connectivity. The module follows the **Java ServiceLoader pattern** via Gradle's serviceloader plugin to auto-register the `SQLiteDriver` class as an `IJDBCDriver` implementation.

**Key Components:**
- `SQLiteDriver.java`: Extends `GenericJDBCDriver`, builds connection URLs like `jdbc:sqlite:database_path`
- `ModuleConfig.bx`: BoxLang module descriptor defining metadata and runtime integration
- ServiceLoader registration: Auto-generates `META-INF/services/ortus.boxlang.runtime.jdbc.drivers.IJDBCDriver`

## Critical Build Dependencies

**Must build module structure before testing:**
```bash
./gradlew shadowJar createModuleStructure
```

Tests depend on `build/module/` containing the compiled JAR and BoxLang descriptors. The `BaseIntegrationTest.loadModule()` loads from `./build/module` path, not source directories.

**BoxLang Runtime Dependency:**
- First tries local `../boxlang/build/libs/boxlang-{version}.jar`
- Falls back to `downloadBoxLang` task fetching from downloads.ortussolutions.com
- Tests run against the full BoxLang runtime, not mocked interfaces

## Project-Specific Patterns

**Token Replacement System:**
```
@build.version@ → actual version from gradle.properties
@build.number@ → BUILD_ID env var or "0"
```
Applied to `box.json`, `ModuleConfig.bx` during `createModuleStructure` task.

**Branch-Based Versioning:**
- `development` branch: Forces `-snapshot` suffix
- Other branches: Uses BUILD_ID for release builds

**Testing Architecture:**
- `BaseIntegrationTest`: Loads full BoxLang runtime + module via `ModuleRecord`
- `SQLiteDriverTest`: Unit tests for driver URL building logic
- `IntegrationTest`: End-to-end database operations via `queryExecute()`

## BoxLang Module Conventions

**Service Registration:**
The module registers as driver name `"sqlite"` (lowercase) but class name is `"SQLite"`. Datasource configs use:
```javascript
{ "driver": "sqlite", "database": "/path/or/memory:dbname" }
```

**Module Structure:**
- `src/main/bx/ModuleConfig.bx`: Module entry point with `this.mapping = "sqlite"`
- `src/main/java/`: Java implementations (drivers, BIFs, components, etc.)
- Java classes auto-discovered via ServiceLoader interfaces in `build.gradle`

## Development Workflow

**Essential Commands:**
```bash
./gradlew build              # Compile, test, package
./gradlew createModuleStructure  # Required before integration tests
./gradlew spotlessApply      # Format code per Ortus standards
./gradlew bumpPatchVersion   # Increment version in gradle.properties
```

**Testing Pattern:**
Integration tests require the module to be built and structured first. Tests create in-memory SQLite DBs and verify BoxLang's `queryExecute()` works end-to-end.

## Database Connection Examples

**In-Memory:** `database: "memory:testDB;create=true"`
**File-Based:** `database: "/absolute/path/to/file.db"`

The driver validation requires `database` property - throws `IllegalArgumentException` if missing.
