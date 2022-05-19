package com.sns_practice.sns_practice._01_Controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

@Controller
public class loginKakaoController {


    //카카오 로그인
    @GetMapping(value = "/login/getKakaoAuthUrl")
    @ResponseBody
    public String getKakaoAuthUrl() throws Exception {
        String reqUrl =
                "https://kauth.kakao.com/oauth/authorize"
                        + "?client_id=5719a074f0e031b6a83b69dd3777f748"
                        + "&redirect_uri=http://localhost:8181/login/oauth_kakao"
                        + "&response_type=code";

        return reqUrl;
    }

    //카카오 로그아웃
    @GetMapping(value = "/logout/kakao")
    @ResponseBody
    public String kakaoLogout() throws Exception {
        String reqUrl ="https://kauth.kakao.com/oauth/logout" +
                        "?client_id=5719a074f0e031b6a83b69dd3777f748" +
                        "&logout_redirect_uri=http://localhost:8181/";
        return reqUrl;
    }
//

    // 카카오 연동정보 조회
    @GetMapping(value = "/login/oauth_kakao")
    public String oauthKakao(
            @RequestParam(value = "code", required = false) String code
            , Model model, HttpSession session) throws Exception {

        System.out.println("여기!");

        System.out.println("#########" + code);
        String access_Token = getAccessToken(code);
        System.out.println("###access_Token#### : " + access_Token);

        HashMap<String, Object> userInfo = getUserInfo(access_Token);
        System.out.println("###access_Token#### : " + access_Token);
        System.out.println("###userInfo#### : " + userInfo.get("email"));
        System.out.println("###nickname#### : " + userInfo.get("nickname"));
        System.out.println("userInfo=>"+userInfo);
//        JSONObject kakaoInfo =  new JSONObject(userInfo);
        JsonParser parser = new JsonParser();
//        JsonElement kakaoInfo = parser.parse(String.valueOf(userInfo));
        session.setAttribute("user", userInfo.get("id"));
        model.addAttribute("info",userInfo);
        System.out.println("------>>>"+model.getAttribute("info"));
        return "/home"; //본인 원하는 경로 설정
    }


    //카카오 연결 끊기
    @GetMapping(value = "/disconnect/kakao")
    public String disconnetKakao(String accessToken){

        String reqURL = "https://kapi.kakao.com";

        try {
            URL url = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //  URL연결은 입출력에 사용 될 수 있고, POST 혹은 PUT 요청을 하려면 setDoOutput을 true로 설정해야함.
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            //	POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("Authorization=Bearer "+accessToken);
            bw.write(sb.toString());
            bw.flush();
            //    결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            //    요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            //    Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            JsonElement element = parser.parse(result);


            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "/index";
    }


   //토큰발급
    public String getAccessToken (String authorize_code) {
        String access_Token = "";
        String refresh_Token = "";
        String reqURL = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //  URL연결은 입출력에 사용 될 수 있고, POST 혹은 PUT 요청을 하려면 setDoOutput을 true로 설정해야함.
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            //	POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=5719a074f0e031b6a83b69dd3777f748");  //본인이 발급받은 key
            sb.append("&redirect_uri=http://localhost:8181/login/oauth_kakao");     // 본인이 설정해 놓은 경로
            sb.append("&code=" + authorize_code);
            bw.write(sb.toString());
            bw.flush();

            //    결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            //    요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            //    Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            JsonElement element = parser.parse(result);

            access_Token = element.getAsJsonObject().get("access_token").getAsString();
            refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();

            System.out.println("access_token : " + access_Token);
            System.out.println("refresh_token : " + refresh_Token);

            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return access_Token;
    }

//    //유저정보조회
    public HashMap<String, Object> getUserInfo (String access_Token) {

        //    요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언
        HashMap<String, Object> userInfo = new HashMap<String, Object>();
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            //    요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty("Authorization", "Bearer " + access_Token);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            JsonElement element = parser.parse(result);

            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();


            // 자료는 넘어오는거 sout 으로 확인하셔서 알아서 담아가시면 됩니다.
            String nickname = properties.getAsJsonObject().get("nickname").getAsString();
//            String email = kakao_account.getAsJsonObject().get("email").getAsString();
            userInfo.put("accessToken", access_Token);
            userInfo.put("nickname", nickname);
            userInfo.put("snsType", "kakao");
            userInfo.put("id", element.getAsJsonObject().get("id"));
            userInfo.put("lastLoc", element.getAsJsonObject().get("connected_at"));
            userInfo.put("profileImage", properties.getAsJsonObject().get("profile_image").getAsString());

//            userInfo.put("email", email);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return userInfo;
    }
//



}
