import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/login/login.service';
import SharedModule from 'app/shared/shared.module';
import { CommonModule } from '@angular/common';
import { faUser, faHistory, faCog, faSignOutAlt } from '@fortawesome/free-solid-svg-icons';

@Component({
  standalone: true,
  selector: 'jhi-profilo-utente',
  templateUrl: './profilo-utente.component.html',
  styleUrl: './profilo-utente.component.scss',
  imports: [SharedModule, RouterModule, CommonModule],
})
export class ProfiloUtenteComponent {
  isLoading = false;

  faUser = faUser;
  faHistory = faHistory;
  faCog = faCog;
  faSignOut = faSignOutAlt;

  private accountService = inject(AccountService);
  private loginService = inject(LoginService);
  private router = inject(Router);

  account = this.accountService.trackCurrentAccount();

  logout(): void {
    this.isLoading = true;
    this.loginService.logout();

    setTimeout(() => {
      this.router.navigate(['/']).then(() => {
        this.isLoading = false;
      });
    }, 800);
  }
}
