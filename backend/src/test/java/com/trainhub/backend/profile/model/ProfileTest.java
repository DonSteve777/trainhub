package com.trainhub.backend.profile.model;

import com.trainhub.backend.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias para el modelo Profile")
class ProfileTest {

    private Profile profile;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        
        profile = new Profile();
    }

    @Test
    @DisplayName("Debería crear un perfil con constructor por defecto")
    void shouldCreateProfileWithDefaultConstructor() {
        assertNotNull(profile);
        assertNull(profile.getId());
        assertNull(profile.getUser());
        assertNull(profile.getFullName());
        assertNull(profile.getBio());
        assertNull(profile.getProfilePictureUrl());
        assertNull(profile.getLocation());
    }

    @Test
    @DisplayName("Debería crear un perfil con constructor que recibe usuario")
    void shouldCreateProfileWithUserConstructor() {
        Profile profileWithUser = new Profile(user);
        
        assertNotNull(profileWithUser);
        assertEquals(user, profileWithUser.getUser());
        assertNull(profileWithUser.getId());
        assertNull(profileWithUser.getFullName());
        assertNull(profileWithUser.getBio());
        assertNull(profileWithUser.getProfilePictureUrl());
        assertNull(profileWithUser.getLocation());
    }

    @Test
    @DisplayName("Debería establecer y obtener el ID correctamente")
    void shouldSetAndGetId() {
        Long id = 1L;
        profile.setId(id);
        assertEquals(id, profile.getId());
    }

    @Test
    @DisplayName("Debería establecer y obtener el usuario correctamente")
    void shouldSetAndGetUser() {
        profile.setUser(user);
        assertEquals(user, profile.getUser());
    }

    @Test
    @DisplayName("Debería establecer y obtener el nombre completo correctamente")
    void shouldSetAndGetFullName() {
        String fullName = "Juan Pérez";
        profile.setFullName(fullName);
        assertEquals(fullName, profile.getFullName());
    }

    @Test
    @DisplayName("Debería establecer y obtener la biografía correctamente")
    void shouldSetAndGetBio() {
        String bio = "Entusiasta del deporte y la vida saludable";
        profile.setBio(bio);
        assertEquals(bio, profile.getBio());
    }

    @Test
    @DisplayName("Debería establecer y obtener la URL de la foto de perfil correctamente")
    void shouldSetAndGetProfilePictureUrl() {
        String pictureUrl = "https://example.com/profile.jpg";
        profile.setProfilePictureUrl(pictureUrl);
        assertEquals(pictureUrl, profile.getProfilePictureUrl());
    }

    @Test
    @DisplayName("Debería establecer y obtener la ubicación correctamente")
    void shouldSetAndGetLocation() {
        String location = "Madrid, España";
        profile.setLocation(location);
        assertEquals(location, profile.getLocation());
    }

    @Test
    @DisplayName("Debería manejar valores nulos correctamente")
    void shouldHandleNullValues() {
        profile.setId(null);
        profile.setUser(null);
        profile.setFullName(null);
        profile.setBio(null);
        profile.setProfilePictureUrl(null);
        profile.setLocation(null);

        assertNull(profile.getId());
        assertNull(profile.getUser());
        assertNull(profile.getFullName());
        assertNull(profile.getBio());
        assertNull(profile.getProfilePictureUrl());
        assertNull(profile.getLocation());
    }

    @Test
    @DisplayName("Debería manejar cadenas vacías correctamente")
    void shouldHandleEmptyStrings() {
        profile.setFullName("");
        profile.setBio("");
        profile.setProfilePictureUrl("");
        profile.setLocation("");

        assertEquals("", profile.getFullName());
        assertEquals("", profile.getBio());
        assertEquals("", profile.getProfilePictureUrl());
        assertEquals("", profile.getLocation());
    }
}
