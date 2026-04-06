package com.trainhub.backend.service;

import com.trainhub.backend.dto.request.NewPostRequest;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.PostRepository;
import com.trainhub.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

        return postRepository.save(post);
    }
}
