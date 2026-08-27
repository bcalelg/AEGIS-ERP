# Instalación local reproducible de AEGIS-ERP

Esta guía parte de una PC Windows nueva y de un clon limpio. No requiere secretos ni configuraciones privadas del desarrollador principal.

## 1. Arquitectura y versiones comprobadas

| Componente | Requisito real del proyecto |
|---|---|
| Git | Cliente Git compatible con GitHub; el repositorio no fija versión |
| Java | JDK 21 LTS |
| Maven | 3.6.3 o superior; el repositorio no incluye Maven Wrapper |
| Node.js | 22.22.3 o superior compatible con Angular 22 |
| npm | 10.9.8 declarado por el proyecto |
| Oracle | Oracle Database XE con PDB `XEPDB1`; la edición exacta no está fijada en el repositorio |
| SQL Developer | Opcional, sin versión fijada; debe soportar Oracle XE y ejecución de scripts |

Compruebe las herramientas en PowerShell:

```powershell
git --version
java -version
mvn -version
node -v
npm -v
```

`mvn -version` es la comprobación decisiva: debe indicar que Maven usa un JDK 21. No instale un JDK futuro para compensar errores del editor.

## 2. Clonar el proyecto

```powershell
Set-Location C:\
git clone https://github.com/bcalelg/AEGIS-ERP.git
Set-Location C:\AEGIS-ERP
git status
```

La rama principal actual es `main`. No use tokens personales dentro de comandos, archivos o capturas.

## 3. Preparar Oracle XE y la PDB

Oracle XE separa el contenedor raíz `CDB$ROOT`, destinado a administración global, de la base conectable `XEPDB1`, donde debe existir el usuario local de AEGIS-ERP.

En SQL Developer cree primero una conexión administrativa cuyo **Service name** sea `XEPDB1`, no el SID del contenedor raíz. Compruebe:

```sql
SHOW CON_NAME;
```

El resultado esperado es:

```text
XEPDB1
```

`ORA-65096` normalmente significa que se intentó crear el usuario local desde `CDB$ROOT`. Corrija la conexión y vuelva a entrar directamente a `XEPDB1`; no conceda DBA como atajo.

## 4. Crear el usuario/esquema AEGIS_ERP

Abra [SETUP_USUARIO_ORACLE.sql](../database/scripts/SETUP_USUARIO_ORACLE.sql), sustituya únicamente `CAMBIAR_PASSWORD_LOCAL` por una contraseña local segura y ejecútelo como script (`F5` en SQL Developer) desde una cuenta administrativa conectada a `XEPDB1`.

El script:

- detiene la ejecución si el contenedor no es `XEPDB1`;
- crea el usuario local `AEGIS_ERP`;
- asigna una cuota de 200 MB en `USERS`;
- concede `CREATE SESSION` para conectar;
- concede `CREATE TABLE` para las 12 tablas que crea `ERP.sql`.

No concede `DBA`, `SYSDBA` ni `ALL PRIVILEGES`. `ERP.sql` no crea vistas, secuencias explícitas, procedimientos, funciones, paquetes, triggers, tipos ni sinónimos, por lo que no necesita privilegios para esos objetos.

Si su instalación no posee los tablespaces `USERS` y `TEMP`, un administrador debe adaptar esos dos nombres a los tablespaces locales equivalentes antes de ejecutar el script.

## 5. Ejecutar ERP.sql como propietario

En SQL Developer cree una segunda conexión:

| Campo | Valor |
|---|---|
| Usuario | `AEGIS_ERP` |
| Password | La contraseña local elegida en el paso anterior |
| Host | `localhost` |
| Puerto | `1521` |
| Tipo | Service name |
| Servicio | `XEPDB1` |

Pruebe la conexión y ejecute [ERP.sql](../database/scripts/ERP.sql) como script (`F5`) y como `AEGIS_ERP`, no como `SYS`. El script crea las tablas, relaciones, catálogos y usuarios iniciales, y finaliza con `COMMIT`.

Verifique la instalación:

```sql
SELECT USER FROM DUAL;

SELECT COUNT(*) AS TOTAL_TABLAS
FROM USER_TABLES;

SELECT TABLE_NAME
FROM USER_TABLES
ORDER BY TABLE_NAME;

SELECT
    TABLE_NAME,
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    STATUS
FROM USER_CONSTRAINTS
ORDER BY TABLE_NAME, CONSTRAINT_NAME;

SELECT COUNT(*) AS TOTAL_USUARIOS FROM USUARIO;
SELECT COUNT(*) AS TOTAL_OPCIONES FROM OPCION;
SELECT COUNT(*) AS TOTAL_ASIGNACIONES FROM ROLE_OPCION;
```

