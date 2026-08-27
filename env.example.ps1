# Plantilla versionable de configuración local para AEGIS-ERP.
# Copie este archivo como env.local.ps1 y sustituya los placeholders.
# Nunca escriba secretos reales en env.example.ps1.

# Oracle XE / XEPDB1
$env:DB_URL = 'jdbc:oracle:thin:@//localhost:1521/XEPDB1'
$env:DB_USERNAME = 'AEGIS_ERP'
$env:DB_PASSWORD = 'AQUI_PASSWORD_ORACLE_LOCAL'

# JWT de autenticación (secreto Base64 independiente, mínimo 32 bytes)
$env:JWT_SECRET = 'AQUI_JWT_SECRET_BASE64'
$env:JWT_EXPIRATION_MINUTES = '60'
$env:AEGIS_COOKIE_NAME = 'AEGIS_ACCESS_TOKEN'
$env:AEGIS_COOKIE_SECURE = 'false'
$env:AEGIS_COOKIE_SAME_SITE = 'Lax'

# Token temporal de recuperación (NO reutilizar JWT_SECRET)
$env:PASSWORD_RECOVERY_SECRET = 'AQUI_PASSWORD_RECOVERY_SECRET_BASE64'
$env:PASSWORD_RECOVERY_EXPIRATION_MINUTES = '15'
$env:PASSWORD_RECOVERY_ISSUER = 'aegis-erp-password-recovery'
$env:PASSWORD_RECOVERY_COOLDOWN_SECONDS = '60'
$env:APP_FRONTEND_URL = 'http://localhost:4200'

# Gmail SMTP. MAIL_PASSWORD debe ser una contraseña de aplicación de Google.
$env:MAIL_HOST = 'smtp.gmail.com'
$env:MAIL_PORT = '587'
$env:MAIL_USERNAME = 'AQUI_CORREO_REMITENTE'
$env:MAIL_PASSWORD = 'AQUI_PASSWORD_APLICACION_GOOGLE'
$env:MAIL_SMTP_AUTH = 'true'
$env:MAIL_STARTTLS = 'true'
