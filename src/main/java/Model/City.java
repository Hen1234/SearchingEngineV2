package Model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The class represents a City in the corpus
 */
public class City {

    String name;
    String currency;
    String country;
    HashMap<String, StringBuilder> locations; //doc->locations
    Long population;


    /**
     * Cosntructor- initialize the fields of the class
     * @param name
     */
    public City(String name) {

        this.name = name;
        currency = "";
        country = "";
        locations = new HashMap<String, StringBuilder>();

    }

    /**
     * Cosntructor- initialize the fields of the class
     * @param name
     * @param currency
     * @param country
     * @param population
     */
    public City(String name, String currency, String country, Long population) {
        this.name = name;
        this.currency = currency;
        this.country = country;
        this.population = population;
        locations = new HashMap<String, StringBuilder>();


    }

    /**
     * Setter
     * @param currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Setter
     * @param country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Setter
     * @param locationsPerDoc
     */
    public void setLocationsPerDoc(HashMap<String, StringBuilder> locationsPerDoc) {
        this.locations = locationsPerDoc;
    }

    /**
     * Setter
     * @param population
     */
    public void setPopulation(long population) {
        this.population = population;
    }

    /**
     * Getter
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter
     * @return currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Getter
     * @return country
     */
    public String getCountry() {
        return country;
    }

    /**
     * Getter
     * @return locations
     */
    public HashMap<String, StringBuilder> getLocations() {
        return locations;
    }

    /**
     * Getter
     * @return population
     */
    public Long getPopulation() {
        return population;
    }

    /**
     * equals method between two cities
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {

        City c1 = (City) o;
        return this.getName().equals(c1.getName());
    }
}
