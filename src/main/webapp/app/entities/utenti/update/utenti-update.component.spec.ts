import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, from, of } from 'rxjs';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { UtentiService } from '../service/utenti.service';
import { IUtenti } from '../utenti.model';
import { UtentiFormService } from './utenti-form.service';

import { UtentiUpdateComponent } from './utenti-update.component';

describe('Utenti Management Update Component', () => {
  let comp: UtentiUpdateComponent;
  let fixture: ComponentFixture<UtentiUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let utentiFormService: UtentiFormService;
  let utentiService: UtentiService;
  let userService: UserService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [UtentiUpdateComponent],
      providers: [
        provideHttpClient(),
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(UtentiUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(UtentiUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    utentiFormService = TestBed.inject(UtentiFormService);
    utentiService = TestBed.inject(UtentiService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const utenti: IUtenti = { id: '58e889ee-0365-4883-a3bf-e668883718c9' };
      const user: IUser = { id: 3944 };
      utenti.user = user;

      const userCollection: IUser[] = [{ id: 3944 }];
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [user];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ utenti });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(expect.objectContaining),
      );
      expect(comp.usersSharedCollection).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const utenti: IUtenti = { id: '58e889ee-0365-4883-a3bf-e668883718c9' };
      const user: IUser = { id: 3944 };
      utenti.user = user;

      activatedRoute.data = of({ utenti });
      comp.ngOnInit();

      expect(comp.usersSharedCollection).toContainEqual(user);
      expect(comp.utenti).toEqual(utenti);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IUtenti>>();
      const utenti = { id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' };
      jest.spyOn(utentiFormService, 'getUtenti').mockReturnValue(utenti);
      jest.spyOn(utentiService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ utenti });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: utenti }));
      saveSubject.complete();

      // THEN
      expect(utentiFormService.getUtenti).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(utentiService.update).toHaveBeenCalledWith(expect.objectContaining(utenti));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IUtenti>>();
      const utenti = { id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' };
      jest.spyOn(utentiFormService, 'getUtenti').mockReturnValue({ id: null });
      jest.spyOn(utentiService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ utenti: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: utenti }));
      saveSubject.complete();

      // THEN
      expect(utentiFormService.getUtenti).toHaveBeenCalled();
      expect(utentiService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IUtenti>>();
      const utenti = { id: 'd017df52-00ef-4255-a48a-ff7f9b902a19' };
      jest.spyOn(utentiService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ utenti });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(utentiService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('should forward to userService', () => {
        const entity = { id: 3944 };
        const entity2 = { id: 6275 };
        jest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
