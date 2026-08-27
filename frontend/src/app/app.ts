import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ConfirmationDialogComponent } from './core/confirmation/confirmation-dialog.component';
import { NotificationComponent } from './core/notifications/notification.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NotificationComponent, ConfirmationDialogComponent],
  template: '<router-outlet /><app-notification /><app-confirmation-dialog />',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {}
