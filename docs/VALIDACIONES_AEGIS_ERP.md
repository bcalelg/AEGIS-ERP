# Validaciones de datos de AEGIS-ERP

Fecha de auditoría: 24 de agosto de 2026.

## Criterio de capas

- **Frontend:** experiencia de usuario, mensajes inline y prevención temprana.
- **Backend:** autoridad para validar requests y reglas de negocio.
- **Oracle:** garantía final de `NOT NULL`, `UNIQUE`, FK y `CHECK` deterministas.

Clasificación de fuentes: **A** requerida por `erp.docx`; **B** definida en `ERP.sql`; **C** backend; **D** frontend; **E** recomendación técnica.

## Matriz Usuario: auditoría y corrección

| Campo | Requisito Word | Restricción Oracle | Backend | Frontend | Brecha encontrada | Corrección |
|---|---|---|---|---|---|---|
| ID_USUARIO | Cuenta de acceso | PK, `VARCHAR2(50)`, `NOT NULL` | `@NotBlank`, 50; trim; duplicado anticipado | required, 50 | Sin brecha clara | Sin cambio; no se altera case-sensitivity del login |
| NOMBRE | Dato del usuario | `VARCHAR2(100) NOT NULL` | required, 100, trim | required, 100 | Sin brecha | Sin regex ASCII; admite José |
| APELLIDO | Dato del usuario | `VARCHAR2(100) NOT NULL` | required, 100, trim | required, 100 | Sin brecha | Sin regex simplista; admite Muñoz, María-José y O'Connor |
| FECHA_NACIMIENTO | Dato del usuario; edad mínima confirmada: 18 | `DATE NOT NULL` | Antes: solo `@Past`; ahora `@Adult` | Antes: required; ahora max dinámico y validator | No calculaba año/mes/día | `fecha <= hoy.minusYears(18)` en Angular y Bean Validation |
| CORREO_ELECTRONICO | Correo válido; necesario para recuperación por correo | 100, UNIQUE; el script lo dejaba nullable | Antes `@Email`; ahora `@NotBlank @Email @Size(100)` | required, email, 100 y mensaje inline | Se aceptaba vacío | `NOT NULL` versionado y validación en ambas capas |
| TELEFONO_MOVIL | Dato móvil; formato internacional razonable | `VARCHAR2(30)`, UNIQUE | Antes solo 30; ahora pattern y 30 | `type=tel`, pattern, 30 | Aceptaba letras | CHECK determinista y validación equivalente |
| ID_GENERO | Selector | FK, `NOT NULL` | existencia en repositorio | selector required | Sin brecha | Sin cambio |
| ID_STATUS_USUARIO | Selector | FK, `NOT NULL` | existencia en repositorio | selector required | Sin brecha | Sin cambio |
| ID_ROLE | Selector | FK, `NOT NULL` | existencia en repositorio | selector required | Sin brecha | Sin cambio |
| ID_SUCURSAL | Selector dependiente | FK, `NOT NULL` | existencia y pertenencia a Empresa | se limpia al cambiar Empresa | Sin brecha | Sin `ID_EMPRESA` nuevo en USUARIO |
| PREGUNTA | Una pregunta | 200, `NOT NULL` | required, trim, 200 | required, 200 | Sin brecha | No se crea catálogo ni múltiples preguntas |
| RESPUESTA | Una respuesta | 200, `NOT NULL` | required en alta; vacía conserva en edición | password input; alta required; edición opcional | Sin brecha | No se devuelve, exporta ni imprime |
| PASSWORD temporal | Política de Empresa | hash 100, `NOT NULL` | confirmación, política y BCrypt | required en alta y confirmación | Sin brecha | Se conserva flujo actual |
| Campos internos | Administración automática | checks/FK/tipos existentes | gestionados por servicios | no forman parte editable del formulario | Sin brecha | Sin cambio |

## Matriz consolidada

