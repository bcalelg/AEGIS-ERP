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
import { NotificationService } from '../../../../core/notifications/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { downloadFile } from '../../../../core/utils/download-file';
import { OpcionFormComponent } from '../opcion-form/opcion-form.component';
import { OpcionMaintenance } from '../models/opcion.models';
import { OpcionService } from '../services/opcion.service';

@Component({
  selector: 'app-opcion-list',
  imports: [FormsModule, OpcionFormComponent],
  templateUrl: './opcion-list.component.html',
  styleUrl: './opcion-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OpcionListComponent implements OnInit {
  private readonly service = inject(OpcionService);
  private readonly permission = inject(PermissionService);
  private readonly notification = inject(NotificationService);
  readonly permissions = signal(this.permission.forPage('opcion'));
  readonly items = signal<OpcionMaintenance[]>([]);
  readonly search = signal('');
  readonly filteredItems = computed(() => {
    const value = this.search().trim().toLowerCase();
    return value
      ? this.items().filter((item) =>
          [item.nombreModulo, item.nombreMenu, item.nombre, item.pagina].some((field) =>
            field.toLowerCase().includes(value),
          ),
        )
      : this.items();
  });
  readonly loading = signal(false);
  readonly error = signal('');
  readonly formOpen = signal(false);
  readonly editing = signal<OpcionMaintenance | null>(null);
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
          this.error.set(error.error?.detail ?? 'No fue posible cargar las opciones.'),
      });
  }
  openCreate(): void {
    this.editing.set(null);
    this.formOpen.set(true);
  }
  openEdit(item: OpcionMaintenance): void {
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
    this.download(this.service.exportCsv(this.search()), 'opciones.csv');
  }
  exportExcel(): void {
    this.download(this.service.exportExcel(this.search()), 'opciones.xlsx');
  }
  exportPdf(): void {
    this.download(this.service.exportPdf(this.search()), 'opciones.pdf');
  }
  private download(request: ReturnType<OpcionService['exportCsv']>, filename: string): void {
    this.error.set('');
    this.exportOpen.set(false);
    request.subscribe({
      next: (blob) => downloadFile(blob, filename),
      error: (error) => this.notification.operationError(error, 'No fue posible exportar.'),
    });
  }
}
