package com.aqualifeplus.aqualifeplus.fishbowl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectDto {
    private boolean success;
    private String fishbowlId;
}
