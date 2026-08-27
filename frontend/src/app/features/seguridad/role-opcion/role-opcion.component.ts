import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, finalize } from 'rxjs';
import { NotificationService } from '../../../core/notifications/notification.service';
import { PermissionService } from '../../../core/services/permission.service';
import { CatalogOption, PermissionKey, RoleOpcionMatrixItem } from './models/role-opcion.models';
import { RoleOpcionService } from './services/role-opcion.service';

@Component({
  selector: 'app-role-opcion',
  imports: [FormsModule],
  templateUrl: './role-opcion.component.html',
  styleUrl: './role-opcion.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleOpcionComponent implements OnInit {
  private readonly service = inject(RoleOpcionService);
  private readonly permission = inject(PermissionService);
  private readonly notification = inject(NotificationService);

  readonly permissions = signal(this.permission.forPage('asignacion_opcion_role'));
  readonly roles = signal<CatalogOption[]>([]);
  readonly modulos = signal<CatalogOption[]>([]);
  readonly selectedRole = signal(0);
  readonly selectedModulo = signal(0);
  readonly matrix = signal<RoleOpcionMatrixItem[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly dirty = signal(false);
  readonly error = signal('');
  readonly pendingSelection = signal<{ role: number; modulo: number } | null>(null);
  readonly grouped = computed(() => {
    const groups = new Map<string, RoleOpcionMatrixItem[]>();
    for (const item of this.matrix()) {
      const group = groups.get(item.nombreMenu) ?? [];
      group.push(item);
      groups.set(item.nombreMenu, group);
    }
    return [...groups.entries()];
  });

  ngOnInit(): void {
    this.loading.set(true);
    forkJoin({ roles: this.service.roles(), modulos: this.service.modulos() })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ roles, modulos }) => {
          this.roles.set(roles);
          this.modulos.set(modulos);
        },
        error: (error) => this.showError(error, 'No fue posible cargar los catálogos.'),
      });
  }

  changeSelection(role: number, modulo: number): void {
    if (this.dirty()) {
      this.pendingSelection.set({ role, modulo });
      return;
    }
    this.applySelection(role, modulo);
  }

  discardChanges(): void {
    const pending = this.pendingSelection();
    this.pendingSelection.set(null);
    if (pending) this.applySelection(pending.role, pending.modulo);
  }

  keepEditing(): void {
    this.pendingSelection.set(null);
  }

  toggle(item: RoleOpcionMatrixItem, permission: PermissionKey, checked: boolean): void {
    this.matrix.update((items) =>
      items.map((current) =>
        current.idOpcion === item.idOpcion ? { ...current, [permission]: checked } : current,
      ),
    );
    this.dirty.set(true);
  }

  save(): void {
    if (!this.selectedRole() || !this.selectedModulo() || !this.permissions()?.cambio) return;
    this.saving.set(true);
    this.error.set('');
    this.service
      .save({
        idRole: this.selectedRole(),
        idModulo: this.selectedModulo(),
        opciones: this.matrix(),
      })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (matrix) => {
          this.matrix.set(matrix);
          this.dirty.set(false);
          this.notification.success('Permisos guardados correctamente.');
        },
        error: (error) =>
          this.notification.operationError(error, 'No fue posible guardar los permisos.'),
      });
  }

  private applySelection(role: number, modulo: number): void {
    this.selectedRole.set(role);
    this.selectedModulo.set(modulo);
    this.matrix.set([]);
    this.dirty.set(false);
    if (!role || !modulo) return;
    this.loading.set(true);
    this.error.set('');
    this.service
      .matrix(role, modulo)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (matrix) => this.matrix.set(matrix),
        error: (error) => this.showError(error, 'No fue posible cargar la matriz.'),
      });
  }

  private showError(error: { error?: { detail?: string } }, fallback: string): void {
    this.error.set(error.error?.detail ?? fallback);
  }
}
