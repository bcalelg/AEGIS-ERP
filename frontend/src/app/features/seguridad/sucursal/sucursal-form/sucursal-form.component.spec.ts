import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { SucursalService } from '../services/sucursal.service';
import { SucursalFormComponent } from './sucursal-form.component';

describe('SucursalFormComponent', () => {
  let fixture: ComponentFixture<SucursalFormComponent>;
  const service = {
    empresaOptions: vi.fn(() => of([{ id: 2, nombre: 'Software Inc.' }])),
    create: vi.fn(() => of({})),
    update: vi.fn(() => of({})),
  };

  async function create(selected: unknown = null) {
    await TestBed.configureTestingModule({
      imports: [SucursalFormComponent],
      providers: [{ provide: SucursalService, useValue: service }],
    }).compileComponents();
    fixture = TestBed.createComponent(SucursalFormComponent);
    fixture.componentRef.setInput('selected', selected);
    fixture.detectChanges();
  }

  it('carga empresas y precarga la asociación al editar', async () => {
    await create({
      id: 1,
      idEmpresa: 2,
      nombreEmpresa: 'Software Inc.',
      nombre: 'Central',
      direccion: 'Guatemala',
    });
    expect(service.empresaOptions).toHaveBeenCalled();
    expect(fixture.componentInstance.form.getRawValue()).toEqual({
      idEmpresa: 2,
      nombre: 'Central',
      direccion: 'Guatemala',
    });
    fixture.componentInstance.save();
    expect(service.update).toHaveBeenCalledWith(1, {
      idEmpresa: 2,
      nombre: 'Central',
      direccion: 'Guatemala',
    });
  });

  it('crea y cancela', async () => {
    await create();
    fixture.componentInstance.form.setValue({
      idEmpresa: 2,
      nombre: 'Norte',
      direccion: 'Zona 1',
    });
    fixture.componentInstance.save();
    expect(service.create).toHaveBeenCalled();
    const cancelled = vi.fn();
    fixture.componentInstance.cancelled.subscribe(cancelled);
    fixture.componentInstance.cancel();
    expect(cancelled).toHaveBeenCalledOnce();
  });
});
