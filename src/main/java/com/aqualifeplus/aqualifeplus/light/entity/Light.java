package com.aqualifeplus.aqualifeplus.light.entity;

import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Light {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Users users;
    @ManyToOne
    private Fishbowl fishbowl;

    private boolean lightReserveState;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime lightStartTime; // Format hh:mm
    @JsonFormat(pattern = "HH:mm")
    private LocalTime lightEndTime;   // Format hh:mm
}