import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { MenuMaintenanceService } from '../services/menu-maintenance.service';
import { MenuFormComponent } from './menu-form.component';

describe('MenuFormComponent', () => {
  let fixture: ComponentFixture<MenuFormComponent>;
  const service = {
    moduloOptions: vi.fn(() => of([{ id: 2, nombre: 'Seguridad' }])),
    create: vi.fn(() => of({})),
    update: vi.fn(() => of({})),
  };

  async function create(selected: unknown = null) {
    await TestBed.configureTestingModule({
      imports: [MenuFormComponent],
      providers: [{ provide: MenuMaintenanceService, useValue: service }],
    }).compileComponents();
    fixture = TestBed.createComponent(MenuFormComponent);
    fixture.componentRef.setInput('selected', selected);
    fixture.detectChanges();
  }

  it('carga módulos y precarga módulo, nombre y orden al editar', async () => {
    await create({
      id: 7,
      idModulo: 2,
      nombreModulo: 'Seguridad',
      nombre: 'Parámetros',
      orden: 1,
    });
    expect(service.moduloOptions).toHaveBeenCalled();
    expect(fixture.componentInstance.form.getRawValue()).toEqual({
      idModulo: 2,
      nombre: 'Parámetros',
      orden: 1,
    });
    fixture.componentInstance.save();
    expect(service.update).toHaveBeenCalledWith(7, {
      idModulo: 2,
      nombre: 'Parámetros',
      orden: 1,
    });
  });

  it('crea y cancela el formulario inline', async () => {
    await create();
    fixture.componentInstance.form.setValue({ idModulo: 2, nombre: 'Acciones', orden: 2 });
    fixture.componentInstance.save();
    expect(service.create).toHaveBeenCalledWith({
      idModulo: 2,
      nombre: 'Acciones',
      orden: 2,
    });
    const cancelled = vi.fn();
    fixture.componentInstance.cancelled.subscribe(cancelled);
    fixture.componentInstance.cancel();
    expect(cancelled).toHaveBeenCalledOnce();
  });
});
