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
import { SucursalFormComponent } from '../sucursal-form/sucursal-form.component';
import { Sucursal } from '../models/sucursal.models';
import { SucursalService } from '../services/sucursal.service';

@Component({
  selector: 'app-sucursal-list',
  imports: [FormsModule, SucursalFormComponent],
  templateUrl: './sucursal-list.component.html',
  styleUrl: './sucursal-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SucursalListComponent implements OnInit {
  private readonly service = inject(SucursalService);
  private readonly permission = inject(PermissionService);
  private readonly notification = inject(NotificationService);
  private readonly confirmation = inject(ConfirmationService);

  readonly permissions = signal(this.permission.forPage('sucursal'));
  readonly items = signal<Sucursal[]>([]);
  readonly search = signal('');
  readonly filteredItems = computed(() => {
    const search = this.search().trim().toLowerCase();
    return search
      ? this.items().filter(
          (item) =>
            item.nombre.toLowerCase().includes(search) ||
            item.nombreEmpresa.toLowerCase().includes(search) ||
            item.direccion.toLowerCase().includes(search),
        )
      : this.items();
  });
  readonly loading = signal(false);
  readonly error = signal('');
  readonly formOpen = signal(false);
  readonly editing = signal<Sucursal | null>(null);
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
          this.error.set(error.error?.detail ?? 'No fue posible cargar las sucursales.'),
      });
  }

  openCreate(): void {
    this.editing.set(null);
    this.formOpen.set(true);
  }

  openEdit(item: Sucursal): void {
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
    this.downloadExport(this.service.exportCsv(this.search()), 'sucursales.csv');
  }

  exportExcel(): void {
    this.downloadExport(this.service.exportExcel(this.search()), 'sucursales.xlsx');
  }

  exportPdf(): void {
    this.downloadExport(this.service.exportPdf(this.search()), 'sucursales.pdf');
  }

  async confirmRemove(item: Sucursal): Promise<void> {
    const confirmed = await this.confirmation.confirm({
      title: 'Eliminar sucursal',
      message: `¿Desea eliminar la sucursal "${item.nombre}"?`,
      warningText: 'Esta acción no se puede deshacer.',
    });
    if (confirmed) this.remove(item);
  }

  remove(item: Sucursal): void {
    this.service.delete(item.id).subscribe({
      next: () => {
        this.confirmation.complete();
        this.load();
        this.notification.success('Sucursal eliminada correctamente.');
      },
      error: (error) => {
        this.confirmation.complete();
        this.notification.operationError(error, 'No fue posible eliminar la sucursal.');
      },
    });
  }

  private downloadExport(
    request: ReturnType<SucursalService['exportCsv']>,
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
