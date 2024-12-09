package com.aqualifeplus.aqualifeplus.fishbowl.entity;

import com.aqualifeplus.aqualifeplus.users.entity.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.function.LongFunction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fishbowl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Unique
    private String fishbowlId;
    @ManyToOne
    private Users users;
}
