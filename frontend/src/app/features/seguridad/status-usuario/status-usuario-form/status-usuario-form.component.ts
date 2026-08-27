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
import { StatusUsuario, StatusUsuarioRequest } from '../models/status-usuario.models';
import { StatusUsuarioService } from '../services/status-usuario.service';

@Component({
  selector: 'app-status-usuario-form',
  imports: [ReactiveFormsModule],
  templateUrl: './status-usuario-form.component.html',
  styleUrl: './status-usuario-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusUsuarioFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly service = inject(StatusUsuarioService);
  private readonly notification = inject(NotificationService);

  readonly selected = input<StatusUsuario | null>(null);
  readonly saved = output<void>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly form = this.formBuilder.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
  });

  constructor() {
    effect(() => {
      const selected = this.selected();
      this.form.reset({ nombre: selected?.nombre ?? '' });
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const request: StatusUsuarioRequest = this.form.getRawValue();
    const selected = this.selected();
    const operation = selected
      ? this.service.update(selected.id, request)
      : this.service.create(request);

    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notification.success(
          selected
            ? 'Estatus de usuario actualizado correctamente.'
            : 'Estatus de usuario creado correctamente.',
        );
        this.saved.emit();
      },
      error: (error) =>
        this.notification.operationError(error, 'No fue posible guardar el estatus de usuario.'),
    });
  }

  cancel(): void {
    this.form.reset();
    this.cancelled.emit();
  }

  showNameError(): boolean {
    const control = this.form.controls.nombre;
    return control.touched && control.invalid;
  }
}
