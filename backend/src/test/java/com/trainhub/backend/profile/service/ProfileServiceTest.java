package com.trainhub.backend.profile.service;

import com.trainhub.backend.profile.model.Profile;
import com.trainhub.backend.auth.model.User;
import com.trainhub.backend.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para ProfileService")
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testProfile = new Profile();
        testProfile.setId(1L);
        testProfile.setUser(testUser);
        testProfile.setFullName("Juan Pérez");
        testProfile.setBio("Entusiasta del deporte");
        testProfile.setProfilePictureUrl("https://example.com/profile.jpg");
        testProfile.setLocation("Madrid, España");
    }

    @Test
    @DisplayName("Debería crear un perfil para un usuario correctamente")
    void shouldCreateProfileForUser() {
        // Given
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // When
        Profile result = profileService.createForUser(testUser);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    @DisplayName("Debería obtener un perfil por ID de usuario cuando existe")
    void shouldGetProfileByUserIdWhenExists() {
        // Given
        Long userId = 1L;
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(testProfile));

        // When
        Profile result = profileService.getByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(testProfile, result);
        verify(profileRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Debería retornar null cuando no existe un perfil para el ID de usuario")
    void shouldReturnNullWhenProfileDoesNotExist() {
        // Given
        Long userId = 999L;
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When
        Profile result = profileService.getByUserId(userId);

        // Then
        assertNull(result);
        verify(profileRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Debería actualizar un perfil correctamente con todos los campos")
    void shouldUpdateProfileWithAllFields() {
        // Given
        String newFullName = "María García";
        String newBio = "Nueva biografía actualizada";
        String newPictureUrl = "https://example.com/new-profile.jpg";
        String newLocation = "Barcelona, España";

        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // When
        Profile result = profileService.updateProfile(testProfile, newFullName, newBio, newPictureUrl, newLocation);

        // Then
        assertNotNull(result);
        assertEquals(newFullName, testProfile.getFullName());
        assertEquals(newBio, testProfile.getBio());
        assertEquals(newPictureUrl, testProfile.getProfilePictureUrl());
        assertEquals(newLocation, testProfile.getLocation());
        verify(profileRepository, times(1)).save(testProfile);
    }

    @Test
    @DisplayName("Debería actualizar un perfil con valores nulos")
    void shouldUpdateProfileWithNullValues() {
        // Given
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // When
        Profile result = profileService.updateProfile(testProfile, null, null, null, null);

        // Then
        assertNotNull(result);
        assertNull(testProfile.getFullName());
        assertNull(testProfile.getBio());
        assertNull(testProfile.getProfilePictureUrl());
        assertNull(testProfile.getLocation());
        verify(profileRepository, times(1)).save(testProfile);
    }

    @Test
    @DisplayName("Debería actualizar un perfil con cadenas vacías")
    void shouldUpdateProfileWithEmptyStrings() {
        // Given
        String emptyString = "";
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // When
        Profile result = profileService.updateProfile(testProfile, emptyString, emptyString, emptyString, emptyString);

        // Then
        assertNotNull(result);
        assertEquals(emptyString, testProfile.getFullName());
        assertEquals(emptyString, testProfile.getBio());
        assertEquals(emptyString, testProfile.getProfilePictureUrl());
        assertEquals(emptyString, testProfile.getLocation());
        verify(profileRepository, times(1)).save(testProfile);
    }

    @Test
    @DisplayName("Debería actualizar solo algunos campos del perfil")
    void shouldUpdateOnlySomeProfileFields() {
        // Given
        String newFullName = "Nuevo Nombre";
        String originalBio = testProfile.getBio();
        String originalPictureUrl = testProfile.getProfilePictureUrl();
        String originalLocation = testProfile.getLocation();

        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        // When
        Profile result = profileService.updateProfile(testProfile, newFullName, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(newFullName, testProfile.getFullName());
        assertEquals(originalBio, testProfile.getBio());
        assertEquals(originalPictureUrl, testProfile.getProfilePictureUrl());
        assertEquals(originalLocation, testProfile.getLocation());
        verify(profileRepository, times(1)).save(testProfile);
    }
}
