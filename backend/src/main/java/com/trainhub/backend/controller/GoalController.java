package com.trainhub.backend.controller;

import com.trainhub.backend.dto.response.GoalResponse;
import com.trainhub.backend.enums.GoalStatus;
import com.trainhub.backend.security.UserPrincipal;
import com.trainhub.backend.service.GoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
}
