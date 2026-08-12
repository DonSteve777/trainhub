package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.GoalMarkResponse;
import com.trainhub.backend.dto.response.GoalParticipantResponse;
import com.trainhub.backend.dto.response.GoalResponse;
import com.trainhub.backend.enums.GoalStatus;
import com.trainhub.backend.model.Goal;
import com.trainhub.backend.model.GoalMark;
import com.trainhub.backend.model.GoalParticipant;
import com.trainhub.backend.repository.GoalMarkRepository;
import com.trainhub.backend.repository.GoalParticipantRepository;
import com.trainhub.backend.repository.GoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public GoalService(GoalRepository goalRepository,
                       GoalParticipantRepository goalParticipantRepository,
                       GoalMarkRepository goalMarkRepository) {
        this.goalRepository = goalRepository;
        this.goalParticipantRepository = goalParticipantRepository;
        this.goalMarkRepository = goalMarkRepository;
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
