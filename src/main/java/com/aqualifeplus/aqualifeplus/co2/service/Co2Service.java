package com.aqualifeplus.aqualifeplus.co2.service;

import com.aqualifeplus.aqualifeplus.co2.dto.Co2RequestDto;
import com.aqualifeplus.aqualifeplus.co2.dto.Co2ResponseDto;
import com.aqualifeplus.aqualifeplus.co2.dto.Co2SuccessDto;
import com.aqualifeplus.aqualifeplus.co2.dto.DeleteCo2SuccessDto;
import java.util.List;

public interface Co2Service {
    List<Co2ResponseDto> co2ReserveList();

    Co2ResponseDto co2Reserve(Long idx);

    Co2SuccessDto co2CreateReserve(Co2RequestDto co2RequestDto);

    Co2SuccessDto co2UpdateReserve(Long idx, Co2RequestDto co2RequestDto);

    DeleteCo2SuccessDto co2DeleteReserve(Long idx);
}
