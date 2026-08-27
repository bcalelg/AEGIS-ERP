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
import { Empresa, EmpresaRequest } from '../models/empresa.models';
import { EmpresaService } from '../services/empresa.service';

@Component({
  selector: 'app-empresa-form',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './empresa-form.component.html',
  styleUrl: './empresa-form.component.css',
})
export class EmpresaFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly service = inject(EmpresaService);
  private readonly notification = inject(NotificationService);

  readonly selected = input<Empresa | null>(null);
  readonly saved = output<void>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly policyFields = [
    { key: 'passwordCantidadMayusculas', label: 'Mayúsculas mínimas' },
    { key: 'passwordCantidadMinusculas', label: 'Minúsculas mínimas' },
    { key: 'passwordCantidadNumeros', label: 'Números mínimos' },
    { key: 'passwordCantidadCaracteresEspeciales', label: 'Caracteres especiales mínimos' },
    { key: 'passwordLargo', label: 'Longitud mínima' },
    { key: 'passwordIntentosAntesDeBloquear', label: 'Intentos antes de bloqueo' },
    { key: 'passwordCantidadPreguntasValidar', label: 'Preguntas de recuperación' },
    { key: 'passwordCantidadCaducidadDias', label: 'Días de vigencia' },
  ] as const;
  readonly form = this.formBuilder.nonNullable.group(
    {
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      direccion: ['', [Validators.required, Validators.maxLength(200)]],
      nit: ['', [Validators.required, Validators.maxLength(20)]],
      passwordCantidadMayusculas: [0, [Validators.required, Validators.min(0)]],
      passwordCantidadMinusculas: [0, [Validators.required, Validators.min(0)]],
      passwordCantidadNumeros: [0, [Validators.required, Validators.min(0)]],
      passwordCantidadCaracteresEspeciales: [0, [Validators.required, Validators.min(0)]],
      passwordLargo: [8, [Validators.required, Validators.min(1)]],
      passwordIntentosAntesDeBloquear: [5, [Validators.required, Validators.min(1)]],
      passwordCantidadPreguntasValidar: [0, [Validators.required, Validators.min(0)]],
      passwordCantidadCaducidadDias: [60, [Validators.required, Validators.min(1)]],
    },
    {
      validators: (control) => {
        const value = control.value;
        return (value.passwordLargo ?? 0) <
          (value.passwordCantidadMayusculas ?? 0) +
            (value.passwordCantidadMinusculas ?? 0) +
            (value.passwordCantidadNumeros ?? 0) +
            (value.passwordCantidadCaracteresEspeciales ?? 0)
          ? { passwordPolicy: true }
          : null;
      },
    },
  );

  constructor() {
    effect(() => {
      const selected = this.selected();
      this.form.reset(
        selected ?? {
          nombre: '',
          direccion: '',
          nit: '',
          passwordCantidadMayusculas: 0,
          passwordCantidadMinusculas: 0,
          passwordCantidadNumeros: 0,
          passwordCantidadCaracteresEspeciales: 0,
          passwordLargo: 8,
          passwordIntentosAntesDeBloquear: 5,
          passwordCantidadPreguntasValidar: 0,
          passwordCantidadCaducidadDias: 60,
        },
      );
    });
  }

  touched(key: string): boolean {
    const control = this.form.get(key);
    return Boolean(control?.touched && control.invalid);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    const request = this.form.getRawValue() as EmpresaRequest;
    const selected = this.selected();
    const operation = selected
      ? this.service.update(selected.idEmpresa, request)
      : this.service.create(request);

    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notification.success(
          selected ? 'Empresa actualizada correctamente.' : 'Empresa creada correctamente.',
        );
        this.saved.emit();
      },
      error: (error) =>
        this.notification.operationError(error, 'No fue posible guardar la empresa.'),
    });
  }

  cancel(): void {
    this.form.reset();
    this.cancelled.emit();
  }
}
