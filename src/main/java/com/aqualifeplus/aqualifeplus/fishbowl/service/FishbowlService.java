package com.aqualifeplus.aqualifeplus.fishbowl.service;

import com.aqualifeplus.aqualifeplus.fishbowl.dto.local.ConnectDto;

public interface FishbowlService {
    ConnectDto connect();

    boolean nameSet(String name);
}
