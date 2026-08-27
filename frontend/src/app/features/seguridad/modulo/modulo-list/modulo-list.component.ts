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
import { ModuloFormComponent } from '../modulo-form/modulo-form.component';
import { Modulo } from '../models/modulo.models';
import { ModuloService } from '../services/modulo.service';

@Component({
  selector: 'app-modulo-list',
  imports: [FormsModule, ModuloFormComponent],
  templateUrl: './modulo-list.component.html',
  styleUrl: './modulo-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModuloListComponent implements OnInit {
  private readonly service = inject(ModuloService);
  private readonly permission = inject(PermissionService);
  private readonly notification = inject(NotificationService);

  readonly permissions = signal(this.permission.forPage('modulo'));
  readonly items = signal<Modulo[]>([]);
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
  readonly editing = signal<Modulo | null>(null);
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
          this.error.set(error.error?.detail ?? 'No fue posible cargar los módulos.'),
      });
  }

  openCreate(): void {
    this.editing.set(null);
    this.formOpen.set(true);
  }

  openEdit(item: Modulo): void {
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
    this.downloadExport(this.service.exportCsv(this.search()), 'modulos.csv');
  }

  exportExcel(): void {
    this.downloadExport(this.service.exportExcel(this.search()), 'modulos.xlsx');
  }

  exportPdf(): void {
    this.downloadExport(this.service.exportPdf(this.search()), 'modulos.pdf');
  }

  private downloadExport(request: ReturnType<ModuloService['exportCsv']>, filename: string): void {
    this.error.set('');
    this.exportOpen.set(false);
    request.subscribe({
      next: (blob) => downloadFile(blob, filename),
      error: (error) => this.notification.operationError(error, 'No fue posible exportar.'),
    });
  }
}
