package com.itayc14.admin.poylgonboundrieschecker;

import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;


public class MathCalculations {

    public static LatLng findNearestPointOnLine(LatLng firstPoint, LatLng secondPoint, LatLng currentLocation) {
        double  x1, x2,
                y1, y2,
                x3, y3,
                x4, y4,
                slope, shortestLengthToLine,
                distanceFromFirstPoint, distanceFromSecondPoint, distanceFromCalculatedPoint;

        x1 = firstPoint.longitude;
        y1 = firstPoint.latitude;
        x2 = secondPoint.longitude;
        y2 = secondPoint.latitude;
        x3 = currentLocation.longitude;
        y3 = currentLocation.latitude;


        double xx = x2 - x1;
        double yy = y2 - y1;
        shortestLengthToLine = ((xx * (x3 - x1)) + (yy * (y3 - y1))) / ((xx * xx) + (yy * yy));


        x4 = x1 + xx * shortestLengthToLine;
        y4 = y1 + yy * shortestLengthToLine;


        //Log.d("tag", "x1,y1: (" + x1 + " ," + y1 + ")\nx2,y2: ("+x2+" ,"+y2+")\nx4,y4: ("+x4+" ,"+y4+")");
        //check if the point stays in polygon line
        if(y2 > y1) {
            if (y4 < y2 && y4 > y1) {
                return new LatLng(y4, x4);
            }
        }else if (y1 == y2){
            if((x2-x1) > 0) { //lat goes (in israel) from east to west
                if (x4 > x1 && x4 < x2)
                    return new LatLng(y4, x4);
            }else {
                if (x4 < x1 && x2 > x2)
                    return new LatLng(y4,x4);
            }
        }else {
            if (y4 > y2 && y4 < y1)
                return new LatLng(y4, x4);
        }


        // if i got this far - the point isnt on the polygon line
        distanceFromFirstPoint = SphericalUtil.computeDistanceBetween(currentLocation, firstPoint);
        distanceFromSecondPoint = SphericalUtil.computeDistanceBetween(currentLocation, secondPoint);
        if(distanceFromFirstPoint < distanceFromSecondPoint)
            return firstPoint;
        else
            return secondPoint;
    }
}
