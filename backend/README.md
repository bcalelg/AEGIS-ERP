# AEGIS-ERP Backend

API REST inicial organizada como monolito modular N-capas. Cada módulo separa `controller`, `service`, `repository`, `dto`, `entity` y `mapper`; los controllers usan DTOs y nunca exponen entidades JPA.

## Requisitos

- Java 21 LTS
- Maven 3.6.3+
- Spring Boot 4.1.0
- Oracle XE, servicio `XEPDB1`, esquema `AEGIS_ERP`

## Configuración y ejecución

Variables: `DB_PASSWORD`, `JWT_SECRET` y `PASSWORD_RECOVERY_SECRET` son obligatorias; `DB_URL` (predeterminada `jdbc:oracle:thin:@//localhost:1521/XEPDB1`) y `DB_USERNAME` (predeterminada `AEGIS_ERP`) son opcionales.

```powershell
cd C:\AEGIS-ERP\backend
$env:DB_PASSWORD = '<su-password-local>'
$env:JWT_SECRET = '<secreto-local-de-al-menos-32-bytes>'
$env:PASSWORD_RECOVERY_SECRET = '<otro-secreto-local-de-al-menos-32-bytes>'
mvn clean verify
mvn spring-boot:run
```

No guarde contraseñas en Git; `.env*` y `application-local.*` están ignorados. Hibernate usa `ddl-auto=validate` y no altera tablas. Oracle y el esquema de Fase 1 deben estar disponibles antes del arranque.

El login acepta `idUsuario` y `password`, conserva la validación BCrypt y emite un JWT cuando la autenticación concluye correctamente.

## Autenticación, cookie y CSRF

La firma JWT usa HMAC-SHA-256 mediante `NimbusJwtEncoder` y `NimbusJwtDecoder`. `JWT_SECRET` es obligatorio y debe aportar al menos 32 bytes. No existe un secreto predeterminado ni se almacena uno en Git. La duración se configura con `JWT_EXPIRATION_MINUTES` y su valor predeterminado es 60 minutos.

```powershell
Set-Location C:\AEGIS-ERP\backend
$env:DB_PASSWORD = '<su-password-local>'
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:PASSWORD_RECOVERY_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:JWT_EXPIRATION_MINUTES = '60'
mvn clean verify
mvn spring-boot:run
```

La API conserva `SessionCreationPolicy.STATELESS`; no utiliza una sesión tradicional de servidor. El JWT se transporta exclusivamente en la cookie HttpOnly `AEGIS_ACCESS_TOKEN` y no se expone en el cuerpo del login. Las operaciones mutables están protegidas con el patrón cookie-to-header de CSRF (`XSRF-TOKEN` / `X-XSRF-TOKEN`).

Cada login exitoso genera un UUID aleatorio que se guarda en `USUARIO.SESION_ACTUAL` y en el claim firmado `jti`. No se guarda el JWT completo ni ningún secreto en esa columna. El logout toma el usuario y el `jti` exclusivamente de Spring Security, limpia `SESION_ACTUAL`, registra la salida y después elimina la cookie.

### Caducidad y cambio obligatorio

La caducidad se calcula con `USUARIO.ULTIMA_FECHA_CAMBIO_PASSWORD` y `EMPRESA.PASSWORD_CANTIDAD_CADUCIDAD_DIAS`. Una fecha nula significa que no puede demostrarse la vigencia de la contraseña y obliga a cambiarla. La caducidad no bloquea la cuenta: establece `REQUIERE_CAMBIAR_PASSWORD = 1`.

Mientras el claim firmado `password_change_required` sea verdadero, Spring Security solo permite `GET /api/auth/me`, `POST /api/auth/change-password`, `POST /api/auth/logout` y el acceso necesario a CSRF. Los endpoints del menú y del negocio quedan rechazados. Tras un cambio exitoso se guarda el nuevo hash BCrypt, se actualiza `ULTIMA_FECHA_CAMBIO_PASSWORD`, se limpia el indicador y se renuevan tanto el `jti` como la cookie.

### Recuperación por correo

La recuperación pública utiliza un token HMAC temporal separado del JWT de autenticación. Requiere un `PASSWORD_RECOVERY_SECRET` independiente, con al menos 32 bytes; su vigencia se configura con `PASSWORD_RECOVERY_EXPIRATION_MINUTES` (15 minutos de forma predeterminada). `APP_FRONTEND_URL` define el origen del enlace enviado por correo.

El envío SMTP se configura con `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH` y `MAIL_STARTTLS`. La respuesta de solicitud siempre es genérica para no revelar si una cuenta existe. Al restablecer se aplica la política de la Empresa, se genera un nuevo BCrypt, se actualiza la fecha de cambio y se elimina `SESION_ACTUAL`, invalidando las sesiones de autenticación anteriores. El token también queda inutilizable porque está vinculado al hash vigente de la contraseña.

Ejemplo de configuración para desarrollo, independiente del proveedor:

```powershell
$env:MAIL_HOST = 'smtp.example.com'
$env:MAIL_PORT = '587'
$env:MAIL_USERNAME = 'example'
$env:MAIL_PASSWORD = 'example'
$env:MAIL_SMTP_AUTH = 'true'
$env:MAIL_STARTTLS = 'true'
$env:APP_FRONTEND_URL = 'http://localhost:4200'
```

Las credenciales SMTP deben suministrarse mediante variables de entorno y no deben versionarse. Los valores anteriores son únicamente placeholders; deben sustituirse localmente por la configuración del proveedor elegido.

El logout auditado requiere que el catálogo real `TIPO_ACCESO` contenga exactamente `Salida del Sistema`. El script actual no incluye ese valor y, por restricción del proyecto, esta implementación no modifica automáticamente Oracle ni `/database`.

### Limitación académica pendiente

**PENDIENTE DE ACLARACIÓN ACADÉMICA:** `PASSWORD_CANTIDAD_PREGUNTAS_VALIDAR` permite N, pero `USUARIO` actualmente modela una sola `PREGUNTA`/`RESPUESTA`.

Temporalmente se conserva una sola pregunta y respuesta, pero no participa en la recuperación por correo. No se creó ninguna tabla ni se reutilizó una columna existente para almacenar tokens.

## Mi Perfil

El autoservicio autenticado utiliza `GET/PUT /api/security/profile` y `GET/PUT/DELETE /api/security/profile/photo`. La identidad siempre procede del JWT validado; no se recibe un identificador de usuario desde el cliente y no se requieren permisos administrativos del mantenimiento Usuarios. El `PUT` del perfil acepta exclusivamente correo electrónico y teléfono móvil; propiedades adicionales se rechazan con HTTP 400 para impedir *mass assignment*.

La fotografía se almacena en `USUARIO.FOTOGRAFIA` (`BLOB`) y se entrega como respuesta binaria con `Cache-Control: no-store`. Se aceptan JPEG, PNG y WebP hasta 2 MB después de validar MIME y firma binaria. La fotografía y cualquier Base64 permanecen fuera del JWT.

## Estándar de formato

El código Java nuevo debe mantenerse formateado y legible: anotaciones y atributos en líneas separadas, métodos claramente delimitados, parámetros largos distribuidos verticalmente y una instrucción por línea. Este estándar aplica a los próximos CRUD sin modificar la arquitectura Spring Boot existente.
