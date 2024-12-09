package com.aqualifeplus.aqualifeplus.fishbowl.repository;

import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FishbowlRepository extends JpaRepository<Fishbowl, Long> {
    void deleteByFishbowlIdIn(List<String> deleteFishbowlList);

    Optional<Fishbowl> findByFishbowlIdAndUsers(String fishbowlToken, Users users);
}
