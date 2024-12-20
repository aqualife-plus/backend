package com.aqualifeplus.aqualifeplus.filter.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
import com.aqualifeplus.aqualifeplus.common.redis.RedisService;
import com.aqualifeplus.aqualifeplus.config.FirebaseConfig;
import com.aqualifeplus.aqualifeplus.filter.dto.FilterRequestDto;
import com.aqualifeplus.aqualifeplus.filter.dto.FilterResponseDto;
import com.aqualifeplus.aqualifeplus.filter.dto.UpdateFilterResponseDto;
import com.aqualifeplus.aqualifeplus.filter.entity.Filter;
import com.aqualifeplus.aqualifeplus.filter.repository.FilterRepository;
import com.aqualifeplus.aqualifeplus.firebase.repository.FirebaseHttpRepository;
import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.fishbowl.repository.FishbowlRepository;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.aqualifeplus.aqualifeplus.users.repository.UsersRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FilterServiceImpl implements FilterService {
    private static final int ADAY = 60 * 60 * 24;

    private final JwtService jwtService;
    private final UsersRepository usersRepository;
    private final FilterRepository filterRepository;
    private final FishbowlRepository fishbowlRepository;

    private final FirebaseConfig firebaseConfig;
    private final FirebaseHttpRepository firebaseHttpRepository;
    private final RedisService redisService;
    private final RedisTemplate<String, String> redisTemplateForFishbowlSettings;

    @Override
    public FilterResponseDto getFilter() {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        Filter filter = filterRepository.findByFishbowl(fishbowl)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FILTER));

        return FilterResponseDto.builder()
                .filterDay(filter.getFilterDay())
                .filterRange(filter.getFilterRange())
                .filterTime(filter.getFilterTime())
                .build();
    }

    @Override
    @Transactional
    public UpdateFilterResponseDto updateFilter(FilterRequestDto filterRequestDto) {
        String accessToken = firebaseConfig.getAccessToken();
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl = fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));

        Filter filter = filterRepository.findByFishbowl(fishbowl)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FILTER));
        filter.setFilterDay(filterRequestDto.getFilterDay());
        filter.setFilterRange(filterRequestDto.getFilterRange());
        filter.setFilterTime(filterRequestDto.getFilterTime());

        String url = users.getUserId() + "/" + fishbowl.getFishbowlId() + "/" + "filterData";

        Map<String, Object> maps = new HashMap<>();
        maps.put("filterRange", filterRequestDto.getFilterRange());

        firebaseHttpRepository.updateFirebaseData(maps, url, accessToken);

        //해당 값으로 되어 있는 redis 데이터 다 삭제
        redisService.deleteReserveUsePatternInRedis(
                redisTemplateForFishbowlSettings,
                users.getUserId() + "/" + fishbowl.getFishbowlId() + "/filter/*/*");

        // 그 다음에 뺀 값을 절댓값으로 감싼다
        List<Integer> weekDayList = new ArrayList<>();
        int weekDayIntegerValue = LocalDateTime.now().getDayOfWeek().getValue();
        for (int i = 0; i < 7; i++) {
            if (filter.getFilterDay().charAt(i) == '1') {
                weekDayList.add(i - weekDayIntegerValue >= 0 ?
                        i - weekDayIntegerValue :
                        i - weekDayIntegerValue + 7);
            }
        }
        // 그리고 현재 시간을 기준으로 만료시간을 설정
        for (int weekDay : weekDayList) {
            String key =
                    redisService.makeKey(
                            users,
                            fishbowl,
                            filter.getId(), "filter",
                            LocalDateTime.now().plusDays(weekDay).getDayOfWeek().toString());

            redisService.saveData(redisTemplateForFishbowlSettings,
                    key, "",
                    redisService.getExpirationTime(filter.getFilterTime(), LocalTime.now()) + ((long) weekDay * ADAY),
                    TimeUnit.SECONDS);
        }

        return UpdateFilterResponseDto.builder()
                .success(true)
                .build();
    }
}