En un esquema nuevo, `TOTAL_TABLAS` debe ser 12. Las credenciales académicas iniciales están identificadas en el comentario inmediatamente anterior a los `INSERT INTO USUARIO` de `ERP.sql`; úselas solo en desarrollo y complete el cambio obligatorio solicitado al entrar.

### Reinicio de instalación — destructivo

No hay comandos destructivos automatizados. Si realmente necesita borrar un esquema, deténgase, respalde lo necesario y solicite al administrador de Oracle una operación explícita. Nunca ejecute un `DROP USER ... CASCADE` como parte del arranque normal.

## 6. Crear la configuración local

La raíz contiene [env.example.ps1](../env.example.ps1), que solo tiene placeholders. Cree su copia privada:

```powershell
Set-Location C:\AEGIS-ERP
Copy-Item env.example.ps1 env.local.ps1
code env.local.ps1
```

La contraseña de `DB_PASSWORD` debe coincidir con la elegida al crear `AEGIS_ERP`. Cada desarrollador puede usar una diferente. `env.local.ps1`, `.env`, `.env.*`, `application-local.properties`, `application-local.yml` y `*.local.ps1` están ignorados; `env.example.ps1` sí debe versionarse.

### Variables utilizadas por el backend

| Variable | Uso | Obligatoria |
|---|---|---|
| `DB_URL` | JDBC Oracle | No; predetermina `jdbc:oracle:thin:@//localhost:1521/XEPDB1` |
| `DB_USERNAME` | Esquema Oracle y schema Hibernate | No; predetermina `AEGIS_ERP` |
| `DB_PASSWORD` | Contraseña Oracle | Sí |
| `JWT_SECRET` | Firma del JWT de autenticación | Sí |
| `JWT_EXPIRATION_MINUTES` | Vigencia JWT | No; predetermina 60 |
| `AEGIS_COOKIE_NAME` | Nombre de cookie HttpOnly | No |
| `AEGIS_COOKIE_SECURE` | Marca Secure | No; `false` solo para HTTP local |
| `AEGIS_COOKIE_SAME_SITE` | Política SameSite | No; predetermina `Lax` |
| `PASSWORD_RECOVERY_SECRET` | Firma exclusiva del token de recuperación | Sí |
| `PASSWORD_RECOVERY_EXPIRATION_MINUTES` | Vigencia de recuperación | No; predetermina 15 |
| `PASSWORD_RECOVERY_ISSUER` | Emisor del token | No |
| `PASSWORD_RECOVERY_COOLDOWN_SECONDS` | Espera entre solicitudes | No; predetermina 60 |
| `APP_FRONTEND_URL` | Base del enlace enviado | No; predetermina `http://localhost:4200` |
| `MAIL_HOST` | Servidor SMTP | Necesaria para correo real |
| `MAIL_PORT` | Puerto SMTP | Necesaria para correo real |
| `MAIL_USERNAME` | Remitente SMTP | Necesaria para correo real |
| `MAIL_PASSWORD` | Credencial SMTP | Necesaria para correo real |
| `MAIL_SMTP_AUTH` | Activa SMTP AUTH | Necesaria para Gmail |
| `MAIL_STARTTLS` | Activa STARTTLS | Necesaria para Gmail |

### Generar secretos independientes

En PowerShell 7 genere 64 bytes aleatorios para `JWT_SECRET`:

