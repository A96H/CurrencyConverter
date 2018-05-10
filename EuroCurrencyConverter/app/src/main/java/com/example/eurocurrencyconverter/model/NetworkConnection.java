package com.example.eurocurrencyconverter.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.eurocurrencyconverter.controller.MainActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Asad on 2017-11-21.
 */

public class NetworkConnection {

    private MainActivity main;
    private CurrencyModel model;

    public NetworkConnection(MainActivity main, String link, CurrencyModel model) {
        this.main = main;
        this.model = model;
        new XMLParser().execute(link);
    }

    public class XMLParser extends AsyncTask<String, Void, ArrayList<CountryRate>> {

        /**
         * Connects to the XML-page online and calls the CurrencyParser-class.
         *
         * @param strings the URL of the XML
         * @return a list containing the new currencies from the XML-page
         */
        @Override
        protected ArrayList<CountryRate> doInBackground(String... strings) {

            HttpURLConnection http = null;
            InputStream xmlStream = null;
            ArrayList<CountryRate> countryList = new ArrayList<>();
            try {
                URL url = new URL(strings[0]);
                http = (HttpURLConnection) url.openConnection();
//                http = connectToNetwork(strings[0]);
                CurrencyParser parser = new CurrencyParser();
                if (http != null) {
                    parser.parse(http.getInputStream(), countryList, main.getOldTime());
                    main.setNewTime(parser.getNewTime());

                }
//                Log.i("Parser", "Amount of items in itemList is: " + countryList.size() + " and date is " + oldTime.getTime());
//                Log.i("Times: ", "New time = " + newTime + " oldTime = " + oldTime);
                //adapter.notifyDataSetChanged();
            } catch (MalformedURLException e) {
                Log.i("ParserError", "Malformed URL Exception");
                e.printStackTrace();
            } catch (IOException e) {
                Log.i("ParserError", "IOException");
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                Log.i("ParserError", "XMLPullParserException");
                e.printStackTrace();
            } finally {
                if (xmlStream != null)
                    try {
                        xmlStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (http != null)
                    http.disconnect();
            }

            ArrayList<CountryRate> copyList;
            copyList = countryList;
            return copyList;
        }

        /**
         * Executed after doInBackground finishes. If the time of the newly parsed XML-page is
         * newer than the time of the file in internal storage, then update the file with the new
         * rates from the parsed XML-page.
         *
         * @param countryRates the list received from doInBackground containing the latest rates
         */
        @Override
        protected void onPostExecute(ArrayList<CountryRate> countryRates) {
            model.setLastUpdate();
            Log.i("Parser", "In onPostExecute()");
            //Update the String arrays with data from updated currencyList
            if (main.getNewTime().getTime() > main.getOldTime().getTime()) {
//                Log.i("File", "Writing to file");
                model.setCurrencyList(countryRates);
                model.updateCurrencies();
                main.setOldTime(main.getNewTime());
                model.setOldTime(main.getOldTime());
            }
        }
    }
}
