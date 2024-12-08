package com.aqualifeplus.aqualifeplus.fishbowl.service;

import com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase.Co2;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.CreateSuccessDto;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.DeleteSuccessDto;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.UpdateSuccessDto;
import java.util.List;
import java.util.Map;

public interface Co2Service {
    List<Map<String, Co2>> co2ReserveList();

    Co2 co2Reserve(String idx);

    CreateSuccessDto co2CreateReserve(Co2 co2);

    UpdateSuccessDto co2UpdateReserve(String idx, Co2 co2);

    DeleteSuccessDto co2DeleteReserve(String idx);
}
