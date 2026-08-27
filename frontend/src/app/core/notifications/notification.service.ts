import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import {
  AppNotification,
  NOTIFICATION_DURATION,
  NotificationOptions,
  NotificationType,
} from './notification.models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private static readonly maxVisible = 3;
  private readonly destroyRef = inject(DestroyRef);
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();
  private nextId = 0;

  readonly notifications = signal<AppNotification[]>([]);

  constructor() {
    this.destroyRef.onDestroy(() => this.clear());
  }

  success(message: string, options?: NotificationOptions): number {
    return this.show('success', message, options);
  }

  info(message: string, options?: NotificationOptions): number {
    return this.show('info', message, options);
  }

  warning(message: string, options?: NotificationOptions): number {
    return this.show('warning', message, options);
  }

  error(message: string, options?: NotificationOptions): number {
    return this.show('error', message, options);
  }

  operationError(error: unknown, fallback: string): number {
    return this.error(this.safeBusinessDetail(error) ?? fallback);
  }

  dismiss(id: number): void {
    this.cancelTimer(id);
    this.notifications.update((items) => items.filter((item) => item.id !== id));
  }

  clear(): void {
    for (const timer of this.timers.values()) clearTimeout(timer);
    this.timers.clear();
    this.notifications.set([]);
  }

  private show(type: NotificationType, message: string, options: NotificationOptions = {}): number {
    const id = ++this.nextId;
    const duration =
      options.duration === undefined ? NOTIFICATION_DURATION[type] : options.duration;
    const notification: AppNotification = { id, type, message: message.trim(), duration };

    this.notifications.update((items) => {
      const next = [...items, notification];
      const removed = next.slice(0, Math.max(0, next.length - NotificationService.maxVisible));
      for (const item of removed) this.cancelTimer(item.id);
      return next.slice(-NotificationService.maxVisible);
    });

    if (duration !== null && duration > 0) {
      this.timers.set(
        id,
        setTimeout(() => this.dismiss(id), duration),
      );
    }
    return id;
  }

  private cancelTimer(id: number): void {
    const timer = this.timers.get(id);
    if (timer !== undefined) clearTimeout(timer);
    this.timers.delete(id);
  }

  private safeBusinessDetail(error: unknown): string | null {
    const detail = (error as { error?: { detail?: unknown } })?.error?.detail;
    if (typeof detail !== 'string' || !detail.trim()) return null;
    const message = detail.trim();
    const technical =
      /(ORA-\d+|stack\s*trace|java\.|org\.spring|exception|\bSQL\b|\bJWT\b|bcrypt|hash)/i;
    return technical.test(message) ? null : message;
  }
}
