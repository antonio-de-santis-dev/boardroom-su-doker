import { AfterViewInit, Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import SharedModule from 'app/shared/shared.module';
import { PasswordResetInitService } from './password-reset-init.service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'jhi-password-reset-init',
  standalone: true,
  imports: [SharedModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './password-reset-init.component.html',
  styleUrl: './password-reset-init.component.scss',
})
export default class PasswordResetInitComponent implements AfterViewInit {
  emailInput = viewChild<ElementRef>('email');

  success = signal(false);
  isLoading = signal(false);
  resetRequestForm;

  private readonly passwordResetInitService = inject(PasswordResetInitService);
  private readonly fb = inject(FormBuilder);

  constructor() {
    this.resetRequestForm = this.fb.group({
      email: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email]],
    });
  }

  ngAfterViewInit(): void {
    this.emailInput()?.nativeElement.focus();
  }

  requestReset(): void {
    this.isLoading.set(true);
    this.passwordResetInitService.save(this.resetRequestForm.get(['email'])!.value).subscribe({
      next: () => {
        this.success.set(true);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }
}
