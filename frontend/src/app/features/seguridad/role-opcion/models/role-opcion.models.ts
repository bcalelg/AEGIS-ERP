export interface CatalogOption {
  id: number;
  nombre: string;
}

export interface RoleOpcionMatrixItem {
  idOpcion: number;
  nombreOpcion: string;
  nombreMenu: string;
  ordenMenu: number;
  ordenOpcion: number;
  consultar: boolean;
  alta: boolean;
  baja: boolean;
  cambio: boolean;
  imprimir: boolean;
  exportar: boolean;
}

export interface RoleOpcionSaveRequest {
  idRole: number;
  idModulo: number;
  opciones: RoleOpcionMatrixItem[];
}

export type PermissionKey = 'consultar' | 'alta' | 'baja' | 'cambio' | 'imprimir' | 'exportar';
