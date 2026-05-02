package com.trainhub.backend.dto.response;

/**
 * DTO de respuesta con el número de notificaciones no leídas.
 */
public class UnreadCountResponse {

    private long count;

    public UnreadCountResponse(long count) {
        this.count = count;
    }

    public long getCount() { return count; }
}
