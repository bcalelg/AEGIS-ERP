import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ConfirmationService } from '../../../../core/confirmation/confirmation.service';
import { NotificationService } from '../../../../core/notifications/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { downloadFile } from '../../../../core/utils/download-file';
import { EmpresaFormComponent } from '../empresa-form/empresa-form.component';
import { Empresa } from '../models/empresa.models';
import { EmpresaService } from '../services/empresa.service';
@Component({
  selector: 'app-empresa-list',
  imports: [FormsModule, EmpresaFormComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './empresa-list.component.html',
  styleUrl: './empresa-list.component.css',
})
export class EmpresaListComponent implements OnInit {
  private service = inject(EmpresaService);
  private permission = inject(PermissionService);
  private notification = inject(NotificationService);
  private confirmation = inject(ConfirmationService);
  readonly permissions = signal(this.permission.forPage('empresa'));
  readonly items = signal<Empresa[]>([]);
  readonly page = signal(0);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly formOpen = signal(false);
  readonly editing = signal<Empresa | null>(null);
  readonly exportOpen = signal(false);
  search = '';
  ngOnInit() {
    this.load(0);
  }
  load(page: number) {
    this.loading.set(true);
    this.error.set('');
    this.service
      .list(this.search, page)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => {
          this.items.set(r.content);
          this.page.set(r.page);
          this.totalElements.set(r.totalElements);
          this.totalPages.set(r.totalPages);
        },
        error: (e) => this.error.set(e.error?.detail ?? 'No fue posible cargar las empresas.'),
      });
  }
  openCreate(): void {
    this.editing.set(null);
    this.formOpen.set(true);
  }

  openEdit(item: Empresa): void {
    this.editing.set(item);
    this.formOpen.set(true);
  }

  closeForm(): void {
    this.formOpen.set(false);
    this.editing.set(null);
  }

  saved(): void {
    this.closeForm();
    this.load(this.page());
  }
  async confirmRemove(item: Empresa): Promise<void> {
    const confirmed = await this.confirmation.confirm({
      title: 'Eliminar empresa',
      message: `¿Desea eliminar la empresa "${item.nombre}"?`,
      warningText: 'Esta acción no se puede deshacer.',
    });
    if (confirmed) this.remove(item);
  }

  remove(item: Empresa) {
    this.service.delete(item.idEmpresa).subscribe({
      next: () => {
        this.confirmation.complete();
        this.load(this.page());
        this.notification.success('Empresa eliminada correctamente.');
      },
      error: (e) => {
        this.confirmation.complete();
        this.notification.operationError(e, 'No fue posible eliminar la empresa.');
      },
    });
  }
  print() {
    this.service.print(this.search).subscribe({
      next: (r) => {
        this.items.set(r.content);
        setTimeout(() => window.print());
      },
      error: (e) => this.notification.operationError(e, 'No fue posible preparar la impresión.'),
    });
  }
  exportCsv() {
    this.downloadExport(this.service.exportCsv(this.search), 'empresas.csv');
  }
  exportExcel() {
    this.downloadExport(this.service.exportExcel(this.search), 'empresas.xlsx');
  }
  exportPdf() {
    this.downloadExport(this.service.exportPdf(this.search), 'empresas.pdf');
  }
  private downloadExport(request: ReturnType<EmpresaService['exportCsv']>, filename: string) {
    this.exportOpen.set(false);
    request.subscribe({
      next: (blob) => downloadFile(blob, filename),
      error: (e) => this.notification.operationError(e, 'No fue posible exportar.'),
    });
  }
}
