package com.example.user.gps_mission;

import android.location.Location;

//거리 차이를 확인하는 클래스
//싱글톤으로 만들었음

/**
 * Created by user on 2016-08-17.
 */
public class GetDistance {
    private static GetDistance ourInstance = new GetDistance();

    public static GetDistance getInstance() {
        return ourInstance;
    }

    private GetDistance() {
    }

    public double getDistanceLogic(double latA, double lngA, double latB,  double lngB) {

        double distanceLogic;

        Location locationA = new Location("point A");

        locationA.setLatitude(latA);
        locationA.setLongitude(lngA);

        Location locationB = new Location("point B");

        locationB.setLatitude(latB);
        locationB.setLongitude(lngB);

        distanceLogic = locationA.distanceTo(locationB);

        return distanceLogic;
    }
}
