package com.sns_practice.sns_practice._01_Controller;

import com.sns_practice.sns_practice._99_Utils.GoogleOAuthRequest;
import net.minidev.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;

@Controller
public class loginGoogleController {



    //구글 로그인
    @GetMapping(value = "login/getGoogleAuthUrl")
    @ResponseBody
    public String getGoogleAuthUrl() throws Exception {
        String reqUrl = "https://accounts.google.com" +
                "/o/oauth2/v2/auth?client_id=" +
                "482544352642-bcm2rk88c7bt103ji8mofi7br0evoaat.apps.googleusercontent.com" +
                "&redirect_uri=" +
                "http://localhost:8181/oauth_google" +
                "&response_type=code&scope=email%20profile%20openid&access_type=offline";
        return reqUrl;
    }
//
    // 구글 연동정보 조회
    @RequestMapping(value = "/oauth_google")
    public String oauth_google(@RequestParam(value = "code") String authCode,
                               Model model, HttpSession session) throws Exception {

        // restTemplate 호출
        RestTemplate restTemplate = new RestTemplate();
        GoogleOAuthRequest googleOAuthRequestParam = GoogleOAuthRequest.builder()
                .clientId("482544352642-bcm2rk88c7bt103ji8mofi7br0evoaat.apps.googleusercontent.com")
                .clientSecret("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")
                .code(authCode)
                .redirectUri("http://localhost:8181" + "/oauth_google")
                .grantType("authorization_code")
                .build();

        ResponseEntity<JSONObject> apiResponse = restTemplate.postForEntity("https://oauth2.googleapis.com" + "/token", googleOAuthRequestParam, JSONObject.class);
        JSONObject responseBody = apiResponse.getBody();

        //   id_token은 jwt 형식
        String jwtToken = responseBody.getAsString("id_token");
        String requestUrl = UriComponentsBuilder.fromHttpUrl("https://oauth2.googleapis.com" + "/tokeninfo").queryParam("id_token", jwtToken).toUriString();

        JSONObject resultJson = restTemplate.getForObject(requestUrl, JSONObject.class);

        // 구글 정보조회 성공
        if (resultJson != null) {

            //  회원 고유 식별자
            String googleUniqueNo = resultJson.getAsString("sub");


            /**

             TO DO : 리턴받은 googleUniqueNo 해당하는 회원정보 조회 후 로그인 처리 후 메인으로 이동

             */
            HashMap<String,Object> info = new HashMap<>();
            info.put("accessToken",jwtToken);
            info.put("nickname", resultJson.getAsString("name"));
            info.put("snsType","google");
            info.put("id",resultJson.getAsString("sub"));
            info.put("lastLoc",new Date());
            info.put("profileImage",resultJson.getAsString("picture"));
            session.setAttribute("user",resultJson.getAsString("sub"));
            model.addAttribute("info",info);
            System.out.println("-->"+model.getAttribute("info"));
            // 구글 정보조회 실패
        } else {
            throw new Exception("구글 정보조회에 실패했습니다.");
        }
        return "/home";
    }
}
