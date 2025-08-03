package com.paras.Arthra.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.paras.Arthra.dto.AuthDTO;
import com.paras.Arthra.dto.ProfileDTO;
import com.paras.Arthra.entity.ProfileEntity;
import com.paras.Arthra.repository.ProfileRepository;
import com.paras.Arthra.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor //Lombok annotation used in Java to automatically generate a constructor that takes as parameters all final fields and fields marked with @NonNull in your class.
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ProfileDTO registerProfile(ProfileDTO profileDTO){
        ProfileEntity newProfile=toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile=profileRepository.save(newProfile);

        String activationLink="http://localhost:8080/api/v1.0/activate?token="+newProfile.getActivationToken();
        String subject="Activate your Money Manager Account ";
        String body="Click on the following link to activate your account: "+activationLink;

        emailService.sendEmail(newProfile.getEmail(), subject, body);
        return toDTO(newProfile);
    }

    public ProfileEntity toEntity(ProfileDTO profileDTO){
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity){
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken){
        return profileRepository.findByActivationToken(activationToken).map((profile) -> {
            profile.setIsActive(true);
            profileRepository.save(profile);
            return true;
        }).orElse(false);
    }

    public boolean isAccountActive(String email){
        return profileRepository.findByEmail(email).map(ProfileEntity::getIsActive).orElse(false);
    }

    public ProfileEntity getCurrentProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName()).orElseThrow(()->new UsernameNotFoundException("Profile not found with email: "+authentication.getName()));
    }
    
    public ProfileDTO getPublicProfile(String email){
        ProfileEntity currentUser=null;
        if(email==null){
            currentUser=getCurrentProfile();
        }else{
            currentUser=profileRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("Profile not found with email: "+email));
        }

        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }


    
    public Map<String,Object>authenticateAndGenerateToken(AuthDTO authDTO){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authDTO.getEmail(), // paras@gmail.com
                authDTO.getPassword())); // Paras@123

            /*
                Step-by-Step Spring Security Authentication Flow (with details on principal, credentials, and token fields):

                1. The client sends a login request (usually via an API endpoint, like /login) with the user's email and password.

                2. In your service or controller, authentication is initiated using:
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword())
                );

                3. A UsernamePasswordAuthenticationToken is created, where:
                - principal: set to the user's email address (e.g., "paras@gmail.com").
                - credentials: set to the user's raw password input (e.g., "Paras@123").
                - authenticated: set to false (the token only carries credentials at this stage, but does NOT represent an authenticated user).

                4. The authenticationManager (specifically, your ProviderManager instance, configured to use DaoAuthenticationProvider) receives this token.

                5. DaoAuthenticationProvider starts the authentication process:
                - Extracts the principal (email) from the token.
                - Calls your configured UserDetailsService (AppUserDetailService) method:
                    loadUserByUsername(principal) — here, principal is the email from the token.

                6. In AppUserDetailService.loadUserByUsername(String email):
                - The profileRepository queries the MySQL database for a user ProfileEntity with this email.
                - If that user is not found, a UsernameNotFoundException is thrown — authentication fails immediately.
                - If found, constructs and returns a UserDetails object containing:
                    • username: the email from above,
                    • password: the encoded (usually bcrypt) password from the database,
                    • authorities: user permissions/roles (empty list here).

                7. DaoAuthenticationProvider receives this UserDetails object.

                8. DaoAuthenticationProvider retrieves credentials (raw password) from the authentication token
                and the encoded password from the UserDetails fetched from the database.

                9. The PasswordEncoder you configured is now used to check if the raw password (credentials in token) matches the encoded password in the UserDetails from the DB.

                10. If passwords match:
                - DaoAuthenticationProvider creates a new authenticated Authentication object:
                    • Sets the principal to the email (user identifier).
                    • Clears the credentials (removes the raw password for security).
                    • Sets authenticated = true.
                - This authentication object is returned from authenticationManager.

                - For a traditional (stateful/session-based) setup:
                    • Spring Security would store this authenticated object in the SecurityContext,
                    making the user's authentication available for the whole session.

                - For a stateless JWT-based setup (like this project):
                    • The SecurityContext is only populated for the duration of the current login request.
                    • You typically use this object to generate a JWT token (with the user's identity/claims) and return it to the client.
                    • The server does NOT store any persistent session or authentication context.
                    • For future requests, authentication info is rebuilt per request from the JWT token (via your filter),
                    so the authenticated user is made available in the SecurityContext only for each request with a valid JWT.
                    
                11. If passwords do not match, or no user exists for the provided email:
                    - An AuthenticationException (like BadCredentialsException or UsernameNotFoundException) is thrown, 
                    and the login attempt fails.
                    - No security context is established.

                Key Point:
                The "principal" is always the user identifier (email), and "credentials" are the raw password BEFORE authentication; after authentication, credentials are usually cleared for security.
                The entire workflow is automated by the flow of AuthenticationManagers, Providers, and your UserDetailsService — you only provide the glue code and configuration.
            */

            String token=jwtUtil.generateToken(authDTO.getEmail());

            return Map.of(
                "Token",token,
                "user",getPublicProfile(authDTO.getEmail())
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }
}
