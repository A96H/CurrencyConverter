package com.example.eurocurrencyconverter.model;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Asad on 2017-11-14.
 */

/**
 * Class that parses the currencies and their rates from an XML-document from a given URL.
 */
public class CurrencyParser {

    private boolean fileIsOutdated;
    private Date newTime;

    /**
     * Parses the XML-document, divides the XML-document up in different sections and handles them
     * accordingly.
     *
     * @param inputStream
     * @param items
     * @param oldTime
     * @throws XmlPullParserException
     */
    public void parse(InputStream inputStream, ArrayList<CountryRate> items, Date oldTime) throws XmlPullParserException {
        fileIsOutdated = true;
        newTime = new Date();
        XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
        parser.setInput(inputStream, null);
        int parseEvent = parser.getEventType();

        items.add(new CountryRate("EUR", "1.00"));

        while (parseEvent != XmlPullParser.END_DOCUMENT && fileIsOutdated) {
            switch (parseEvent) {
                case XmlPullParser.START_DOCUMENT:
//                    Log.i("Parsing", "Start document");
                    break;
                case XmlPullParser.START_TAG:
                    String tagName = parser.getName();
//                    Log.i("Parsing", "Tag name: " + tagName);
                    if (tagName.equalsIgnoreCase("CUBE")) {
                        try {
//                            Log.i("Parsing", "Trying to parse " + tagName);
                            parseItem(parser, items, oldTime);
                        } catch (IOException e) {
                            Log.i("Parsing", "Couldn't parse item " + tagName);
                            e.printStackTrace();
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    Log.i("Parsing", "End tag");
            }
            try {
                parseEvent = parser.next();
            } catch (IOException e) {
                Log.i("Parsing", "Couldn't go to parser.next()");
                e.printStackTrace();
            }
        }
    }

    /**
     * Extracts the time written in the XML and compares it with the time extracted from the
     * internal file to see if file is outdated. If it is, start extracting each currency and their
     * rate and insert them into a temporary items-list but if it isn't, then stop the parsing.
     *
     * @param parser
     * @param items
     * @param oldTime
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void parseItem(XmlPullParser parser, ArrayList<CountryRate> items, Date oldTime) throws IOException, XmlPullParserException {
        int parseEvent;
//        Log.i("Parsing", "Name of parsed item is: " + parser.getName());
//        Log.i("Parsing", "Number of attributes: " + parser.getAttributeCount());
        String name, item = "";
        // Continue until end of </item>
        if (parser.getAttributeCount() == 1) {
//            Log.i("Parsing", "Date is " + parser.getAttributeValue(null, "time"));
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                this.newTime = inputFormat.parse(parser.getAttributeValue(null, "time"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (oldTime.getTime() >= newTime.getTime()) {
//                Log.i("Parsing", "File isn't outdated");
                fileIsOutdated = false;
            }

//            Log.i("Parsing", "Date is now " + oldTime.getTime());
        }
        if (parser.getAttributeCount() == 2) {
            parseEvent = parser.next();
            name = parser.getText();
//            Log.i("Parsing", "Attribute prefix: " + parser.getAttributeNamespace(0));
//            Log.i("Parsing", "Country of parsed item is: " + parser.getAttributeValue(null, "currency"));
//            Log.i("Parsing", "Rate of parsed item is: " + parser.getAttributeValue(null, "rate"));
            items.add(new CountryRate(parser.getAttributeValue(null, "currency"), parser.getAttributeValue(null, "rate")));
//            Log.i("Parsing", "Number of attributes: " + parser.getAttributeCount());
//            if (parser.getAttributeCount() > 0) {
//                Log.i("Parsing", "Testing: " + parser.getAttributeName(0));
//            }
        }
    }

    public Date getNewTime() {
        return newTime;
    }
}
