import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { ModuloService } from '../services/modulo.service';
import { ModuloFormComponent } from './modulo-form.component';

describe('ModuloFormComponent', () => {
  let fixture: ComponentFixture<ModuloFormComponent>;
  const service = {
    create: vi.fn(() => of({ id: 2, nombre: 'Inventario', orden: 2 })),
    update: vi.fn(() => of({ id: 1, nombre: 'Seguridad', orden: 3 })),
  };
  const notification = {
    success: vi.fn(),
    operationError: vi.fn(),
  };

  async function create(selected: { id: number; nombre: string; orden: number } | null = null) {
    await TestBed.configureTestingModule({
      imports: [ModuloFormComponent],
      providers: [
        { provide: ModuloService, useValue: service },
        { provide: NotificationService, useValue: notification },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(ModuloFormComponent);
    fixture.componentRef.setInput('selected', selected);
    fixture.detectChanges();
  }

  it('carga Nombre y Orden y actualiza', async () => {
    await create({ id: 1, nombre: 'Seguridad', orden: 1 });
    expect(fixture.componentInstance.form.getRawValue()).toEqual({
      nombre: 'Seguridad',
      orden: 1,
    });
    fixture.componentInstance.form.setValue({ nombre: 'Seguridad', orden: 3 });
    fixture.componentInstance.save();
    expect(service.update).toHaveBeenCalledWith(1, { nombre: 'Seguridad', orden: 3 });
    expect(notification.success).toHaveBeenCalledWith('Módulo actualizado correctamente.');
  });

  it('crea, cancela y valida orden positivo', async () => {
    await create();
    fixture.componentInstance.form.setValue({ nombre: 'Inventario', orden: 2 });
    fixture.componentInstance.save();
    expect(service.create).toHaveBeenCalledWith({ nombre: 'Inventario', orden: 2 });
    expect(notification.success).toHaveBeenCalledWith('Módulo creado correctamente.');
    fixture.componentInstance.form.setValue({ nombre: 'Inválido', orden: 0 });
    fixture.componentInstance.save();
    expect(fixture.componentInstance.form.invalid).toBe(true);
    const cancelled = vi.fn();
    fixture.componentInstance.cancelled.subscribe(cancelled);
    fixture.componentInstance.cancel();
    expect(cancelled).toHaveBeenCalledOnce();
  });
});
