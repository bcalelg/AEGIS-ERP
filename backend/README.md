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

El login acepta `idUsuario` y `password`, conserva la validación BCrypt y emite un JWT cuando la autenticación concluye correctamente.

## JWT stateless

JWT ya esta implementado. La firma usa HMAC-SHA-256 mediante `NimbusJwtEncoder` y `NimbusJwtDecoder`. La referencia anterior a una fase futura queda reemplazada por esta seccion.

`JWT_SECRET` es obligatorio y debe aportar al menos 32 bytes. No existe un secreto predeterminado ni se almacena uno en Git. La duracion se configura con `JWT_EXPIRATION_MINUTES` y su valor predeterminado es 60 minutos.

```powershell
Set-Location C:\AEGIS-ERP\backend
$env:DB_PASSWORD = '<su-password-local>'
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:JWT_EXPIRATION_MINUTES = '60'
mvn clean verify
mvn spring-boot:run
```

La API usa `SessionCreationPolicy.STATELESS`; no habilita form login ni HTTP Basic. CSRF esta desactivado porque el Bearer token se envia explicitamente en un encabezado y no mediante cookies automaticas.

### Login y Bearer token

```powershell
$loginBody = @{
    idUsuario = 'Administrador'
    password = 'ITAdmin'
} | ConvertTo-Json

$login = Invoke-RestMethod `
    -Method Post `
    -Uri 'http://localhost:8080/api/auth/login' `
    -ContentType 'application/json' `
    -Body $loginBody

$login
$headers = @{ Authorization = "Bearer $($login.accessToken)" }
Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/auth/me' -Headers $headers
```

El login exitoso devuelve `tokenType=Bearer`, `accessToken`, `expiresIn` en segundos y los datos publicos existentes del usuario. El JWT contiene unicamente `sub`, `role`, `iat` y `exp`.

Para probar el rechazo sin token:

```powershell
try {
    Invoke-RestMethod -Method Get -Uri 'http://localhost:8080/api/auth/me'
} catch {
    $_.Exception.Response.StatusCode
    $_.ErrorDetails.Message
}
```

Endpoints publicos: `GET /api/health`, `GET /api/health/database` y `POST /api/auth/login`. Cualquier otro endpoint, incluido `GET /api/auth/me`, requiere `Authorization: Bearer <token>`.
