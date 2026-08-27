export type ConfirmationType = 'danger';

export interface ConfirmationOptions {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: ConfirmationType;
  warningText?: string;
}

export interface ConfirmationRequest {
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
  type: ConfirmationType;
  warningText: string | null;
}
