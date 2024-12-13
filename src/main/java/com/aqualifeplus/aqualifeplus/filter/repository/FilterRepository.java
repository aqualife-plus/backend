package com.aqualifeplus.aqualifeplus.filter.repository;

import com.aqualifeplus.aqualifeplus.filter.entity.Filter;
import com.aqualifeplus.aqualifeplus.fishbowl.entity.Fishbowl;
import com.aqualifeplus.aqualifeplus.users.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterRepository extends JpaRepository<Filter, Long> {
    Optional<Filter> findByUsersAndFishbowl(Users users, Fishbowl fishbowl);

    void deleteAllByFishbowlIn(List<Fishbowl> fishbowlList);

    void deleteByUsersAndFishbowl(Users users, Fishbowl fishbowl);
}
