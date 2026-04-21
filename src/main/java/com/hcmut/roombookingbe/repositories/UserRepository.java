package com.hcmut.roombookingbe.repositories;

import com.hcmut.roombookingbe.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
