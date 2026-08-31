import { Routes } from '@angular/router';
import { authGuard, changePasswordGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'change-password',
    canActivate: [changePasswordGuard],
    loadComponent: () =>
      import('./features/auth/change-password/change-password.component').then(
        (m) => m.ChangePasswordComponent,
      ),
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password.component').then(
        (m) => m.ForgotPasswordComponent,
      ),
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password.component').then(
        (m) => m.ResetPasswordComponent,
      ),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/main-layout/main-layout.component').then((m) => m.MainLayoutComponent),
    children: [
      {
        path: 'empresas',
        loadComponent: () =>
          import('./features/seguridad/empresa/empresa-list/empresa-list.component').then(
            (m) => m.EmpresaListComponent,
          ),
      },
      {
        path: 'generos',
        loadComponent: () =>
          import('./features/seguridad/genero/genero-list/genero-list.component').then(
            (m) => m.GeneroListComponent,
          ),
      },
      {
        path: 'estatus-usuarios',
        loadComponent: () =>
          import('./features/seguridad/status-usuario/status-usuario-list/status-usuario-list.component').then(
            (m) => m.StatusUsuarioListComponent,
          ),
      },
      {
        path: 'roles',
        loadComponent: () =>
          import('./features/seguridad/role/role-list/role-list.component').then(
            (m) => m.RoleListComponent,
          ),
      },
      {
        path: 'modulos',
        loadComponent: () =>
          import('./features/seguridad/modulo/modulo-list/modulo-list.component').then(
            (m) => m.ModuloListComponent,
          ),
      },
      {
        path: 'menus',
        loadComponent: () =>
          import('./features/seguridad/menu/menu-list/menu-list.component').then(
            (m) => m.MenuListComponent,
          ),
      },
      {
        path: 'opciones',
        loadComponent: () =>
          import('./features/seguridad/opcion/opcion-list/opcion-list.component').then(
            (m) => m.OpcionListComponent,
          ),
      },
      {
        path: 'asignar-opciones-rol',
        loadComponent: () =>
          import('./features/seguridad/role-opcion/role-opcion.component').then(
            (m) => m.RoleOpcionComponent,
          ),
      },
      {
        path: 'sucursales',
        loadComponent: () =>
          import('./features/seguridad/sucursal/sucursal-list/sucursal-list.component').then(
            (m) => m.SucursalListComponent,
          ),
      },
      {
        path: 'usuarios',
        loadComponent: () =>
          import('./features/seguridad/usuario/usuario-list/usuario-list.component').then(
            (m) => m.UsuarioListComponent,
          ),
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/profile/profile.component').then((m) => m.ProfileComponent),
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'construction/:pagina',
        loadComponent: () =>
          import('./features/construction/construction.component').then(
            (m) => m.ConstructionComponent,
          ),
      },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: '**', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '' },
];
