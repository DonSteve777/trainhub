package com.trainhub.backend.service;

import com.trainhub.backend.dto.request.NewPostRequest;
import com.trainhub.backend.dto.response.UserTimeHistoryResponse;
import com.trainhub.backend.dto.response.UserTimeHistoryResponse.TimeEntry;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.PostRepository;
import com.trainhub.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para la gestión de posts (entrenamientos).
 */
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crea un nuevo post para el usuario autenticado.
     * El totalTime se calcula como la suma de los 16 tiempos parciales.
     *
     * @param userId         id del usuario autenticado
     * @param request        datos del entrenamiento
     * @return el Post persistido
     * @throws EntityNotFoundException si el usuario no existe
     */
    @Transactional
    public Post createPost(Integer userId, NewPostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + userId));

        int totalTime = request.getR1Time() + request.getR2Time()
                + request.getR3Time() + request.getR4Time()
                + request.getR5Time() + request.getR6Time()
                + request.getR7Time() + request.getR8Time()
                + request.getSkiErg() + request.getSledPush()
                + request.getSledPull() + request.getBurpeeBroadJump()
                + request.getRow() + request.getFarmersCarry()
                + request.getSandbagLunges() + request.getWallBalls();

        Post post = new Post(
                user,
                request.getR1Time(), request.getR2Time(),
                request.getR3Time(), request.getR4Time(),
                request.getR5Time(), request.getR6Time(),
                request.getR7Time(), request.getR8Time(),
                request.getSkiErg(), request.getSledPush(),
                request.getSledPull(), request.getBurpeeBroadJump(),
                request.getRow(), request.getFarmersCarry(),
                request.getSandbagLunges(), request.getWallBalls(),
                totalTime,
                LocalDateTime.now()
        );
        post.setCategory(request.getCategory());

        if (request.getMateUsername() != null && !request.getMateUsername().isBlank()) {
            userRepository.findByUsername(request.getMateUsername())
                    .ifPresent(post::setMate);
        }

        return postRepository.save(post);
    }

    /**
     * Devuelve el histórico de tiempos del usuario agrupado en tres colecciones:
     * total, workouts (suma de las 8 estaciones) y runs (suma de las 8 carreras).
     * Cada entrada incluye el tiempo en segundos y la fecha del entrenamiento.
     *
     * @param userId id del usuario autenticado
     * @return histórico de tiempos del usuario
     */
    public UserTimeHistoryResponse getUserTimeHistory(Integer userId) {
        List<Object[]> rows = postRepository.findUserPostTimes(userId, LocalDateTime.now().minusWeeks(4));

        List<TimeEntry> totalHistory    = new ArrayList<>();
        List<TimeEntry> runsHistory     = new ArrayList<>();
        List<TimeEntry> workoutsHistory = new ArrayList<>();

        for (Object[] row : rows) {
            LocalDateTime date = (LocalDateTime) row[17];

            totalHistory.add(new TimeEntry((Integer) row[0], date));

            int runsSum = 0;
            for (int i = 1; i <= 8; i++) runsSum += (Integer) row[i];
            runsHistory.add(new TimeEntry(runsSum, date));

            int workoutsSum = 0;
            for (int i = 9; i <= 16; i++) workoutsSum += (Integer) row[i];
            workoutsHistory.add(new TimeEntry(workoutsSum, date));
        }

        return new UserTimeHistoryResponse(totalHistory, workoutsHistory, runsHistory);
    }
}
