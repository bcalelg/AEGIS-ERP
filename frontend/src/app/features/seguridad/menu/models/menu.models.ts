export interface MenuMaintenance {
  id: number;
  idModulo: number;
  nombreModulo: string;
  nombre: string;
  orden: number;
}

export interface MenuRequest {
  idModulo: number;
  nombre: string;
  orden: number;
}

export interface ModuloOption {
  id: number;
  nombre: string;
}
