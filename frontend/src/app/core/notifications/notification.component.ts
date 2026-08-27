import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AppNotification } from './notification.models';
import { NotificationService } from './notification.service';

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationComponent {
  readonly notification = inject(NotificationService);

  cssClass(item: AppNotification): string {
    const variants = {
      success: 'notification-toast--success',
      info: 'notification-toast--info',
      warning: 'notification-toast--warning',
      error: 'notification-toast--error',
    } as const;
    return variants[item.type];
  }

  icon(item: AppNotification): string {
    const icons = {
      success: 'bi-check-circle-fill',
      info: 'bi-info-circle-fill',
      warning: 'bi-exclamation-triangle-fill',
      error: 'bi-x-octagon-fill',
    } as const;
    return icons[item.type];
  }

  label(item: AppNotification): string {
    const labels = {
      success: 'Éxito',
      info: 'Información',
      warning: 'Advertencia',
      error: 'Error',
    } as const;
    return labels[item.type];
  }
}
