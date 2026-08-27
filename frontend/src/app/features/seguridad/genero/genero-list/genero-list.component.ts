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
import { GeneroFormComponent } from '../genero-form/genero-form.component';
import { Genero } from '../models/genero.models';
import { GeneroService } from '../services/genero.service';

@Component({
  selector: 'app-genero-list',
  imports: [FormsModule, GeneroFormComponent],
  templateUrl: './genero-list.component.html',
  styleUrl: './genero-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GeneroListComponent implements OnInit {
  private readonly service = inject(GeneroService);
  private readonly permission = inject(PermissionService);
  private readonly notification = inject(NotificationService);
  private readonly confirmation = inject(ConfirmationService);

  readonly permissions = signal(this.permission.forPage('genero'));
  readonly items = signal<Genero[]>([]);
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
  readonly editing = signal<Genero | null>(null);
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
          this.error.set(error.error?.detail ?? 'No fue posible cargar los géneros.'),
      });
  }

  openCreate(): void {
    this.editing.set(null);
    this.formOpen.set(true);
  }

  openEdit(item: Genero): void {
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
    this.service.print().subscribe({
      next: (items) => {
        this.items.set(items);
        setTimeout(() => window.print());
      },
      error: (error) =>
        this.notification.operationError(error, 'No fue posible preparar la impresión.'),
    });
  }

  exportCsv(): void {
    this.downloadExport(this.service.exportCsv(this.search()), 'generos.csv');
  }

  exportExcel(): void {
    this.downloadExport(this.service.exportExcel(this.search()), 'generos.xlsx');
  }

  exportPdf(): void {
    this.downloadExport(this.service.exportPdf(this.search()), 'generos.pdf');
  }

  private downloadExport(request: ReturnType<GeneroService['exportCsv']>, filename: string): void {
    this.error.set('');
    this.exportOpen.set(false);
    request.subscribe({
      next: (blob) => downloadFile(blob, filename),
      error: (error) => this.notification.operationError(error, 'No fue posible exportar.'),
    });
  }
  async confirmRemove(item: Genero): Promise<void> {
    const confirmed = await this.confirmation.confirm({
      title: 'Eliminar género',
      message: `¿Desea eliminar el género "${item.nombre}"?`,
      warningText: 'Esta acción no se puede deshacer.',
    });
    if (confirmed) this.remove(item);
  }

  remove(item: Genero): void {
    this.service.delete(item.id).subscribe({
      next: () => {
        this.confirmation.complete();
        this.load();
        this.notification.success('Género eliminado correctamente.');
      },
      error: (error) => {
        this.confirmation.complete();
        this.notification.operationError(error, 'No fue posible eliminar el género.');
      },
    });
  }
}
