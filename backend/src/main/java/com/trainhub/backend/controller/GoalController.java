package com.trainhub.backend.controller;

import com.trainhub.backend.dto.request.NewGoalMarkRequest;
import com.trainhub.backend.dto.request.NewGoalRequest;
import com.trainhub.backend.dto.response.GoalJoinResponse;
import com.trainhub.backend.dto.response.GoalMarkResponse;
import com.trainhub.backend.dto.response.GoalResponse;
import com.trainhub.backend.enums.GoalStatus;
import com.trainhub.backend.security.UserPrincipal;
import com.trainhub.backend.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API de objetivos (vista /objetivos).
 */
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    /**
     * Lista los objetivos en los que participa el usuario autenticado,
     * con participantes y marcas.
     *
     * @param status filtro opcional: ACTIVE | ACHIEVED | EXPIRED
     */
    @GetMapping
    public ResponseEntity<List<GoalResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) GoalStatus status) {
        return ResponseEntity.ok(goalService.listForUser(principal.getId(), status));
    }

    /**
     * Crea un objetivo activo; el usuario autenticado queda como participante owner.
     */
    @PostMapping
    public ResponseEntity<GoalResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NewGoalRequest request) {
        GoalResponse goal = goalService.createGoal(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(goal);
    }

    /**
     * Acoge al usuario autenticado a un objetivo activo ajeno.
     */
    @PostMapping("/{goalId}/join")
    public ResponseEntity<GoalJoinResponse> join(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Integer goalId) {
        GoalJoinResponse body = goalService.joinGoal(principal.getId(), goalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * Registra una marca del usuario autenticado en un objetivo activo en el que participa.
     *
     * @param goalId id del objetivo
     * @param request valor y comentario opcional
     */
    @PostMapping("/{goalId}/marks")
    public ResponseEntity<GoalMarkResponse> addMark(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Integer goalId,
            @Valid @RequestBody NewGoalMarkRequest request) {
        GoalMarkResponse mark = goalService.addMark(principal.getId(), goalId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mark);
    }
}
