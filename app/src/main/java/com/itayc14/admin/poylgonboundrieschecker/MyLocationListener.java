package com.itayc14.admin.poylgonboundrieschecker;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;


public class MyLocationListener implements android.location.LocationListener {

    private ArrayList<LatLng> outerBoundriesList;
    private Context context;
    private Location currentLocation;
    private boolean checkOnce;
    private MapsActivity.PlaceMarkAdder placeMark;
    private double distanceToNearestPoint;

    public MyLocationListener(Context context, MapsActivity.PlaceMarkAdder adder){
        this.context = context;
        checkOnce = true;
        placeMark = adder;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;

        if(outerBoundriesList != null) { //it takes time to upload the polygon..
            if (checkOnce) {
                placeMark.addPlaceMark(getCurrentLocation(), "המיקום שלי", false, null);
                if (!noticeUserIfInsideAllowedArea())
                        checkShortestDistanceToPolygon();
                checkOnce = false;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private boolean noticeUserIfInsideAllowedArea(){
        boolean isInsideAllowedArea = PolyUtil.containsLocation(getCurrentLocation(), outerBoundriesList, false);
        if(isInsideAllowedArea) {
            Toast.makeText(context, R.string.inside_allowed_area, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


    public void checkShortestDistanceToPolygon(){
        LatLng nearestPoint = null, pointOnNextLine = null;
        boolean firstLoop = true;

        for(int i=0; i<outerBoundriesList.size(); i++) {
            if(i == outerBoundriesList.size()-1) {
                placeMark.addPlaceMark(nearestPoint, "shortest", true, distanceToNearestPoint);
                placeMark.drawLineToPlaceMark(getCurrentLocation(), nearestPoint);
                return; // finished..
            }else
                pointOnNextLine = MathCalculations.findNearestPointOnLine(outerBoundriesList.get(i), outerBoundriesList.get(i + 1), getCurrentLocation());

            if(firstLoop) {
                nearestPoint = pointOnNextLine;
                firstLoop = false;
            }
            distanceToNearestPoint = SphericalUtil.computeDistanceBetween(getCurrentLocation(), nearestPoint);
            if(SphericalUtil.computeDistanceBetween(getCurrentLocation(), pointOnNextLine)
                < distanceToNearestPoint){
                nearestPoint = pointOnNextLine;
            }
        }

        // i didnt know about this method... but im glade to find out about her:
        //shortestDistance = PolyUtil.distanceToLine(getCurrentLocation(), outerBoundriesList.get(0), outerBoundriesList.get(1));
    }

    public LatLng getCurrentLocation(){
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        return latLng;
    }

    public void setOuterBoundriesList(ArrayList<LatLng> outerBoundriesList) {
        this.outerBoundriesList = outerBoundriesList;
    }

    public double getDistanceToNearestPoint(){
        return distanceToNearestPoint;
    }
}
