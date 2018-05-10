package com.example.eurocurrencyconverter.controller;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eurocurrencyconverter.R;
import com.example.eurocurrencyconverter.model.CountryRate;
import com.example.eurocurrencyconverter.model.CurrencyModel;
import com.example.eurocurrencyconverter.model.CurrencyParser;
import com.example.eurocurrencyconverter.model.NetworkConnection;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * MainActivity of the application EuroCurrencyConverter.
 */
public class MainActivity extends AppCompatActivity {

    private Spinner spinner1, spinner2;
    private ArrayAdapter<String> adapter1, adapter2;
    private Date oldTime, newTime;
    private CurrencyModel model;
    private EditText inputValue;
    private String countryFrom, countryTo;
    private TextView resultView, lastUpdateView;
    private boolean isConnected;
    private boolean isWifiConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputValue = (EditText) findViewById(R.id.inputValue);
        resultView = (TextView) findViewById(R.id.conversionResult);
        lastUpdateView = (TextView) findViewById(R.id.lastUpdateView);

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner2 = (Spinner) findViewById(R.id.spinner2);

        model = CurrencyModel.getInstance();

        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, model.getCurrency2());
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, model.getCurrency2());

        adapter1.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        spinner1.setAdapter(adapter1);
        adapter2.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        spinner2.setAdapter(adapter2);

        spinner1.setOnItemSelectedListener(new Spinner1Listener());
        spinner2.setOnItemSelectedListener(new Spinner2Listener());

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(isConnected) {
            isWifiConnected = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }

        model.firstTempFile(this);
        oldTime = model.getOldTimeFromFile(this);

        model.readFromFile(this);
        model.writeToFile(this);
        adapter1.notifyDataSetChanged();

        updateLastUpdateText();
        inputValue.addTextChangedListener(inputWatcher);
    }

    @Override
    protected void onStart() {
        Log.i("Starting", "In onStart()");
        Log.i("Starting", "Current time: " + Calendar.getInstance().getTimeInMillis()
                + " and  lastUpdate: " + model.getLastUpdate().getTime());
        super.onStart();

        if (isConnected) {
            //If wifi is connected and 10 minutes has passed since last time currencies were parsed
            //from the XML-page online
            if(isWifiConnected && Calendar.getInstance().getTimeInMillis()
                    >= model.getLastUpdate().getTime() + 600000) {
                updateCurrency();
            //If data usage is on and one hour has passed since last time currencies were parsed
            //from the XML-page online
            }else if(!isWifiConnected && Calendar.getInstance().getTimeInMillis()
                    >= model.getLastUpdate().getTime() + 3600000){
                updateCurrency();
            }
        }else{
            showToast("No internet connection, rates might not be up to date!");
        }
    }

    private void updateLastUpdateText(){
        lastUpdateView.setText("Currencies last updated: " + oldTime);
    }

    /**
     * Watches if input text is changed by the user.
     */
    private final TextWatcher inputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        /**
         * If user inputs a value, result is calculated and shown to the user
         *
         * @param editable the text
         */
        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.length() == 0){
                model.setInputValue(null);
                updateResultText();
            }
            if (editable.length() > 0) {
                BigDecimal inputBD = new BigDecimal(inputValue.getText().toString());
                model.setInputValue(inputBD);
                model.calculateCurrency();
                updateResultText();
            }
        }
    };

    /**
     * Begins the parsing by starting a background thread.
     */
    private void updateCurrency() {
        NetworkConnection nw = new NetworkConnection(this, model.getCurrencyRateURL(), model);
        updateLastUpdateText();
        writeToFile();

        adapter1.notifyDataSetChanged();
        adapter2.notifyDataSetChanged();
    }

    /**
     * Updates the result shown to the user.
     */
    private void updateResultText() {
        resultView.setText(model.getInputValue() + " " + model.getCountryFrom() + " = " + model.getResult() + " " + model.getCountryTo());
    }

    /**
     * Calls the method in the model which writes the updated rates to a file and stores it.
     */
    private void writeToFile() {
        model.writeToFile(this);
    }

    /**
     * Shows a temporary message to the user.
     *
     * @param msg the message shown
     */
    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * Listens if items in the spinner is changed by the user.
     */
    private class Spinner1Listener implements AdapterView.OnItemSelectedListener {

        /**
         * Is triggered when an item is selected from the countryFrom-spinner, calculates the
         * result using the newly chosen currency and updates the view for the user by calling
         * updateResultText().
         *
         * @param adapterView
         * @param view
         * @param i
         * @param l
         */
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            countryFrom = (String) spinner1.getSelectedItem();
            model.setCountryFrom(countryFrom);
            model.calculateCurrency();
            updateResultText();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    /**
     * Listens if items in the spinner is changed by the user.
     */
    private class Spinner2Listener implements AdapterView.OnItemSelectedListener {

        /**
         * Is triggered when an item is selected from the countryTo-spinner, calculates the result
         * using the newly chosen currency and updates the view for the user by calling
         * updateResultText().
         *
         * @param adapterView
         * @param view
         * @param i
         * @param l
         */
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            countryTo = (String) spinner2.getSelectedItem();
            model.setCountryTo(countryTo);
            model.calculateCurrency();
            updateResultText();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public void setNewTime(Date newTime){
        this.newTime = newTime;
    }

    public Date getNewTime(){
        return this.newTime;
    }

    public void setOldTime(Date oldTime){
        this.oldTime = oldTime;
    }

    public Date getOldTime(){
        return this.oldTime;
    }
}
