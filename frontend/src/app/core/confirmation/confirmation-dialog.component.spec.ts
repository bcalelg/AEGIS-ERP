import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';
import { ConfirmationService } from './confirmation.service';

describe('ConfirmationDialogComponent', () => {
  let fixture: ComponentFixture<ConfirmationDialogComponent>;
  let service: ConfirmationService;

  async function open(): Promise<{ result: Promise<boolean> }> {
    await TestBed.configureTestingModule({
      imports: [ConfirmationDialogComponent],
    }).compileComponents();
    fixture = TestBed.createComponent(ConfirmationDialogComponent);
    service = TestBed.inject(ConfirmationService);
    const result = service.confirm({
      title: 'Eliminar empresa',
      message: '¿Desea eliminar la empresa "Software Inc."?',
      warningText: 'Esta acción no se puede deshacer.',
    });
    fixture.detectChanges();
    await Promise.resolve();
    return { result };
  }

  it('renderiza título, mensaje, icono, botones y semántica dialog', async () => {
    await open();
    const dialog: HTMLElement = fixture.nativeElement.querySelector('[role="dialog"]');
    expect(dialog.textContent).toContain('Eliminar empresa');
    expect(dialog.textContent).toContain('Software Inc.');
    expect(dialog.textContent).toContain('Esta acción no se puede deshacer.');
    expect(dialog.getAttribute('aria-modal')).toBe('true');
    expect(dialog.getAttribute('data-confirmation-type')).toBe('danger');
    expect(dialog.querySelector('.bi-exclamation-triangle-fill')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.confirmation-overlay')).not.toBeNull();
  });

  it('enfoca Cancelar inicialmente y lo resuelve como false', async () => {
    const { result } = await open();
    const element = fixture.nativeElement as HTMLElement;
    const cancel = Array.from(element.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === 'Cancelar',
    )!;
    expect(document.activeElement).toBe(cancel);
    cancel.click();
    await expect(result).resolves.toBe(false);
  });

  it('confirma únicamente desde el botón destructivo', async () => {
    const { result } = await open();
    const element = fixture.nativeElement as HTMLElement;
    const confirm = Array.from(element.querySelectorAll<HTMLButtonElement>('button')).find(
      (button) => button.textContent?.trim() === 'Eliminar',
    )!;
    confirm.click();
    await expect(result).resolves.toBe(true);
    fixture.detectChanges();
    expect(confirm.disabled).toBe(true);
    expect(confirm.textContent).toContain('Eliminando...');
    service.complete();
  });

  it('la X cancela y tiene etiqueta accesible', async () => {
    const { result } = await open();
    const close: HTMLButtonElement = fixture.nativeElement.querySelector('[aria-label="Cerrar"]');
    close.click();
    await expect(result).resolves.toBe(false);
  });

  it('el backdrop no confirma ni cierra accidentalmente', async () => {
    const { result } = await open();
    const overlay: HTMLElement = fixture.nativeElement.querySelector('.confirmation-overlay');
    overlay.click();
    expect(service.request()).not.toBeNull();
    service.cancel();
    await expect(result).resolves.toBe(false);
  });

  it('Escape cancela y restaura el scroll del body', async () => {
    const { result } = await open();
    expect(document.body.style.overflow).toBe('hidden');
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    fixture.detectChanges();
    await expect(result).resolves.toBe(false);
    expect(document.body.style.overflow).toBe('');
  });
});
