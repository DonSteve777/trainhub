package com.trainhub.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

    
public class UserCreationDTO {


    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "El username no puede estar vacío")
    @Size(min = 4, max = 20, message = "El username debe tener entre 4 y 20 caracteres")
    private String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    // 🧱 Constructor vacío
    public UserCreationDTO() {}

    // 🧱 Constructor completo
    public UserCreationDTO(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // 💻 Getters y setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }


}
