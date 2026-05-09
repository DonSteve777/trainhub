package com.trainhub.backend.service;

import com.trainhub.backend.dto.request.NewPostRequest;
import com.trainhub.backend.dto.response.FriendTimeHistoryResponse;
import com.trainhub.backend.dto.response.PersonalRecordsResponse;
import com.trainhub.backend.dto.response.PersonalRecordsResponse.RecordEntry;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Calcula los records personales all-time del usuario para cada segmento HYROX:
     * tiempo total, suma de runs, y cada una de las 8 estaciones por separado.
     * Un campo es null si el usuario no tiene ningún post.
     *
     * @param userId id del usuario
     * @return records personales del usuario
     */
    public PersonalRecordsResponse getUserPersonalRecords(Integer userId) {
        List<Object[]> rows = postRepository.findAllUserPostTimes(userId);

        RecordEntry bestTotal         = null;
        RecordEntry bestRunning       = null;
        RecordEntry bestSkiErg        = null;
        RecordEntry bestSledPush      = null;
        RecordEntry bestSledPull      = null;
        RecordEntry bestBurpeeBj      = null;
        RecordEntry bestRow           = null;
        RecordEntry bestFarmersCarry  = null;
        RecordEntry bestSandbagLunges = null;
        RecordEntry bestWallBalls     = null;

        for (Object[] row : rows) {
            LocalDateTime date = (LocalDateTime) row[17];

            int total = (Integer) row[0];
            if (total > 0 && (bestTotal == null || total < bestTotal.getTime())) {
                bestTotal = new RecordEntry(total, date);
            }

            int runSum = 0;
            for (int i = 1; i <= 8; i++) runSum += (Integer) row[i];
            if (runSum > 0 && (bestRunning == null || runSum < bestRunning.getTime())) {
                bestRunning = new RecordEntry(runSum, date);
            }

            int skiErg = (Integer) row[9];
            if (skiErg > 0 && (bestSkiErg == null || skiErg < bestSkiErg.getTime())) {
                bestSkiErg = new RecordEntry(skiErg, date);
            }

            int sledPush = (Integer) row[10];
            if (sledPush > 0 && (bestSledPush == null || sledPush < bestSledPush.getTime())) {
                bestSledPush = new RecordEntry(sledPush, date);
            }

            int sledPull = (Integer) row[11];
            if (sledPull > 0 && (bestSledPull == null || sledPull < bestSledPull.getTime())) {
                bestSledPull = new RecordEntry(sledPull, date);
            }

            int burpee = (Integer) row[12];
            if (burpee > 0 && (bestBurpeeBj == null || burpee < bestBurpeeBj.getTime())) {
                bestBurpeeBj = new RecordEntry(burpee, date);
            }

            int rowErg = (Integer) row[13];
            if (rowErg > 0 && (bestRow == null || rowErg < bestRow.getTime())) {
                bestRow = new RecordEntry(rowErg, date);
            }

            int farmers = (Integer) row[14];
            if (farmers > 0 && (bestFarmersCarry == null || farmers < bestFarmersCarry.getTime())) {
                bestFarmersCarry = new RecordEntry(farmers, date);
            }

            int sandbag = (Integer) row[15];
            if (sandbag > 0 && (bestSandbagLunges == null || sandbag < bestSandbagLunges.getTime())) {
                bestSandbagLunges = new RecordEntry(sandbag, date);
            }

            int wallBalls = (Integer) row[16];
            if (wallBalls > 0 && (bestWallBalls == null || wallBalls < bestWallBalls.getTime())) {
                bestWallBalls = new RecordEntry(wallBalls, date);
            }
        }

        return new PersonalRecordsResponse(
                bestTotal, bestRunning,
                bestSkiErg, bestSledPush, bestSledPull, bestBurpeeBj,
                bestRow, bestFarmersCarry, bestSandbagLunges, bestWallBalls
        );
    }

    /**
     * Devuelve el histórico de tiempos de todos los amigos del usuario, agrupado por amigo.
     * Sin límite de fecha. Se usa para pintar marcadores de amigos en las gráficas de progresión.
     *
     * @param userId id del usuario autenticado
     * @return lista de históricos de tiempos, uno por amigo
     */
    public List<FriendTimeHistoryResponse> getFriendsTimeHistory(Integer userId) {
        List<Object[]> rows = postRepository.findFriendsPostTimesWithUser(userId);

        Map<String, FriendBuilder> builders = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String username = (String) row[0];
            LocalDateTime date = (LocalDateTime) row[18];

            FriendBuilder builder = builders.computeIfAbsent(username, FriendBuilder::new);

            builder.totalHistory.add(new FriendTimeHistoryResponse.TimeEntry((Integer) row[1], date));

            int runsSum = 0;
            for (int i = 2; i <= 9; i++) runsSum += (Integer) row[i];
            builder.runsHistory.add(new FriendTimeHistoryResponse.TimeEntry(runsSum, date));

            int workoutsSum = 0;
            for (int i = 10; i <= 17; i++) workoutsSum += (Integer) row[i];
            builder.workoutsHistory.add(new FriendTimeHistoryResponse.TimeEntry(workoutsSum, date));
        }

        return builders.values().stream()
                .map(b -> new FriendTimeHistoryResponse(b.username, b.totalHistory, b.workoutsHistory, b.runsHistory))
                .collect(Collectors.toList());
    }

    private static class FriendBuilder {
        final String username;
        final List<FriendTimeHistoryResponse.TimeEntry> totalHistory    = new ArrayList<>();
        final List<FriendTimeHistoryResponse.TimeEntry> workoutsHistory = new ArrayList<>();
        final List<FriendTimeHistoryResponse.TimeEntry> runsHistory     = new ArrayList<>();

        FriendBuilder(String username) { this.username = username; }
    }
}
