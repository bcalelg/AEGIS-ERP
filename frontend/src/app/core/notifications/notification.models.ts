export type NotificationType = 'success' | 'info' | 'warning' | 'error';

export interface AppNotification {
  id: number;
  type: NotificationType;
  message: string;
  duration: number | null;
}

export interface NotificationOptions {
  duration?: number | null;
}

export const NOTIFICATION_DURATION = {
  success: 3000,
  info: 3000,
  warning: 5000,
  error: null,
} as const satisfies Record<NotificationType, number | null>;
