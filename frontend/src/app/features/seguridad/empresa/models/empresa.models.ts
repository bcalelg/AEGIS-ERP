export interface Empresa {
  idEmpresa: number;
  nombre: string;
  direccion: string;
  nit: string;
  passwordCantidadMayusculas: number;
  passwordCantidadMinusculas: number;
  passwordCantidadCaracteresEspeciales: number;
  passwordCantidadCaducidadDias: number;
  passwordLargo: number;
  passwordIntentosAntesDeBloquear: number;
  passwordCantidadNumeros: number;
  passwordCantidadPreguntasValidar: number;
  fechaCreacion: string;
  usuarioCreacion: string;
  fechaModificacion?: string;
  usuarioModificacion?: string;
}
export type EmpresaRequest = Omit<
  Empresa,
  'idEmpresa' | 'fechaCreacion' | 'usuarioCreacion' | 'fechaModificacion' | 'usuarioModificacion'
>;
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
