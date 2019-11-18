package edu.cmu.cs.cs214.hw5.core;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A wrapper of geographical address. This class is immutable and for self-use. It is mainly for the purpose
 * of reducing number of OpenStreetMap queries by detecting duplicate addresses.
 */
class GeoAddress {
    
    /**
     * Country name of address.
     */
    private final String country;
    
    /**
     * State name of address.
     */
    private final String state;

    /**
     * City name of address.
     */
    private final String city;

    /**
     * County name of address.
     */
    private final String county;

    /**
     * Street name of address.
     */
    private final String street;

    /**
     * Hashcode of this object.
     */
    private final int hash;

    /**
     * Initialize a geoAddress object.
     * 
     * @param country country name of address.
     * @param state state name of address.
     * @param city city name of address.
     * @param county county name of address.
     * @param street street name of address.
     */
    GeoAddress(String country, String state, String city, String county, String street) {
        this.country = formatAddressValue(country);
        this.state = formatAddressValue(state);
        this.city = formatAddressValue(city);
        this.county = formatAddressValue(county);
        this.street = formatAddressValue(street);
        int hash = this.country == null ? 0 : this.country.hashCode();
        hash = hash * 31 + (this.state == null ? 0 : this.state.hashCode());
        hash = hash * 31 + (this.city == null ? 0 : this.city.hashCode());
        hash = hash * 31 + (this.county == null ? 0 : this.county.hashCode());
        hash = hash * 31 + (this.street == null ? 0 :this.street.hashCode());
        this.hash = hash;
        if (isEmptyAddress())
            throw new IllegalArgumentException("At Least One Attribute Should Be Non-Empty");
    }

    /**
     * Check if all fields of this object are empty.
     * 
     * @return true if all fields are empty and false otherwise.
     */
    private boolean isEmptyAddress() {
        return country == null && state == null && city == null && county == null && street == null;
    }

    /**
     * Preprocess and format the given string by removing punctuations and unnecessary whitespaces.
     * 
     * @param s string to be processed.
     * @return the formatted value.
     */
    static String formatAddressValue(String s) {
        if (s == null)
            return null;
        s = s.strip();
        if (s.equals(""))
            return null;
        return s;
    }

    /**
     * Return hashcode of this object.
     * 
     * @return hashcode of this object.
     */
    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * Check if this object is equals to the given object.
     * 
     * @param o given object to compare.
     * @return true if the two objects are equal and false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GeoAddress)) {
            return false;
        }
        if (this == o)
            return true;
        GeoAddress addr = (GeoAddress) o;
        return Objects.equals(this.country, addr.country) && Objects.equals(this.state, addr.state)
                       && Objects.equals(this.city, addr.city) && Objects.equals(this.county, addr.county)
                       && Objects.equals(this.street, addr.street);
    }

    /**
     * Return a string representing this object.
     * 
     * @return a string representing this object.
     */
    @Override
    public String toString() {
        return String.join(
                ", ",
                Arrays.asList(country, state, city, county, street).stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Return the country name of this address.
     * 
     * @return country name of this address.
     */
    String getCountry() {
        return country;
    }

    /**
     * Return the state name of this address.
     * 
     * @return state name of this address.
     */
    String getState() {
        return state;
    }

    /**
     * Return the city name of this address.
     * 
     * @return city name of this address.
     */
    String getCity() {
        return city;
    }

    /**
     * Return the county name of this address.
     * 
     * @return county name of this address.
     */
    String getCounty() {
        return county;
    }

    /**
     * Return the street name of this address.
     * 
     * @return street name of this address.
     */
    String getStreet() {
        return street;
    }
}