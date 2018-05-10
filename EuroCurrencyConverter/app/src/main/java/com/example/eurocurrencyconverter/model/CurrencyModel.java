package com.example.eurocurrencyconverter.model;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.eurocurrencyconverter.R;
import com.example.eurocurrencyconverter.controller.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Asad on 2017-11-17.
 */

/**
 * Model of the currency converter to keep the state of the application.
 */
public class CurrencyModel {
    private static final String currencyRateURL = "http://maceo.sth.kth.se/Home/eurofxref"; // URL only for testing
    //private static final String currencyRateURL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml"; //Official URL
    private final String filename = "currencies";
    public static ArrayList<CountryRate> currencyList;
    private String[] currency1, currency2;
    private Date oldTime, newTime;
    private static CurrencyModel model;
    private String countryFrom, countryTo;
    private BigDecimal inputValue, result;
    private Date lastUpdate;

    /**
     * Constructor of the model. Temporarily inputs a value in the currency-arrays for the adapters.
     */
    private CurrencyModel() {

        currency1 = new String[32];
        currency1[0] = "TEMP";
        currency2 = new String[32];
        currency2[0] = "TEMP";

        oldTime = new Date();
        newTime = new Date();
        lastUpdate = new Date();
        lastUpdate.setTime(250);

        currencyList = new ArrayList<>();
    }

    /**
     * To make the model a singleton in order to keep the state of the application.
     *
     * @return the instance of the model
     */
    public static CurrencyModel getInstance() {
        if (model == null) {
            model = new CurrencyModel();
        }
        return model;
    }

    /**
     * Get the URL of the XML-page where currency rates are updated.
     *
     * @return the URL of the XML-page
     */
    public String getCurrencyRateURL(){
        return currencyRateURL;
    }

    /**
     * Replaces the currencyList with newList.
     *
     * @param newList the list that will replace the current currencyList
     */
    public void setCurrencyList(ArrayList<CountryRate> newList) {
        this.currencyList = newList;
    }

    /**
     * Sets oldTime to a specified value.
     *
     * @param oldTime the time that will replace current oldTime
     */
    public void setOldTime(Date oldTime) {
        this.oldTime = oldTime;
    }

    /**
     * Updates the time since the application last connected to the internet to get the latest
     * currency.
     */
    public void setLastUpdate(){
        this.lastUpdate = Calendar.getInstance().getTime();
    }

    /**
     * Returns the time when the application last connected to the internet to get the latest
     * currency by parsing an XML-page.
     *
     * @return the time of last XML-parsing
     */
    public Date getLastUpdate(){
        return this.lastUpdate;
    }

    /**
     * Update the currency String-arrays for the adapters in MainActivity.
     */
    public void updateCurrencies() {

        int counter = 0;

        for (CountryRate cr : currencyList) {
            currency1[counter] = cr.country;
            currency2[counter] = cr.country;
            counter++;
        }
    }

    /**
     * Returns the reference to the first String-array.
     *
     * @return the reference to currency1
     */
    public String[] getCurrency1() {

        int counter = 0;

        for (CountryRate cr : currencyList) {
            currency1[counter] = cr.country;
        }
        return currency1;
    }

    /**
     * Returns the reference to the second String-array.
     *
     * @return the reference to currency2
     */
    public String[] getCurrency2() {

        int counter = 0;

        for (CountryRate cr : currencyList) {
            currency2[counter] = cr.country;
        }
        return currency2;
    }

    /**
     * Sets the currency chosen by the user which the conversion is to be converted from.
     *
     * @param countryFrom chosen currency which the conversion is to be converted from
     */
    public void setCountryFrom(String countryFrom) {
        this.countryFrom = countryFrom;
    }

    /**
     * Returns the currency chosen by the user which the conversion is to be converted from.
     *
     * @return currency chosen which the conversion is to be converted from
     */
    public String getCountryFrom() {
        return countryFrom;
    }

    /**
     * Sets the currency chosen by the user which the conversion is to be converted to.
     *
     * @param countryTo chosen currency which the conversion is to be converted to
     */
    public void setCountryTo(String countryTo) {
        this.countryTo = countryTo;
    }

    /**
     * Returns the currency chosen by the user which the conversion is to be converted to.
     *
     * @return chosen currency which the conversion is to be converted to
     */
    public String getCountryTo() {
        return countryTo;
    }

    /**
     * Sets the value written by the user that is to be converted.
     *
     * @param inputValue value written by the user
     */
    public void setInputValue(BigDecimal inputValue) {
        if(inputValue == null){
            this.result = new BigDecimal(0.00);
        }
        this.inputValue = inputValue;
    }

    /**
     * Returns the value that is to be converted.
     *
     * @return value written by the user
     */
    public BigDecimal getInputValue() {
        if(inputValue == null){
            return new BigDecimal(0.00);
        }
        return inputValue;
    }

