import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NotificationService } from '../../core/notifications/notification.service';
import { UserProfile } from '../../core/profile/profile.models';
import { ProfileService } from '../../core/profile/profile.service';
import { ProfileComponent } from './profile.component';

describe('ProfileComponent', () => {
  let fixture: ComponentFixture<ProfileComponent>;
  const profile: UserProfile = {
    idUsuario: 'mlorenzana',
    nombre: 'Marco',
    apellido: 'Lorenzana',
    correoElectronico: 'marco@example.com',
    telefonoMovil: '555-1000',
    fechaNacimiento: '1990-01-01',
    genero: 'Masculino',
    estatus: 'Activo',
    empresa: 'Software Inc.',
    sucursal: 'Central',
    role: 'Administrador',
    fotografiaDisponible: false,
    fotografiaUrl: null,
  };
  const profiles = {
    avatarUrl: signal<string | null>(null),
    get: vi.fn(() => of(profile)),
    update: vi.fn(() => of({ ...profile, correoElectronico: 'nuevo@example.com' })),
    loadPhoto: vi.fn(() => of(null)),
    uploadPhoto: vi.fn(() => {
      profiles.avatarUrl.set('blob:new-avatar');
      return of('blob:new-avatar');
    }),
    deletePhoto: vi.fn(() => of(void 0)),
  };
  const notification = { success: vi.fn(), operationError: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    profiles.avatarUrl.set(null);
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: vi.fn(() => 'blob:preview'),
    });
    Object.defineProperty(URL, 'revokeObjectURL', { configurable: true, value: vi.fn() });
    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        provideRouter([]),
        { provide: ProfileService, useValue: profiles },
        { provide: NotificationService, useValue: notification },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(ProfileComponent);
    fixture.detectChanges();
  });

  it('carga datos, muestra iniciales y mantiene campos administrativos sin inputs', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('ML');
    expect(element.textContent).toContain('mlorenzana');
    expect(element.textContent).toContain('Software Inc.');
    expect(element.textContent).toContain('Administrador');
    expect(element.textContent).toContain('Marco');
    expect(element.textContent).toContain('Lorenzana');
    expect(element.textContent).toContain('1990-01-01');
    expect(element.textContent).toContain('Masculino');
    expect(element.textContent).toContain('Central');
    expect(element.textContent).toContain('Activo');
    expect(element.querySelectorAll('input:not([type="file"])')).toHaveLength(2);
    const passwordLink = element.querySelector<HTMLAnchorElement>('a[href="/change-password"]');
    expect(passwordLink?.textContent).toContain('Cambiar contraseña');
  });

  it('envía únicamente correo y teléfono y muestra la notificación global', () => {
    fixture.componentInstance.form.patchValue({ correoElectronico: 'nuevo@example.com' });
    fixture.componentInstance.save();

    expect(profiles.update).toHaveBeenCalledWith({
      correoElectronico: 'nuevo@example.com',
      telefonoMovil: '555-1000',
    });
    expect(notification.success).toHaveBeenCalledWith('Perfil actualizado correctamente.');
  });

  it('valida formato y tamaño, crea preview y actualiza el avatar sin recargar', () => {
    fixture.componentInstance.selectPhoto(eventWith(new File(['x'], 'x.svg', { type: 'image/svg+xml' })));
    expect(fixture.componentInstance.photoError()).toContain('JPEG');

    fixture.componentInstance.selectPhoto(
      eventWith(new File([new Uint8Array(2 * 1024 * 1024 + 1)], 'large.png', { type: 'image/png' })),
    );
    expect(fixture.componentInstance.photoError()).toContain('2 MB');

    const valid = new File(['png'], 'photo.png', { type: 'image/png' });
    fixture.componentInstance.selectPhoto(eventWith(valid));
    expect(fixture.componentInstance.previewUrl()).toBe('blob:preview');
    fixture.componentInstance.savePhoto();
    expect(profiles.uploadPhoto).toHaveBeenCalledWith(valid);
    expect(profiles.avatarUrl()).toBe('blob:new-avatar');
    expect(notification.success).toHaveBeenCalledWith('Fotografía actualizada correctamente.');
  });

  function eventWith(file: File): Event {
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { configurable: true, value: [file] });
    return { target: input } as unknown as Event;
  }
});
