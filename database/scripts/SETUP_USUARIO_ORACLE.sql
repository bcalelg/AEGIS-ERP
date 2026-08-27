-- AEGIS-ERP - Preparación mínima del esquema local en Oracle XE.
-- Ejecutar como cuenta administrativa conectada directamente a XEPDB1.
-- Antes de ejecutar, sustituir CAMBIAR_PASSWORD_LOCAL por una contraseña local segura.
-- Este script no elimina ni reemplaza usuarios u objetos existentes.

WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;

DECLARE
    v_container VARCHAR2(128);
BEGIN
    v_container := SYS_CONTEXT('USERENV', 'CON_NAME');

    IF UPPER(v_container) <> 'XEPDB1' THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'Contenedor incorrecto: conectese directamente a la PDB XEPDB1.'
        );
    END IF;
END;
/

CREATE USER AEGIS_ERP
    IDENTIFIED BY "CAMBIAR_PASSWORD_LOCAL"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA 200M ON USERS;

-- Permite que el usuario de la aplicación inicie sesión en XEPDB1.
GRANT CREATE SESSION TO AEGIS_ERP;

-- ERP.sql crea 12 tablas propias, incluidas columnas IDENTITY y constraints.
-- No crea vistas, paquetes, procedimientos, funciones, triggers ni sinónimos.
GRANT CREATE TABLE TO AEGIS_ERP;

PROMPT Usuario AEGIS_ERP creado en XEPDB1 con privilegios mínimos.
