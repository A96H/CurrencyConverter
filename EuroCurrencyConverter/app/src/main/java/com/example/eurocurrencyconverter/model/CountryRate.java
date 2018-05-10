package com.example.eurocurrencyconverter.model;

/**
 * Created by Asad on 2017-11-15.
 */

/**
 * Class that defines the structure of the list objects, with each currency and its corresponding
 * rate is one single object in the list
 */
public class CountryRate {
    public final String country;
    public final String rate;

    public CountryRate(String country, String rate) {
        this.country = country;
        this.rate = rate;
    }
}
