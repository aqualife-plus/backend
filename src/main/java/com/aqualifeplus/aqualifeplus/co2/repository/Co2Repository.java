package com.aqualifeplus.aqualifeplus.co2.repository;

import com.aqualifeplus.aqualifeplus.co2.entity.Co2;
import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Co2Repository extends JpaRepository<Co2, Long> {
    List<Co2> findAllByFishbowlAndUsers(Fishbowl fishbowl, Users users);
    Optional<Co2> findByIdAndUsers(Long id, Users users);

    void deleteByIdAndUsers(long id, Users users);
}
