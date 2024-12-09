package com.aqualifeplus.aqualifeplus.co2.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Co2SuccessDto {
    private boolean success;
    private Co2ResponseDto co2ResponseDto;
}
