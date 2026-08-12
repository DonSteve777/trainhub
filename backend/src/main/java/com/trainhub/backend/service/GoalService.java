package com.trainhub.backend.service;

import com.trainhub.backend.dto.request.NewGoalMarkRequest;
import com.trainhub.backend.dto.request.NewGoalRequest;
import com.trainhub.backend.dto.response.GoalMarkResponse;
import com.trainhub.backend.dto.response.GoalParticipantResponse;
import com.trainhub.backend.dto.response.GoalResponse;
import com.trainhub.backend.enums.GoalDirection;
import com.trainhub.backend.enums.GoalStatus;
import com.trainhub.backend.enums.GoalUnit;
import com.trainhub.backend.model.Goal;
import com.trainhub.backend.model.GoalMark;
import com.trainhub.backend.model.GoalParticipant;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.GoalMarkRepository;
import com.trainhub.backend.repository.GoalParticipantRepository;
import com.trainhub.backend.repository.GoalRepository;
import com.trainhub.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de objetivos (vista /objetivos).
 */
@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalParticipantRepository goalParticipantRepository;
    private final GoalMarkRepository goalMarkRepository;
    private final UserRepository userRepository;

    public GoalService(GoalRepository goalRepository,
                       GoalParticipantRepository goalParticipantRepository,
                       GoalMarkRepository goalMarkRepository,
                       UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.goalParticipantRepository = goalParticipantRepository;
        this.goalMarkRepository = goalMarkRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lista los objetivos en los que participa el usuario, con participantes y marcas.
     *
     * @param userId id del usuario autenticado
     * @param status filtro opcional ({@code null} = todos)
     */
    @Transactional(readOnly = true)
    public List<GoalResponse> listForUser(Integer userId, GoalStatus status) {
        List<Goal> goals = status != null
                ? goalRepository.findByParticipantUserIdAndStatus(userId, status)
                : goalRepository.findByParticipantUserId(userId);

        if (goals.isEmpty()) {
            return List.of();
        }

        List<Integer> goalIds = goals.stream().map(Goal::getId).toList();

        Map<Integer, List<GoalParticipant>> participantsByGoal = goalParticipantRepository
                .findByGoalIdsWithUser(goalIds)
                .stream()
                .collect(Collectors.groupingBy(
                        gp -> gp.getId().getGoalId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Integer, List<GoalMark>> marksByGoal = goalMarkRepository
                .findByGoalIds(goalIds)
                .stream()
                .collect(Collectors.groupingBy(
                        gm -> gm.getGoal().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<GoalResponse> result = new ArrayList<>(goals.size());
        for (Goal goal : goals) {
            result.add(toResponse(
                    goal,
                    participantsByGoal.getOrDefault(goal.getId(), List.of()),
                    marksByGoal.getOrDefault(goal.getId(), List.of()),
                    userId
            ));
        }
        return result;
    }

    /**
     * Crea un objetivo activo y al usuario autenticado como participante owner.
     *
     * @param userId id del usuario autenticado
     * @param request datos del objetivo
     * @return objetivo creado con participante owner
     */
    @Transactional
    public GoalResponse createGoal(Integer userId, NewGoalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        GoalUnit unit = request.getUnit();
        GoalDirection direction = directionForUnit(unit);
        if (request.getDirection() != null && request.getDirection() != direction) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La dirección no coincide con la unidad seleccionada");
        }

        OffsetDateTime now = OffsetDateTime.now();
        int weeks = Math.max(1, request.getWeeks());
        OffsetDateTime deadline = now.plusDays((long) weeks * 7)
                .withHour(23).withMinute(59).withSecond(59).withNano(999_000_000);

        String description = request.getDescription();
        if (description != null) {
            description = description.trim();
            if (description.isEmpty()) {
                description = null;
            }
        }

        Goal goal = new Goal();
        goal.setTitle(request.getTitle().trim());
        goal.setDescription(description);
        goal.setMetricLabel(request.getMetricLabel().trim());
        goal.setTargetValue(request.getTargetValue());
        goal.setUnit(unit);
        goal.setDirection(direction);
        goal.setWeeks(weeks);
        goal.setDeadline(deadline);
        goal.setStatus(GoalStatus.ACTIVE);
        goal.setCreatedBy(user);
        goal.setCreatedAt(LocalDateTime.now());
        goal = goalRepository.save(goal);

        GoalParticipant participant = new GoalParticipant(goal, user, true, now, deadline);
        goalParticipantRepository.save(participant);

        return toResponse(goal, List.of(participant), List.of(), userId);
    }

    /**
     * Registra una marca del usuario autenticado en un objetivo activo en el que participa.
     *
     * @param userId id del usuario autenticado
     * @param goalId id del objetivo
     * @param request valor y comentario opcional
     * @return marca persistida
     */
    @Transactional
    public GoalMarkResponse addMark(Integer userId, Integer goalId, NewGoalMarkRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Objetivo no encontrado"));

        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Solo se pueden registrar marcas en objetivos activos");
        }

        if (!goalParticipantRepository.existsByIdGoalIdAndIdUserId(goalId, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No participas en este objetivo");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String note = request.getNote();
        if (note != null) {
            note = note.trim();
            if (note.isEmpty()) {
                note = null;
            }
        }

        GoalMark mark = new GoalMark(
                goal,
                user,
                request.getValue(),
                note,
                OffsetDateTime.now()
        );
        mark = goalMarkRepository.save(mark);
        return toMarkResponse(mark);
    }

    private GoalDirection directionForUnit(GoalUnit unit) {
        return unit == GoalUnit.TIME ? GoalDirection.LOWER : GoalDirection.HIGHER;
    }

    private GoalResponse toResponse(Goal goal,
                                    List<GoalParticipant> participants,
                                    List<GoalMark> marks,
                                    Integer currentUserId) {
        Map<Integer, List<GoalMark>> marksByUser = marks.stream()
                .collect(Collectors.groupingBy(
                        gm -> gm.getUser().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<GoalParticipantResponse> participantResponses = new ArrayList<>(participants.size());
        for (GoalParticipant gp : participants) {
            Integer uid = gp.getUser().getId();
            List<GoalMarkResponse> markResponses = marksByUser
                    .getOrDefault(uid, Collections.emptyList())
                    .stream()
                    .map(this::toMarkResponse)
                    .toList();

            participantResponses.add(new GoalParticipantResponse(
                    uid,
                    gp.getUser().getUsername(),
                    gp.getUser().getPhotoUrl(),
                    gp.isOwner(),
                    uid.equals(currentUserId),
                    gp.getStartedAt(),
                    gp.getEndsAt(),
                    markResponses
            ));
        }

        return new GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getMetricLabel(),
                goal.getTargetValue(),
                goal.getUnit().name().toLowerCase(),
                goal.getDirection().name().toLowerCase(),
                goal.getWeeks(),
                goal.getDeadline(),
                goal.getStatus().name(),
                goal.getCreatedAt(),
                participantResponses
        );
    }

    private GoalMarkResponse toMarkResponse(GoalMark mark) {
        return new GoalMarkResponse(
                mark.getId(),
                mark.getUser().getId(),
                mark.getValue(),
                mark.getNote(),
                mark.getRecordedAt()
        );
    }
}
