package com.aqualifeplus.aqualifeplus.repository;

import com.aqualifeplus.aqualifeplus.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

}
