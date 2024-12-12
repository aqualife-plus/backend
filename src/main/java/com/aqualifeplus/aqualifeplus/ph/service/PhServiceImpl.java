package com.aqualifeplus.aqualifeplus.ph.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.config.FirebaseConfig;
import com.aqualifeplus.aqualifeplus.firebase.repository.FirebaseHttpRepository;
import com.aqualifeplus.aqualifeplus.ph.dto.UpdatePhLimitRequestDto;
import com.aqualifeplus.aqualifeplus.ph.dto.UpdatePhLimitResponseDto;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhServiceImpl implements PhService{
    private final JwtService jwtService;
    private final FirebaseConfig firebaseConfig;
    private final UsersRepository usersRepository;
    private final FirebaseHttpRepository firebaseHttpRepository;

    @Override
    public UpdatePhLimitResponseDto updatePhLimit(UpdatePhLimitRequestDto updatePhLimitRequestDto) {
        String accessToken = firebaseConfig.getAccessToken();
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        String fishbowlId = jwtService.getFishbowlToken();

        String url = users.getUserId() + "/" + fishbowlId + "/" + "ph";
        Map<String, Double> settingPhLimitMaps = new HashMap<>();
        settingPhLimitMaps.put("warningMaxPh",
                updatePhLimitRequestDto.getWarningMaxPh());
        settingPhLimitMaps.put("warningMinPh",
                updatePhLimitRequestDto.getWarningMinPh());

        firebaseHttpRepository.updateFirebaseData(
                settingPhLimitMaps, url, accessToken);

        return UpdatePhLimitResponseDto.builder()
                .success(true)
                .warningMaxPh(updatePhLimitRequestDto.getWarningMaxPh())
                .warningMinPh(updatePhLimitRequestDto.getWarningMinPh())
                .build();
    }
}
