import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ProfileService } from './profile.service';

describe('ProfileService', () => {
  let service: ProfileService;
  let http: HttpTestingController;

  beforeEach(() => {
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: vi.fn(() => 'blob:avatar-new'),
    });
    Object.defineProperty(URL, 'revokeObjectURL', { configurable: true, value: vi.fn() });
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ProfileService);
    http = TestBed.inject(HttpTestingController);
  });

  it('consume el perfil sin enviar un ID arbitrario', () => {
    service.get().subscribe((profile) => expect(profile.idUsuario).toBe('propio'));
    const request = http.expectOne('/api/security/profile');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.keys()).toEqual([]);
    request.flush({ idUsuario: 'propio' });
  });

  it('carga y reemplaza el ObjectURL compartido del avatar', () => {
    service.loadPhoto().subscribe();
    http.expectOne('/api/security/profile/photo').flush(new Blob(['photo'], { type: 'image/png' }));
    expect(service.avatarUrl()).toBe('blob:avatar-new');

    service.uploadPhoto(new File(['new'], 'photo.png', { type: 'image/png' })).subscribe();
    const upload = http.expectOne('/api/security/profile/photo');
    expect(upload.request.method).toBe('PUT');
    expect(upload.request.body).toBeInstanceOf(FormData);
    upload.flush(new Blob(['new'], { type: 'image/png' }));
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:avatar-new');
  });

  it('elimina la foto y restaura el fallback', () => {
    service.avatarUrl.set('blob:avatar-old');
    service.deletePhoto().subscribe();
    const request = http.expectOne('/api/security/profile/photo');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
    expect(service.avatarUrl()).toBeNull();
  });
});
