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
import { Role, RoleRequest } from '../models/role.models';
import { RoleService } from '../services/role.service';

@Component({
  selector: 'app-role-form',
  imports: [ReactiveFormsModule],
  templateUrl: './role-form.component.html',
  styleUrl: './role-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly service = inject(RoleService);
  private readonly notification = inject(NotificationService);

  readonly selected = input<Role | null>(null);
  readonly saved = output<void>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly form = this.formBuilder.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(50)]],
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
    const request: RoleRequest = this.form.getRawValue();
    const selected = this.selected();
    const operation = selected
      ? this.service.update(selected.id, request)
      : this.service.create(request);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notification.success(
          selected ? 'Rol actualizado correctamente.' : 'Rol creado correctamente.',
        );
        this.saved.emit();
      },
      error: (error) => this.notification.operationError(error, 'No fue posible guardar el rol.'),
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
