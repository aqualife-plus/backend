package com.aqualifeplus.aqualifeplus.filter;

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
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Filter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Users users;
    private String filterDay; // "7x4" or "0/1"
    private int filterRange;  // Range 1-4
    @JsonFormat(pattern = "HH:mm")
    private LocalTime filterTime; // Format hh:mm
}
