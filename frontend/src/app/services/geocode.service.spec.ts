import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { GeocodeService } from './geocode.service';
import { GeocodingResult } from '../models/geocoding-result.model';

describe('GeocodeService', () => {
  let service: GeocodeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GeocodeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call GET /geocode with query parameter', () => {
    const mockResults: GeocodingResult[] = [
      {
        name: 'Test Place',
        houseNumber: '123',
        street: 'Main St',
        city: 'Springfield',
        postcode: '12345',
        latitude: 39.78,
        longitude: -89.65,
        score: 4.5,
        formattedAddress: '123 Main St, Springfield 12345',
      },
    ];

    service.geocode('123 Main St').subscribe((results) => {
      expect(results.length).toBe(1);
      expect(results[0].formattedAddress).toBe(
        '123 Main St, Springfield 12345'
      );
    });

    const req = httpMock.expectOne('/geocode?q=123%20Main%20St');
    expect(req.request.method).toBe('GET');
    req.flush(mockResults);
  });

  it('should return empty array when no results', () => {
    service.geocode('nonexistent').subscribe((results) => {
      expect(results.length).toBe(0);
    });

    const req = httpMock.expectOne('/geocode?q=nonexistent');
    req.flush([]);
  });
});
