package com.gurula.stockMate.oauth;

import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberService;
import com.gurula.stockMate.config.ConfigProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Controller
public class Oauth2Controller {
    private final ConfigProperties configProperties;
    private final MemberService memberService;
    private final RestTemplate restTemplate;
    private final JwtTool jwtTool;

    public Oauth2Controller(ConfigProperties configProperties, MemberService memberService, RestTemplate restTemplate, JwtTool jwtTool) {
        this.configProperties = configProperties;
        this.memberService = memberService;
        this.restTemplate = restTemplate;
        this.jwtTool = jwtTool;
    }


    @GetMapping("/callback")
    public String handleOAuth2Callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response
    ) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "grant_type=authorization_code" +
                "&code=" + code +
                "&redirect_uri=" + configProperties.getGlobalDomain() + "callback" +
                "&client_id=" + configProperties.getClientId() +
                "&client_secret=" + configProperties.getClientSecret();

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                configProperties.getTokenUri(),
                HttpMethod.POST,
                requestEntity,
                Map.class);

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userRequestEntity = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                configProperties.getUserInfoUri(),
                HttpMethod.GET,
                userRequestEntity,
                Map.class);

        Map<String, Object> userInfo = userResponse.getBody();

        String userId = (String) userInfo.get("userId");
        String userName = (String) userInfo.get("displayName");
        final String pictureUrl = (String) userInfo.get("pictureUrl");

        Optional<Member> memberOpt = memberService.findByUserId(userId);
        Member member;
        if (memberOpt.isEmpty()) {
            member = new Member();
            member.setName(userName);
            member.setPictureUrl(pictureUrl);
            member.setUserId(userId);
            member.setRole(Role.USER);
            memberService.save(member);
        } else {
            member = memberOpt.get();
        }

        // 製作 JWT token
        final String token = jwtTool.createToken(member.getId(), Duration.ofDays(365));
        System.out.println("token = " + token);

        // 設定 Cookie
        ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", token)
                .secure(true)
                .path("/")
                .maxAge(30790400)   // 365天
                .sameSite("Strict")
                .build();

        response.setHeader("Set-Cookie", cookie.toString());

        return "redirect:" + configProperties.getGlobalDomain() + "index.html";
    }
}
