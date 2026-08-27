export interface LoginRequest {
  idUsuario: string;
  password: string;
}
export interface LoginResponse {
  authenticated: boolean;
  expiresIn: number;
  idUsuario: string;
  nombre: string;
  apellido: string;
  role: string;
  requiereCambiarPassword: boolean;
}
export interface CurrentUserResponse {
  idUsuario: string;
  nombre: string;
  apellido: string;
  role: string;
  requiereCambiarPassword: boolean;
}
export interface ChangePasswordRequest {
  passwordActual: string;
  passwordNueva: string;
  passwordConfirmacion: string;
}
export interface ForgotPasswordRequest {
  identifier: string;
}
export interface ForgotPasswordResponse {
  message: string;
}
export interface ResetPasswordRequest {
  token: string;
  passwordNueva: string;
  passwordConfirmacion: string;
}
export interface AuthUser {
  idUsuario: string;
  nombre?: string;
  apellido?: string;
  role: string;
  requiereCambiarPassword: boolean;
}
