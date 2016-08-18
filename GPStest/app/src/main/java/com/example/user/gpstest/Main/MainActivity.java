package com.example.user.gpstest.Main;

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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.gpstest.Camera.CameraActivity;
import com.example.user.gpstest.R;

public class MainActivity extends AppCompatActivity {
    TextView logView1;
    TextView logView2;
    TextView dislogView;
    EditText getLatB;
    EditText getLngB;

    double disValue;    //거리 차이

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkGpsService();

        Log.d("Main", "onCreate");

        logView1 = (TextView) findViewById(R.id.log);
        logView2 = (TextView) findViewById(R.id.log2);
        getLatB = (EditText) findViewById(R.id.editText1);
        getLngB = (EditText) findViewById(R.id.editText2);
        getLatB.setText("0");
        getLngB.setText("0");


        logView1.setText("GPS 가 잡혀야 좌표가 구해짐(lat)");
        logView2.setText("GPS 가 잡혀야 좌표가 구해짐(lng)");
        dislogView = (TextView) findViewById(R.id.dislog);
        dislogView.setText("거리계산중......");

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
                //위치 정보를 가져올 수 있는 메소드
                //위치 이동이나 시간 경과등으로 인해 호출된다.
                //최신 위치는 location 파라메터가 가지고 있으니 최신 위치를 가져오려면 location 파라메터를 이용하면 됨
                //즉 처음의 위치를 알고싶으면 처음의 값을 저장 해 주고 비교하면 됨.

                double lat = location.getLatitude();
                double lng = location.getLongitude();

                logView1.setText("현재 위도 : " +lat);
                logView2.setText("현재 경도 : " +lng);
                String strLat = getLatB.getText().toString();
                String strlng = getLngB.getText().toString();

                double lat2 = Double.parseDouble(strLat);
                double lng2 = Double.parseDouble(strlng);

                Object o1 = lat2;
                Object o2 = lng2;

                if(!o1.getClass().getName().equals("java.lang.Double")
                        || !o2.getClass().getName().equals("java.lang.Double")) {
                    dislogView.setText("위도, 경도에 문자가 아닌 실수를 입력 하세요.");
                } else {
                    GetDistance dis = GetDistance.getInstance();
                    disValue = dis.getDistanceLogic(lat, lat2, lng, lng2);
                    dislogView.setText(""+ disValue);
                    if(disValue < 10000) {
                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                finish();
                                startActivity(new Intent(getApplicationContext(), CameraActivity.class));
                                //여기에 딜레이 후 시작할 작업들을 입력
                            }
                        }, 500);
                    }
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                //위치공급자의 상태가 바뀔 때 호출
                //켬 -> 끔 or 끔 -> 켬
                logView1.setText("onStatusChanged");
                logView2.setText("onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
                //위치 공급자가 사용 가능해 질 때 호출
                //즉 GPS를 켜면 호출됨
                logView1.setText("GPS가 켜졌습니다.");
                logView2.setText("잠시만 기다려 주세요.");
            }

            public void onProviderDisabled(String provider) {
                //위치 공급자가 사용 불가능해질(disabled) 때 호출
                //GPS 꺼지면 여기서 예외처리 해주면 됨
                logView1.setText("GPS가 종료되었습니다.");
                logView2.setText("GPS를 다시 켜주세요.");
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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
    public void goCamera(View view) {
        switch (view.getId()) {
            case R.id.btn:
                startActivity(new Intent(getApplicationContext(), CameraActivity.class));
                finish();
                break;

        }
    }


}
