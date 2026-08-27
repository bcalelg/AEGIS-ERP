import { DOCUMENT } from '@angular/common';
import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { ConfirmationOptions, ConfirmationRequest } from './confirmation.models';

@Injectable({ providedIn: 'root' })
export class ConfirmationService {
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
  private resolver: ((confirmed: boolean) => void) | null = null;
  private returnFocus: HTMLElement | null = null;

  readonly request = signal<ConfirmationRequest | null>(null);
  readonly processing = signal(false);

  constructor() {
    this.destroyRef.onDestroy(() => this.settle(false, false));
  }

  confirm(options: ConfirmationOptions): Promise<boolean> {
    if (this.processing()) return Promise.resolve(false);
    if (this.request()) this.settle(false, false);

    this.returnFocus =
      this.document.activeElement instanceof HTMLElement ? this.document.activeElement : null;
    this.request.set({
      title: options.title,
      message: options.message,
      confirmText: options.confirmText ?? 'Eliminar',
      cancelText: options.cancelText ?? 'Cancelar',
      type: options.type ?? 'danger',
      warningText: options.warningText ?? null,
    });

    return new Promise<boolean>((resolve) => {
      this.resolver = resolve;
    });
  }

  accept(): void {
    if (!this.request() || this.processing()) return;
    this.processing.set(true);
    const resolve = this.resolver;
    this.resolver = null;
    resolve?.(true);
  }

  cancel(): void {
    if (this.processing()) return;
    this.settle(false);
  }

  complete(): void {
    if (!this.request()) return;
    this.close(true);
  }

  private settle(confirmed: boolean, restoreFocus = true): void {
    const resolve = this.resolver;
    this.resolver = null;
    resolve?.(confirmed);
    this.close(restoreFocus);
  }

  private close(restoreFocus: boolean): void {
    const trigger = this.returnFocus;
    this.returnFocus = null;
    this.processing.set(false);
    this.request.set(null);

    if (restoreFocus && trigger?.isConnected) {
      setTimeout(() => trigger.focus());
    }
  }
}
