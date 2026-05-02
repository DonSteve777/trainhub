package com.trainhub.backend.dto.response;

/**
 * DTO de respuesta al dar o quitar like a un post.
 * Devuelve el nuevo estado del like y el conteo actualizado.
 */
public class LikeToggleResponse {

    private boolean liked;
    private long likesCount;

    public LikeToggleResponse(boolean liked, long likesCount) {
        this.liked = liked;
        this.likesCount = likesCount;
    }

    public boolean isLiked() { return liked; }
    public long getLikesCount() { return likesCount; }
}
