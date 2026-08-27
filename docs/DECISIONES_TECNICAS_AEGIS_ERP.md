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
