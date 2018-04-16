package com.hr.airpollution;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.hr.airpollution.utility.APIConnector;
import com.hr.airpollution.utility.AddressUtil;
import com.hr.airpollution.utility.GPSUtil;
import com.hr.airpollution.utility.GeoPoint;
import com.hr.airpollution.utility.GeoTrans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    private ViewPager mViewPager;

    // GPSTracker class
    private GPSUtil gps;

    private TextView poultt;
    private LinearLayout indicator;
    private int mDotCount;
    private LinearLayout[] mDots;
    private ViewPager mviewPager;
    private List<String> listItem = new ArrayList<>();
    private viewPagerAdepter mfragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        indicator = (LinearLayout) findViewById(R.id.viewpagerindicator);
        mviewPager = (ViewPager) findViewById(R.id.pager);
        setData();
        //////////////////////////////상태바없애기//////////////////////////
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        /////////////////////////////////////////////
        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.nested);
        scrollView.setFillViewport(true);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        ////////// GPS 관련 ////////////////////
        if (!isPermission) {
            callPermission();
        }

        gps = new GPSUtil(MainActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            Toast.makeText(getApplicationContext(), "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude,
                    Toast.LENGTH_LONG).show();

            GeoPoint in_pt = new GeoPoint(longitude, latitude);
            final GeoPoint tm_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);
            System.out.println("tm : xTM=" + tm_pt.getX() + ", yTM=" + tm_pt.getY());

            /*
             * 1. 가장 가까운 측정소 가져오는 API를 실행한다.
             * 2. 1번을 기반으로 측정소에서 미세먼지 농도 측정한 결과를 가져온다.
             */
            getLatestObserve(tm_pt.getX(), tm_pt.getY());

            String address = AddressUtil.getAddress(getApplicationContext(), latitude, longitude);
            Toast.makeText(getApplicationContext(), address, Toast.LENGTH_LONG).show();
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
        ////////////////////////

        ////// 푸시 관련 ///////
