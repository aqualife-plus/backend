package com.aqualifeplus.aqualifeplus.auth.entity;

import com.aqualifeplus.aqualifeplus.common.enum_type.LoginPlatform;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class CustomOAuthUsers implements OAuth2User {
    private final OAuth2User oAuth2User;
    private final Map<String, Object> additionalAttributes;

    @Override
    public Map<String, Object> getAttributes() {
        // 기존 속성과 추가 속성을 병합하여 반환
        Map<String, Object> combinedAttributes = new HashMap<>(oAuth2User.getAttributes());
        combinedAttributes.putAll(additionalAttributes);
        return combinedAttributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return "test";
    }

    public String getEmail() {
        return (String) getAttributes().get("email");
    }
    public String getPassword() {
        return (String) getAttributes().get("password");
    }
    public String getNickname() {
        return (String) getAttributes().get("nickname");
    }
    public String getPhoneNumber() {
        return (String) getAttributes().get("phoneNumber");
    }
    public LoginPlatform getAccountType() {
        return (LoginPlatform) getAttributes().get("accountType");
    }
}
