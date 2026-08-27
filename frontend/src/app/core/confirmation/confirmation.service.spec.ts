import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ConfirmationService } from './confirmation.service';

describe('ConfirmationService', () => {
  function create(): ConfirmationService {
    TestBed.configureTestingModule({});
    return TestBed.inject(ConfirmationService);
  }

  it('abre una confirmación danger con texto dinámico y botones por defecto', () => {
    const service = create();
    void service.confirm({
      title: 'Eliminar empresa',
      message: '¿Desea eliminar la empresa "Software Inc."?',
      type: 'danger',
    });

    expect(service.request()).toEqual({
      title: 'Eliminar empresa',
      message: '¿Desea eliminar la empresa "Software Inc."?',
      confirmText: 'Eliminar',
      cancelText: 'Cancelar',
      type: 'danger',
      warningText: null,
    });
  });

  it('devuelve true al confirmar', async () => {
    const service = create();
    const result = service.confirm({ title: 'Eliminar', message: 'Confirme.' });
    service.accept();
    await expect(result).resolves.toBe(true);
    expect(service.processing()).toBe(true);
    expect(service.request()).not.toBeNull();
    service.complete();
    expect(service.request()).toBeNull();
  });

  it('devuelve false al cancelar', async () => {
    const service = create();
    const result = service.confirm({ title: 'Eliminar', message: 'Confirme.' });
    service.cancel();
    await expect(result).resolves.toBe(false);
  });

  it('permite personalizar botones y advertencia', () => {
    const service = create();
    void service.confirm({
      title: 'Borrar registro',
      message: 'Mensaje',
      confirmText: 'Borrar',
      cancelText: 'Volver',
      warningText: 'No reversible.',
    });
    expect(service.request()).toMatchObject({
      confirmText: 'Borrar',
      cancelText: 'Volver',
      warningText: 'No reversible.',
    });
  });

  it('cancela la anterior al recibir una segunda confirmación', async () => {
    const service = create();
    const first = service.confirm({ title: 'Primera', message: 'Primera' });
    const second = service.confirm({ title: 'Segunda', message: 'Segunda' });
    await expect(first).resolves.toBe(false);
    expect(service.request()?.title).toBe('Segunda');
    service.cancel();
    await expect(second).resolves.toBe(false);
  });

  it('ignora confirmaciones nuevas mientras una eliminación está en progreso', async () => {
    const service = create();
    const first = service.confirm({ title: 'Primera', message: 'Primera' });
    service.accept();
    await expect(first).resolves.toBe(true);
    await expect(service.confirm({ title: 'Segunda', message: 'Segunda' })).resolves.toBe(false);
    expect(service.request()?.title).toBe('Primera');
    service.complete();
  });

  it('devuelve el foco al elemento que abrió la confirmación', async () => {
    vi.useFakeTimers();
    const trigger = document.createElement('button');
    document.body.appendChild(trigger);
    trigger.focus();
    const service = create();
    const result = service.confirm({ title: 'Eliminar', message: 'Confirme.' });
    service.cancel();
    await expect(result).resolves.toBe(false);
    vi.runAllTimers();
    expect(document.activeElement).toBe(trigger);
    trigger.remove();
    vi.useRealTimers();
  });
});
