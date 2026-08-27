import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({});
    service = TestBed.inject(NotificationService);
  });

  afterEach(() => {
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });

  it('muestra success y lo retira después de 3000 ms', () => {
    service.success('Empresa creada correctamente.');
    expect(service.notifications()[0]).toMatchObject({
      type: 'success',
      message: 'Empresa creada correctamente.',
      duration: 3000,
    });

    vi.advanceTimersByTime(2999);
    expect(service.notifications()).toHaveLength(1);
    vi.advanceTimersByTime(1);
    expect(service.notifications()).toHaveLength(0);
  });

  it('aplica 3000 ms a info y 5000 ms a warning', () => {
    service.info('Los datos fueron actualizados.');
    service.warning('Hay cambios sin guardar.');

    vi.advanceTimersByTime(3000);
    expect(service.notifications().map((item) => item.type)).toEqual(['warning']);
    vi.advanceTimersByTime(2000);
    expect(service.notifications()).toHaveLength(0);
  });

  it('mantiene error hasta que se cierre manualmente', () => {
    const id = service.error('No fue posible guardar la empresa.');
    vi.advanceTimersByTime(60000);
    expect(service.notifications()).toHaveLength(1);

    service.dismiss(id);
    expect(service.notifications()).toHaveLength(0);
  });

  it('admite duración configurable y cancela timers al limpiar', () => {
    service.success('Temporal', { duration: 1000 });
    service.info('También temporal');
    service.clear();
    vi.advanceTimersByTime(5000);
    expect(service.notifications()).toHaveLength(0);
  });

  it('conserva como máximo las tres notificaciones más recientes', () => {
    service.error('Uno');
    service.error('Dos');
    service.error('Tres');
    service.error('Cuatro');
    expect(service.notifications().map((item) => item.message)).toEqual(['Dos', 'Tres', 'Cuatro']);
  });

  it('conserva detalles de negocio y oculta información técnica', () => {
    service.operationError(
      { error: { detail: 'No es posible eliminar el rol porque posee usuarios asociados.' } },
      'No fue posible eliminar el rol.',
    );
    service.operationError(
      { error: { detail: 'ORA-02292: integrity constraint violated' } },
      'No fue posible eliminar el rol.',
    );

    expect(service.notifications().map((item) => item.message)).toEqual([
      'No es posible eliminar el rol porque posee usuarios asociados.',
      'No fue posible eliminar el rol.',
    ]);
  });
});