    /**
     * Calculates the value using the inputValue written by the user and the currencies chosen.
     */
    public void calculateCurrency() {

        Log.i("Calculator", "Amount to convert " + inputValue);

        if (inputValue == null) {
            return;
        }

        Log.i("Calculator", "Currency1 and Currency2 size is " + currency1.length + " and " + currency2.length);
        Log.i("Calculator", "CountryFrom and CountryTo is " + countryFrom + " and " + countryTo);
        Log.i("Calculator", "Amount to convert " + inputValue);

        int counterFrom = 0;
        for (String country : currency1) {
            if (country.equals(countryFrom)) {
                break;
            }
            counterFrom++;
        }

        int counterTo = 0;
        for (String country : currency2) {
            if (country.equals(countryTo)) {
                break;
            }
            counterTo++;
        }

        BigDecimal rateFrom = new BigDecimal(currencyList.get(counterFrom).rate);
        BigDecimal rateTo = new BigDecimal(currencyList.get(counterTo).rate);

        Log.i("Calculator", "Counting with " + rateFrom + " and " + rateTo);

        result = inputValue.divide(rateFrom, 20, BigDecimal.ROUND_HALF_UP);
        result = result.multiply(rateTo).setScale(2, BigDecimal.ROUND_HALF_UP);

        Log.i("Calculator", "The answer is: " + result);
    }

    /**
     * Returns the result given by the calculation in calculateCurrency().
     *
     * @return the result of the calculation
     */
    public BigDecimal getResult() {
        return result;
    }

    /**
     * Writes a temporary file only when the application is installed and there is no
     * "currencies"-file present. Writes only two lines in the temporary file. First line is an
     * arbitrary number of 100 which the application later reads as the time since the document
     * was last updated (oldTime) and notices that the XML-page has a newer time (newTime) and
     * updates the file with the currencies and their rates from the XML-page. The second line is
     * the currency EURO and its rate which is for the adapter in MainActivity.
     *
     * @param context context of MainActivity
     */
    public void firstTempFile(Context context) {
        Log.i("File Writer", "Writing file first time " + context.getFilesDir());
        File file = new File(context.getFilesDir().getPath() + "/" + filename);
        if (file.exists())
            Log.i("File Writer", "File's in storage: " + context.getFilesDir().getPath());
        else {

            PrintWriter writer = null;
            try {
                OutputStream os = context.openFileOutput(filename, Context.MODE_PRIVATE);
                writer = new PrintWriter(os);
                writer.println("100");
                currencyList.add(new CountryRate("EUR", "1.00"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                if (writer != null)
                    writer.close();
            }
        }
    }

    /**
     * Reads the first line of the file "currencies" in internal storage which is the time since
     * the currencies in the document were last updated and returns it.
     *
     * @param context context of MainActivity
     * @return the time which specifies when the currencies in the document were last updated
     */
    public Date getOldTimeFromFile(Context context) {
        Log.i("File Writer", "Writing file" + context.getFilesDir());
        BufferedReader reader = null;
//        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            InputStream is = context.openFileInput(filename);
            reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            Log.i("File", "First line is: " + line);

            oldTime.setTime(Long.valueOf(line).longValue());

            Log.i("File", "oldTime after reading from file " + oldTime);
        } catch (IOException ioe) {
            Log.i("File", "Error reading from file");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                Log.i("File", "Error closing buffered reader");
                ioe.printStackTrace();
            }
        }
        return oldTime;
    }

    /**
     * Updates the values in the file with the new time (oldTime) in milliseconds counting from
     * epoch and the rates of each currency by iterating through the ArrayList currencyList.
     *
     * @param context context of MainActivity
     */
    public void writeToFile(Context context) {
        Log.i("File Writer", "Writing file" + context.getFilesDir());
        PrintWriter writer = null;
        try {
            OutputStream os = context.openFileOutput(filename, Context.MODE_PRIVATE);
            writer = new PrintWriter(os);
            writer.println(oldTime.getTime());

            for (CountryRate cr : currencyList) {
                writer.println(cr.country);
                writer.println(cr.rate);
            }
        } catch (IOException ioe) {
            Log.i("File", "Error writing to file");
            ioe.printStackTrace();
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    /**
     * Reads the values of the existing file in internal storage.
     *
     * @param context context of MainActivity
     */
    public void readFromFile(Context context) {
        BufferedReader reader = null;
        try {
            currencyList.clear();
            InputStream is = context.openFileInput(filename);
            reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            line = reader.readLine();
            int counter = 0;
            while (line != null) {
                String country = line;
                line = reader.readLine();
                currencyList.add(new CountryRate(country, line));

                line = reader.readLine();
            }
            updateCurrencies();
        } catch (IOException ioe) {
            Log.i("File", "Error reading from file");
            ioe.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                Log.i("File", "Error closing buffered reader");
            }
        }
    }
}
