import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { NotificationService } from '../../../../core/notifications/notification.service';
import {
  MenuOption,
  OpcionCreateRequest,
  OpcionMaintenance,
  OpcionUpdateRequest,
} from '../models/opcion.models';
import { OpcionService } from '../services/opcion.service';

@Component({
  selector: 'app-opcion-form',
  imports: [ReactiveFormsModule],
  templateUrl: './opcion-form.component.html',
  styleUrl: './opcion-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OpcionFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly service = inject(OpcionService);
  private readonly notification = inject(NotificationService);

  readonly selected = input<OpcionMaintenance | null>(null);
  readonly saved = output<void>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly loadingMenus = signal(true);
  readonly menus = signal<MenuOption[]>([]);
  readonly error = signal('');
  readonly form = this.formBuilder.nonNullable.group({
    idMenu: [{ value: 0, disabled: true }, [Validators.required, Validators.min(1)]],
    nombre: ['', [Validators.required, Validators.maxLength(50)]],
    pagina: ['', [Validators.required, Validators.maxLength(100)]],
    orden: [1, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    this.loadMenus();
    effect(() => {
      const selected = this.selected();
      this.error.set('');
      this.form.reset({
        idMenu: selected?.idMenu ?? 0,
        nombre: selected?.nombre ?? '',
        pagina: selected?.pagina ?? '',
        orden: selected?.orden ?? 1,
      });
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');
    const selected = this.selected();
    const operation = selected
      ? this.service.update(selected.id, this.updateRequest())
      : this.service.create(this.form.getRawValue() as OpcionCreateRequest);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notification.success(
          selected ? 'Opción actualizada correctamente.' : 'Opción creada correctamente.',
        );
        this.saved.emit();
      },
      error: (error) =>
        this.notification.operationError(error, 'No fue posible guardar la opción.'),
    });
  }

  cancel(): void {
    this.error.set('');
    this.cancelled.emit();
  }

  invalid(name: 'idMenu' | 'nombre' | 'pagina' | 'orden'): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }

  private updateRequest(): OpcionUpdateRequest {
    const value = this.form.getRawValue();
    return {
      idMenu: value.idMenu,
      nombre: value.nombre,
      orden: value.orden,
    };
  }

  private loadMenus(): void {
    this.service
      .menuOptions()
      .pipe(
        finalize(() => {
          this.loadingMenus.set(false);
          this.form.controls.idMenu.enable();
        }),
      )
      .subscribe({
        next: (menus) => this.menus.set(menus),
        error: (error) => this.error.set(error.error?.detail ?? 'No fue posible cargar los menús.'),
      });
  }
}
