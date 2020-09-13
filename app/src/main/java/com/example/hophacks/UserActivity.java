package com.example.hophacks;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserActivity extends FragmentActivity implements OnMapReadyCallback {
    Button btnLogOut;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Location mLastLocation;
    private LocationManager locationManager;
    private DatabaseReference databaseReference;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 200;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION};
    private GoogleMap mMap;
    private Button submit;
    private TextView maskText;
    private TextView sanText;
    private CheckBox maskCheckBox;
    private CheckBox sanCheckBox;
    private LinearLayout checkBoxLayout;
    private PlacesClient placesClient;
    private HashMap<String, Checkboxs> checkboxsHashMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        checkboxsHashMap = new HashMap<>();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        databaseReference = FirebaseDatabase.getInstance().getReference("checkboxs");
        final boolean isAuth;
        if(bundle != null){
            isAuth = bundle.getBoolean("Auth", false);
        } else {
            isAuth = false;
        }
        checkBoxLayout = findViewById(R.id.checkboxs);
        sanCheckBox = findViewById(R.id.sanitizer_available);
        maskCheckBox = findViewById(R.id.mask_required);

        maskText = findViewById(R.id.MaskRequired);
        sanText = findViewById(R.id.SanitizerAvailable);
        submit = findViewById(R.id.submit);
        checkBoxLayout.setVisibility(View.INVISIBLE);
        // Initialize the SDK
        Places.initialize(getApplicationContext(), getString(R.string.apikey));

        // Create a new PlacesClient instance
        placesClient = Places.createClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_CODE_LOCATION_PERMISSION);
        }
        final RequestQueue queue = Volley.newRequestQueue(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ID, Place.Field.NAME));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mLastLocation == null) {

            mLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }
        if (mLastLocation == null) {
            mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (mLastLocation != null ) {

            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=" +
                    mLastLocation.getLatitude() +
                    "," +
                    mLastLocation.getLongitude() +
                    "&radius=800" +
                    "&type=restaurant" +
                    "&key=AIzaSyCqucIl0BRboqsKNSnvImqrpEGf36uaZrA";

            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        public void onResponse(JSONObject response) {
                            // response
                            Log.d("Response", response.toString());
                            try {
                                //String busyness =  response.getJSONObject("analysis").getString("venue_live_busyness");
                                JSONArray array = response.getJSONArray("results");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject jsonObject = (JSONObject)array.get(i);
                                    final String placeId = jsonObject.getString("place_id");
                                    place_ids.add(placeId);

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Log.d("Error.Response", error.toString());
                            Toast.makeText(getApplicationContext(), error.toString(),
                                    Toast.LENGTH_SHORT);
                        }
                    }
            ) {

                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();

                    return params;
                }
            };
            Log.d("response", postRequest.getBodyContentType());
            queue.add(postRequest);

        }

        mapFragment.getMapAsync(this);
        btnLogOut = (Button) findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent I = new Intent(UserActivity.this, ActivityLogin.class);
                startActivity(I);
            }
        });

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull final Place place) {
                // TODO: Get info about the selected place.
                Log.i("Success", "Place: " + place.getName() + ", " + place.getId());
                Log.i("Success", place.getLatLng().toString());
                mMap.clear();
                String countPeople;

                if (place != null) {
                    String url = "https://besttime.app/api/v1/forecasts/live?api_key_private=pri_2a47f86f684e43ebaafe49299e435b23&" +
                            "venue_name=" + place.getName() +
                            "&venue_address=" + place.getAddress();
                    JsonObjectRequest postRequest = new JsonObjectRequest (Request.Method.POST, url, null,
                            new Response.Listener<JSONObject>()
                            {
                                public void onResponse(JSONObject response) {
                                    // response
                                    Log.d("Response", response.toString());
                                    try {

                                        String busyness =  response.getString("status") == "Error" ? "0" : response.getJSONObject("analysis").getString("venue_live_busyness");
                                        int busy = Integer.parseInt(busyness);
                                        if (busy==0){
                                            mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                                                    .title(place.getName()).snippet("Busyness: "
                                                            + (busy  > 50 ? "busy" : "not busy") + "\n"
                                                            + maskText.getText().toString() +  ( ": Yes\n")
                                                            + sanText.getText().toString() + (": Yes\n")));
                                        }
                                        else {
                                            databaseReference.child("checkboxs").addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot snapshot) {
                                                    try {
                                                        //if (snapshot.getValue() != null) {
                                                        Log.d("", "In if before for");
                                                        for (DataSnapshot val : snapshot.getChildren()) {
                                                            //I am not sure what record are you specifically looking for
                                                            //This is if you are getting the Key which is the record ID for your Coupon Object
                                                            Log.d("New", "" + val.getKey());
                                                            if (val.getKey().contains(place.getId())) {
                                                                //Do what you want with the record
                                                                Checkboxs checkboxs = val.getValue(Checkboxs.class);
                                                                Log.v("", "" + checkboxs.maskRequired);
                                                                mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                                                                        .title(place.getName()).snippet("Busyness: "
                                                                                + (busy > 50 ? "busy" : "not busy") + "\n"
                                                                                + maskText.getText().toString() + (checkboxs.maskRequired ? ": Yes\n" : ": No\n")
                                                                                + sanText.getText().toString() + (checkboxs.sanitizerAvailable ? ": Yes\n" : ": No\n")));
                                                            }


                                                        }
                                                    /*} else {
                                                        Log.d("","In else");
                                                        mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                                                                .title(place.getName()).snippet("Busyness: "
                                                                        + (busy  > 50 ? "busy" : "not busy") + "\n"
                                                                        + maskText.getText().toString() +  ( ": Yes\n")
                                                                        + sanText.getText().toString() + (": Yes\n")));
                                                    }*/
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }


                                        if (isAuth)
                                            checkBoxLayout.setVisibility(View.VISIBLE);
                                        submit.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Checkboxs checkboxs = new Checkboxs(maskCheckBox.isChecked(),sanCheckBox.isChecked());
                                                databaseReference.child("checkboxs").child(place.getId()).setValue(checkboxs);
                                                checkBoxLayout.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    } catch (JSONException e) {

                                        String busyness =  "0";
                                        int busy = Integer.parseInt(busyness);
                                        databaseReference.child("checkboxs").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                try {
                                                    //if (snapshot.getValue() != null) {
                                                        for(DataSnapshot val : snapshot.getChildren()){
                                                            //I am not sure what record are you specifically looking for
                                                            //This is if you are getting the Key which is the record ID for your Coupon Object
                                                            if(val.getKey().contains(place.getId())){
                                                                //Do what you want with the record
                                                                Checkboxs checkboxs = val.getValue(Checkboxs.class);
                                                                Log.d("","In 2 if");
                                                                Log.v("",""+checkboxs.maskRequired);
                                                                mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                                                                        .title(place.getName()).snippet("Busyness: "
                                                                                + (busy  > 50 ? "busy" : "not busy") + "\n"
                                                                                + maskText.getText().toString() +  (checkboxs.maskRequired ? ": Yes\n": ": No\n")
                                                                                + sanText.getText().toString() + (checkboxs.sanitizerAvailable ? ": Yes\n": ": No\n")));
                                                            }


                                                        }
                                                   /* } else {
                                                        Log.d("","In 2 else");
                                                        mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                                                                .title(place.getName()).snippet("Busyness: "
                                                                        + (busy  > 50 ? "busy" : "not busy") + "\n"
                                                                        + maskText.getText().toString() +  ( ": Yes\n")
                                                                        + sanText.getText().toString() + (": Yes\n")));
                                                    }*/
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });



                                        if (isAuth)
                                            checkBoxLayout.setVisibility(View.VISIBLE);
                                        submit.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Checkboxs checkboxs = new Checkboxs(maskCheckBox.isChecked(),sanCheckBox.isChecked());
                                                databaseReference.child("checkboxs").child(place.getId()).setValue(checkboxs);
                                                checkBoxLayout.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    Log.d("Error.Response", error.toString());
                                    Toast.makeText(getApplicationContext(), error.toString(),
                                            Toast.LENGTH_SHORT);
                                }
                            }
                    ) {

                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map<String, String>  params = new HashMap<>();

                            return params;
                        }
                    };
                    Log.d("response", postRequest.getBodyContentType());
                    queue.add(postRequest);
                } else {
                    Log.d("loc", "no location");
                }

                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            }


            @Override
            public void onError(@NotNull Status status) {
                // TODO: Handle the error.
                Log.i("Error", "An error occurred: " + status);
            }
        });
    }
    List<String> place_ids = new ArrayList<>();
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                // Remove the marker
                marker.remove();
            }
        });
        final RequestQueue queue = Volley.newRequestQueue(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_CODE_LOCATION_PERMISSION);
        }

        // Add a marker in Sydney and move the camera

        for (String place_id: place_ids) {
            final FetchPlaceRequest request = FetchPlaceRequest.newInstance(place_id,
                    Arrays.asList(Place.Field.ADDRESS, Place.Field.ID, Place.Field.NAME));
            placesClient = Places.createClient(UserActivity.this);
            placesClient.fetchPlace(request).addOnSuccessListener((place_response) -> {
                Place place = place_response.getPlace();
                String besttime_url = "https://besttime.app/api/v1/forecasts/live?api_key_private=pri_2a47f86f684e43ebaafe49299e435b23&" +
                        "venue_name=" + place.getName() +
                        "&venue_address=" + place.getAddress();
                JsonObjectRequest post_request = new JsonObjectRequest (Request.Method.POST, besttime_url, null,
                        new Response.Listener<JSONObject>()
                        {
                            public void onResponse(JSONObject response) {
                                // response
                                Log.d("Response", response.toString());
                                try {
                                    String busyness =  response.getString("status") == "Error" ? "0" : response.getJSONObject("analysis").getString("venue_live_busyness");
                                    int busy = Integer.parseInt(busyness);
                                    Log.i("success", "Place found: " + place.getName());
                                    mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                                            .title(place.getName()));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", error.toString());
                                Toast.makeText(getApplicationContext(), error.toString(),
                                        Toast.LENGTH_SHORT);
                            }
                        }
                ) {

                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String>  params = new HashMap<>();

                        return params;
                    }
                };
                Log.d("response", post_request.getBodyContentType());
                queue.add(post_request);


            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    Log.e("Error", "Place not found: " + exception.getMessage());
                    final int statusCode = apiException.getStatusCode();
                    // TODO: Handle error with given status code.
                }
            });
        }
        LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(UserActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(UserActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(UserActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Your current location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
        mMap.getUiSettings().setZoomGesturesEnabled(false);
    }
}