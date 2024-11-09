package com.aqualifeplus.aqualifeplus.service;

import com.aqualifeplus.aqualifeplus.entity.CustomOAuthUsers;
import com.aqualifeplus.aqualifeplus.enum_type.LoginPlatform;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class CustomOAuthUserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributeMap = new HashMap<>();

        if (registrationId.equals("naver")) {
            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            attributeMap.put("email", response.get("id"));
            attributeMap.put("password", response.get("id"));
            attributeMap.put("nickname", response.get("name"));
            attributeMap.put("phoneNumber", ((String)response.get("mobile")).replace("-", ""));
            attributeMap.put("accountType", LoginPlatform.NAVER);
        } else if (registrationId.equals("google")) {
            attributeMap.put("email", oAuth2User.getAttribute("sub"));
            attributeMap.put("password", oAuth2User.getAttribute("sub"));
            attributeMap.put("nickname", oAuth2User.getAttribute("name"));
            attributeMap.put("phoneNumber", null);
            attributeMap.put("accountType", LoginPlatform.GOOGLE);
        }

        return new CustomOAuthUsers(oAuth2User, attributeMap);
    }
}

