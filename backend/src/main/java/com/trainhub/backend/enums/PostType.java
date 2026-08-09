package com.trainhub.backend.enums;

/**
 * Tipo de contenido que representa un post.
 */
public enum PostType {
    CHECKIN,
    BOX_WOD,
    BOX_CHALLENGE,
    BOX_ANNOUNCEMENT;

    /**
     * @return true si el post pertenece al contenido publicado por un box (WOD, reto o anuncio).
     */
    public boolean isBoxContent() {
        return this == BOX_WOD || this == BOX_CHALLENGE || this == BOX_ANNOUNCEMENT;
    }
}
