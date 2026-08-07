package com.trainhub.backend.dto.response;

/**
 * DTO de respuesta al apuntarse o desapuntarse de un reto de box.
 * Devuelve el nuevo estado de participación y el conteo actualizado.
 */
public class ParticipationToggleResponse {

    private boolean joined;
    private long participantsCount;

    public ParticipationToggleResponse(boolean joined, long participantsCount) {
        this.joined = joined;
        this.participantsCount = participantsCount;
    }

    public boolean isJoined() { return joined; }
    public long getParticipantsCount() { return participantsCount; }
}
