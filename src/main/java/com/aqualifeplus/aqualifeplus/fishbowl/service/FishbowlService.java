package com.aqualifeplus.aqualifeplus.fishbowl.service;

import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.ConnectDto;
import com.aqualifeplus.aqualifeplus.users.dto.SuccessDto;

public interface FishbowlService {
    ConnectDto connect();

    SuccessDto createFishbowlName(String name);

    SuccessDto updateFishbowlName(String name);
}
