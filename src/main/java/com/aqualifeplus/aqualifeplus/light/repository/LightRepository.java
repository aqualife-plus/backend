package com.aqualifeplus.aqualifeplus.light.repository;

import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.light.entity.Light;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LightRepository extends JpaRepository<Light, Long> {
    List<Light> findAllByFishbowlAndUsers(Fishbowl fishbowl, Users users);
    Optional<Light> findByIdAndUsers(Long id, Users users);

    void deleteByIdAndUsers(long id, Users users);

    void deleteAllByFishbowlIn(List<Fishbowl> fishbowlList);

    void deleteAllByFishbowl(Fishbowl fishbowl);
}
