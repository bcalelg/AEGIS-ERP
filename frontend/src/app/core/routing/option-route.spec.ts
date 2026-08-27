import { describe, expect, it } from 'vitest';
import { normalizePageIdentifier, routeForOptionPage } from './option-route';

describe('option route strategy', () => {
  it('normalizes legacy and logical page identifiers', () => {
    expect(normalizePageIdentifier('empresa')).toBe('empresa');
    expect(normalizePageIdentifier(' EMPRESA.PHP ')).toBe('empresa');
  });

  it('routes Empresa to its Angular CRUD and other pages to construction', () => {
    expect(routeForOptionPage('empresa')).toEqual(['/empresas']);
    expect(routeForOptionPage('genero')).toEqual(['/generos']);
    expect(routeForOptionPage('status_usuario.php')).toEqual(['/estatus-usuarios']);
    expect(routeForOptionPage('role.php')).toEqual(['/roles']);
    expect(routeForOptionPage('modulo.php')).toEqual(['/modulos']);
    expect(routeForOptionPage('sucursal.php')).toEqual(['/sucursales']);
    expect(routeForOptionPage('menu.php')).toEqual(['/menus']);
    expect(routeForOptionPage('opcion.php')).toEqual(['/opciones']);
    expect(routeForOptionPage('asignacion_opcion_role.php')).toEqual(['/asignar-opciones-rol']);
    expect(routeForOptionPage('pendiente.php')).toEqual(['/construction', 'pendiente']);
  });
});