| Entidad | Campo | Regla | Fuente | Frontend | Backend | Oracle | Estado |
|---|---|---|---|---|---|---|---|
| Usuario | Fecha nacimiento | Edad ≥ 18 según fecha completa | A/E | Sí | Sí | `NOT NULL`; sin CHECK dinámico | Implementado |
| Usuario | Correo | Obligatorio, formato válido, ≤100 y único | A/B/E | Sí | Sí | `NOT NULL`, UNIQUE, longitud | Implementado |
| Usuario | Teléfono | Opcional; ≤30; solo `0-9 + - ( )` y espacios; único | B/E | Sí | Sí | UNIQUE y CHECK | Implementado |
| Usuario | Empresa–Sucursal | Sucursal existente y perteneciente a Empresa | A/B/C/D | Sí | Sí | FK a Sucursal | Implementado |
| Usuario | Género/Estatus/Rol | ID requerido y existente | A/B/C/D | Sí | Sí | FK | Implementado |
| Usuario | Pregunta/Respuesta | Una; trim; ≤200; respuesta no expuesta | A/B/C/D | Sí | Sí | `NOT NULL`, longitud | Implementado |
| Empresa | Texto/políticas | required, longitudes, positivos y coherencia de longitud | A/B/C/D | Sí | Sí | tipos/UNIQUE | Implementado |
| Sucursal | Empresa/nombre/dirección | required, longitudes, empresa existente y duplicado compuesto | A/B/C/D | Sí | Sí | FK/UNIQUE | Implementado |
| Género/Estatus/Rol | Nombre | required, maxlength y único | A/B/C/D | Sí | Sí | `NOT NULL`/UNIQUE | Implementado |
| Módulo/Menú/Opción | Nombre, orden y padre | required, maxlength, orden ≥1, padre existente | A/B/C/D | Sí | Sí | FK/UNIQUE/CHECK | Implementado |

## Reglas deliberadamente no trasladadas a Oracle

- **Edad mínima:** depende de la fecha actual. No se usa `SYSDATE`/`CURRENT_DATE` en un `CHECK` ni se crea trigger; queda en Angular y Spring Boot.
- **Formato completo de correo:** no se implementa una regex RFC en Oracle. La base garantiza presencia, longitud y unicidad; backend y frontend validan formato.
- **Nombres y apellidos:** no se agrega regex que excluya caracteres internacionales, guiones o apóstrofes.
- **Política de contraseña:** continúa en el backend usando la configuración de Empresa; no se duplica con reglas SQL artificiales.

## SQL manual para instancia existente

**No fue ejecutado contra Oracle.** El desarrollador debe revisar los resultados de los diagnósticos antes de aplicar cada cambio.

### 1. Correo obligatorio

Paso 1 — diagnóstico:

```sql
SELECT ID_USUARIO, CORREO_ELECTRONICO
FROM USUARIO
WHERE CORREO_ELECTRONICO IS NULL
   OR TRIM(CORREO_ELECTRONICO) IS NULL;
```

Si devuelve filas, completar correos válidos y únicos antes del `ALTER`.

Paso 2 — aplicación manual:

```sql
ALTER TABLE USUARIO
MODIFY (CORREO_ELECTRONICO VARCHAR2(100) NOT NULL);
```

Paso 3 — comprobación:

```sql
SELECT NULLABLE, DATA_LENGTH
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME = 'USUARIO'
  AND COLUMN_NAME = 'CORREO_ELECTRONICO';
```

Resultado esperado: `NULLABLE = 'N'` y `DATA_LENGTH = 100`.

### 2. Caracteres permitidos en teléfono

Paso 1 — diagnóstico:

```sql
SELECT ID_USUARIO, TELEFONO_MOVIL
FROM USUARIO
WHERE TELEFONO_MOVIL IS NOT NULL
  AND NOT REGEXP_LIKE(TELEFONO_MOVIL, '^[0-9+() -]+$');
```

Si devuelve filas, no aplicar la constraint hasta corregir los datos.

Paso 2 — aplicación manual:

```sql
ALTER TABLE USUARIO
ADD CONSTRAINT CK_USUARIO_TELEFONO
CHECK (
    TELEFONO_MOVIL IS NULL
    OR REGEXP_LIKE(TELEFONO_MOVIL, '^[0-9+() -]+$')
);
```

Paso 3 — comprobación:

```sql
SELECT CONSTRAINT_NAME, STATUS, VALIDATED
FROM USER_CONSTRAINTS
WHERE TABLE_NAME = 'USUARIO'
  AND CONSTRAINT_NAME = 'CK_USUARIO_TELEFONO';
```

Resultado esperado: constraint `ENABLED` y `VALIDATED`.

## Datos actuales incompatibles

No se consultó ni modificó la instancia Oracle por restricción expresa de la tarea. Los `SELECT` anteriores son el mecanismo obligatorio para determinar incompatibilidades reales antes de aplicar los cambios manuales.
