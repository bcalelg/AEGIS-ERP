import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { NotificationComponent } from './notification.component';
import { NotificationService } from './notification.service';

describe('NotificationComponent', () => {
  let fixture: ComponentFixture<NotificationComponent>;
  let service: NotificationService;

  afterEach(() => vi.useRealTimers());

  async function create(): Promise<void> {
    vi.useFakeTimers();
    await TestBed.configureTestingModule({ imports: [NotificationComponent] }).compileComponents();
    fixture = TestBed.createComponent(NotificationComponent);
    service = TestBed.inject(NotificationService);
  }

  it('renderiza texto, tipo y semántica accesible', async () => {
    await create();
    service.error('No fue posible guardar la empresa.');
    fixture.detectChanges();

    const toast = fixture.nativeElement.querySelector('[data-notification-type="error"]');
    expect(toast.textContent).toContain('No fue posible guardar la empresa.');
    expect(toast.classList).toContain('notification-toast--error');
    expect(toast.getAttribute('role')).toBe('alert');
    expect(toast.getAttribute('aria-live')).toBe('assertive');
  });

  it('aplica la variante visual y semántica de cada tipo', async () => {
    await create();
    const variants = [
      { type: 'success', css: 'notification-toast--success', role: 'status' },
      { type: 'info', css: 'notification-toast--info', role: 'status' },
      { type: 'warning', css: 'notification-toast--warning', role: 'alert' },
      { type: 'error', css: 'notification-toast--error', role: 'alert' },
    ] as const;

    for (const variant of variants) {
      service.clear();
      service[variant.type](`Mensaje ${variant.type}`);
      fixture.detectChanges();
      const toast = fixture.nativeElement.querySelector(
        `[data-notification-type="${variant.type}"]`,
      );
      expect(toast.classList).toContain(variant.css);
      expect(toast.getAttribute('role')).toBe(variant.role);
      expect(toast.querySelector('.notification-icon-shell')).not.toBeNull();
    }
  });

  it('permite cierre manual mediante un botón accesible', async () => {
    await create();
    service.warning('Hay cambios sin guardar.');
    fixture.detectChanges();

    const close: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[aria-label="Cerrar notificación"]',
    );
    close.click();
    fixture.detectChanges();
    expect(service.notifications()).toHaveLength(0);
  });
});