//        getPush();
        ////////////////////////
    }

    private void getPush() {
        FirebaseMessaging.getInstance().subscribeToTopic("notice"); // 여기서 notice를 구독시킴
        final String url = "https://fcm.googleapis.com/fcm/send";
        final String parameters = "{" +
                "\"data\": {" +
                "\"message\": {" +
                "\"content\": \"먼지가 너무 많아요!! ㅠ.ㅠ\"" +
                "}" +
                "}," +
                "\"to\": \"/topics/notice\"" +
                "}";
        // /topics/notice로 보냄. (to에 디바이스 토큰을 이용하는 방법도 있음.)

        try {
            new Thread() {
                public void run() {
                    try {
                        String result = sendPost(url, parameters);
                        System.out.println("푸시결과:" + result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        ////////////////////////
    }

    /*
     * 1. 가장 가까운 측정소 가져오는 API를 실행한다.
     * 2. 1번을 기반으로 측정소에서 미세먼지 농도 측정한 결과를 가져온다.
     */
    private void getLatestObserve(final double tmX, final double tmY) {
        new Thread() { // 1번
            public void run() {
                try {
                    String result = APIConnector.getNearbyMsrstnList(tmX, tmY);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray jsonArray = new JSONArray(jsonObject.get("list").toString());
                        JSONObject jsonObject1 = new JSONObject(jsonArray.get(0).toString());

                        final String nearByStationName = jsonObject1.get("stationName").toString();
                        System.out.println("가장가까운 측정소:" + nearByStationName);

                        new Thread() { // 2번
                            public void run() {
                                try {
                                    String result;
                                    result = APIConnector.getMsrstnAcctoRltmMesureDnsty(nearByStationName);
                                    try {
                                        JSONObject jsonObject = new JSONObject(result);
                                        JSONArray jsonArray = new JSONArray(jsonObject.get("list").toString());
                                        System.out.println(jsonArray);
                                        JSONObject latestObserver = new JSONObject(jsonArray.get(0).toString());

                                        int 미세먼지농도 = Integer.parseInt(latestObserver.get("pm10Value").toString());
                                        int 미세먼지등급;
                                        if (latestObserver.get("pm10Grade").toString().equals("")) {
                                            미세먼지등급 = Integer.parseInt(latestObserver.get("pm10Grade1h").toString());
                                        } else {
                                            미세먼지등급 = Integer.parseInt(latestObserver.get("pm10Grade").toString());
                                        }

//                                        int 초미세먼지농도 = Integer.parseInt(latestObserver.get("pm25Value").toString());
//                                        int 초미세먼지등급 = Integer.parseInt(latestObserver.get("pm25Grade").toString());

                                        System.out.println("가장 최근 미세먼지농도: " + 미세먼지농도);
                                        System.out.println("가장 최근 미세먼지등급: " + 미세먼지등급);
//                                        System.out.println("가장 최근 초미세먼지농도: " + 초미세먼지농도);
//                                        System.out.println("가장 최근 초미세먼지등급: " + 초미세먼지등급);

                                        result = "";
                                        switch (미세먼지등급) {
                                            case 1:
                                                result = "좋 음";
                                                break;
                                            case 2:
                                                result = " 보 통";
                                                break;
                                            case 3:
                                                result = "나 쁨";
                                                break;
                                            case 4:
                                                result = "매 우 나 쁨";
                                                break;
                                        }
                                        Message message = uiHandler.obtainMessage();
                                        message.what = 1; // 후처리 번호
                                        message.obj = result; // 전달할 데이터
                                        uiHandler.sendMessage(message);

                                        //GRADE 값 : 1 좋음 / 2 보통 / 3 나쁨 / 4 매우나쁨
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessFineLocation = true;
        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessCoarseLocation = true;
        }
        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    public String sendPost(String url, String parameters) throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "key=AAAA8E4-YGk:APA91bG2Pz3-2Nw7pCLjeIe3cWW-DXcMwu0LsNhk6G0uGB6xNZoRXfnKi_R6PVEnoUZzqkp85QqCxLN6v9wfkcVZ_Ua5B2lnmPldQqqzoQdY6gCA6Jelt5E5rkkejHES3EXvg_NcFs-O");

        //post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.write(parameters.getBytes("UTF-8"));
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("Post parameters : " + parameters);
        System.out.println("Response Code : " + responseCode);

        StringBuilder response = new StringBuilder();

        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }
        //result
        System.out.println(response.toString());
        return response.toString();
    }

    // 권한 요청 (GPS)
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    private final UIHandler uiHandler = new UIHandler();

    @SuppressLint("HandlerLeak")
    private class UIHandler extends Handler {

        UIHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            // 전달받은 what(후처리 번호), obj(데이터)를 토대로 UI 작업을 한다.

            switch (msg.what) { // what ID 값에 따른 UI 후처리
                case 1: // getObserve(tmX, tmY)
                    poultt.setText(msg.obj.toString());
                    break;
            }
        }
    }

    private void setData() {
        listItem.add("Ini adalah fragment 1");
        listItem.add("Ini adalah fragment 2");
        listItem.add("Ini adalah fragment 3");
        listItem.add("Ini adalah fragment 4");
        listItem.add("Ini adalah fragment 5");

        mfragmentAdapter = new viewPagerAdepter(this, getSupportFragmentManager(), listItem);
        mviewPager.setAdapter(mfragmentAdapter);
        mviewPager.setCurrentItem(0);
        mviewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < mDotCount; i++) {
                    mDots[i].setBackgroundResource(R.drawable.viewpager_indicator_nonselcted_item);
                }
                mDots[position].setBackgroundResource(R.drawable.viewpager_indicator_selcted_item);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setUiPageViewController();

    }

    private void setUiPageViewController() {

        mDotCount = mfragmentAdapter.getCount();
        mDots = new LinearLayout[mDotCount];

        for (int i = 0; i < mDotCount; i++) {
            mDots[i] = new LinearLayout(this);
            mDots[i].setBackgroundResource(R.drawable.viewpager_indicator_nonselcted_item);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);
            indicator.addView(mDots[i], params);
            mDots[0].setBackgroundResource(R.drawable.viewpager_indicator_selcted_item);
        }
    }
}