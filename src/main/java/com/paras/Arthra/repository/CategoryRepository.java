package com.paras.Arthra.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paras.Arthra.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity,Long>{
    List<CategoryEntity>findByProfileId(Long profileId);
    Optional<CategoryEntity>findByIdAndProfileId(Long id,Long profileId);
    List<CategoryEntity>findByTypeAndProfileId(String type,Long profile);
    Boolean existsByNameAndProfileId(String name,Long profileId);
}
