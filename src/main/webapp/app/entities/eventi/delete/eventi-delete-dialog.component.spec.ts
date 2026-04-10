jest.mock('@ng-bootstrap/ng-bootstrap');

import { ComponentFixture, TestBed, fakeAsync, inject, tick } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { EventiService } from '../service/eventi.service';

import { EventiDeleteDialogComponent } from './eventi-delete-dialog.component';

describe('Eventi Management Delete Component', () => {
  let comp: EventiDeleteDialogComponent;
  let fixture: ComponentFixture<EventiDeleteDialogComponent>;
  let service: EventiService;
  let mockActiveModal: NgbActiveModal;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [EventiDeleteDialogComponent],
      providers: [provideHttpClient(), NgbActiveModal],
    })
      .overrideTemplate(EventiDeleteDialogComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(EventiDeleteDialogComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(EventiService);
    mockActiveModal = TestBed.inject(NgbActiveModal);
  });

  describe('confirmDelete', () => {
    it('should call delete service on confirmDelete', inject(
      [],
      fakeAsync(() => {
        jest.spyOn(service, 'delete').mockReturnValue(of(new HttpResponse({ body: {} })));

        comp.confirmDelete('9fec3727-3421-4967-b213-ba36557ca194');
        tick();

        expect(service.delete).toHaveBeenCalledWith('9fec3727-3421-4967-b213-ba36557ca194');
        expect(mockActiveModal.close).toHaveBeenCalledWith('deleted');
      }),
    ));

    it('should not call delete service on clear', () => {
      jest.spyOn(service, 'delete');

      comp.cancel();

      expect(service.delete).not.toHaveBeenCalled();
      expect(mockActiveModal.close).not.toHaveBeenCalled();
      expect(mockActiveModal.dismiss).toHaveBeenCalled();
    });
  });
});
