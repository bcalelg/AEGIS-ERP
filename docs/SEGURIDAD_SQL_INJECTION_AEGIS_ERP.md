# Auditoría defensiva de SQL Injection de AEGIS-ERP

## Alcance

Se revisaron el login, recuperación de contraseña, repositorios de usuarios, filtros de mantenimientos, consultas JPQL/nativas, uso de `JdbcTemplate`, ordenamiento y respuestas de error del backend. Las pruebas se ejecutan únicamente contra la aplicación local y usan payloads no destructivos.

## Flujo real del login

`POST /api/auth/login` recibe `idUsuario` y `password`. `AuthenticationService` consulta `UsuarioRepository.findForAuthentication(idUsuario)`. La consulta JPQL usa `where u.idUsuario = :idUsuario`; Hibernate enlaza el texto recibido como valor y no lo incorpora a la estructura SQL.

La contraseña no forma parte de ninguna consulta. Después de recuperar al usuario, se verifica mediante `PasswordEncoder.matches(passwordEnClaro, hashBCrypt)`. El hash y la contraseña no se incluyen en respuestas ni logs.

## Mecanismos y consultas revisados

- Los métodos derivados de Spring Data y todas las consultas JPQL usan parámetros enlazados.
- Las consultas nativas encontradas son conteos de dependencias y usan parámetros nombrados (`:idGenero`, `:idRole`, `:idModulo`, `:idStatusUsuario` o `:idSucursal`). No concatenan entrada.
- El único `JdbcTemplate` se usa en el diagnóstico interno de Oracle con SQL constante y sin datos del request.
- No se encontró uso de `EntityManager`, `Statement`, `createQuery` o `createNativeQuery` construido dinámicamente.
- No se encontró SQL dinámico de segundo orden.

## Recuperación de contraseña

`UsuarioRepository.findForPasswordRecovery(identifier)` compara el identificador mediante los parámetros enlazados `:identifier` contra usuario o correo. La respuesta permanece genérica para evitar enumeración y solo se intenta enviar correo cuando existe una cuenta activa con correo registrado. Un payload de inyección es un identificador inexistente, no una expresión SQL.

## Búsquedas, LIKE y ordenamiento

El buscador paginado de Empresa usa `:search` dentro de `concat('%', :search, '%')`. Los demás mantenimientos cargan mediante repositorios Spring Data y filtran colecciones en Java; no construyen SQL con el texto de búsqueda.

`%` y `_` conservan su semántica de comodines en el buscador JPQL de Empresa. Esto puede ampliar resultados, pero no cambia la consulta ni constituye SQL Injection. Si el producto requiere búsqueda literal, el escape de comodines debe tratarse como una decisión funcional separada.

Empresa acepta ordenamiento de Spring Data `Pageable`. El nombre se resuelve como propiedad persistente y no se concatena mediante SQL escrito por AEGIS-ERP. No existe un `ORDER BY` manual construido con parámetros del cliente.

## Hallazgo y corrección

No se encontró SQL Injection. Sí se identificó una causa independiente capaz de producir HTTP 500 para un usuario inexistente: el intento se audita buscando el catálogo `TIPO_ACCESO`; si ese catálogo está incompleto, `AccessAuditService` lanza una excepción antes de que se entregue el 401.

La autenticación rechazada ahora conserva siempre la respuesta genérica 401 aunque falle su registro de auditoría. La falla de auditoría se registra internamente sin incluir el identificador ingresado, contraseña, JWT, cookie, hash ni token. El manejador global también registra excepciones inesperadas con un identificador de correlación y mantiene un `ProblemDetail` genérico hacia el cliente.

## Pruebas de regresión

Se cubren directamente contra `/api/auth/login`:

- usuario `' OR '1'='1` y contraseña arbitraria;
- variantes con comillas, comentarios y expresiones booleanas;
- usuario normal y payload en contraseña;
- ausencia de `Set-Cookie`;
- HTTP 401 y detalle genérico sin errores Oracle.

Una prueba del servicio verifica además que el texto exacto llega sin transformación a `findForAuthentication` y que una falla del catálogo de auditoría no lo convierte en 500.

## Demostración manual

1. En Usuario escribir `' OR '1'='1` y en Contraseña `123456`.
2. Enviar el formulario.
3. Debe mostrarse credenciales incorrectas, HTTP 401, sin cookie JWT y sin acceso al dashboard.
4. Repetir con un usuario válido y contraseña `' OR '1'='1`; el resultado debe ser el mismo.

## Limitaciones y hallazgos separados

Esta auditoría no sustituye pruebas dinámicas con una instancia Oracle configurada idénticamente a producción. La posible inyección de fórmulas en CSV/Excel es una categoría distinta y no se modificó en esta tarea. Tampoco se amplió el alcance a una auditoría general de XSS, CSRF o JWT.

## Explicación para defensa académica

AEGIS-ERP evita SQL Injection mediante consultas parametrizadas proporcionadas por Spring Data JPA/JDBC preparado. Los valores introducidos por el usuario se envían como parámetros y nunca se concatenan directamente al SQL. La contraseña además no se compara mediante SQL, sino mediante BCrypt en la capa de aplicación.
