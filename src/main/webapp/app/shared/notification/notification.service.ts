import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface NotificationMessage {
  text: string;
  type: 'success' | 'error';
}

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private notificationSubject = new Subject<NotificationMessage | null>();
  notificationState$ = this.notificationSubject.asObservable();

  show(text: string, type: 'success' | 'error' = 'success'): void {
    this.notificationSubject.next({ text, type });
    setTimeout(() => {
      this.notificationSubject.next(null);
    }, 3000);
  }
}
