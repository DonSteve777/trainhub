package com.trainhub.backend.profile.controller;

import com.trainhub.backend.profile.model.Profile;
import com.trainhub.backend.auth.model.User;
import com.trainhub.backend.auth.security.CustomUserDetailsService;
import com.trainhub.backend.profile.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public ProfileController(ProfileService profileService, CustomUserDetailsService userDetailsService) {
        this.profileService = profileService;
        this.userDetailsService = userDetailsService;
    }
// doc
    @GetMapping("/me")
    public ResponseEntity<Profile> myProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userDetailsService.loadUserEntityByUsername(username);
        Profile profile = profileService.getByUserId(user.getId());
        return ResponseEntity.ok(profile);
    }
// doc
    @PutMapping("/me")
    public ResponseEntity<Profile> updateMyProfile(@RequestBody Map<String, String> payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userDetailsService.loadUserEntityByUsername(username);
        Profile profile = profileService.getByUserId(user.getId());
        Profile updated = profileService.updateProfile(
                profile,
                payload.getOrDefault("fullName", profile.getFullName()),
                payload.getOrDefault("bio", profile.getBio()),
                payload.getOrDefault("profilePictureUrl", profile.getProfilePictureUrl()),
                payload.getOrDefault("location", profile.getLocation())
        );
        return ResponseEntity.ok(updated);
    }


    
}


