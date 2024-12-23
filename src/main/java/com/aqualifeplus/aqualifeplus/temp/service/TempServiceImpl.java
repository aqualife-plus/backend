package com.aqualifeplus.aqualifeplus.temp.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.config.FirebaseConfig;
import com.aqualifeplus.aqualifeplus.firebase.repository.FirebaseHttpRepository;
import com.aqualifeplus.aqualifeplus.ph.dto.UpdatePhLimitResponseDto;
import com.aqualifeplus.aqualifeplus.temp.dto.UpdateTempLimitRequestDto;
import com.aqualifeplus.aqualifeplus.temp.dto.UpdateTempLimitResponseDto;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TempServiceImpl implements TempService{
    private final FirebaseConfig firebaseConfig;
    private final UsersRepository usersRepository;
    private final JwtService jwtService;
    private final FirebaseHttpRepository firebaseHttpRepository;

    @Override
    public UpdateTempLimitResponseDto updateTempLimit(UpdateTempLimitRequestDto updateTempLimitRequestDto) {
        String accessToken = firebaseConfig.getAccessToken();
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        String fishbowlId = jwtService.getFishbowlToken();

        if (fishbowlId == null) {
            throw new CustomException(ErrorCode.NULL_ERROR_FISHBOWL_TOKEN);
        }

        String url = users.getUserId() + "/" + fishbowlId + "/" + "temp";
        Map<String, Double> settingTempLimitMaps = new HashMap<>();
        settingTempLimitMaps.put("tempStay",
                updateTempLimitRequestDto.getTempStay());

        firebaseHttpRepository.updateFirebaseData(
                settingTempLimitMaps, url, accessToken);

        return UpdateTempLimitResponseDto.builder()
                .success(true)
                .tempStay(updateTempLimitRequestDto.getTempStay())
                .build();
    }
}
