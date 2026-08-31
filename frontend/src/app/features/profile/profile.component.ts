import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { NotificationService } from '../../core/notifications/notification.service';
import { UserProfile } from '../../core/profile/profile.models';
import { ProfileService } from '../../core/profile/profile.service';

const MAX_PHOTO_BYTES = 2 * 1024 * 1024;
const PHOTO_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfileComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly profiles = inject(ProfileService);
  private readonly notification = inject(NotificationService);

  readonly profile = signal<UserProfile | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly photoSaving = signal(false);
  readonly selectedPhoto = signal<File | null>(null);
  readonly previewUrl = signal<string | null>(null);
  readonly photoError = signal('');
  readonly avatarUrl = this.profiles.avatarUrl;
  readonly form = this.fb.nonNullable.group({
    correoElectronico: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    telefonoMovil: ['', [Validators.maxLength(30), Validators.pattern(/^[0-9+()\-\s]+$/)]],
  });

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    this.releasePreview();
  }

  load(): void {
    this.loading.set(true);
    this.profiles
      .get()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (profile) => {
          this.profile.set(profile);
          this.form.reset({
            correoElectronico: profile.correoElectronico,
            telefonoMovil: profile.telefonoMovil ?? '',
          });
          if (profile.fotografiaDisponible && !this.avatarUrl()) {
            this.profiles.loadPhoto().subscribe();
          }
        },
        error: (error) =>
          this.notification.operationError(error, 'No fue posible cargar el perfil.'),
      });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.profiles
      .update(this.form.getRawValue())
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (profile) => {
          this.profile.set(profile);
          this.notification.success('Perfil actualizado correctamente.');
        },
        error: (error) =>
          this.notification.operationError(error, 'No fue posible actualizar el perfil.'),
      });
  }

  selectPhoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.releasePreview();
    this.selectedPhoto.set(null);
    this.photoError.set('');
    if (!file) return;
    if (!PHOTO_TYPES.has(file.type)) {
      this.photoError.set('Seleccione una fotografía JPEG, PNG o WebP.');
      input.value = '';
      return;
    }
    if (!file.size) {
      this.photoError.set('La fotografía está vacía.');
      input.value = '';
      return;
    }
    if (file.size > MAX_PHOTO_BYTES) {
      this.photoError.set('La fotografía no puede exceder 2 MB.');
      input.value = '';
      return;
    }
    this.selectedPhoto.set(file);
    this.previewUrl.set(URL.createObjectURL(file));
  }

  savePhoto(): void {
    const file = this.selectedPhoto();
    if (!file) return;
    this.photoSaving.set(true);
    this.profiles
      .uploadPhoto(file)
      .pipe(finalize(() => this.photoSaving.set(false)))
      .subscribe({
        next: () => {
          this.profile.update((profile) =>
            profile ? { ...profile, fotografiaDisponible: true } : profile,
          );
          this.releasePreview();
          this.selectedPhoto.set(null);
          this.notification.success('Fotografía actualizada correctamente.');
        },
        error: (error) =>
          this.notification.operationError(error, 'No fue posible actualizar la fotografía.'),
      });
  }

  deletePhoto(): void {
    this.photoSaving.set(true);
    this.profiles
      .deletePhoto()
      .pipe(finalize(() => this.photoSaving.set(false)))
      .subscribe({
        next: () => {
          this.profile.update((profile) =>
            profile ? { ...profile, fotografiaDisponible: false } : profile,
          );
          this.releasePreview();
          this.selectedPhoto.set(null);
          this.notification.success('Fotografía eliminada correctamente.');
        },
        error: (error) =>
          this.notification.operationError(error, 'No fue posible eliminar la fotografía.'),
      });
  }

  initials(): string {
    const profile = this.profile();
    const names = [profile?.nombre, profile?.apellido].filter(
      (value): value is string => Boolean(value),
    );
    return names.length
      ? names.map((value) => value[0]).join('').slice(0, 2).toUpperCase()
      : profile?.idUsuario.slice(0, 2).toUpperCase() || 'AE';
  }

  private releasePreview(): void {
    const url = this.previewUrl();
    if (url) URL.revokeObjectURL(url);
    this.previewUrl.set(null);
  }
}
