export interface Permisos {
  consultar: boolean;
  alta: boolean;
  baja: boolean;
  cambio: boolean;
  imprimir: boolean;
  exportar: boolean;
}
export interface OpcionMenu {
  idOpcion: number;
  nombre: string;
  pagina: string;
  ordenMenu: number;
  permisos: Permisos;
}
export interface MenuGrupo {
  idMenu: number;
  nombre: string;
  ordenMenu: number;
  opciones: OpcionMenu[];
}
export interface ModuloMenu {
  idModulo: number;
  nombre: string;
  ordenMenu: number;
  menus: MenuGrupo[];
}
