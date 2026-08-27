export function normalizePageIdentifier(page: string): string {
  return page
    .trim()
    .toLowerCase()
    .replace(/\.php$/, '');
}

const optionRoutes: Readonly<Record<string, readonly string[]>> = {
  empresa: ['/empresas'],
  genero: ['/generos'],
  status_usuario: ['/estatus-usuarios'],
  role: ['/roles'],
  modulo: ['/modulos'],
  menu: ['/menus'],
  opcion: ['/opciones'],
  asignacion_opcion_role: ['/asignar-opciones-rol'],
  sucursal: ['/sucursales'],
  usuario: ['/usuarios'],
};

export function routeForOptionPage(page: string): readonly string[] {
  const logicalPage = normalizePageIdentifier(page);
  return optionRoutes[logicalPage] ?? ['/construction', logicalPage];
}
