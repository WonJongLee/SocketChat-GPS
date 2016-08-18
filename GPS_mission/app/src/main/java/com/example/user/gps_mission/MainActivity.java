package com.example.user.gps_mission;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    TextView Latv, Lngv, Logv, NPosition;

    ArrayList<Character> referenceNamePosition;
    ArrayList<Double> referenceLatPosition;
    ArrayList<Double> referenceLngPosition;
    double disValue[] = {0, 0, 0, 0, 0};

    static boolean mflag = false;

    //현재 시간 알아오기


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkGpsService();
        getItem();

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled=" + isGPSEnabled);
        Log.d("Main", "isNetworkEnabled=" + isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //시간알아오기
                TimeZone jst = TimeZone.getTimeZone("JST");
                Calendar cal = Calendar.getInstance(jst);
                //위치 정보를 가져올 수 있는 메소드
                //위치 이동이나 시간 경과등으로 인해 호출된다.
                //최신 위치는 location 파라메터가 가지고 있으니 최신 위치를 가져오려면 location 파라메터를 이용하면 됨
                //즉 처음의 위치를 알고싶으면 처음의 값을 저장 해 주고 비교하면 됨.

                double lat = location.getLatitude();
                double lng = location.getLongitude();

                Latv.setText("" + lat);
                Lngv.setText("" + lng);

                //5개의 지정된 장소와 거리를 비교 후 disValue에 저장한다.
                GetDistance dis = GetDistance.getInstance();
                for (int i = 0; i < 5; i++) {
                    disValue[i] = dis.getDistanceLogic(lat, lng, referenceLatPosition.get(i), referenceLngPosition.get(i));
                }

                //가장 가까운 역과의 거리
                double minDistance = getMin(disValue);

                //가장 가까운 역의 이름
                String minReferenceName;

                char beforePosition = 'A';

                for (int i = 0; i < disValue.length; i++) {
                    if (minDistance == disValue[i]) {
                        minReferenceName = referenceNamePosition.get(i).toString();
                        NPosition.setText(minReferenceName);
                    }

                    if (disValue[i] < 100 && !mflag) {
                        Logv.append("\n" + referenceNamePosition.get(i) + " 지점"
                                + "\n시간 : " + cal.get(Calendar.YEAR) + "년 " + (cal.get(Calendar.MONTH) + 1) + "월 " + cal.get(Calendar.DATE) + "일 " + cal.get(Calendar.HOUR_OF_DAY) + "시 " + cal.get(Calendar.MINUTE) + "분 " + cal.get(Calendar.SECOND) + "초 "
                                + "\n거리 : " + disValue[i]
                                + "\n탐지위치\nLat : " + lat + "\nLng : " + lng
                                + "\n-------------------------------------------------------------------------------"
                        );
                        beforePosition = referenceNamePosition.get(i);
                        mflag = true;
                    } else if (disValue[i] >= 100 && mflag) {
                        Logv.append("\n" + beforePosition + " 에서 출발"
                                + "\n-------------------------------------------------------------------------------");
                        mflag = false;
                    }
                }


            }

//                Logv.append("\n" + referenceNamePosition.get(i) + " 지점"
//                        + "\n시간 : " + cal.get(Calendar.YEAR) + "년 " + (cal.get(Calendar.MONTH) + 1) + "월 " + cal.get(Calendar.DATE) + "일 " + cal.get(Calendar.HOUR_OF_DAY) + "시 " + cal.get(Calendar.MINUTE) + "분 " + cal.get(Calendar.SECOND) + "초 "
//                        + "\n거리 : " + disValue[i]
//                        + "\n탐지위치\nLat : " + lat + "\nLng : " + lng
//                        + "\n-------------------------------------------------------------------------------"
//                );
//                Logv.append("\n" + referenceNamePosition.get(i) + " 지점에서 출발"
//                        + "\n-------------------------------------------------------------------------------");
//                mflag = false;

            public void onStatusChanged(String provider, int status, Bundle extras) {
                //위치공급자의 상태가 바뀔 때 호출
                //켬 -> 끔 or 끔 -> 켬
                Latv.setText("onStatusChanged");
                Lngv.setText("onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
                //위치 공급자가 사용 가능해 질 때 호출
                //즉 GPS를 켜면 호출됨
                Latv.setText("GPS가 켜졌습니다.");
                Lngv.setText("잠시만 기다려 주세요.");
            }

            public void onProviderDisabled(String provider) {
                //위치 공급자가 사용 불가능해질(disabled) 때 호출
                //GPS 꺼지면 여기서 예외처리 해주면 됨
                Latv.setText("GPS가 종료되었습니다.");
                Lngv.setText("GPS를 다시 켜주세요.");
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
    }

    private boolean checkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        Log.d(gps, "check GPS Page");

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("GPS 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n" +
                    "GPS 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "GPS를 켜야 사용 가능합니다.", Toast.LENGTH_SHORT);
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
        }
    }


    public void getItem() {
        referenceNamePosition = new ArrayList<Character>();
        referenceLatPosition = new ArrayList<Double>();
        referenceLngPosition = new ArrayList<Double>();

        Latv = (TextView) findViewById(R.id.lat);
        Lngv = (TextView) findViewById(R.id.lng);
        Logv = (TextView) findViewById(R.id.log);
        NPosition = (TextView) findViewById(R.id.NearestPosition);

        Latv.setText("GPS 탐색중입니다.");
        Lngv.setText("잠시만 기다려 주세요.");


        referenceNamePosition.add('A');
        referenceNamePosition.add('B');
        referenceNamePosition.add('C');
        referenceNamePosition.add('D');
        referenceNamePosition.add('E');

        referenceLatPosition.add(37.49568);
        referenceLatPosition.add(37.49502);
        referenceLatPosition.add(37.49455);
        referenceLatPosition.add(37.49413);
        referenceLatPosition.add(37.49373);

        referenceLngPosition.add(127.12259);
        referenceLngPosition.add(127.12140);
        referenceLngPosition.add(127.12063);
        referenceLngPosition.add(127.11979);
        referenceLngPosition.add(127.11907);

    }

    private double getMin(double arr[]) {
        double min = arr[0];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < min) min = arr[i];
        }
        return min;
    }
}
