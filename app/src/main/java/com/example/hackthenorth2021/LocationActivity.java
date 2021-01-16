package com.example.hackthenorth2021;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.internal.location.zzn;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class LocationActivity extends AppCompatActivity {
    private final String TAG = LocationActivity.class.getSimpleName();
    private Spinner spinnerType;
    private Button buttonFind;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat = 0;
    private double currentLong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        spinnerType = findViewById(R.id.sp_type);
        buttonFind = findViewById(R.id.bt_find);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);

        String[] placeTypeList = {"shoes", "clothes", "books"};

        String[] placeNameList = {"Shoes Donation", "Clothes Donation", "Books Donation"};

        // set adapter on spinner
        spinnerType.setAdapter(new ArrayAdapter<>(LocationActivity.this
                , android.R.layout.simple_spinner_dropdown_item, placeNameList));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(LocationActivity.this
                , Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: permissions granted");
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(LocationActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        buttonFind.setOnClickListener(new View.OnClickListener() {
            Object dataTransfer[] = new Object[2];

            @Override
            public void onClick(View v) {
                // Get selected position of spinner
                int i = spinnerType.getSelectedItemPosition();
                // initialize url
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + currentLat + "," + currentLong +
                        "&radius=30000" + //radius = 30 km
                        "&keyword=" + placeTypeList[i] +
                        "&key=" + getResources().getString(R.string.google_map_key);

                Log.d("MapsActivity", "url= " + url);
                // Execute place task method
//                new PlaceTask().execute(url);
                dataTransfer[0] = map;
                dataTransfer[1] = url;

                new GetNearbyPlacesData().execute(dataTransfer);
            }
        });

    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Task<Location> task = mFusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map = googleMap;
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(currentLat, currentLong), 10
                            ));
                            Log.d("CurrentLocation:", "lat: "+ currentLat + ", lng: " + currentLong);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Call method when permission is granted
                getCurrentLocation();
            } else {
                Toast.makeText(this,
                        R.string.location_permission_denied,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

     private class PlaceTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            try {
                data = downloadUrl(strings[0]);
                Log.d("Data from URL", "data: " + data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

         protected void onPostExecute(String s) {
            new GetNearbyPlacesData().execute(s);
         }
    }

    public String downloadUrl(String string) throws IOException {
        URL url = new URL(string);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String data = builder.toString();
        reader.close();
        Log.d("Download URL", "Returning data = " + data);
        return data;
    }

    private class GetNearbyPlacesData extends AsyncTask<Object, String, String> {
        private String googlePlacesData;
        private GoogleMap mMap;
        private String url;

//        protected List<HashMap<String, String>> doInBackground(String... strings) {
//            // create new json parser class
//            JsonParser jsonParser = new JsonParser();
//            List<HashMap<String, String>> mapList = null;
//            // initialize json object
//            JSONObject object = null;
//            try {
//                object = new JSONObject(strings[0]);
//                // parse json object
//                mapList = jsonParser.parseResult(object.toString());
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return mapList;
//        }
        @Override
        protected String doInBackground(Object... objects){
            mMap = (GoogleMap)objects[0];
            url = (String)objects[1];

            try {
                googlePlacesData = downloadUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return googlePlacesData;
        }

        protected void onPostExecute(String s){
            List<HashMap<String, String>> nearbyPlaceList;
            JsonParser parser = new JsonParser();
            nearbyPlaceList = parser.parseResult(s);
            Log.d("Nearby Places Data","called parse method");
            showNearbyPlaces(nearbyPlaceList);
        }

        private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
            mMap.clear();
            for (int i=0; i < nearbyPlaceList.size(); i++) {

                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

                double lat = Double.parseDouble(googlePlace.get("lat"));
                double lng = Double.parseDouble(googlePlace.get("lng"));
                String placeName = googlePlace.get("name");

                Log.d("Search locations", "name: " + placeName);

                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(placeName);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        }
    }
}
