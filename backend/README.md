# AEGIS-ERP Backend

API REST inicial organizada como monolito modular N-capas. Cada módulo separa `controller`, `service`, `repository`, `dto`, `entity` y `mapper`; los controllers usan DTOs y nunca exponen entidades JPA.

## Requisitos

- Java 21 LTS
- Maven 3.6.3+
- Spring Boot 4.1.0
- Oracle XE, servicio `XEPDB1`, esquema `AEGIS_ERP`

## Configuración y ejecución

Variables: `DB_PASSWORD` es obligatoria; `DB_URL` (predeterminada `jdbc:oracle:thin:@//localhost:1521/XEPDB1`) y `DB_USERNAME` (predeterminada `AEGIS_ERP`) son opcionales.

```powershell
cd C:\AEGIS-ERP\backend
$env:DB_PASSWORD = '<su-password-local>'
mvn clean verify
mvn spring-boot:run
```

No guarde contraseñas en Git; `.env*` y `application-local.*` están ignorados. Hibernate usa `ddl-auto=validate` y no altera tablas. Oracle y el esquema de Fase 1 deben estar disponibles antes del arranque.

Endpoints: `GET http://localhost:8080/api/health` y `GET http://localhost:8080/api/health/database`. El segundo consulta Oracle mediante `JdbcTemplate`. Spring Security permite temporalmente sólo ambos health checks.
