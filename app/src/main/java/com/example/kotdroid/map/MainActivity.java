package com.example.kotdroid.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mGoogleMap;

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    private LocationCallback mLocationCallback;

    private MarkerOptions markerOptions;

    private Marker marker;

    private Marker marker2, marker1;

    private Circle circle;

    private Polyline mPolyline;

    private ArrayList<Marker> markers = new ArrayList<>();

    private Polygon polygon;
    private final static int POLYGON_POINTS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isGooglePlayServicesAvailable()) {
            Toast.makeText(this, "Perfect", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_main);
            initMap();
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (null != locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        goToZoomedLocation("I'm here", location.getLatitude(), location.getLongitude(), 25f);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Can't fetch the location", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability mGoogleApiAvailability = GoogleApiAvailability.getInstance();
        int isAvailable = mGoogleApiAvailability.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (mGoogleApiAvailability.isUserResolvableError(isAvailable)) {
            Dialog dialog = mGoogleApiAvailability.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else
            Toast.makeText(this, "Can't connect to the play services", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (null != mGoogleMap) {

            /** for drawing polyline adding onlong click listener on map */
            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    setMarker("locality", latLng.latitude, latLng.longitude);
                }
            });


            /**for adding dragListener on mGoogleMap*/
            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);

                    LatLng latLng = marker.getPosition();
                    List<Address> addressList = null;

                    try {
                        addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Address address = addressList.get(0);
                    marker.setTitle(address.getLocality());
                    marker.showInfoWindow();
                }
            });


            /**for adding infoWindow adapter*/
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View view = getLayoutInflater().inflate(R.layout.snippet_window, null);

                    TextView tvLat = view.findViewById(R.id.tvLat);
                    TextView tvLng = view.findViewById(R.id.tvLng);
                    TextView tvLocality = view.findViewById(R.id.tvLocality);

                    ImageView ivImage = view.findViewById(R.id.ivImage);

                    tvLat.setText("Latitude : " + marker.getPosition().latitude);
                    tvLng.setText("Longitude : " + marker.getPosition().longitude);

                    tvLocality.setText(marker.getTitle());

                    return view;
                }
            });
        }
        /**
         *
         * way 1 for enabling the user's current location*/
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mGoogleMap.setMyLocationEnabled(true);

        /**
         *
         * way 2 for enabling the user's current location
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

    }

    private void goToZoomedLocation(String locality, double lat, double lng, float zoomValue) {
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomValue);
        mGoogleMap.animateCamera(mCameraUpdate);
        setMarker(locality, lat, lng);
    }

    private void setMarker(String locality, double lat, double lng) {

        markerOptions = new MarkerOptions()
                .title(locality)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_arker_point))
                .position(new LatLng(lat, lng));

        markers.add(mGoogleMap.addMarker(markerOptions));
        if (markers.size() > 2) {
            drawLine();
        }

        //drawing polygon
//        if (markers.size() == POLYGON_POINTS) {
//            removeEveryThing();
//        }
//        markerOptions = new MarkerOptions()
//                .title(locality)
//                .draggable(true)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_arker_point))
//                .position(new LatLng(lat, lng));
//
//        markers.add(mGoogleMap.addMarker(markerOptions));
//
//
//        //draw polygon
//        if (markers.size() == POLYGON_POINTS) {
//            drawPolygon();
//        }


//        circle = drawCircle(lat, lng);
//
//        if (null == marker1) {
//            marker1 = mGoogleMap.addMarker(markerOptions);
//        } else if (null == marker2) {
//            marker2 = mGoogleMap.addMarker(markerOptions);
//            drawLine();
//        } else {
//            removeEverything();
//            marker1 = mGoogleMap.addMarker(markerOptions);
//        }

    }

    private void drawPolygon() {
        PolygonOptions mPolygonOptions = new PolygonOptions()
                .fillColor(0x33ff0000)
                .strokeColor(Color.BLUE)
                .strokeWidth(5);

        for (int i = 0; i < POLYGON_POINTS; i++) {
            mPolygonOptions.add(markers.get(i).getPosition());
        }
        polygon = mGoogleMap.addPolygon(mPolygonOptions);
    }

    private void removeEveryThing() {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
        polygon.remove();
        polygon = null;
    }

    private void drawLine() {
//        PolylineOptions mPolylineOptions = new PolylineOptions()
//                .add(marker1.getPosition())
//                .add(marker2.getPosition())
//                .color(Color.BLUE)
//                .width(10);
//
//        mPolyline = mGoogleMap.addPolyline(mPolylineOptions);

        for (int i = markers.size() - 2; i < markers.size(); i++) {
            PolylineOptions mPolygonOptions = new PolylineOptions()
                    .add(markers.get(markers.size() - 2).getPosition())
                    .add(markers.get(markers.size() - 1).getPosition())
                    .color(getResources().getColor(R.color.skyBlue))
                    .width(10);

            mGoogleMap.addPolyline(mPolygonOptions);
            if (i != 0) {
                markers.get(i).remove();
            }
        }

    }

//

    private Circle drawCircle(double lat, double lng) {
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(lat, lng))
                .radius(1500)
                .fillColor(0x33ff0000)
                .strokeColor(Color.BLUE)
                .strokeWidth(2);

        return mGoogleMap.addCircle(circleOptions);
    }

    public void geoLocate(View view) {
        EditText editText = findViewById(R.id.etLocality);
        String location = editText.getText().toString().trim();

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(location, 1);
            if (addressList.size() == 0) {
                Toast.makeText(this, "Sorry location not found", Toast.LENGTH_SHORT).show();
            } else {
                Address address = addressList.get(0);
                String locality = address.getLocality();

                Toast.makeText(this, locality, Toast.LENGTH_LONG).show();
                goToZoomedLocation(locality, address.getLatitude(), address.getLongitude(), 15f);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuHybrid:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.menuNone:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.menuNormal:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.menuSatellite:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.menuTerrain:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3000);

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
        @SuppressLint("RestrictedApi")
        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);

        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}
