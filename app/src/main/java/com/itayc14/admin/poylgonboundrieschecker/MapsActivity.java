package com.itayc14.admin.poylgonboundrieschecker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlGeometry;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;


import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private LocationManager lcManager;
    private static final int LOCATION_PERMISSION_CODE = 127;
    private LocationRequest locationRequest;
    private MyLocationListener myLocationListener;
    private ArrayList<LatLng> outerBoundriesList;
    private KmlLayer layer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //initiate variables
        lcManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        outerBoundriesList = new ArrayList<>();
        PlaceMarkAdder adder = new PlaceMarkAdder() {
            @Override
            public void addPlaceMark(LatLng point, String pointName, boolean moveMapCamerToPoint, @Nullable  Double showDistanceToPoint) {
                addPlaceMarkToMap(point, pointName, moveMapCamerToPoint);
                if(showDistanceToPoint != null) {
                    DecimalFormat df = new DecimalFormat("#.##");
                    String distance = df.format(showDistanceToPoint)+" meters";
                    if(showDistanceToPoint > 1000)
                        distance = df.format(showDistanceToPoint/1000)+"km";
                    Toast.makeText(MapsActivity.this,"Nearest distance to polygon: "+ distance, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void drawLineToPlaceMark(LatLng from, LatLng to) {
                PolylineOptions options = new PolylineOptions();
                options.add(from, to)
                        .width(3)
                        .color(Color.BLUE);

                mMap.addPolyline(options);
            }
        };
        myLocationListener = new MyLocationListener(this, adder);



        //pop up gps setting if not turned on
        if (!lcManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        requestLocation();
    }

    public void requestLocation(){
        int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(checkPermission == PackageManager.PERMISSION_GRANTED)
            lcManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, myLocationListener);
        else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(32.0852999, 34.78176759999997)));
        //load kml file data
        try {
            layer = new KmlLayer(mMap, R.raw.allowed_area, this);
            layer.addLayerToMap();

            KmlPolygon polygon = null;
            for (KmlContainer container : layer.getContainers()) {
                for (KmlPlacemark placemark : container.getPlacemarks()) {
                    KmlGeometry geometry = placemark.getGeometry();
                    polygon = (KmlPolygon) geometry;
                }
            }
            outerBoundriesList = polygon.getOuterBoundaryCoordinates();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        myLocationListener.setOuterBoundriesList(outerBoundriesList);
    }

    public void addPlaceMarkToMap(LatLng point, String pointName, boolean moveCameraToPoint) {
        mMap.addMarker(new MarkerOptions().position(point).title(pointName));
        if(moveCameraToPoint) {
            CameraPosition cp = new CameraPosition(point, 15.5f, 0, 0);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp), 2500, null);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_CODE){
            if(grantResults.length > 0){
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    requestLocation();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(checkPermission == PackageManager.PERMISSION_GRANTED)
            lcManager.removeUpdates(myLocationListener);
    }

    public interface PlaceMarkAdder{
        public void addPlaceMark(LatLng point, String pointName, boolean moveMapCamerToPoint, @Nullable Double showDistanceToPoint);
        public void drawLineToPlaceMark(LatLng from, LatLng to);
    }



}
