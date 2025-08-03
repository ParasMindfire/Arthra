package com.paras.Arthra.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.paras.Arthra.dto.CategoryDTO;
import com.paras.Arthra.entity.CategoryEntity;
import com.paras.Arthra.entity.ProfileEntity;
import com.paras.Arthra.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;


    public CategoryDTO saveCategory(CategoryDTO categoryDTO){
        ProfileEntity profile=profileService.getCurrentProfile();
        if(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())){
            throw new RuntimeException("Category with this name already exist");
        }

        CategoryEntity newCategory=toEntity(categoryDTO, profile);
        newCategory=categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    public List<CategoryDTO> getCategoriesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity>categories=categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type){
        ProfileEntity profile=profileService.getCurrentProfile();
        List<CategoryEntity>entities=categoryRepository.findByTypeAndProfileId(type,profile.getId());
        return entities.stream().map(this::toDTO).toList();
    }

    public CategoryDTO updateCategory(Long categoryId,CategoryDTO dto){
        ProfileEntity profile=profileService.getCurrentProfile();
        CategoryEntity existingCategory=categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                                        .orElseThrow(()->new RuntimeException("Category not found or not accessable"));
        existingCategory.setName(dto.getName());
        existingCategory.setIcon(dto.getIcon());
        existingCategory=categoryRepository.save(existingCategory);
        return toDTO(existingCategory);
    }















    
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType())
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .profileId(entity.getProfile() != null ?  entity.getProfile().getId(): null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updateddAt(entity.getUpdateddAt())
                .type(entity.getType())
                .build();

    }
}
