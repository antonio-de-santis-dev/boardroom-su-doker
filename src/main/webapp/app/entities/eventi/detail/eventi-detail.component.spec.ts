import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { EventiDetailComponent } from './eventi-detail.component';

describe('Eventi Management Detail Component', () => {
  let comp: EventiDetailComponent;
  let fixture: ComponentFixture<EventiDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventiDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./eventi-detail.component').then(m => m.EventiDetailComponent),
              resolve: { eventi: () => of({ id: 'db33bd35-08c1-44ea-80d0-f2cc6e6bc4e6' }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(EventiDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EventiDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load eventi on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', EventiDetailComponent);

      // THEN
      expect(instance.eventi()).toEqual(expect.objectContaining({ id: 'db33bd35-08c1-44ea-80d0-f2cc6e6bc4e6' }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
