export interface Sucursal {
  id: number;
  idEmpresa: number;
  nombreEmpresa: string;
  nombre: string;
  direccion: string;
}

export interface SucursalRequest {
  idEmpresa: number;
  nombre: string;
  direccion: string;
}

export interface EmpresaOption {
  id: number;
  nombre: string;
}
