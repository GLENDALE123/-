package com.hr.airpollution.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * APIConnector를 통하여 외부 API를 호출할 수 있다.
 */
public class APIConnector {

    /**
     * 측정소별 실시간 측정정보 조회
     *
     * @return
     * @throws IOException
     */
    public static String getMsrstnAcctoRltmMesureDnsty(String stationName) throws IOException {
        HttpURLConnection conn;

        StringBuilder urlBuilder = new StringBuilder("http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=vWrQTRp%2BZF8PWf6%2Fi3p6XjJJHJNmwnRzwW2gXYw5b5EI%2FCI58nwenZ%2FRYz%2FDEyjjC2IkZ%2BtTvdechGz%2FCJeHqg%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
        urlBuilder.append("&" + URLEncoder.encode("dataTerm", "UTF-8") + "=" + URLEncoder.encode("DAILY", "UTF-8")); /*페이지 번호*/
        urlBuilder.append("&" + URLEncoder.encode("stationName", "UTF-8") + "=" + URLEncoder.encode(stationName, "UTF-8")); /*시도 이름 (서울, 부산, 대구, 인천, 광주, 대전, 울산, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주, 세종)*/
        urlBuilder.append("&" + URLEncoder.encode("ver", "UTF-8") + "=" + URLEncoder.encode("1.3", "UTF-8")); /*요청 데이터기간 (시간 : HOUR, 하루 : DAILY)*/
        urlBuilder.append("&" + URLEncoder.encode("_returnType", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*요청 데이터기간 (시간 : HOUR, 하루 : DAILY)*/

        URL url = new URL(urlBuilder.toString());
        conn = (HttpURLConnection) url.openConnection();

        return getData(conn, url);
    }

    /**
     * 시도별 실시간 측정정보 조회
     *
     * @return
     * @throws IOException
     */
    private String getCtprvnRltmMesureDnsty(String sidoName) throws IOException {
        HttpURLConnection conn;

        StringBuilder urlBuilder = new StringBuilder("http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=vWrQTRp%2BZF8PWf6%2Fi3p6XjJJHJNmwnRzwW2gXYw5b5EI%2FCI58nwenZ%2FRYz%2FDEyjjC2IkZ%2BtTvdechGz%2FCJeHqg%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("pageSize", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*페이지 번호*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
        urlBuilder.append("&" + URLEncoder.encode("startPage", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
        urlBuilder.append("&" + URLEncoder.encode("sidoName", "UTF-8") + "=" + URLEncoder.encode(sidoName, "UTF-8")); /*시도 이름 (서울, 부산, 대구, 인천, 광주, 대전, 울산, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주, 세종)*/
        urlBuilder.append("&" + URLEncoder.encode("ver", "UTF-8") + "=" + URLEncoder.encode("1.3", "UTF-8")); /*요청 데이터기간 (시간 : HOUR, 하루 : DAILY)*/
        urlBuilder.append("&" + URLEncoder.encode("_returnType", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*요청 데이터기간 (시간 : HOUR, 하루 : DAILY)*/

        URL url = new URL(urlBuilder.toString());
        conn = (HttpURLConnection) url.openConnection();

        return getData(conn, url);
    }

    /**
     * 근접측정소 목록 조회
     *
     * @param gpsX
     * @param gpsY
     * @return
     * @throws IOException
     */
    public static String getNearbyMsrstnList(double gpsX, double gpsY) throws IOException {
        HttpURLConnection conn;

        StringBuilder urlBuilder = new StringBuilder("http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=vWrQTRp%2BZF8PWf6%2Fi3p6XjJJHJNmwnRzwW2gXYw5b5EI%2FCI58nwenZ%2FRYz%2FDEyjjC2IkZ%2BtTvdechGz%2FCJeHqg%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("tmX", "UTF-8") + "=" + gpsX); /*GPS를 통해 찾은 경도*/
        urlBuilder.append("&" + URLEncoder.encode("tmY", "UTF-8") + "=" + gpsY); /*GPS를 통해 찾은 위도*/
        urlBuilder.append("&" + URLEncoder.encode("_returnType", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*요청 데이터기간 (시간 : HOUR, 하루 : DAILY)*/

        URL url = new URL(urlBuilder.toString());
        conn = (HttpURLConnection) url.openConnection();

        return getData(conn, url);
    }

    /**
     * 해당 URL과 쿼리스트링을 토대로 데이터를 가져온다.
     *
     * @param conn
     * @param url
     * @return
     * @throws IOException
     */
    public static String getData(HttpURLConnection conn, URL url) throws IOException {
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        return sb.toString();
    }
}
