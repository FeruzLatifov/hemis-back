// =====================================================
// HEMIS Domain Module
// =====================================================
// Purpose: JPA entities mapped to legacy ministry.sql schema
// Dependencies: common
// =====================================================

val liquibaseRuntime by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    description = "Classpath for Liquibase CLI tasks (no Gradle plugin)."
}

dependencies {
    // Internal dependencies
    api(project(":common"))

    // Spring Data JPA (version from BOM)
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // PostgreSQL Driver (version from BOM)
    runtimeOnly("org.postgresql:postgresql")

    // Liquibase CLI dependencies (used by custom JavaExec tasks)
    val liquibaseVersion = "4.31.1"
    liquibaseRuntime("org.liquibase:liquibase-core:$liquibaseVersion")
    liquibaseRuntime("org.postgresql:postgresql")
    liquibaseRuntime("info.picocli:picocli:4.7.5")
    liquibaseRuntime("org.yaml:snakeyaml:2.2")

    // Hibernate (version from BOM via spring-boot-starter-data-jpa)
    // No need to declare explicitly

    // Lombok (MUST be before MapStruct)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // MapStruct for Entity ↔ DTO mapping
    // CRITICAL: Version not in BOM, must be explicit
    val mapstructVersion = "1.6.3"
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${mapstructVersion}")

    // Lombok + MapStruct integration (MUST be after both)
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // Spring Boot Validation
    api("org.springframework.boot:spring-boot-starter-validation")

    // Redis moved to app module (domain layer doesn't need caching)
    // implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // implementation("org.springframework.boot:spring-boot-starter-cache")
}

// =====================================================
// Liquibase helper utilities (env loader + CLI tasks)
// =====================================================

data class DbConfig(
    val url: String,
    val username: String,
    val password: String
)

fun Project.loadEnvFile(): Map<String, String> {
    val envFile = rootProject.file(".env")
    if (!envFile.exists()) return emptyMap()
    return envFile.readLines()
        .filter { it.isNotBlank() && !it.trim().startsWith("#") && it.contains("=") }
        .associate {
            val (k, v) = it.split("=", limit = 2)
            k.trim() to v.trim()
        }
}

fun Project.resolveDbConfig(): DbConfig {
    val envMap = loadEnvFile()
    fun lookup(key: String, default: String) =
        System.getenv(key)?.takeIf { it.isNotBlank() }
            ?: envMap[key]?.takeIf { it.isNotBlank() }
            ?: default

    val host = lookup("DB_MASTER_HOST", "localhost")
    val port = lookup("DB_MASTER_PORT", "5432")
    val db = lookup("DB_MASTER_NAME", "test_hemis")
    val user = lookup("DB_MASTER_USERNAME", "postgres")
    val pass = lookup("DB_MASTER_PASSWORD", "postgres")

    return DbConfig(
        url = "jdbc:postgresql://$host:$port/$db",
        username = user,
        password = pass
    )
}

val changelogRelativePath = "src/main/resources/db/changelog/db.changelog-master.yaml"

fun JavaExec.configureLiquibase(command: String, commandArgs: List<String> = emptyList()) {
    val db = project.resolveDbConfig()
    val searchPath = listOf(
        project.layout.projectDirectory.file("src/main/resources").asFile.absolutePath,
        project.layout.projectDirectory.file("src/main/resources/db/changelog").asFile.absolutePath,
        project.layout.projectDirectory.file("src/main/resources/db/changelog/changesets").asFile.absolutePath
    ).joinToString(",")
    group = "liquibase"
    description = "Liquibase $command"
    classpath = project.configurations.getByName("liquibaseRuntime")
    mainClass.set("liquibase.integration.commandline.Main")
    workingDir = project.projectDir

    val globalArgs = listOf(
        "--url=${db.url}",
        "--username=${db.username}",
        "--password=${db.password}",
        "--changeLogFile=$changelogRelativePath"
    )

    args = globalArgs + listOf(command) + commandArgs
    environment("LIQUIBASE_SEARCH_PATH", searchPath)
}

// =====================================================
// Liquibase Tasks (Liquibase 4.x Modern CLI)
// =====================================================

tasks.register<JavaExec>("liquibaseStatus") {
    description = "Show database migration status"
    configureLiquibase("status", listOf("--verbose"))
}

tasks.register<JavaExec>("liquibaseUpdate") {
    description = "Apply all pending migrations"
    configureLiquibase("update")
}

tasks.register<JavaExec>("liquibaseRollbackCount") {
    description = "Rollback N changesets (usage: -Pcount=5)"
    val count = project.findProperty("count")?.toString() ?: "1"
    configureLiquibase("rollbackCount", listOf(count))
}

tasks.register<JavaExec>("liquibaseRollbackToTag") {
    description = "Rollback to specific tag (usage: -Ptag=v3-users-migrated)"
    val tag = project.findProperty("tag")?.toString()
        ?: throw GradleException("Tag required! Use: -Ptag=v3-users-migrated")
    configureLiquibase("rollback", listOf(tag))
}

tasks.register<JavaExec>("liquibaseRollbackSQL") {
    description = "Generate rollback SQL for N changesets (usage: -Pcount=5)"
    val count = project.findProperty("count")?.toString() ?: "1"
    configureLiquibase("rollbackCountSql", listOf(count))
}

tasks.register<JavaExec>("liquibaseListTags") {
    description = "List all database tags"
    // Note: Use liquibaseHistory to see tags
    // tag-exists only checks if a specific tag exists
    configureLiquibase("history", listOf("--format=TEXT"))
}

tasks.register<JavaExec>("liquibaseHistory") {
    description = "Show migration history"
    configureLiquibase("history", listOf("--format=TEXT"))
}

// Backward compatibility aliases
tasks.register<JavaExec>("status") {
    description = "Alias for liquibaseStatus"
    configureLiquibase("status", listOf("--verbose"))
}

tasks.register<JavaExec>("update") {
    description = "Alias for liquibaseUpdate"
    configureLiquibase("update")
}

tasks.register<JavaExec>("rollbackCount") {
    description = "Alias for liquibaseRollbackCount (DEPRECATED - use liquibaseRollbackCount)"
    val count = project.findProperty("liquibase.commandValue")?.toString()
        ?: project.findProperty("rollbackCount")?.toString()
        ?: project.findProperty("count")?.toString()
        ?: "1"
    configureLiquibase("rollbackCount", listOf(count))
}

// =====================================================
// Annotation Processor Configuration
// =====================================================

// Force Liquibase core version to avoid Scope class conflicts

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "-Amapstruct.defaultComponentModel=spring",  // Generate @Component
            "-Amapstruct.unmappedTargetPolicy=WARN"     // Warn on unmapped fields
        )
    )
}

// =====================================================
// Module Role
// =====================================================
// - JPA Entity classes with @Table(name="hemishe_e_*")
// - @Column(name="legacy_name") for all fields
// - Spring Data JPA repositories (NO delete methods)
// - Base entity classes (audit columns pattern)
// - MapStruct mappers for Entity ↔ DTO conversion
//
// CRITICAL CONSTRAINTS:
// - Table/column names MUST match ministry.sql exactly
// - NO schema generation (ddl-auto=none)
// - NO DELETE operations (repository methods prohibited)
// - Liquibase migrations ONLY (Flyway disabled)
// =====================================================
