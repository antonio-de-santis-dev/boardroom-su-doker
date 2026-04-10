import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RisultatiSalaComponent } from './risultati-sala.component';

describe('RisultatiSalaComponent', () => {
  let component: RisultatiSalaComponent;
  let fixture: ComponentFixture<RisultatiSalaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RisultatiSalaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RisultatiSalaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
