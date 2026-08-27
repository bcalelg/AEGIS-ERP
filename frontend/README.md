# AEGIS-ERP Frontend

Aplicación Angular standalone para autenticación JWT, layout administrativo y menú dinámico.

## Requisitos

- Node.js 22.22.3 o superior compatible con Angular 22
- npm
- Backend AEGIS-ERP activo en `http://localhost:8080`

## Desarrollo

```powershell
Set-Location C:\AEGIS-ERP\frontend
npm install
npm start
```

Abrir `http://localhost:4200`. El comando `npm start` utiliza `proxy.conf.json` para enviar `/api` al backend local, por lo que no se necesita CORS durante desarrollo.

## Validación

```powershell
npm run build
npm test -- --watch=false
```

## Autenticación

`AuthService` conserva únicamente el usuario público en memoria. La sesión se restaura con `GET /api/auth/me`; Spring transporta el JWT en una cookie HttpOnly y Angular usa su soporte XSRF estándar. El interceptor se limita al manejo de respuestas 401.

El frontend no decodifica el JWT ni utiliza permisos como mecanismo de seguridad. El backend continúa siendo la autoridad para identidad, menú y autorización.

## Cambio obligatorio de contraseña

El estado restaurado mediante `GET /api/auth/me` incluye `requiereCambiarPassword`. Cuando es verdadero, los guards redirigen a `/change-password` e impiden entrar al shell administrativo. La pantalla utiliza Reactive Forms y permite únicamente cambiar la contraseña o cerrar sesión.

Después de un cambio exitoso, el backend renueva la cookie HttpOnly y devuelve el estado público actualizado. Angular actualiza el usuario en memoria y habilita la navegación normal. Esta redirección frontend complementa la restricción central del backend; no constituye por sí sola el control de seguridad.

## Estándar de estructura para CRUD

Los nuevos mantenimientos frontend deben separar presentación, estilos y lógica con esta estructura:

```text
feature/
├── feature-list/
│   ├── feature-list.component.ts
│   ├── feature-list.component.html
│   └── feature-list.component.css
├── feature-form/
│   ├── feature-form.component.ts
│   ├── feature-form.component.html
│   └── feature-form.component.css
├── models/
└── services/
```

Los estilos compartidos por el shell o por varios componentes permanecen en `src/styles.scss`. Los archivos CSS locales contienen únicamente reglas propias del componente.

### Formato de código

El TypeScript nuevo debe conservar imports, propiedades y métodos en formato vertical y legible. Los templates HTML y estilos específicos deben permanecer separados en sus archivos de componente. Prettier y la configuración .prettierrc del frontend definen el formato automático del proyecto.
