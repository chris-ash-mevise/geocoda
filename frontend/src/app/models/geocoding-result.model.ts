export interface GeocodingResult {
  name: string;
  houseNumber: string;
  street: string;
  city: string;
  postcode: string;
  latitude: number;
  longitude: number;
  score: number;
  formattedAddress: string;
}
