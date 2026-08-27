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

    expect(changePassword?.getAttribute('href')).toBe('/change-password');
    expect(changePassword?.getAttribute('href')).not.toContain('construction');
    expect(element.textContent).toContain('Cerrar sesión');
  });
});
