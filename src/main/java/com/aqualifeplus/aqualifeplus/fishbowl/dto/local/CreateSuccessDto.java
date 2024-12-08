package com.aqualifeplus.aqualifeplus.fishbowl.dto.local;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSuccessDto {
    private boolean success;
    private String reserveId;
}
