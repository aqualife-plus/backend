package com.aqualifeplus.aqualifeplus.fishbowl.service;

import com.aqualifeplus.aqualifeplus.fishbowl.dto.firebase.Light;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.CreateSuccessDto;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.DeleteSuccessDto;
import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.UpdateSuccessDto;
import java.util.List;
import java.util.Map;

public interface LightService {
    List<Map<String, Light>> lightReserveList();

    Light lightReserve(String idx);

    CreateSuccessDto lightCreateReserve(Light light);

    UpdateSuccessDto lightUpdateReserve(String idx, Light light);

    DeleteSuccessDto lightDeleteReserve(String idx);
}
