package com.sns_practice.sns_practice._01_Controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;

@Controller
public class loginNaverController {

    @GetMapping("/naver/oauth")
    @ResponseBody
    public String naverConnect() {
        // state용 난수 생성
        SecureRandom random = new SecureRandom();
        String state = new BigInteger(130, random).toString(32);

        String reqUrl =
                "https://nid.naver.com/oauth2.0/authorize"
                        + "?client_id=DHIKFAYck3YKv6kuKefF"
                        + "&redirect_uri=http://localhost:8181/naver/callback"
                        + "&response_type=code"
                        + "&state=" + state;
        System.out.println("???here?");
        return reqUrl;
    }

    @GetMapping("/naver/logout")
    @ResponseBody
    public String naverLogout() {

        String reqUrl = "http://nid.naver.com/nidlogin.logout?url=http://localhost:8181/";
//                        "client_id=5719a074f0e031b6a83b69dd3777f748"+
//                        "&redirect_uri=http://localhost:8181/" ;
        return reqUrl;
    }

    // 네이버 연동정보 조회
    @GetMapping(value = "/naver/callback")
    public String oauthNaver(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state") String state
            , Model model, HttpSession session) throws Exception {

        System.out.println("여기!");

        System.out.println("#########" + code);
        String access_Token = getAccessToken(code,state);
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




    public String getAccessToken(String code, String state) {
        String access_Token = "";
        String refresh_Token = "";
        String reqURL = "https://nid.naver.com/oauth2.0/token";

        try{

            System.out.println("code->"+code);
            System.out.println("state->"+state);

            URL url = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //  URL연결은 입출력에 사용 될 수 있고, POST 혹은 PUT 요청을 하려면 setDoOutput을 true로 설정해야함.
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=DHIKFAYck3YKv6kuKefF");  //본인이 발급받은 key
            sb.append("&client_secret=94_O0zM5Tb");  //본인이 발급받은 key
            sb.append("&redirect_uri=http://localhost:8181/naver/callback");     // 본인이 설정해 놓은 경로
            sb.append("&code=" + code);
            sb.append("&state=" + state);
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

    //유저정보조회
    public HashMap<String, Object> getUserInfo (String access_Token) {

        //    요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언
        HashMap<String, Object> userInfo = new HashMap<String, Object>();
        String reqURL = "https://openapi.naver.com/v1/nid/me";
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

            JsonObject naver_account = element.getAsJsonObject().get("response").getAsJsonObject();


            String nickname = naver_account.getAsJsonObject().get("nickname").getAsString();
            String email = naver_account.getAsJsonObject().get("email").getAsString();
            userInfo.put("accessToken", access_Token);
            userInfo.put("nickname", nickname);
            userInfo.put("snsType", "naver");
            userInfo.put("email", email);
            userInfo.put("lastLoc", new Date());
            userInfo.put("id", naver_account.getAsJsonObject().get("id"));
            userInfo.put("birthday", naver_account.getAsJsonObject().get("birthday"));
            userInfo.put("age", naver_account.getAsJsonObject().get("age"));
            userInfo.put("birthyear", naver_account.getAsJsonObject().get("birthyear"));
            System.out.println("여기까진 왓니?");
            userInfo.put("profileImage", naver_account.getAsJsonObject().get("profile_image").getAsString());


        } catch (IOException e) {
            e.printStackTrace();
        }

        return userInfo;
    }






}