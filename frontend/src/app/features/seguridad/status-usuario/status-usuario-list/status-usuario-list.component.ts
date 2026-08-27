import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ConfirmationService } from '../../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { downloadFile } from '../../../../core/utils/download-file';
import { StatusUsuarioFormComponent } from '../status-usuario-form/status-usuario-form.component';
import { StatusUsuario } from '../models/status-usuario.models';
import { StatusUsuarioService } from '../services/status-usuario.service';

@Component({
  selector: 'app-status-usuario-list',
  imports: [FormsModule, StatusUsuarioFormComponent],
  templateUrl: './status-usuario-list.component.html',
  styleUrl: './status-usuario-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusUsuarioListComponent implements OnInit {
  private readonly service = inject(StatusUsuarioService);
  private readonly permission = inject(PermissionService);
  private readonly notification = inject(NotificationService);
  private readonly confirmation = inject(ConfirmationService);

  readonly permissions = signal(this.permission.forPage('status_usuario'));
  readonly items = signal<StatusUsuario[]>([]);
  readonly search = signal('');
  readonly filteredItems = computed(() => {
    const search = this.search().trim().toLowerCase();
    return search
      ? this.items().filter((item) => item.nombre.toLowerCase().includes(search))
      : this.items();
  });
  readonly loading = signal(false);
  readonly error = signal('');
  readonly formOpen = signal(false);
  readonly editing = signal<StatusUsuario | null>(null);
  readonly exportOpen = signal(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');
    this.service
      .list()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (items) => this.items.set(items),
        error: (error) =>
          this.error.set(error.error?.detail ?? 'No fue posible cargar los estatus de usuario.'),
      });
  }

  openCreate(): void {
    this.editing.set(null);
    this.formOpen.set(true);
  }

  openEdit(item: StatusUsuario): void {
    this.editing.set(item);
    this.formOpen.set(true);
  }

  closeForm(): void {
    this.formOpen.set(false);
    this.editing.set(null);
  }

  saved(): void {
    this.closeForm();
    this.load();
  }

  print(): void {
    this.error.set('');
    this.service.print(this.search()).subscribe({
      next: (items) => {
        this.items.set(items);
        setTimeout(() => window.print());
      },
      error: (error) =>
        this.notification.operationError(error, 'No fue posible preparar la impresión.'),
    });
  }

  exportCsv(): void {
    this.downloadExport(this.service.exportCsv(this.search()), 'estatus-usuarios.csv');
  }

  exportExcel(): void {
    this.downloadExport(this.service.exportExcel(this.search()), 'estatus-usuarios.xlsx');
  }

  exportPdf(): void {
    this.downloadExport(this.service.exportPdf(this.search()), 'estatus-usuarios.pdf');
  }

  async confirmRemove(item: StatusUsuario): Promise<void> {
    const confirmed = await this.confirmation.confirm({
      title: 'Eliminar estatus de usuario',
      message: `¿Desea eliminar el estatus de usuario "${item.nombre}"?`,
      warningText: 'Esta acción no se puede deshacer.',
    });
    if (confirmed) this.remove(item);
  }

  remove(item: StatusUsuario): void {
    this.service.delete(item.id).subscribe({
      next: () => {
        this.confirmation.complete();
        this.load();
        this.notification.success('Estatus de usuario eliminado correctamente.');
      },
      error: (error) => {
        this.confirmation.complete();
        this.notification.operationError(error, 'No fue posible eliminar el estatus de usuario.');
      },
    });
  }

  private downloadExport(
    request: ReturnType<StatusUsuarioService['exportCsv']>,
    filename: string,
  ): void {
    this.error.set('');
    this.exportOpen.set(false);
    request.subscribe({
      next: (blob) => downloadFile(blob, filename),
      error: (error) => this.notification.operationError(error, 'No fue posible exportar.'),
    });
  }
}
