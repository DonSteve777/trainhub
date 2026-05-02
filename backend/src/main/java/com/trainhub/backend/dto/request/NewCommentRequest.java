package com.trainhub.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de petición para crear un comentario en un post.
 */
public class NewCommentRequest {

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
    private String content;

    public NewCommentRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
