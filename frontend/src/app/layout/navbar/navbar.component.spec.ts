import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NavbarComponent } from './navbar.component';

describe('NavbarComponent user menu', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarComponent],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('links password change to the real authenticated feature', () => {
    const fixture = TestBed.createComponent(NavbarComponent);
    fixture.componentInstance.dropdownOpen.set(true);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    const links = Array.from(element.querySelectorAll<HTMLAnchorElement>('a.dropdown-item'));
    const changePassword = links.find((link) => link.textContent?.includes('Cambiar contraseña'));
    const profile = links.find((link) => link.textContent?.includes('Mi perfil'));

    expect(profile?.getAttribute('href')).toBe('/profile');
    expect(changePassword?.getAttribute('href')).toBe('/change-password');
    expect(changePassword?.getAttribute('href')).not.toContain('construction');
    expect(element.textContent).toContain('Cerrar sesión');
  });

  it('muestra fotografía cuando existe e iniciales como fallback', () => {
    const fixture = TestBed.createComponent(NavbarComponent);
    fixture.componentRef.setInput('user', {
      idUsuario: 'admin',
      nombre: 'Administrador',
      apellido: 'IT',
      role: 'Administrador',
      requiereCambiarPassword: false,
    });
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('AI');
    expect((fixture.nativeElement as HTMLElement).querySelector('img.avatar')).toBeNull();

    fixture.componentRef.setInput('avatarUrl', 'blob:profile-photo');
    fixture.detectChanges();
    expect(
      (fixture.nativeElement as HTMLElement).querySelector<HTMLImageElement>('img.avatar')?.src,
    ).toContain('blob:profile-photo');
  });
});
