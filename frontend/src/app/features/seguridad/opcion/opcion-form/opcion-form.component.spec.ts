import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { OpcionService } from '../services/opcion.service';
import { OpcionFormComponent } from './opcion-form.component';

describe('OpcionFormComponent', () => {
  let fixture: ComponentFixture<OpcionFormComponent>;
  const service = {
    menuOptions: vi.fn(() => of([{ id: 2, nombre: 'Parámetros', modulo: 'Seguridad' }])),
    create: vi.fn(() => of({})),
    update: vi.fn(() => of({})),
  };
  async function create(selected: unknown = null) {
    await TestBed.configureTestingModule({
      imports: [OpcionFormComponent],
      providers: [{ provide: OpcionService, useValue: service }],
    }).compileComponents();
    fixture = TestBed.createComponent(OpcionFormComponent);
    fixture.componentRef.setInput('selected', selected);
    fixture.detectChanges();
  }

  it('edición muestra página readonly y conserva editables menú, nombre y orden', async () => {
    await create({
      id: 7,
      idMenu: 2,
      nombreMenu: 'Parámetros',
      nombreModulo: 'Seguridad',
      nombre: 'Empresas',
      pagina: 'empresa.php',
      orden: 1,
    });
    expect(fixture.componentInstance.form.getRawValue()).toEqual({
      idMenu: 2,
      nombre: 'Empresas',
      pagina: 'empresa.php',
      orden: 1,
    });
    const page = fixture.nativeElement.querySelector('#opcion-pagina') as HTMLInputElement;
    const menu = fixture.nativeElement.querySelector('#opcion-menu') as HTMLSelectElement;
    const name = fixture.nativeElement.querySelector('#opcion-nombre') as HTMLInputElement;
    const order = fixture.nativeElement.querySelector('#opcion-orden') as HTMLInputElement;
    expect(page.readOnly).toBe(true);
    expect(page.value).toBe('empresa.php');
    expect(menu.disabled).toBe(false);
    expect(name.readOnly).toBe(false);
    expect(order.readOnly).toBe(false);
    fixture.componentInstance.form.patchValue({
      idMenu: 2,
      nombre: 'Administración de Empresas',
      pagina: 'ruta_inventada',
      orden: 2,
    });
    fixture.componentInstance.save();
    expect(service.update).toHaveBeenCalledWith(7, {
      idMenu: 2,
      nombre: 'Administración de Empresas',
      orden: 2,
    });
  });

  it('crea y cancela el formulario inline', async () => {
    await create();
    const page = fixture.nativeElement.querySelector('#opcion-pagina') as HTMLInputElement;
    expect(page.readOnly).toBe(false);
    fixture.componentInstance.form.setValue({
      idMenu: 2,
      nombre: 'Nueva',
      pagina: 'nueva.php',
      orden: 9,
    });
    fixture.componentInstance.save();
    expect(service.create).toHaveBeenCalled();
    const cancelled = vi.fn();
    fixture.componentInstance.cancelled.subscribe(cancelled);
    fixture.componentInstance.cancel();
    expect(cancelled).toHaveBeenCalledOnce();
  });
});
