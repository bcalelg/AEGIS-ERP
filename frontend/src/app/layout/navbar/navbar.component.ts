import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthUser } from '../../core/models/auth.models';
@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  readonly user = input<AuthUser | null>(null);
  readonly avatarUrl = input<string | null>(null);
  readonly toggleMenu = output<void>();
  readonly logout = output<void>();
  readonly dropdownOpen = signal(false);
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
  closeAndLogout() {
    this.dropdownOpen.set(false);
    this.logout.emit();
  }
}
