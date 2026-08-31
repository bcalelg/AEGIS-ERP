export interface UserProfile {
  idUsuario: string;
  nombre: string;
  apellido: string;
  correoElectronico: string;
  telefonoMovil: string | null;
  fechaNacimiento: string;
  genero: string;
  estatus: string;
  empresa: string;
  sucursal: string;
  role: string;
  fotografiaDisponible: boolean;
  fotografiaUrl: string | null;
}

export interface ProfileUpdateRequest {
  correoElectronico: string;
  telefonoMovil: string;
}
