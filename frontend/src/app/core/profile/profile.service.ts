import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, map, Observable, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProfileUpdateRequest, UserProfile } from './profile.models';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly url = `${environment.apiUrl}/security/profile`;
  private objectUrl: string | null = null;

  readonly avatarUrl = signal<string | null>(null);

  constructor() {
    this.destroyRef.onDestroy(() => this.clearAvatar());
  }

  get(): Observable<UserProfile> {
    return this.http.get<UserProfile>(this.url);
  }

  update(request: ProfileUpdateRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(this.url, request);
  }

  loadPhoto(): Observable<string | null> {
    return this.http
      .get(`${this.url}/photo`, { observe: 'response', responseType: 'blob' })
      .pipe(
        map((response) => (response.body?.size ? this.setAvatar(response.body) : null)),
        catchError(() => {
          this.clearAvatar();
          return of(null);
        }),
      );
  }

  uploadPhoto(file: File): Observable<string | null> {
    const data = new FormData();
    data.append('file', file);
    return this.http.put(`${this.url}/photo`, data, { responseType: 'blob' }).pipe(
      map((blob) => this.setAvatar(blob)),
    );
  }

  deletePhoto(): Observable<void> {
    return this.http.delete<void>(`${this.url}/photo`).pipe(tap(() => this.clearAvatar()));
  }

  clearAvatar(): void {
    if (this.objectUrl) URL.revokeObjectURL(this.objectUrl);
    this.objectUrl = null;
    this.avatarUrl.set(null);
  }

  private setAvatar(blob: Blob): string {
    this.clearAvatar();
    this.objectUrl = URL.createObjectURL(blob);
    this.avatarUrl.set(this.objectUrl);
    return this.objectUrl;
  }
}
