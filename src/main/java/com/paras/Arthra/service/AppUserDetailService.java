package com.paras.Arthra.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.paras.Arthra.entity.ProfileEntity;
import com.paras.Arthra.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailService implements UserDetailsService {
    private final ProfileRepository profileRepository;

    //AppUserDetailService.loadUserByUsername() method returns is what gets stored in the SecurityContext.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ProfileEntity existingProfile=profileRepository.findByEmail(email)
            .orElseThrow(()->new UsernameNotFoundException("Profile not found with email "+email));

        return User.builder().  
                    username(existingProfile.getEmail())
                    .password(existingProfile.getPassword())
                    .authorities(Collections.emptyList())
                    .build();
    }
    
}
