import { ChangeDetectionStrategy, Component } from '@angular/core';
@Component({
  selector: 'app-footer',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<footer class="app-footer">
    <span>AEGIS-ERP</span><span>Gestión segura y centralizada</span>
  </footer>`,
})
export class FooterComponent {}