```powershell
$bytes = New-Object byte[] 64
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

Copie el resultado únicamente en `JWT_SECRET`. Ejecute nuevamente las tres líneas para obtener otro valor y colóquelo en `PASSWORD_RECOVERY_SECRET`. Los dos valores **no deben ser iguales**.

Si Windows PowerShell 5.1 indica que `Fill` no existe, use esta alternativa .NET compatible:

```powershell
$bytes = New-Object byte[] 64
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
[Convert]::ToBase64String($bytes)
```

Repita el bloque completo para el segundo secreto. No use palabras, nombres del proyecto ni cadenas cortas.

## 7. Configurar Gmail SMTP

La plantilla usa la configuración compatible con Gmail confirmada para este proyecto:

```powershell
$env:MAIL_HOST = 'smtp.gmail.com'
$env:MAIL_PORT = '587'
$env:MAIL_SMTP_AUTH = 'true'
$env:MAIL_STARTTLS = 'true'
```

`MAIL_USERNAME` es la cuenta remitente. `MAIL_PASSWORD` debe ser una **contraseña de aplicación de Google**, no la contraseña normal de Gmail. Si el equipo comparte una cuenta AEGIS, entregue ambas credenciales solo por un canal privado y autorizado, preferiblemente a quienes realmente probarán SMTP. Nunca las publique en GitHub, README, commits, issues o capturas.

## 8. Cargar y comprobar variables

Desde la raíz:

```powershell
Set-Location C:\AEGIS-ERP
. .\env.local.ps1
```

La sintaxis es **punto, espacio, ruta**. Así las variables permanecen en la sesión actual. Ejecutar solo `.\env.local.ps1` crea otro alcance y no es el mecanismo recomendado. Al abrir otra PowerShell debe volver a cargar el archivo.

Compruebe valores no sensibles:

```powershell
$env:DB_URL
$env:DB_USERNAME
$env:MAIL_HOST
$env:MAIL_PORT
$env:MAIL_USERNAME
$env:MAIL_SMTP_AUTH
$env:MAIL_STARTTLS
```

Compruebe secretos sin mostrarlos:

```powershell
if ($env:DB_PASSWORD) { 'DB_PASSWORD cargado' } else { 'DB_PASSWORD NO cargado' }
if ($env:JWT_SECRET) { 'JWT_SECRET cargado' } else { 'JWT_SECRET NO cargado' }
if ($env:PASSWORD_RECOVERY_SECRET) { 'PASSWORD_RECOVERY_SECRET cargado' } else { 'PASSWORD_RECOVERY_SECRET NO cargado' }
if ($env:MAIL_PASSWORD) { 'MAIL_PASSWORD cargada' } else { 'MAIL_PASSWORD NO cargada' }
```

## 9. Iniciar el backend

Use la misma PowerShell donde cargó las variables:

```powershell
Set-Location C:\AEGIS-ERP
. .\env.local.ps1
Set-Location backend
mvn spring-boot:run
```

No hay `mvnw` en el repositorio, por lo que Maven debe estar instalado. Spring escucha en `http://localhost:8080`. Espere mensajes equivalentes a `Tomcat started on port 8080` y `Started AegisErpApplication`; PID y tiempo varían.

En otra PowerShell compruebe:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8080/api/health/database
```

Ambos endpoints son públicos. El segundo confirma la conexión real con Oracle.

## 10. Iniciar el frontend

Abra otra PowerShell:

```powershell
Set-Location C:\AEGIS-ERP\frontend
npm install
npm start
```

Abra `http://localhost:4200`. Backend y frontend deben permanecer activos en terminales separadas.

El servidor de desarrollo de Angular recibe las peticiones `/api` en 4200 y `proxy.conf.json` las reenvía a Spring en 8080:

```text
Navegador :4200
      ↓ /api
Proxy Angular
      ↓
Spring Boot :8080
      ↓
Oracle XEPDB1
```

Por eso DevTools puede mostrar `http://localhost:4200/api/...` aunque Spring escuche en 8080.

## 11. Orden de arranque y checklist funcional

Orden recomendado: Oracle XE → comprobar `XEPDB1` → cargar `env.local.ps1` → backend → health checks → frontend → navegador → login.

- [ ] Oracle XE está activo.
- [ ] `XEPDB1` está accesible.
- [ ] `AEGIS_ERP` conecta en SQL Developer.
- [ ] `ERP.sql` fue ejecutado como `AEGIS_ERP`.
- [ ] Las 12 tablas fueron creadas.
- [ ] Variables locales cargadas.
- [ ] Backend inicia en 8080.
- [ ] `/api/health` responde.
- [ ] `/api/health/database` responde.
- [ ] Frontend inicia en 4200.
- [ ] Pantalla de login abre.
- [ ] Login válido y cambio obligatorio funcionan.
- [ ] Dashboard abre.
- [ ] Menú dinámico carga según permisos.
- [ ] Empresa funciona.
- [ ] Sucursales funciona.
- [ ] Géneros funciona.
- [ ] Estatus Usuario funciona.
- [ ] Roles funciona.
- [ ] Módulos funciona.
- [ ] Menús funciona.
- [ ] Opciones funciona.
- [ ] Usuarios funciona.
- [ ] Asignación `ROLE_OPCION` funciona.
- [ ] Logout funciona.

## 12. Recuperación y cambio de contraseña

Prueba opcional de recuperación SMTP:

1. Configure todas las variables `MAIL_*` y `APP_FRONTEND_URL`.
2. Inicie Spring desde esa misma sesión.
3. Abra “¿Olvidaste tu contraseña?”.
4. Solicite recuperación para un usuario cuyo correo sea accesible.
5. Revise entrada y spam, abra el enlace y establezca una contraseña que cumpla la política de Empresa.
6. Pruebe el login con la nueva contraseña.

