package com.trainhub.backend.profile.service;

import com.trainhub.backend.profile.model.Profile;
import com.trainhub.backend.auth.model.User;
import com.trainhub.backend.profile.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Autowired
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Profile createForUser(User user) {
        Profile profile = new Profile();
        profile.setUser(user);
        return profileRepository.save(profile);
    }

    public Profile getByUserId(Long userId) {
        return profileRepository.findByUserId(userId).orElse(null);
    }

    public Profile updateProfile(Profile profile, String fullName, String bio, String pictureUrl, String location) {
        profile.setFullName(fullName);
        profile.setBio(bio);
        profile.setProfilePictureUrl(pictureUrl);
        profile.setLocation(location);
        return profileRepository.save(profile);
    }
}


