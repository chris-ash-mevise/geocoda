import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { GeocodeService } from './services/geocode.service';
import { GeocodingResult } from './models/geocoding-result.model';

@Component({
  selector: 'app-root',
  imports: [FormsModule, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  query = '';
  results: GeocodingResult[] = [];
  loading = false;
  searched = false;
  errorMessage = '';

  constructor(private geocodeService: GeocodeService) {}

  search(): void {
    const trimmed = this.query.trim();
    if (!trimmed) {
      return;
    }

    this.loading = true;
    this.searched = true;
    this.errorMessage = '';
    this.results = [];

    this.geocodeService.geocode(trimmed).subscribe({
      next: (results) => {
        this.results = results;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'An error occurred while geocoding. Please try again.';
        this.loading = false;
      },
    });
  }
}
