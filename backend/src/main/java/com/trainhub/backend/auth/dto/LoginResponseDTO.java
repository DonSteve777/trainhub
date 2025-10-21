package com.trainhub.backend.auth.dto;

public record LoginResponseDTO(
    String token,
    String type,
    Long id,
    String username,
    String email
) {
    // Constructor secundario con type por defecto
    public LoginResponseDTO(String token, Long id, String username, String email) {
        this(token, "Bearer", id, username, email);
    }
}
