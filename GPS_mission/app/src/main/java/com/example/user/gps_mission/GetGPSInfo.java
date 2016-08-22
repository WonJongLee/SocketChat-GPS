package com.example.user.gps_mission;

import android.location.Location;

//거리 차이를 확인하는 클래스
//싱글톤으로 만들었음

/**
 * Created by user on 2016-08-17.
 */
public class GetGPSInfo {
    private static GetGPSInfo ourInstance = new GetGPSInfo();

    public static GetGPSInfo getInstance() {
        return ourInstance;
    }

    private GetGPSInfo() {
    }

    public double getDistanceLogic(double latA, double lngA, double latB, double lngB) {

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

    //방위각 구하는 공식 http://drkein.tistory.com/117 에서 퍼옴
    public short getBearingAtoB(double latA, double lngA, double latB, double lngB) {
        // 현재 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Cur_Lat_radian = latA * (3.141592 / 180);
        double Cur_Lon_radian = lngA * (3.141592 / 180);


        // 목표 위치 : 위도나 경도는 지구 중심을 기반으로 하는 각도이기 때문에 라디안 각도로 변환한다.
        double Dest_Lat_radian = latB * (3.141592 / 180);
        double Dest_Lon_radian = lngB * (3.141592 / 180);

        // radian distance
        double radian_distance = 0;
        radian_distance = Math.acos(Math.sin(Cur_Lat_radian) * Math.sin(Dest_Lat_radian) + Math.cos(Cur_Lat_radian) * Math.cos(Dest_Lat_radian) * Math.cos(Cur_Lon_radian - Dest_Lon_radian));

        // 목적지 이동 방향을 구한다.(현재 좌표에서 다음 좌표로 이동하기 위해서는 방향을 설정해야 한다. 라디안값이다.
        double radian_bearing = Math.acos((Math.cos(Dest_Lat_radian) - Math.cos(Cur_Lat_radian) * Math.cos(radian_distance)) / (Math.cos(Cur_Lat_radian) * Math.sin(radian_distance)));        // acos의 인수로 주어지는 x는 360분법의 각도가 아닌 radian(호도)값이다.

        double true_bearing = 0;
        if (Math.sin(Dest_Lon_radian - Cur_Lon_radian) < 0) {
            true_bearing = radian_bearing * (180 / 3.141592);
            true_bearing = 360 - true_bearing;
        } else {
            true_bearing = radian_bearing * (180 / 3.141592);
        }

        return (short) true_bearing;
    }

    public byte getDirection(int bearing) {
        //A -> E 상행(0x01)
        //E -> A 하행(0x02)
        //예외처리(0x00)
        if (85 <= bearing && bearing <= 145) {
            return 0x02;
        } else if (265 <= bearing && bearing <= 325) {
            return 0x01;
        } else {
            return 0x00;
        }
    }

}
