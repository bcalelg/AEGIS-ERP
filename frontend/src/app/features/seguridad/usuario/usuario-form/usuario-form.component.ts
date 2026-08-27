import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { NotificationService } from '../../../../core/notifications/notification.service';
import {
  Usuario,
  UsuarioCreateRequest,
  UsuarioOption,
  UsuarioUpdateRequest,
} from '../models/usuario.models';
import { UsuarioService } from '../services/usuario.service';

type ControlName =
  | 'idUsuario'
  | 'nombre'
  | 'apellido'
  | 'fechaNacimiento'
  | 'correoElectronico'
  | 'telefonoMovil'
  | 'password'
  | 'passwordConfirmacion'
  | 'pregunta'
  | 'respuesta'
  | 'idEmpresa'
  | 'idSucursal'
  | 'idGenero'
  | 'idStatusUsuario'
  | 'idRole';

const PHONE_PATTERN = /^[0-9+()\-\s]+$/;

export function minimumAdultBirthDate(reference = new Date()): string {
  const maximum = new Date(reference.getFullYear() - 18, reference.getMonth(), reference.getDate());
  const year = maximum.getFullYear();
  const month = String(maximum.getMonth() + 1).padStart(2, '0');
  const day = String(maximum.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function adultValidator(control: AbstractControl<string>): ValidationErrors | null {
  if (!control.value) return null;
  return control.value <= minimumAdultBirthDate() ? null : { underage: true };
}

@Component({
  selector: 'app-usuario-form',
  imports: [ReactiveFormsModule],
  templateUrl: './usuario-form.component.html',
  styleUrl: './usuario-form.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsuarioFormComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly service = inject(UsuarioService);
  private readonly notification = inject(NotificationService);

  readonly selected = input<Usuario | null>(null);
  readonly saved = output<void>();
  readonly cancelled = output<void>();
  readonly saving = signal(false);
  readonly loadingCatalogs = signal(true);
  readonly loadingBranches = signal(false);
  readonly companies = signal<UsuarioOption[]>([]);
  readonly branches = signal<UsuarioOption[]>([]);
  readonly genders = signal<UsuarioOption[]>([]);
  readonly statuses = signal<UsuarioOption[]>([]);
  readonly roles = signal<UsuarioOption[]>([]);
  readonly maximumBirthDate = minimumAdultBirthDate();
  readonly form = this.formBuilder.nonNullable.group({
    idUsuario: ['', [Validators.required, Validators.maxLength(50)]],
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    fechaNacimiento: ['', [Validators.required, adultValidator]],
    correoElectronico: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    telefonoMovil: ['', [Validators.maxLength(30), Validators.pattern(PHONE_PATTERN)]],
    password: [''],
    passwordConfirmacion: [''],
    pregunta: ['', [Validators.required, Validators.maxLength(200)]],
    respuesta: ['', Validators.maxLength(200)],
    idEmpresa: [0, [Validators.required, Validators.min(1)]],
    idSucursal: [{ value: 0, disabled: true }, [Validators.required, Validators.min(1)]],
    idGenero: [0, [Validators.required, Validators.min(1)]],
    idStatusUsuario: [0, [Validators.required, Validators.min(1)]],
    idRole: [0, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    this.loadCatalogs();
    effect(() => this.prepare(this.selected()));
  }

  companyChanged(): void {
    const id = this.form.controls.idEmpresa.value;
    this.form.controls.idSucursal.setValue(0);
    this.branches.set([]);
    if (id > 0) this.loadBranches(id);
    else this.form.controls.idSucursal.disable();
  }

  save(): void {
    const selected = this.selected();
    this.applyPasswordValidators(!selected);
    if (!selected && this.form.controls.password.value !== this.form.controls.passwordConfirmacion.value) {
      this.form.controls.passwordConfirmacion.setErrors({ mismatch: true });
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.saving.set(true);
    const operation = selected
      ? this.service.update(selected.idUsuario, this.updateRequest(value))
      : this.service.create(value as UsuarioCreateRequest);
    operation.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notification.success(
          selected ? 'Usuario actualizado correctamente.' : 'Usuario creado correctamente.',
        );
        this.saved.emit();
      },
      error: (error) => this.notification.operationError(error, 'No fue posible guardar el usuario.'),
    });
  }

  cancel(): void {
    this.cancelled.emit();
  }

  invalid(name: ControlName): boolean {
    const control = this.form.controls[name];
    return control.touched && control.invalid;
  }

  private loadCatalogs(): void {
    forkJoin({
      companies: this.service.empresaOptions(),
      genders: this.service.generoOptions(),
      statuses: this.service.statusOptions(),
      roles: this.service.roleOptions(),
    })
      .pipe(finalize(() => this.loadingCatalogs.set(false)))
      .subscribe({
        next: ({ companies, genders, statuses, roles }) => {
          this.companies.set(companies);
          this.genders.set(genders);
          this.statuses.set(statuses);
          this.roles.set(roles);
        },
        error: (error) =>
          this.notification.operationError(error, 'No fue posible cargar los catálogos del usuario.'),
      });
  }

  private prepare(selected: Usuario | null): void {
    this.applyPasswordValidators(!selected);
    if (selected) this.form.controls.idUsuario.disable();
    else this.form.controls.idUsuario.enable();
    this.form.reset({
      idUsuario: selected?.idUsuario ?? '',
      nombre: selected?.nombre ?? '',
      apellido: selected?.apellido ?? '',
      fechaNacimiento: selected?.fechaNacimiento ?? '',
      correoElectronico: selected?.correoElectronico ?? '',
      telefonoMovil: selected?.telefonoMovil ?? '',
      password: '',
      passwordConfirmacion: '',
      pregunta: selected?.pregunta ?? '',
      respuesta: '',
      idEmpresa: selected?.idEmpresa ?? 0,
      idSucursal: selected?.idSucursal ?? 0,
      idGenero: selected?.idGenero ?? 0,
      idStatusUsuario: selected?.idStatusUsuario ?? 0,
      idRole: selected?.idRole ?? 0,
    });
    this.branches.set([]);
    if (selected) this.loadBranches(selected.idEmpresa, selected.idSucursal);
    else this.form.controls.idSucursal.disable();
  }

  private loadBranches(idEmpresa: number, selectedBranch = 0): void {
    this.loadingBranches.set(true);
    this.form.controls.idSucursal.disable();
    this.service
      .sucursalOptions(idEmpresa)
      .pipe(finalize(() => this.loadingBranches.set(false)))
      .subscribe({
        next: (items) => {
          this.branches.set(items);
          this.form.controls.idSucursal.enable();
          const compatible = items.some((item) => item.id === selectedBranch);
          this.form.controls.idSucursal.setValue(compatible ? selectedBranch : 0);
        },
        error: (error) =>
          this.notification.operationError(error, 'No fue posible cargar las sucursales.'),
      });
  }

  private applyPasswordValidators(create: boolean): void {
    const password = this.form.controls.password;
    const confirmation = this.form.controls.passwordConfirmacion;
    const answer = this.form.controls.respuesta;
    password.setValidators(create ? [Validators.required] : []);
    confirmation.setValidators(create ? [Validators.required] : []);
    answer.setValidators(create ? [Validators.required, Validators.maxLength(200)] : [Validators.maxLength(200)]);
    password.updateValueAndValidity({ emitEvent: false });
    confirmation.updateValueAndValidity({ emitEvent: false });
    answer.updateValueAndValidity({ emitEvent: false });
  }

  private updateRequest(value: UsuarioCreateRequest): UsuarioUpdateRequest {
    return {
      nombre: value.nombre,
      apellido: value.apellido,
      fechaNacimiento: value.fechaNacimiento,
      correoElectronico: value.correoElectronico,
      telefonoMovil: value.telefonoMovil,
      pregunta: value.pregunta,
      respuesta: value.respuesta,
      idEmpresa: value.idEmpresa,
      idSucursal: value.idSucursal,
      idGenero: value.idGenero,
      idStatusUsuario: value.idStatusUsuario,
      idRole: value.idRole,
    };
  }
}
