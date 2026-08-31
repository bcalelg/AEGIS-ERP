# Decisiones técnicas de AEGIS-ERP

## OPCION.PAGINA como identificador técnico inmutable

`OPCION.PAGINA` es el identificador lógico que conecta el menú dinámico y los permisos con la resolución de rutas Angular en `option-route.ts`. No representa necesariamente una URL física.

Regla administrativa:

- **Creación:** Menú, Nombre, Página y Orden son editables.
- **Edición:** Menú, Nombre y Orden continúan editables; Página se muestra, pero es inmutable.
- El contrato backend de actualización no contiene `pagina`.
- El método de dominio de actualización conserva el valor original de `OPCION.PAGINA`.
- Cambiar Menú, Nombre u Orden actualiza la misma entidad: no elimina ni recrea `ID_OPCION`.
- Como `ROLE_OPCION` referencia `ID_OPCION`, las asignaciones existentes se conservan.
- El menú dinámico refleja el nuevo Nombre, Menú u Orden y continúa entregando la Página original.

La inmutabilidad se aplica en la aplicación y no mediante trigger o constraint Oracle, porque la regla distingue creación de actualización y pertenece al contrato funcional del mantenimiento.

## Protección operativa de la navegación dinámica

Módulos, Menús y Opciones forman parte de la configuración estructural del menú dinámico. Por seguridad operativa, la interfaz administrativa no expone la acción Eliminar. Los endpoints y restricciones de backend/base de datos permanecen protegidos como defensa adicional.

## Mi Perfil y fotografía del usuario autenticado

- `USUARIO.FOTOGRAFIA` ya existe como `BLOB`; la funcionalidad reutiliza ese campo y no modifica el esquema Oracle.
- Mi Perfil es autoservicio y permanece separado del mantenimiento administrativo Usuarios. No depende de permisos `USUARIO.CONSULTAR` o `USUARIO.CAMBIO`.
- La identidad se obtiene de la autenticación JWT actual (`authentication.getName()`); los endpoints no aceptan un `ID_USUARIO` seleccionable.
- Mi Perfil implementa autoservicio limitado. El usuario autenticado puede modificar únicamente su información de contacto y fotografía. Los datos de identidad, seguridad y asignación organizacional son administrados desde el mantenimiento de Usuarios.
- El `ProfileUpdateRequest` contiene exclusivamente correo electrónico y teléfono móvil, y el servicio asigna esos campos de forma explícita. Nombre, apellidos, usuario, fecha de nacimiento, género, empresa, sucursal, rol, estatus, contraseña, recuperación y auditoría no forman parte del contrato de autoservicio.
- Como protección contra *mass assignment* u *over-posting*, cualquier propiedad JSON adicional en el `PUT /api/security/profile` se rechaza con HTTP 400. La aplicación no usa copia masiva de propiedades para este flujo.
- La fotografía se transfiere como contenido binario mediante `/api/security/profile/photo`, no como Base64 dentro del perfil.
- Se admiten JPEG, PNG y WebP hasta 2 MB. El backend valida archivo no vacío, MIME permitido y firma binaria compatible, rechazando SVG y contenido arbitrario.
- La fotografía no forma parte del JWT ni aumenta el tamaño de su cookie HttpOnly.
- Angular administra la fotografía mediante un `Blob` y un `ObjectURL` compartido por Mi Perfil, navbar y sidebar. Al reemplazarla o cerrar sesión libera el URL anterior.
- Cuando no existe fotografía, la interfaz conserva el fallback circular con las iniciales del nombre y apellido.
