import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/auth/auth.service';
import { MenuService } from '../../core/services/menu.service';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { NavbarComponent } from '../navbar/navbar.component';
import { FooterComponent } from '../footer/footer.component';
@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, SidebarComponent, NavbarComponent, FooterComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css',
})
export class MainLayoutComponent implements OnInit {
  readonly auth = inject(AuthService);
  readonly menu = inject(MenuService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly sidebarCollapsed = signal(false);
  ngOnInit() {
    this.menu
      .load()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ error: () => this.menu.clear() });
  }
  logout() {
    this.auth
      .logout()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.menu.clear();
          void this.router.navigate(['/login']);
        },
        error: () => {
          this.menu.clear();
          void this.router.navigate(['/login']);
        },
      });
  }
}
