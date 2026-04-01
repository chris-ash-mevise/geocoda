import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GeocodingResult } from '../models/geocoding-result.model';

@Injectable({
  providedIn: 'root',
})
export class GeocodeService {
  private readonly apiUrl = '/geocode';

  constructor(private http: HttpClient) {}

  geocode(query: string): Observable<GeocodingResult[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<GeocodingResult[]>(this.apiUrl, { params });
  }
}
