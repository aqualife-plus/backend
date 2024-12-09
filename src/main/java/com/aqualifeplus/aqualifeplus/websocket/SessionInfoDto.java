package com.aqualifeplus.aqualifeplus.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionInfoDto {
    private Long userId;
    private String fishbowlId;
}
