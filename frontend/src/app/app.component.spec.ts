import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { AppComponent } from './app.component';
import { GeocodingResult } from './models/geocoding-result.model';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should render title', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('GeoCoda');
  });

  it('should have an empty query initially', () => {
    expect(component.query).toBe('');
    expect(component.results).toEqual([]);
    expect(component.loading).toBeFalse();
    expect(component.searched).toBeFalse();
  });

  it('should not search when query is empty', () => {
    component.query = '  ';
    component.search();
    httpMock.expectNone('/geocode');
    expect(component.loading).toBeFalse();
  });

  it('should search and display results', fakeAsync(() => {
    const mockResults: GeocodingResult[] = [
      {
        name: 'Test',
        houseNumber: '42',
        street: 'Oak Ave',
        city: 'Portland',
        postcode: '97201',
        latitude: 45.52,
        longitude: -122.68,
        score: 3.5,
        formattedAddress: '42 Oak Ave, Portland 97201',
      },
    ];

    component.query = '42 Oak Ave';
    component.search();

    expect(component.loading).toBeTrue();

    const req = httpMock.expectOne('/geocode?q=42%20Oak%20Ave');
    req.flush(mockResults);
    tick();

    expect(component.loading).toBeFalse();
    expect(component.results.length).toBe(1);
    expect(component.results[0].formattedAddress).toBe('42 Oak Ave, Portland 97201');

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    const resultItems = compiled.querySelectorAll('.result-item');
    expect(resultItems.length).toBe(1);
    expect(resultItems[0].querySelector('.result-address')?.textContent).toContain('42 Oak Ave, Portland 97201');
  }));

  it('should show no results message when search returns empty', fakeAsync(() => {
    component.query = 'nonexistent';
    component.search();

    const req = httpMock.expectOne('/geocode?q=nonexistent');
    req.flush([]);
    tick();

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.no-results')?.textContent).toContain('No results found');
  }));

  it('should show error message on failure', fakeAsync(() => {
    component.query = 'test';
    component.search();

    const req = httpMock.expectOne('/geocode?q=test');
    req.error(new ProgressEvent('error'));
    tick();

    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toContain('error occurred');

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.error-message')).toBeTruthy();
  }));
});
