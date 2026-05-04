package com.hcmut.roombookingbe.repositories;

import com.hcmut.roombookingbe.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    boolean existsByEmail(String email);

    @Query(value = "SELECT * FROM users WHERE " +
           "id != :excludeId AND " +
           "(:role IS NULL OR role = :role) AND " +
           "(:status IS NULL OR status = :status) AND " +
           "(:keyword IS NULL OR LOWER(full_name) LIKE LOWER('%' || CAST(:keyword AS VARCHAR) || '%') " +
           "OR LOWER(email) LIKE LOWER('%' || CAST(:keyword AS VARCHAR) || '%'))",
           nativeQuery = true)
    List<User> findUsersWithFilters(
            @Param("excludeId") Long excludeId,
            @Param("role") String role,
            @Param("status") String status,
            @Param("keyword") String keyword
    );
}
