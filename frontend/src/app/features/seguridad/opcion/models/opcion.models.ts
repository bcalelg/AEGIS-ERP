export interface OpcionMaintenance {
  id: number;
  idMenu: number;
  nombreMenu: string;
  nombreModulo: string;
  nombre: string;
  pagina: string;
  orden: number;
}

export interface OpcionCreateRequest {
  idMenu: number;
  nombre: string;
  pagina: string;
  orden: number;
}

export interface OpcionUpdateRequest {
  idMenu: number;
  nombre: string;
  orden: number;
}

export interface MenuOption {
  id: number;
  nombre: string;
  modulo: string;
}
