package com.aqualifeplus.aqualifeplus.firebase.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {
    private boolean warning;
    private boolean onOff;

    public static Device startDeviceData() {
        return Device.builder()
                .warning(false)
                .onOff(false)
                .build();
    }
}
