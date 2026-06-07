package com.unyt.gym.repository;

import com.unyt.gym.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findByRole(String role);
    List<Staff> findByRoleAndStatus(String role, String status);
    Optional<Staff> findByEmail(String email);
}
