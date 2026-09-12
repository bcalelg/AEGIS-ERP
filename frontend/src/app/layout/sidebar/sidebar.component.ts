import { routeForOptionPage } from '../../core/routing/option-route';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  effect,
  inject,
  input,
  output,
  signal,
  untracked,
} from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthUser } from '../../core/models/auth.models';
import { MenuGrupo, ModuloMenu } from '../../core/models/menu.models';
@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  readonly modules = input<ModuloMenu[]>([]);
  readonly user = input<AuthUser | null>(null);
  readonly avatarUrl = input<string | null>(null);
  readonly collapsed = input(false);
  readonly navigate = output<void>();
  readonly logout = output<void>();
  readonly profileOpen = signal(false);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly openedModuleId = signal<number | null>(null);
  private readonly openedMenuId = signal<number | null>(null);
  private readonly currentUrl = signal(this.router.url);
  constructor() {
    effect(() => {
      const modules = this.modules();
      untracked(() => this.expandActiveBranch(modules));
    });
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((event) => {
        this.currentUrl.set(event.urlAfterRedirects);
        this.expandActiveBranch(this.modules());
      });
  }
  optionRoute(page: string) {
    return routeForOptionPage(page);
  }
  isModuleOpen(id: number) {
    return this.openedModuleId() === id;
  }
  isOpen(id: number) {
    return this.openedMenuId() === id;
  }
  toggleModule(id: number) {
    if (this.isModuleOpen(id)) {
      this.openedModuleId.set(null);
      this.openedMenuId.set(null);
      return;
    }
    this.openedModuleId.set(id);
    this.openedMenuId.set(null);
  }
  toggleMenu(moduleId: number, menuId: number) {
    this.openedModuleId.set(moduleId);
    this.openedMenuId.update((opened) => (opened === menuId ? null : menuId));
  }
  isCurrentMenu(menu: MenuGrupo) {
    this.currentUrl();
    return menu.opciones.some((option) => this.isOptionRoute(option.pagina));
  }
  displayName() {
    const user = this.user();
    return [user?.nombre, user?.apellido].filter(Boolean).join(' ') || user?.idUsuario || 'Usuario';
  }
  initials() {
    const user = this.user(),
      names = [user?.nombre, user?.apellido].filter((value): value is string => Boolean(value));
    return names.length
      ? names
          .map((value) => value[0])
          .join('')
          .slice(0, 2)
          .toUpperCase()
      : user?.idUsuario.slice(0, 2).toUpperCase() || 'AE';
  }
  closeProfile() {
    this.profileOpen.set(false);
    this.navigate.emit();
  }
  closeAndLogout() {
    this.profileOpen.set(false);
    this.logout.emit();
  }
  private isOptionRoute(page: string) {
    return this.router.isActive(this.router.createUrlTree(this.optionRoute(page)), {
      paths: 'exact',
      queryParams: 'ignored',
      fragment: 'ignored',
      matrixParams: 'ignored',
    });
  }
  private expandActiveBranch(modules: ModuloMenu[]) {
    const activeBranch = modules
      .flatMap((module) => module.menus.map((menu) => ({ module, menu })))
      .find(({ menu }) => menu.opciones.some((option) => this.isOptionRoute(option.pagina)));
    if (!activeBranch) return;
    this.openedModuleId.set(activeBranch.module.idModulo);
    this.openedMenuId.set(activeBranch.menu.idMenu);
  }
}
