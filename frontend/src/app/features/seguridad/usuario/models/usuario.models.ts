export interface Usuario {
  idUsuario: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  correoElectronico: string | null;
  telefonoMovil: string | null;
  pregunta: string;
  idEmpresa: number;
  nombreEmpresa: string;
  idSucursal: number;
  nombreSucursal: string;
  idGenero: number;
  nombreGenero: string;
  idStatusUsuario: number;
  nombreStatusUsuario: string;
  idRole: number;
  nombreRole: string;
  ultimaFechaIngreso: string | null;
  requiereCambiarPassword: boolean;
}

export type UsuarioSummary = Omit<Usuario, 'pregunta'>;

export interface UsuarioOption {
  id: number;
  nombre: string;
}

export interface UsuarioCreateRequest {
  idUsuario: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  correoElectronico: string;
  telefonoMovil: string;
  password: string;
  passwordConfirmacion: string;
  pregunta: string;
  respuesta: string;
  idEmpresa: number;
  idSucursal: number;
  idGenero: number;
  idStatusUsuario: number;
  idRole: number;
}

export interface UsuarioUpdateRequest {
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  correoElectronico: string;
  telefonoMovil: string;
  pregunta: string;
  respuesta: string;
  idEmpresa: number;
  idSucursal: number;
  idGenero: number;
  idStatusUsuario: number;
  idRole: number;
}
