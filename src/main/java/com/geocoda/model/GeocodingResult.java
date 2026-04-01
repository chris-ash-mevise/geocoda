package com.geocoda.model;

/**
 * Represents a single geocoding result returned by the API.
 */
public class GeocodingResult {

    private String name;
    private String houseNumber;
    private String street;
    private String city;
    private String postcode;
    private double latitude;
    private double longitude;
    private float score;

    public GeocodingResult() {
    }

    public GeocodingResult(String name, String houseNumber, String street,
                           String city, String postcode,
                           double latitude, double longitude, float score) {
        this.name = name;
        this.houseNumber = houseNumber;
        this.street = street;
        this.city = city;
        this.postcode = postcode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.score = score;
    }

    /**
     * Returns a formatted address string combining available address components.
     */
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (houseNumber != null && !houseNumber.isEmpty()) {
            sb.append(houseNumber).append(" ");
        }
        if (street != null && !street.isEmpty()) {
            sb.append(street);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(city);
        }
        if (postcode != null && !postcode.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(postcode);
        }
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
