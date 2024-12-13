package com.aqualifeplus.aqualifeplus.filter.service;

import com.aqualifeplus.aqualifeplus.auth.jwt.JwtService;
import com.aqualifeplus.aqualifeplus.common.exception.CustomException;
import com.aqualifeplus.aqualifeplus.common.exception.ErrorCode;
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
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FilterServiceImpl implements FilterService {
    private final FilterRepository filterRepository;
    private final FirebaseConfig firebaseConfig;
    private final UsersRepository usersRepository;
    private final FishbowlRepository fishbowlRepository;
    private final JwtService jwtService;
    private final FirebaseHttpRepository firebaseHttpRepository;


    @Override
    public FilterResponseDto getFilter() {
        Users users = usersRepository.findByEmail(jwtService.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
        Fishbowl fishbowl =
                fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        Filter filter = filterRepository.findByUsersAndFishbowl(users, fishbowl)
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
        Fishbowl fishbowl =
                fishbowlRepository.findByFishbowlIdAndUsers(jwtService.getFishbowlToken(), users)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FISHBOWL_ID_USE_THIS_USER_ID));
        Filter filter = filterRepository.findByUsersAndFishbowl(users, fishbowl)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FILTER));
        filter.setFilterDay(filterRequestDto.getFilterDay());
        filter.setFilterRange(filterRequestDto.getFilterRange());
        filter.setFilterTime(filterRequestDto.getFilterTime());

        String url = users.getUserId() + "/" + fishbowl.getFishbowlId() + "/" + "filterData";

        Map<String, Object> maps = new HashMap<>();
        maps.put("filterRange", filterRequestDto.getFilterRange());

        firebaseHttpRepository.updateFirebaseData(maps, url, accessToken);

        return UpdateFilterResponseDto.builder()
                .success(true)
                .build();
    }
}