Si el POST responde 200 pero no llega correo, la respuesta genérica es deliberada para no revelar usuarios. Compruebe variables en la misma sesión, logs del backend, remitente, contraseña de aplicación, spam y dirección almacenada del usuario.

Para probar cambio de contraseña: inicie sesión, complete el cambio con la política empresarial, cierre sesión, entre con la nueva contraseña y confirme que la anterior es rechazada.

## 13. Arranque normal después de la primera instalación

Antes de actualizar, confirme que no tenga trabajo local pendiente:

```powershell
Set-Location C:\AEGIS-ERP
git status
git pull origin main
. .\env.local.ps1
Set-Location backend
mvn spring-boot:run
```

Frontend, en otra PowerShell:

```powershell
Set-Location C:\AEGIS-ERP\frontend
npm start
```

Si `git status` muestra modificaciones, no haga reset automáticamente. Revise y preserve su trabajo antes de ejecutar `git pull`.

## 14. Ejecutar pruebas

Backend:

```powershell
Set-Location C:\AEGIS-ERP\backend
mvn clean verify
```

Frontend Angular 22 (el builder actual ejecuta una sola vez):

```powershell
Set-Location C:\AEGIS-ERP\frontend
npm run build
npm test
```

No agregue `--watch=false`: el builder `@angular/build:unit-test` configurado actualmente no reconoce ese argumento.

## 15. Información confidencial

| Elemento | GitHub | ¿Compartir? | Método correcto |
|---|---|---|---|
| Código fuente | Sí | Equipo | Repositorio |
| `ERP.sql` | Sí | Equipo | Repositorio |
| `SETUP_USUARIO_ORACLE.sql` | Sí | Equipo | Repositorio, sin contraseña real |
| `env.example.ps1` | Sí | Equipo | Repositorio, solo placeholders |
| `env.local.ps1` | No | No normalmente | Solo PC local |
| `DB_USERNAME` | Puede documentarse | Equipo | Documentación/configuración |
| `DB_PASSWORD` | No | Solo si fuera imprescindible | Canal privado autorizado |
| `JWT_SECRET` | No | No | Generación local |
| `PASSWORD_RECOVERY_SECRET` | No | No | Generación local independiente |
| `MAIL_USERNAME` | No en repositorio | Solo autorizados | Canal privado |
| `MAIL_PASSWORD` | Nunca | Solo autorizados | Gestor de secretos/canal privado |
| JWT emitidos | Nunca | No | Memoria/cookie HttpOnly |
| Tokens de recuperación | Nunca | Solo usuario destinatario | Enlace de correo temporal |
| Cookies | Nunca | No | Almacenamiento administrado por navegador |

GitHub contiene código, documentación, scripts sin secretos y plantillas. Los secretos permanecen locales; las credenciales SMTP compartidas permanecen privadas. Nunca coloque passwords, App Passwords, tokens o cookies reales en commits, documentación, issues o capturas.

## 16. Solución de problemas

### Oracle

- `ORA-65096`: conectado a `CDB$ROOT`; conecte la cuenta administrativa directamente a `XEPDB1`.
- `ORA-01017`: usuario o contraseña incorrectos; haga coincidir Oracle con `DB_USERNAME`/`DB_PASSWORD`.
- `ORA-12514`: el listener no reconoce el servicio; revise que XE/PDB estén activos y use service name `XEPDB1`.
- Backend no conecta: compruebe `DB_URL`, credenciales y `/api/health/database`.
- Usuario sin privilegios: ejecute/revise `SETUP_USUARIO_ORACLE.sql`; no conceda DBA como solución rápida.

### Java y Maven

```powershell
java -version
mvn -version
```

Si VS Code marca errores pero `mvn clean verify` termina en `BUILD SUCCESS`, puede ser caché o un JDK distinto en Java Language Server. Use `Java: Clean Java Language Server Workspace`, `Java: Configure Java Runtime` y `Developer: Reload Window`.

### Frontend

```powershell
node -v
npm -v
npm install
```

Si Spring funciona pero `/api` falla, revise que `npm start` haya cargado `proxy.conf.json` y que 8080 esté disponible. Un error WebSocket/HMR no implica por sí solo que la API HTTP o el backend hayan fallado.

### SMTP

```powershell
$env:MAIL_HOST
$env:MAIL_PORT
$env:MAIL_USERNAME
if ($env:MAIL_PASSWORD) { 'MAIL_PASSWORD cargada' } else { 'MAIL_PASSWORD NO cargada' }
```

Las variables deben existir en la misma sesión desde la que se inicia Spring. Para Gmail confirme SMTP AUTH, STARTTLS, puerto 587 y una contraseña de aplicación vigente.
