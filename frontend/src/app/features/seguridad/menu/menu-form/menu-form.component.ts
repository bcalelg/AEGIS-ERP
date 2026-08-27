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
import { MenuMaintenance, MenuRequest, ModuloOption } from '../models/menu.models';
import { MenuMaintenanceService } from '../services/menu-maintenance.service';

@Component({
  selector: 'app-menu-form',
  imports: [ReactiveFormsModule],
  templateUrl: './menu-form.component.html',
  styleUrl: './menu-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MenuFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly service = inject(MenuMaintenanceService);
  private readonly notification = inject(NotificationService);

  readonly selected = input<MenuMaintenance | null>(null);
  readonly saved = output<void>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly loadingModules = signal(true);
  readonly modules = signal<ModuloOption[]>([]);
  readonly error = signal('');
  readonly form = this.formBuilder.nonNullable.group({
    idModulo: [{ value: 0, disabled: true }, [Validators.required, Validators.min(1)]],
    nombre: ['', [Validators.required, Validators.maxLength(50)]],
    orden: [1, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    this.loadModules();
    effect(() => {
      const selected = this.selected();
      this.error.set('');
      this.form.reset({
        idModulo: selected?.idModulo ?? 0,
        nombre: selected?.nombre ?? '',
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
    const request: MenuRequest = this.form.getRawValue();
    const selected = this.selected();
    const operation = selected
      ? this.service.update(selected.id, request)
      : this.service.create(request);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notification.success(
          selected ? 'Menú actualizado correctamente.' : 'Menú creado correctamente.',
        );
        this.saved.emit();
      },
      error: (error) => this.notification.operationError(error, 'No fue posible guardar el menú.'),
    });
  }

  cancel(): void {
    this.error.set('');
    this.cancelled.emit();
  }

  invalid(name: 'idModulo' | 'nombre' | 'orden'): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }

  private loadModules(): void {
    this.service
      .moduloOptions()
      .pipe(
        finalize(() => {
          this.loadingModules.set(false);
          this.form.controls.idModulo.enable();
        }),
      )
      .subscribe({
        next: (modules) => this.modules.set(modules),
        error: (error) =>
          this.error.set(error.error?.detail ?? 'No fue posible cargar los módulos.'),
      });
  }
}
