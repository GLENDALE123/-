package com.hr.airpollution;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
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
import android.util.Log;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    // GPSTracker class
    private GPSUtil gps;
    private LinearLayout indicator;
    private int mDotCount;
    private LinearLayout[] mDots;
    private ViewPager mviewPager;
    private List<JSONObject> listItem = new ArrayList<>();
    private viewPagerAdepter mfragmentAdapter;
    String saveFileName = "airpollution_data.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (!(fileExist(saveFileName))) { // 저장 데이터가 없을 경우 만들어준다. (초기 1회 실행)
        // saveSpotList = JSONArray.   spot 정보에 관한 JSONObject를 담아둔다.

        String content = "{" +
                "\"saveSpotList\" : [{" +
                "\"text\": \"나 쁨\"," +
                "\"grade\": \"3\"," +
                "\"address\": \"장안구 정자2동\"," +
                "\"currentDateTimeString\": \"업데이트 04/18 오후 05:58\"" +
                "}] " +
                "}";

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(saveFileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        indicator = (LinearLayout) findViewById(R.id.viewpagerindicator);
        mviewPager = (ViewPager) findViewById(R.id.pager);
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
            String address = AddressUtil.getAddress(getApplicationContext(), latitude, longitude);
            getLatestObserve(address, tm_pt.getX(), tm_pt.getY());
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
    private void getLatestObserve(final String address, final double tmX, final double tmY) {
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
                                        int 미세먼지등급 = Integer.parseInt(latestObserver.get("pm10Grade1h").toString());

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
                                                result = "보 통";
                                                break;
                                            case 3:
                                                result = "나 쁨";
                                                break;
                                            case 4:
                                                result = "매 우 나 쁨";
                                                break;
                                        }
                                        Date yourDate = new Date();

                                        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("업데이트 MM/dd a hh:mm", Locale.KOREA);
                                        String date = DATE_FORMAT.format(yourDate);
                                        System.out.println(date);

                                        JSONObject currentLocationInfo = new JSONObject();
                                        int addressLatestIndex;
                                        String finalAddress;
                                        if (address.contains("동 ")) {
                                            addressLatestIndex = address.indexOf("동 ");
                                            finalAddress = address.substring(5, addressLatestIndex + 2);
                                        } else if (address.contains("면 ")) {
                                            addressLatestIndex = address.indexOf("면 ");
                                            finalAddress = address.substring(5, addressLatestIndex + 2);
                                        } else if (address.contains("읍 ")) {
                                            addressLatestIndex = address.indexOf("읍 ");
                                            finalAddress = address.substring(5, addressLatestIndex + 2);
                                        } else {
                                            finalAddress = address;
                                        }

                                        if (finalAddress.contains("시 ")) {
                                            addressLatestIndex = finalAddress.indexOf("시 ");
                                            finalAddress = finalAddress.substring(addressLatestIndex + 2, finalAddress.length());
                                        } else if (finalAddress.contains("군 ")) {
                                            addressLatestIndex = finalAddress.indexOf("구 ");
                                            finalAddress = finalAddress.substring(addressLatestIndex + 2, finalAddress.length());
                                        }

                                        currentLocationInfo.put("text", result);
                                        currentLocationInfo.put("grade", 미세먼지등급);
                                        currentLocationInfo.put("address", finalAddress);
                                        currentLocationInfo.put("currentDateTimeString", date);

                                        Message message = uiHandler.obtainMessage();
                                        message.what = 1; // 후처리 번호
                                        message.obj = currentLocationInfo; // 전달할 데이터
                                        uiHandler.sendMessage(message);
                                        //GRADE 값 : 1 좋음 / 2 보통 / 3 나쁨 / 4 매우나쁨

                                        //주소, //업데이트 시간, //미세먼지등급텍스트, //미세먼지등급이모티콘
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
                    try {
                        setData((JSONObject) msg.obj); // data binding
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public boolean fileExist(String fileName) {
        File file = getBaseContext().getFileStreamPath(fileName);
        return file.exists();
    }

    private void setData(final JSONObject currentLocationInfo) throws IOException {
        listItem.add(currentLocationInfo);

        FileInputStream fileInputStream = this.openFileInput(saveFileName);
        byte[] buffer = new byte[1024];
        StringBuilder sb = new StringBuilder();
        int len = 0;
        while ((len = fileInputStream.read(buffer)) > 0) {
            sb.append(new String(buffer, 0, len));
        }
        fileInputStream.close();
        JSONObject jsonObject;
        JSONArray spotList = null;
        try {
            jsonObject = new JSONObject(String.valueOf(sb));
            spotList = new JSONArray(jsonObject.get("saveSpotList").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < spotList.length(); i++) {
            try {
                listItem.add((JSONObject) spotList.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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