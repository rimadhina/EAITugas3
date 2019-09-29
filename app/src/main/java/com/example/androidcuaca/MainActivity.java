package com.example.androidcuaca;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
//import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.androidcuaca.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
{

    private String TAG = MainActivity.class.getSimpleName();
    private ListView lv;

    ArrayList<HashMap<String, String>> cityList;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private FusedLocationProviderClient fusedLocation;
    Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocation = LocationServices.getFusedLocationProviderClient(this);
        cityList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        String str_lat_start = "29.287260";
        String str_lon_start = "29.287260";
        double lat_start = Double.parseDouble(str_lat_start);
        double lon_start = Double.parseDouble(str_lon_start);
        lastLocation = new Location("point A");
        lastLocation.setLatitude(lat_start);
        lastLocation.setLongitude(lon_start);
        new GetMetaWeatherData(lastLocation).execute();




    }

    public void getStarted()
    {
        //Start with app permissions, and then proceed to get location
        if (!checkPermissions()) {
            requestPermissions();
        }
        else
        {
            getLastLocation();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation()
    {
        fusedLocation.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null)
                        {
                            lastLocation = task.getResult();
                            //Once the location has been obtained, pass this information to the GetContacts class and obtain the corresponding JSON data
                            new GetMetaWeatherData(lastLocation).execute();
                        } else
                            {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
//                            showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });
    }


    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_activity);
        if (container != null) {
//            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
//        Snackbar.make(findViewById(android.R.id.content),
//                getString(mainTextStringId),
//                Snackbar.LENGTH_INDEFINITE)
//                .setAction(getString(actionStringId), listener).show();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions()
    {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale)
        {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

//            showSnackbar(R.string.permission_rationale, android.R.string.ok,
//                    new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            // Request permission
//                            startLocationPermissionRequest();
//                        }
//                    });
        }
        else startLocationPermissionRequest();
    }

    private class GetMetaWeatherData extends AsyncTask<Void, Void, Void>
    {
        Location currentLocation;

        GetMetaWeatherData(Location loc)
        {
            currentLocation = loc;
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            URL_HandlerClass sh = new URL_HandlerClass();

            String longi = Double.toString(currentLocation.getLongitude());
            String lat = Double.toString(currentLocation.getLatitude());

            // Making a request to URL using obtained coordinates and getting response

            int[] cars = {1047378, 1154781, 1132599, 2459115, 44418, 2471217, 2344116 };



            //Passing this URL to a method in the URL_HandlerClass class to obtain the JSON data as a string

            if (cars != null) {
                try {
                    // Getting JSON Array
                    for (int i=0; i<cars.length; i++)
                    {
                        int woeid1 = cars[i];

                        String url = "https://www.metaweather.com/api/location/"+woeid1;
//                        String url = "https://www.metaweather.com/api/location/1154781/";
                        String jsonStr = sh.makeURLrequest(url);

                        Log.e(TAG, "Response from url: " + jsonStr);

                        JSONObject cities = new JSONObject(jsonStr);

                        JSONArray consolideted_weather = cities.getJSONArray("consolidated_weather");
                        JSONObject jsonObject = consolideted_weather.getJSONObject(0);
                        String distance = "Current Temperature: "+jsonObject.getString("the_temp");
                        String title = cities.getString("title");
                        String location_type = "Weather : " + jsonObject.getString("weather_state_name");
                        String woeid = "Min Temperature : " + jsonObject.getString("min_temp");
                        String latt_long = "Max Temperature : " + jsonObject.getString("max_temp");

                        //single hashmap for each new city
                        HashMap<String, String> city = new HashMap<>();

                        // adding each key and value to the hashmap
                        // each set of 5 key-value pairs represents one city, and one element in the ArrayList
                        city.put("distance", distance);
                        city.put("title", title);
                        city.put("location_type", location_type);
                        city.put("woeid", woeid);
                        city.put("latt_long", latt_long);

                        // adding the city to the arrayList
                        cityList.add(city);

                    }



                }
                catch (final JSONException e)
                {
                    Log.e(TAG, "Data parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            else {
                Log.e(TAG, "Couldn't get JSON data");
                Toast.makeText(getApplicationContext(), "Couldn't get JSON data", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            //Creating an adapter that enables population of the textViews with the retrieved data
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, cityList,
                    R.layout.list_item, new String[]{"title","location_type","latt_long", "distance", "woeid"},
                    new int[]{R.id.title, R.id.location_type, R.id.latt_long, R.id.distance, R.id.woeid});
            lv.setAdapter(adapter);
        }
    }
}