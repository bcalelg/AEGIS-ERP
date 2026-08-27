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
import { Genero, GeneroRequest } from '../models/genero.models';
import { GeneroService } from '../services/genero.service';

@Component({
  selector: 'app-genero-form',
  imports: [ReactiveFormsModule],
  templateUrl: './genero-form.component.html',
  styleUrl: './genero-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GeneroFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly service = inject(GeneroService);
  private readonly notification = inject(NotificationService);

  readonly selected = input<Genero | null>(null);
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
    const request: GeneroRequest = this.form.getRawValue();
    const selected = this.selected();
    const operation = selected
      ? this.service.update(selected.id, request)
      : this.service.create(request);

    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notification.success(
          selected ? 'Género actualizado correctamente.' : 'Género creado correctamente.',
        );
        this.saved.emit();
      },
      error: (error) =>
        this.notification.operationError(error, 'No fue posible guardar el género.'),
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
