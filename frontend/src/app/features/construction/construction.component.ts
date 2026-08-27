import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { PermissionService } from '../../core/services/permission.service';
@Component({
  selector: 'app-construction',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './construction.component.html',
  styleUrl: './construction.component.css',
})
export class ConstructionComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly permissionService = inject(PermissionService);
  readonly page = toSignal(this.route.paramMap.pipe(map((params) => params.get('pagina') ?? '')), {
    initialValue: '',
  });
  readonly permissions = computed(() => this.permissionService.forPage(this.page()));
}
