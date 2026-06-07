package com.unyt.gym.repository;

import com.unyt.gym.entity.PTPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PTPackageRepository extends JpaRepository<PTPackage, Long> {
    List<PTPackage> findByActiveTrue();
}
