import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SidebarComponent } from './sidebar.component';

describe('SidebarComponent user menu', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarComponent],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('links password change to the real feature with correctly encoded labels', () => {
    const fixture = TestBed.createComponent(SidebarComponent);
    fixture.componentInstance.profileOpen.set(true);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    const links = Array.from(element.querySelectorAll<HTMLAnchorElement>('a.dropdown-item'));
    const changePassword = links.find((link) => link.textContent?.includes('Cambiar contraseña'));

    expect(changePassword?.getAttribute('href')).toBe('/change-password');
    expect(changePassword?.getAttribute('href')).not.toContain('construction');
    expect(element.textContent).toContain('Cerrar sesión');
  });
});
