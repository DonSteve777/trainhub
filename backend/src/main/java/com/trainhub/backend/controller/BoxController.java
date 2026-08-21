package com.trainhub.backend.controller;

import com.trainhub.backend.dto.response.BoxResponse;
import com.trainhub.backend.repository.BoxRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones sobre boxes.
 */
@RestController
@RequestMapping("/api/boxes")
public class BoxController {

    @Autowired
    private BoxRepository boxRepository;

    /**
     * Lista todos los boxes ordenados por nombre.
     *
     * @return lista de boxes con id, nombre y ciudad
     */
    @GetMapping
    public ResponseEntity<List<BoxResponse>> listBoxes() {
        List<BoxResponse> boxes = boxRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(box -> new BoxResponse(
                        box.getId(),
                        box.getName(),
                        box.getCity() != null ? box.getCity().getName() : null
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(boxes);
    }
}
