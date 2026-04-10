import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from './notification.service';

@Component({
  selector: 'jhi-notification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="notificationService.notificationState$ | async as note" class="notification-container" [ngClass]="note.type">
      <div class="notification-content">
        <span class="icon">{{ note.type === 'success' ? '✓' : '✕' }}</span>
        <p>{{ note.text }}</p>
      </div>
    </div>
  `,
  styleUrls: ['./notification.component.scss'],
})
export class NotificationComponent {
  public notificationService = inject(NotificationService);
}
