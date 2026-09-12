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
    const profile = links.find((link) => link.textContent?.includes('Mi perfil'));

    expect(profile?.getAttribute('href')).toBe('/profile');
    expect(changePassword?.getAttribute('href')).toBe('/change-password');
    expect(changePassword?.getAttribute('href')).not.toContain('construction');
    expect(element.textContent).toContain('Cerrar sesión');
  });

  it('mantiene una sola rama de módulo y menú abierta', () => {
    const fixture = TestBed.createComponent(SidebarComponent);
    fixture.componentRef.setInput('modules', [
      {
        idModulo: 1,
        nombre: 'Seguridad',
        ordenMenu: 1,
        menus: [
          { idMenu: 10, nombre: 'Parámetros', ordenMenu: 1, opciones: [] },
          { idMenu: 11, nombre: 'Acciones', ordenMenu: 2, opciones: [] },
        ],
      },
      {
        idModulo: 2,
        nombre: 'Contabilidad',
        ordenMenu: 2,
        menus: [{ idMenu: 20, nombre: 'Nomenclatura', ordenMenu: 1, opciones: [] }],
      },
    ]);
    fixture.detectChanges();

    fixture.componentInstance.toggleModule(1);
    fixture.componentInstance.toggleMenu(1, 10);
    expect(fixture.componentInstance.isModuleOpen(1)).toBe(true);
    expect(fixture.componentInstance.isOpen(10)).toBe(true);

    fixture.componentInstance.toggleModule(2);
    fixture.componentInstance.toggleMenu(2, 20);
    expect(fixture.componentInstance.isModuleOpen(1)).toBe(false);
    expect(fixture.componentInstance.isOpen(10)).toBe(false);
    expect(fixture.componentInstance.isModuleOpen(2)).toBe(true);
    expect(fixture.componentInstance.isOpen(20)).toBe(true);
  });
});
