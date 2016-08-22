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
import android.support.annotation.IntegerRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    TextView Latv, Lngv, Logv, NPosition;

    ArrayList<String> referenceNamePosition;
    ArrayList<Double> referenceLatPosition;
    ArrayList<Double> referenceLngPosition;
    double disValue[] = {0, 1, 2, 3, 4};

    static int stationBuf = -1;

    //mflag = socket에서 통신이 끊겼는지 안끊겼는지를 확인할 수 있는 플래그
    static boolean mflag = false;
    final int DETECTRANGE = 100;

    int YEAR, MONTH, DAY, HOUR, MINNTE, SECOND;
    //운행구분
    int driveFlag = 0;
    int diviceState = 127;
    GetGPSInfo dis = GetGPSInfo.getInstance();
    TransByte tb = TransByte.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkGpsService();

        getItem();

        setLog();

    }

    private boolean checkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(),
                android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

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
        referenceNamePosition = new ArrayList<String>();
        referenceLatPosition = new ArrayList<Double>();
        referenceLngPosition = new ArrayList<Double>();

        Latv = (TextView) findViewById(R.id.lat);
        Lngv = (TextView) findViewById(R.id.lng);
        Logv = (TextView) findViewById(R.id.log);
        NPosition = (TextView) findViewById(R.id.NearestPosition);

        Latv.setText("GPS 탐색중입니다.");
        Lngv.setText("잠시만 기다려 주세요.");


        referenceNamePosition.add("00100-01");
        referenceNamePosition.add("00100-02");
        referenceNamePosition.add("00100-03");
        referenceNamePosition.add("00100-04");
        referenceNamePosition.add("00100-05");

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


        byte[] a = tb.ItoB(123);
        byte[] b = tb.StoB("456");
        byte[] c = tb.ItoB("789");
        byte[] d = tb.mergyByte(a, b);
        byte[] e = tb.mergyByte(d, c);
        byte f[] = {22};
        int[] qwe = tb.BtoI(a);
        int q = 22;
    }

    private double getMin(double arr[]) {
        double min = arr[0];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < min) min = arr[i];
        }
        return min;
    }

    public void buttonClick(View view) {
        FileReadWrite frw = FileReadWrite.getInstance();

        String data = Logv.getText().toString();
        switch (view.getId()) {
            case R.id.loadFile:
                frw.loadButton(Logv);
                Toast.makeText(this, "불러오기 성공!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.saveFile:
                frw.saveButton(data);
                Toast.makeText(this, "파일저장 성공!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setLog() {

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
                //만약 우리가 임의로 결정한 x,y좌표가 아닌 실제 Location 객체와 비교한다면
                //location.distanceTo() 메소드를 사용하여 비교하는 것이 바람직하다.
                for (int i = 0; i < 5; i++) {
                    disValue[i] = dis.getDistanceLogic(lat, lng, referenceLatPosition.get(i), referenceLngPosition.get(i));
                }

                //가장 가까운 역과의 거리
                double minDistance = getMin(disValue);

                //가장 가까운 역의 이름
                String minReferenceName;

                YEAR = cal.get(Calendar.YEAR) - 2000;
                MONTH = (cal.get(Calendar.MONTH) + 1);
                DAY = cal.get(Calendar.DATE);
                HOUR = (cal.get(Calendar.HOUR_OF_DAY)) + 9;
                MINNTE = cal.get(Calendar.MINUTE);
                SECOND = cal.get(Calendar.SECOND);

                //어느 역에 도착했는지 확인 후 메세지
                for (int i = 0; i < disValue.length; i++) {
                    if (minDistance == disValue[i]) {
                        minReferenceName = referenceNamePosition.get(i).toString();
                        NPosition.setText(minReferenceName);
                    }

                    if (disValue[i] < DETECTRANGE && !mflag) {
                        stationBuf = i;
                        logCheck_0x21(lat, lng, location, i);
                        mflag = true;
                    }
                }
                //운행 시작 전
                if (stationBuf == -1) {
                    return;
                } else if (disValue[stationBuf] >= DETECTRANGE && mflag) {

                    //출발지점이 A라면 A가 차고지가 되어 운행시작을 알림
                    if (stationBuf == 0) {
                        logCheck_0x15(lat, lng, location);
                    }
                    //출발지점이 A가 아니라면 그냥 해당 역에서 출발한 것을 알림
                    else {
                        logCheck_0x22(lat, lng, location);
                    }
                    mflag = false;
                }
            }


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

    //운행시작
    public void logCheck_0x15(double lat, double lng, Location location) {
        String inputLogCheck_0x15 = bodyInfo(lat, lng, location) +
                " " + driveFlag + " " + "\n";
        Logv.append(inputLogCheck_0x15);
    }

    //정류장도착
    public void logCheck_0x21(double lat, double lng, Location location, int i) {
        String inputLogCheck_0x21 = bodyInfo(lat, lng, location, i) +
                "\n";
        Logv.append(inputLogCheck_0x21);

    }

    //정류장출발
    public void logCheck_0x22(double lat, double lng, Location location) {
        String inputLogCheck_0x22 = bodyInfo(lat, lng, location) +
                "\n";

        Logv.append(inputLogCheck_0x22);
    }

    //시작, 출발
    public String bodyInfo(double lat, double lng, Location location) {

        int latI = (int) (lat*100000);
        int lngI = (int) (lng*100000);

        String ymd =
                String.format("%02d", YEAR) +
                        String.format("%02d", MONTH) +
                        String.format("%02d", DAY);

        String hms = String.format("%02d", HOUR) +
                String.format("%02d", MINNTE) +
                String.format("%02d", SECOND);


        String splitStr[] = referenceNamePosition.get(stationBuf).split("-");

        String route = String.format("%05d", Integer.parseInt(splitStr[0])) +
                String.format("%02d", Integer.parseInt(splitStr[1])) +
                String.format("3") +
                String.format("12");
        String.format("12");

        return
                (int) disValue[stationBuf] + " " +
                        ymd + " " + hms + " " + ymd + " " + hms + " " +
                        route + " " +
                        lat + " " + lng + " " + 0 + " " +
                        dis.getBearingAtoB(referenceLatPosition.get(stationBuf), referenceLngPosition.get(stationBuf), lat, lng) + " " +
                        (int)location.getSpeed() + " " + diviceState;
    }

    //도착
    public String bodyInfo(double lat, double lng, Location location, int i) {

        int latI = (int) (lat*100000);
        int lngI = (int) (lng*100000);

        String ymd =
                String.format("%02d", YEAR) +
                        String.format("%02d", MONTH) +
                        String.format("%02d", DAY);

        String hms = String.format("%02d", HOUR) +
                String.format("%02d", MINNTE) +
                String.format("%02d", SECOND);


        String splitStr[] = referenceNamePosition.get(stationBuf).split("-");

        String route = String.format("%05d", Integer.parseInt(splitStr[0])) +
                String.format("%02d", Integer.parseInt(splitStr[1])) +
                String.format("3") +
                String.format("12");
        return
                (int) disValue[stationBuf] + " " +
                        ymd + " " + hms + " " + ymd + " " + hms + " " +
                        route + " " +
                        latI + " " + lngI + " " + 0 + " " +
                        dis.getBearingAtoB(lat, lng, referenceLatPosition.get(i), referenceLngPosition.get(i)) + " " +
                        location.getSpeed() + " " + diviceState +
                        "----------------------------------------------";
    }
}
