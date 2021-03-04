package com.lockdownhelp.app;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.lockdownhelp.app.Models.AddressModel;
import com.lockdownhelp.app.R;
import com.lockdownhelp.app.Utils.DataProcessor;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.skyfishjy.library.RippleBackground;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    private AutocompleteSupportFragment places;
    RelativeLayout relDialog;

    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private View mapView;
    private Button btnSelectLocation;
    private TextInputLayout txtFlatNo;
    private TextView txtAddress;
    private RippleBackground ripple_marker_bg;

    private final float DEFAULT_ZOOM=(float)17.0;

    DataProcessor dataProcessor;
    LinearLayout linSearch;

    Geocoder geocoder;
    List<Address>addresses;

    FirebaseAuth mAuth;

    String openType,place;

    ImageButton imgBtnMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnSelectLocation=findViewById(R.id.btnSelectLocation);
        ripple_marker_bg=findViewById(R.id.ripple_marker_bg);
        relDialog=findViewById(R.id.relDialog);

        txtFlatNo=findViewById(R.id.txtFlatNo);
        txtAddress=findViewById(R.id.txtAddress);
        imgBtnMyLocation=findViewById(R.id.imgBtnMyLocation);
        linSearch=findViewById(R.id.linSearch);

        openType=getIntent().getStringExtra("openType");

        geocoder=new Geocoder(this, Locale.getDefault());

        dataProcessor=DataProcessor.getInstance(this);

        mAuth=FirebaseAuth.getInstance();

        final SupportMapFragment mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView=mapFragment.getView();

        mFusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(MapActivity.this);
        Places.initialize(MapActivity.this,"AIzaSyDG5pE1izylOdzxdRvEsyJr9K9rJsRDBY4");
        placesClient=Places.createClient(this);
        final AutocompleteSessionToken token=AutocompleteSessionToken.newInstance();


        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(txtFlatNo.getEditText().getText().toString())){
                    final LatLng currentMarkerLocation =mMap.getCameraPosition().target;
                    ripple_marker_bg.startRippleAnimation();
                    getAddress(currentMarkerLocation.latitude,currentMarkerLocation.longitude);

                }else{
                    txtAddress.setText("Enter Flat Number or House Number");
                    txtAddress.setTextColor(getResources().getColor(R.color.colorRed));
                    txtAddress.setVisibility(View.VISIBLE);
                }

            }
        });

        txtFlatNo.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)){
                    txtAddress.setVisibility(View.VISIBLE);
                    txtAddress.setText("Enter Flat Number or House Number");
                    txtAddress.setTextColor(getResources().getColor(R.color.colorRed));
                }else{
                    txtAddress.setVisibility(View.GONE);
                }
            }
        });


        places=(AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.places_search_fragment);
        places.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.ADDRESS,Place.Field.LAT_LNG,Place.Field.NAME));

        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),DEFAULT_ZOOM));
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MapActivity.this,"Failed to find the place",Toast.LENGTH_SHORT).show();
            }
        });

        imgBtnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
                if (mMap!=null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));
                }
            }
        });

    }

   /* @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        showMyLocationButton();
    }*/

    /*private void showMyLocationButton(){
        if (mapView!=null && mapView.findViewById(Integer.parseInt("1"))!=null&&mMap!=null){
            View locationButton =((View)mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams=(RelativeLayout.LayoutParams)locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.setMargins(0,0,40,relDialog.getHeight()+100);
            //Toast.makeText(MapActivity.this,""+relDialog.getHeight()+" mea "+relDialog.getMeasuredHeight(),Toast.LENGTH_SHORT).show();
        }
    }*/



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        //showMyLocationButton();

        //Check if GPS is enabled or not and then request user to enable

        LocationRequest locationRequest=LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(50000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient= LocationServices.getSettingsClient(MapActivity.this);
        Task<LocationSettingsResponse> task=settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException){
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapActivity.this,51);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

       // showMyLocationButton();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==51){
            if (resultCode==RESULT_OK){
                getDeviceLocation();
            }
        }
    }

    private void getDeviceLocation(){
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()){
                    mLastKnownLocation=task.getResult();
                    if (mLastKnownLocation!=null){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));
                    }else{
                        final LocationRequest locationRequest=LocationRequest.create();
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(5000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationCallback=new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                if (locationRequest==null){
                                    return;
                                }else{
                                    mLastKnownLocation=locationResult.getLastLocation();
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));
                                    mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                }
                            }
                        };
                        mFusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
                    }
                }else{
                    Toast.makeText(MapActivity.this,"Unable to get last Location",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getAddress(final double lat, final double lng){
        relDialog.setEnabled(false);
        linSearch.setEnabled(false);
        try {
            addresses=geocoder.getFromLocation(lat,lng,1);
            final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();

            if ((!TextUtils.equals(dataProcessor.getUserDistrict(),addresses.get(0).getSubAdminArea())||!TextUtils.equals(dataProcessor.getUserState(),addresses.get(0).getAdminArea()))){
                GeoFire geoFire=new GeoFire(databaseReference.child("usersLocation").child(dataProcessor.getUserState()).child(dataProcessor.getUserDistrict()));
                geoFire.removeLocation(mAuth.getCurrentUser().getUid());
            }
            final String address=txtFlatNo.getEditText().getText().toString()+", "+addresses.get(0).getSubLocality()+", "+addresses.get(0).getLocality();

            GeoFire geoFire=new GeoFire(databaseReference.child("usersLocation").child(addresses.get(0).getAdminArea()).child(addresses.get(0).getSubAdminArea()));
            geoFire.setLocation(mAuth.getCurrentUser().getUid(), new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error==null){
                        AddressModel addressModel=new AddressModel();
                        addressModel.setAddress(address);
                        addressModel.setDistrict(addresses.get(0).getSubAdminArea());
                        addressModel.setState(addresses.get(0).getAdminArea());
                        databaseReference.child("Users").child(mAuth.getCurrentUser().getUid()).child("address").setValue(addressModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    dataProcessor.setDistrictAndState(addresses.get(0).getSubAdminArea(),addresses.get(0).getAdminArea());
                                    dataProcessor.setLatLng(String.valueOf(lat),String.valueOf(lng),address);
                                    if (TextUtils.equals(openType,"MainActivity")){
                                        Intent intent=new Intent(MapActivity.this,MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        onBackPressed();
                                        finish();
                                    }
                                }else{
                                    relDialog.setEnabled(true);
                                    linSearch.setEnabled(true);
                                    ripple_marker_bg.stopRippleAnimation();
                                    Toast.makeText(MapActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }else{
                        relDialog.setEnabled(true);
                        linSearch.setEnabled(true);
                        ripple_marker_bg.stopRippleAnimation();
                        Toast.makeText(MapActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            relDialog.setEnabled(true);
            linSearch.setEnabled(true);
            ripple_marker_bg.stopRippleAnimation();
            Toast.makeText(MapActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
        }
    }
}
