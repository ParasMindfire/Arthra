package com.paras.Arthra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paras.Arthra.entity.ProfileEntity;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
    //The method will return an Optional containing the ProfileEntity if a profile with the given email exists in the database.If no such profile is found, it returns an empty Optional (not null)
    Optional<ProfileEntity>findByEmail(String email);
    Optional<ProfileEntity>findByActivationToken(String activationToken);
}
