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
import { EmpresaOption, Sucursal, SucursalRequest } from '../models/sucursal.models';
import { SucursalService } from '../services/sucursal.service';

@Component({
  selector: 'app-sucursal-form',
  imports: [ReactiveFormsModule],
  templateUrl: './sucursal-form.component.html',
  styleUrl: './sucursal-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SucursalFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly service = inject(SucursalService);
  private readonly notification = inject(NotificationService);

  readonly selected = input<Sucursal | null>(null);
  readonly saved = output<void>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly loadingCompanies = signal(true);
  readonly companies = signal<EmpresaOption[]>([]);
  readonly error = signal('');
  readonly form = this.formBuilder.nonNullable.group({
    idEmpresa: [{ value: 0, disabled: true }, [Validators.required, Validators.min(1)]],
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    direccion: ['', [Validators.required, Validators.maxLength(200)]],
  });

  constructor() {
    this.loadCompanies();
    effect(() => {
      const selected = this.selected();
      this.error.set('');
      this.form.reset({
        idEmpresa: selected?.idEmpresa ?? 0,
        nombre: selected?.nombre ?? '',
        direccion: selected?.direccion ?? '',
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
    const request: SucursalRequest = this.form.getRawValue();
    const selected = this.selected();
    const operation = selected
      ? this.service.update(selected.id, request)
      : this.service.create(request);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notification.success(
          selected ? 'Sucursal actualizada correctamente.' : 'Sucursal creada correctamente.',
        );
        this.saved.emit();
      },
      error: (error) =>
        this.notification.operationError(error, 'No fue posible guardar la sucursal.'),
    });
  }

  cancel(): void {
    this.error.set('');
    this.cancelled.emit();
  }

  invalid(name: 'idEmpresa' | 'nombre' | 'direccion'): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }

  private loadCompanies(): void {
    this.service
      .empresaOptions()
      .pipe(
        finalize(() => {
          this.loadingCompanies.set(false);
          this.form.controls.idEmpresa.enable();
        }),
      )
      .subscribe({
        next: (companies) => this.companies.set(companies),
        error: (error) =>
          this.error.set(error.error?.detail ?? 'No fue posible cargar las empresas.'),
      });
  }
}
