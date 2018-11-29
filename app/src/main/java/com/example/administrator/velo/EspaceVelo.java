package com.example.administrator.velo;

/**
 * Created by Administrator on 29/11/2018.
 */

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class EspaceVelo extends AppCompatActivity{

    private String TAG = EspaceVelo.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;


    private static String url = "https://api.myjson.com/bins/100mw2";

    ArrayList<HashMap<String, String>> stationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);

        stationList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.l_ist);

        new Getlocation().execute();
    }


    private class Getlocation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
           super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(EspaceVelo.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray location = jsonObj.getJSONArray("location");

                    // looping through All location
                    for (int i = 0; i < location.length(); i++) {
                        JSONObject c = location.getJSONObject(i);


                        String name = c.getString("name");
                        Double latitude = c.getDouble("latitude");
                        Double longitude = c.getDouble("longitude");
                        String address = c.getString("address");
                        Integer number = c.getInt("number");





                        HashMap<String, String> station = new HashMap<>();

                        // adding each child node to HashMap key => value
                        station.put("name", name);
                        station.put("address", address);
                       // station.put("number", number);


                        // adding station to station list
                        stationList.add(station);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    EspaceVelo.this, stationList,
                    R.layout.list_item, new String[]{"name", "address",
                    }, new int[]{R.id.name,
                    R.id.address});

            lv.setAdapter(adapter);
        }

    }
}

