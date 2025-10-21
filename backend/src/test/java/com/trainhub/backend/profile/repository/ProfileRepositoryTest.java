package com.trainhub.backend.profile.repository;

import com.trainhub.backend.profile.model.Profile;
import com.trainhub.backend.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Pruebas unitarias para ProfileRepository")
class ProfileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProfileRepository profileRepository;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = entityManager.persistAndFlush(testUser);

        // Crear perfil de prueba
        testProfile = new Profile();
        testProfile.setUser(testUser);
        testProfile.setFullName("Juan Pérez");
        testProfile.setBio("Entusiasta del deporte");
        testProfile.setProfilePictureUrl("https://example.com/profile.jpg");
        testProfile.setLocation("Madrid, España");
        testProfile = entityManager.persistAndFlush(testProfile);
    }

    @Test
    @DisplayName("Debería encontrar un perfil por usuario")
    void shouldFindProfileByUser() {
        // When
        Optional<Profile> foundProfile = profileRepository.findByUser(testUser);

        // Then
        assertTrue(foundProfile.isPresent());
        assertEquals(testProfile.getId(), foundProfile.get().getId());
        assertEquals(testUser.getId(), foundProfile.get().getUser().getId());
        assertEquals("Juan Pérez", foundProfile.get().getFullName());
        assertEquals("Entusiasta del deporte", foundProfile.get().getBio());
        assertEquals("https://example.com/profile.jpg", foundProfile.get().getProfilePictureUrl());
        assertEquals("Madrid, España", foundProfile.get().getLocation());
    }

    @Test
    @DisplayName("Debería encontrar un perfil por ID de usuario")
    void shouldFindProfileByUserId() {
        // When
        Optional<Profile> foundProfile = profileRepository.findByUserId(testUser.getId());

        // Then
        assertTrue(foundProfile.isPresent());
        assertEquals(testProfile.getId(), foundProfile.get().getId());
        assertEquals(testUser.getId(), foundProfile.get().getUser().getId());
        assertEquals("Juan Pérez", foundProfile.get().getFullName());
        assertEquals("Entusiasta del deporte", foundProfile.get().getBio());
        assertEquals("https://example.com/profile.jpg", foundProfile.get().getProfilePictureUrl());
        assertEquals("Madrid, España", foundProfile.get().getLocation());
    }

    @Test
    @DisplayName("Debería retornar Optional vacío cuando no existe perfil para el usuario")
    void shouldReturnEmptyOptionalWhenProfileDoesNotExistForUser() {
        // Given
        User nonExistentUser = new User();
        nonExistentUser.setUsername("nonexistent");
        nonExistentUser.setEmail("nonexistent@example.com");
        nonExistentUser.setPassword("password123");
        nonExistentUser = entityManager.persistAndFlush(nonExistentUser);

        // When
        Optional<Profile> foundProfile = profileRepository.findByUser(nonExistentUser);

        // Then
        assertFalse(foundProfile.isPresent());
    }

    @Test
    @DisplayName("Debería retornar Optional vacío cuando no existe perfil para el ID de usuario")
    void shouldReturnEmptyOptionalWhenProfileDoesNotExistForUserId() {
        // Given
        Long nonExistentUserId = 999L;

        // When
        Optional<Profile> foundProfile = profileRepository.findByUserId(nonExistentUserId);

        // Then
        assertFalse(foundProfile.isPresent());
    }

    @Test
    @DisplayName("Debería guardar un nuevo perfil correctamente")
    void shouldSaveNewProfile() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("password123");
        newUser = entityManager.persistAndFlush(newUser);

        Profile newProfile = new Profile();
        newProfile.setUser(newUser);
        newProfile.setFullName("María García");
        newProfile.setBio("Nueva biografía");
        newProfile.setProfilePictureUrl("https://example.com/new-profile.jpg");
        newProfile.setLocation("Barcelona, España");

        // When
        Profile savedProfile = profileRepository.save(newProfile);
        entityManager.flush();

        // Then
        assertNotNull(savedProfile.getId());
        assertEquals(newUser.getId(), savedProfile.getUser().getId());
        assertEquals("María García", savedProfile.getFullName());
        assertEquals("Nueva biografía", savedProfile.getBio());
        assertEquals("https://example.com/new-profile.jpg", savedProfile.getProfilePictureUrl());
        assertEquals("Barcelona, España", savedProfile.getLocation());

        // Verificar que se puede encontrar en la base de datos
        Optional<Profile> foundProfile = profileRepository.findByUserId(newUser.getId());
        assertTrue(foundProfile.isPresent());
        assertEquals(savedProfile.getId(), foundProfile.get().getId());
    }

    @Test
    @DisplayName("Debería actualizar un perfil existente correctamente")
    void shouldUpdateExistingProfile() {
        // Given
        testProfile.setFullName("Nombre Actualizado");
        testProfile.setBio("Biografía actualizada");
        testProfile.setProfilePictureUrl("https://example.com/updated-profile.jpg");
        testProfile.setLocation("Valencia, España");

        // When
        Profile updatedProfile = profileRepository.save(testProfile);
        entityManager.flush();

        // Then
        assertEquals(testProfile.getId(), updatedProfile.getId());
        assertEquals("Nombre Actualizado", updatedProfile.getFullName());
        assertEquals("Biografía actualizada", updatedProfile.getBio());
        assertEquals("https://example.com/updated-profile.jpg", updatedProfile.getProfilePictureUrl());
        assertEquals("Valencia, España", updatedProfile.getLocation());

        // Verificar que los cambios se persistieron
        Optional<Profile> foundProfile = profileRepository.findByUserId(testUser.getId());
        assertTrue(foundProfile.isPresent());
        assertEquals("Nombre Actualizado", foundProfile.get().getFullName());
        assertEquals("Biografía actualizada", foundProfile.get().getBio());
        assertEquals("https://example.com/updated-profile.jpg", foundProfile.get().getProfilePictureUrl());
        assertEquals("Valencia, España", foundProfile.get().getLocation());
    }

    @Test
    @DisplayName("Debería eliminar un perfil correctamente")
    void shouldDeleteProfile() {
        // Given
        Long profileId = testProfile.getId();

        // When
        profileRepository.deleteById(profileId);
        entityManager.flush();

        // Then
        Optional<Profile> foundProfile = profileRepository.findByUserId(testUser.getId());
        assertFalse(foundProfile.isPresent());
    }

    @Test
    @DisplayName("Debería manejar la relación OneToOne con User correctamente")
    void shouldHandleOneToOneRelationshipWithUser() {
        // When
        Optional<Profile> foundProfile = profileRepository.findByUserId(testUser.getId());

        // Then
        assertTrue(foundProfile.isPresent());
        Profile profile = foundProfile.get();
        
        // Verificar que la relación está correctamente establecida
        assertNotNull(profile.getUser());
        assertEquals(testUser.getId(), profile.getUser().getId());
        assertEquals(testUser.getUsername(), profile.getUser().getUsername());
        assertEquals(testUser.getEmail(), profile.getUser().getEmail());
    }
}
