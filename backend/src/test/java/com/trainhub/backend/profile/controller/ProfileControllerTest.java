package com.trainhub.backend.profile.controller;

import com.trainhub.backend.profile.model.Profile;
import com.trainhub.backend.auth.model.User;
import com.trainhub.backend.auth.security.CustomUserDetailsService;
import com.trainhub.backend.profile.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias para ProfileController")
class ProfileControllerTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ProfileController profileController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController).build();
        objectMapper = new ObjectMapper();

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

        // Configurar SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Debería obtener el perfil del usuario autenticado correctamente")
    void shouldGetMyProfile() throws Exception {
        // Given
        when(authentication.getName()).thenReturn("testuser");
        when(userDetailsService.loadUserEntityByUsername("testuser")).thenReturn(testUser);
        when(profileService.getByUserId(1L)).thenReturn(testProfile);

        // When & Then
        mockMvc.perform(get("/api/profiles/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Juan Pérez"))
                .andExpect(jsonPath("$.bio").value("Entusiasta del deporte"))
                .andExpect(jsonPath("$.profilePictureUrl").value("https://example.com/profile.jpg"))
                .andExpect(jsonPath("$.location").value("Madrid, España"));

        verify(authentication, times(1)).getName();
        verify(userDetailsService, times(1)).loadUserEntityByUsername("testuser");
        verify(profileService, times(1)).getByUserId(1L);
    }

    @Test
    @DisplayName("Debería actualizar el perfil del usuario autenticado correctamente")
    void shouldUpdateMyProfile() throws Exception {
        // Given
        Map<String, String> updateData = new HashMap<>();
        updateData.put("fullName", "María García");
        updateData.put("bio", "Nueva biografía");
        updateData.put("profilePictureUrl", "https://example.com/new-profile.jpg");
        updateData.put("location", "Barcelona, España");

        Profile updatedProfile = new Profile();
        updatedProfile.setId(1L);
        updatedProfile.setUser(testUser);
        updatedProfile.setFullName("María García");
        updatedProfile.setBio("Nueva biografía");
        updatedProfile.setProfilePictureUrl("https://example.com/new-profile.jpg");
        updatedProfile.setLocation("Barcelona, España");

        when(authentication.getName()).thenReturn("testuser");
        when(userDetailsService.loadUserEntityByUsername("testuser")).thenReturn(testUser);
        when(profileService.getByUserId(1L)).thenReturn(testProfile);
        when(profileService.updateProfile(any(Profile.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(updatedProfile);

        // When & Then
        mockMvc.perform(put("/api/profiles/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("María García"))
                .andExpect(jsonPath("$.bio").value("Nueva biografía"))
                .andExpect(jsonPath("$.profilePictureUrl").value("https://example.com/new-profile.jpg"))
                .andExpect(jsonPath("$.location").value("Barcelona, España"));

        verify(authentication, times(1)).getName();
        verify(userDetailsService, times(1)).loadUserEntityByUsername("testuser");
        verify(profileService, times(1)).getByUserId(1L);
        verify(profileService, times(1)).updateProfile(any(Profile.class), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Debería actualizar solo algunos campos del perfil")
    void shouldUpdatePartialProfile() throws Exception {
        // Given
        Map<String, String> partialUpdateData = new HashMap<>();
        partialUpdateData.put("fullName", "Nuevo Nombre");
        // No se incluyen bio, profilePictureUrl ni location

        Profile updatedProfile = new Profile();
        updatedProfile.setId(1L);
        updatedProfile.setUser(testUser);
        updatedProfile.setFullName("Nuevo Nombre");
        updatedProfile.setBio(testProfile.getBio()); // Mantiene el valor original
        updatedProfile.setProfilePictureUrl(testProfile.getProfilePictureUrl()); // Mantiene el valor original
        updatedProfile.setLocation(testProfile.getLocation()); // Mantiene el valor original

        when(authentication.getName()).thenReturn("testuser");
        when(userDetailsService.loadUserEntityByUsername("testuser")).thenReturn(testUser);
        when(profileService.getByUserId(1L)).thenReturn(testProfile);
        when(profileService.updateProfile(any(Profile.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(updatedProfile);

        // When & Then
        mockMvc.perform(put("/api/profiles/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Nuevo Nombre"));

        verify(profileService, times(1)).updateProfile(
                eq(testProfile),
                eq("Nuevo Nombre"),
                eq(testProfile.getBio()),
                eq(testProfile.getProfilePictureUrl()),
                eq(testProfile.getLocation())
        );
    }

    @Test
    @DisplayName("Debería manejar actualización con payload vacío")
    void shouldHandleEmptyUpdatePayload() throws Exception {
        // Given
        Map<String, String> emptyData = new HashMap<>();

        when(authentication.getName()).thenReturn("testuser");
        when(userDetailsService.loadUserEntityByUsername("testuser")).thenReturn(testUser);
        when(profileService.getByUserId(1L)).thenReturn(testProfile);
        when(profileService.updateProfile(any(Profile.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testProfile);

        // When & Then
        mockMvc.perform(put("/api/profiles/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyData)))
                .andExpect(status().isOk());

        verify(profileService, times(1)).updateProfile(
                eq(testProfile),
                eq(testProfile.getFullName()),
                eq(testProfile.getBio()),
                eq(testProfile.getProfilePictureUrl()),
                eq(testProfile.getLocation())
        );
    }

    @Test
    @DisplayName("Debería retornar 200 OK cuando el perfil existe")
    void shouldReturnOkWhenProfileExists() {
        // Given
        when(authentication.getName()).thenReturn("testuser");
        when(userDetailsService.loadUserEntityByUsername("testuser")).thenReturn(testUser);
        when(profileService.getByUserId(1L)).thenReturn(testProfile);

        // When
        ResponseEntity<Profile> response = profileController.myProfile();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(testProfile, response.getBody());
    }

    @Test
    @DisplayName("Debería retornar 200 OK cuando se actualiza el perfil")
    void shouldReturnOkWhenUpdatingProfile() {
        // Given
        Map<String, String> updateData = new HashMap<>();
        updateData.put("fullName", "Nuevo Nombre");

        when(authentication.getName()).thenReturn("testuser");
        when(userDetailsService.loadUserEntityByUsername("testuser")).thenReturn(testUser);
        when(profileService.getByUserId(1L)).thenReturn(testProfile);
        when(profileService.updateProfile(any(Profile.class), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testProfile);

        // When
        ResponseEntity<Profile> response = profileController.updateMyProfile(updateData);

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(testProfile, response.getBody());
    }
}
