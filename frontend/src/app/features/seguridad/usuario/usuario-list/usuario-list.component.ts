import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ConfirmationService } from '../../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { downloadFile } from '../../../../core/utils/download-file';
import { Usuario, UsuarioSummary } from '../models/usuario.models';
import { UsuarioService } from '../services/usuario.service';
import { UsuarioFormComponent } from '../usuario-form/usuario-form.component';

@Component({
  selector: 'app-usuario-list',
  imports: [DatePipe, FormsModule, UsuarioFormComponent],
  templateUrl: './usuario-list.component.html',
  styleUrl: './usuario-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsuarioListComponent implements OnInit {
  private readonly service = inject(UsuarioService);
  private readonly permission = inject(PermissionService);
  private readonly notification = inject(NotificationService);
  private readonly confirmation = inject(ConfirmationService);

  readonly permissions = signal(this.permission.forPage('usuario'));
  readonly items = signal<UsuarioSummary[]>([]);
  readonly search = signal('');
  readonly filteredItems = computed(() => {
    const value = this.search().trim().toLowerCase();
    return value
      ? this.items().filter((item) =>
          [item.idUsuario, item.nombre, item.apellido, item.nombreRole, item.nombreEmpresa,
            item.nombreSucursal, item.nombreStatusUsuario]
            .some((field) => field.toLowerCase().includes(value)),
        )
      : this.items();
  });
  readonly loading = signal(false);
  readonly error = signal('');
  readonly formOpen = signal(false);
  readonly editing = signal<Usuario | null>(null);
  readonly exportOpen = signal(false);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.service.list().pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (items) => this.items.set(items),
      error: (error) => this.error.set(error.error?.detail ?? 'No fue posible cargar los usuarios.'),
    });
  }

  openCreate(): void { this.editing.set(null); this.formOpen.set(true); }
  openEdit(item: UsuarioSummary): void {
    this.service.get(item.idUsuario).subscribe({
      next: (detail) => { this.editing.set(detail); this.formOpen.set(true); },
      error: (error) => this.notification.operationError(error, 'No fue posible consultar el usuario.'),
    });
  }
  closeForm(): void { this.formOpen.set(false); this.editing.set(null); }
  saved(): void { this.closeForm(); this.load(); }

  print(): void {
    this.service.print(this.search()).subscribe({
      next: (items) => { this.items.set(items); setTimeout(() => window.print()); },
      error: (error) => this.notification.operationError(error, 'No fue posible preparar la impresión.'),
    });
  }

  exportCsv(): void { this.download(this.service.exportCsv(this.search()), 'usuarios.csv'); }
  exportExcel(): void { this.download(this.service.exportExcel(this.search()), 'usuarios.xlsx'); }
  exportPdf(): void { this.download(this.service.exportPdf(this.search()), 'usuarios.pdf'); }

  async confirmRemove(item: UsuarioSummary): Promise<void> {
    const confirmed = await this.confirmation.confirm({
      title: 'Eliminar usuario',
      message: `¿Desea eliminar el usuario "${item.idUsuario}"?`,
      warningText: 'Esta acción no se puede deshacer.',
    });
    if (confirmed) this.remove(item);
  }

  remove(item: UsuarioSummary): void {
    this.service.delete(item.idUsuario).subscribe({
      next: () => {
        this.confirmation.complete();
        this.load();
        this.notification.success('Usuario eliminado correctamente.');
      },
      error: (error) => {
        this.confirmation.complete();
        this.notification.operationError(error, 'No fue posible eliminar el usuario.');
      },
    });
  }

  fullName(item: UsuarioSummary): string { return `${item.nombre} ${item.apellido}`.trim(); }

  private download(request: ReturnType<UsuarioService['exportCsv']>, filename: string): void {
    this.exportOpen.set(false);
    request.subscribe({
      next: (blob) => downloadFile(blob, filename),
      error: (error) => this.notification.operationError(error, 'No fue posible exportar.'),
    });
  }
}
