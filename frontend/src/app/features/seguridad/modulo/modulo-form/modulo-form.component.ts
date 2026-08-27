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
import { Modulo, ModuloRequest } from '../models/modulo.models';
import { ModuloService } from '../services/modulo.service';

@Component({
  selector: 'app-modulo-form',
  imports: [ReactiveFormsModule],
  templateUrl: './modulo-form.component.html',
  styleUrl: './modulo-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModuloFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly service = inject(ModuloService);
  private readonly notification = inject(NotificationService);

  readonly selected = input<Modulo | null>(null);
  readonly saved = output<void>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly form = this.formBuilder.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(50)]],
    orden: [1, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    effect(() => {
      const selected = this.selected();
      this.form.reset({ nombre: selected?.nombre ?? '', orden: selected?.orden ?? 1 });
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    const request: ModuloRequest = this.form.getRawValue();
    const selected = this.selected();
    const operation = selected
      ? this.service.update(selected.id, request)
      : this.service.create(request);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notification.success(
          selected ? 'Módulo actualizado correctamente.' : 'Módulo creado correctamente.',
        );
        this.saved.emit();
      },
      error: (error) =>
        this.notification.operationError(error, 'No fue posible guardar el módulo.'),
    });
  }

  cancel(): void {
    this.form.reset({ nombre: '', orden: 1 });
    this.cancelled.emit();
  }

  showNameError(): boolean {
    const control = this.form.controls.nombre;
    return control.touched && control.invalid;
  }

  showOrderError(): boolean {
    const control = this.form.controls.orden;
    return control.touched && control.invalid;
  }
}
