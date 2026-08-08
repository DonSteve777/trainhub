package com.trainhub.backend.service;

import com.trainhub.backend.dto.request.NewCheckinRequest;
import com.trainhub.backend.dto.request.NewBoxPostRequest;
import com.trainhub.backend.dto.request.NewPostRequest;
import com.trainhub.backend.dto.response.FriendTimeHistoryResponse;
import com.trainhub.backend.dto.response.PersonalRecordsResponse;
import com.trainhub.backend.dto.response.PersonalRecordsResponse.RecordEntry;
import com.trainhub.backend.dto.response.StreakResponse;
import com.trainhub.backend.dto.response.UserTimeHistoryResponse;
import com.trainhub.backend.dto.response.UserTimeHistoryResponse.TimeEntry;
import com.trainhub.backend.dto.response.WeeklyConstancyResponse;
import com.trainhub.backend.enums.PostType;
import com.trainhub.backend.enums.Role;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.PostRepository;
import com.trainhub.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de posts (entrenamientos).
 */
@Service
public class PostService {

    private static final WeekFields ISO_WEEK = WeekFields.ISO;
    private static final List<Boolean> EMPTY_WEEK_DAYS = List.of(
            false, false, false, false, false, false, false
    );

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final int minDaysPerWeek;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            @Value("${trainhub.streak.min-days-per-week:3}") int minDaysPerWeek
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.minDaysPerWeek = minDaysPerWeek;
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
        post.setPostType(PostType.RESULT);
        post.setBox(user.getBox());

        if (request.getMateUsername() != null && !request.getMateUsername().isBlank()) {
            userRepository.findByUsername(request.getMateUsername())
                    .ifPresent(post::setMate);
        }

        return postRepository.save(post);
    }

    /**
     * Crea un check-in de entrenamiento sin marcas HYROX ni categoría.
     *
     * @param userId         id del usuario autenticado
     * @param request        datos del check-in
     * @return el Post persistido
     * @throws EntityNotFoundException si el usuario no existe
     */
    @Transactional
    public Post createCheckin(Integer userId, NewCheckinRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + userId));

        Post post = new Post();
        post.setUser(user);
        post.setPostType(PostType.CHECKIN);
        post.setBox(user.getBox());
        post.setTrainingTag(request.getTrainingTag());
        post.setDescription(request.getDescription());
        post.setCreationDate(LocalDateTime.now());

        if (request.getMateUsername() != null && !request.getMateUsername().isBlank()) {
            userRepository.findByUsername(request.getMateUsername())
                    .ifPresent(post::setMate);
        }

        return postRepository.save(post);
    }

    /**
     * Crea contenido publicado por el box del administrador autenticado.
     *
     * @param userId         id del administrador autenticado
     * @param request        datos del contenido de box
     * @return el Post persistido
     * @throws EntityNotFoundException si el usuario no existe
     */
    @Transactional
    public Post createBoxPost(Integer userId, NewBoxPostRequest request) {
        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + userId));

        if (admin.getRole() != Role.BOX_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los administradores de box pueden crear contenido de box");
        }

        if (admin.getBox() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El administrador no tiene un box asignado");
        }

        PostType postType = request.getPostType();
        if (postType == null || !postType.isBoxContent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de post no corresponde a contenido de box");
        }

        if (postType == PostType.BOX_CHALLENGE && request.getChallengeDeadline() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los retos de box requieren una fecha límite");
        }

        Post post = new Post();
        post.setUser(admin);
        post.setBox(admin.getBox());
        post.setPostType(postType);
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setTrainingTag(request.getTrainingTag());
        post.setChallengeDeadline(request.getChallengeDeadline());
        post.setCreationDate(LocalDateTime.now());

        return postRepository.save(post);
    }

    /**
     * Calcula la racha actual de días consecutivos con actividad propia.
     *
     * @param userId id del usuario autenticado
     * @return racha actual del usuario
     */
    public StreakResponse getStreak(Integer userId) {
        List<LocalDate> activityDates = postRepository.findActivityDatesForUser(userId);
        if (activityDates.isEmpty()) {
            return new StreakResponse(0);
        }

        LocalDate today = LocalDate.now();
        LocalDate latestActivity = activityDates.get(0);
        LocalDate expectedDate;

        if (latestActivity.equals(today)) {
            expectedDate = today;
        } else if (latestActivity.equals(today.minusDays(1))) {
            expectedDate = today.minusDays(1);
        } else {
            return new StreakResponse(0);
        }

        int currentStreak = 0;
        for (LocalDate activityDate : activityDates) {
            if (!activityDate.equals(expectedDate)) {
                break;
            }

            currentStreak++;
            expectedDate = expectedDate.minusDays(1);
        }

        return new StreakResponse(currentStreak);
    }

    /**
     * Constancia semanal del usuario a partir de su actividad en BD.
     *
     * @param userId id del usuario
     * @return semanas consecutivas, dots L–D de la semana ISO actual y conteo
     */
    public WeeklyConstancyResponse getWeeklyConstancy(Integer userId) {
        return calculateWeeklyConstancy(postRepository.findActivityDatesForUser(userId));
    }

    /**
     * Calcula constancia semanal a partir de fechas de actividad (días distintos con CHECKIN/RESULT).
     * <p>
     * Semanas ISO (L–D). Una semana cuenta si tiene ≥ {@code minDaysPerWeek} días distintos.
     * Si la semana actual aún no llega al mínimo, la racha se ancla en la anterior.
     *
     * @param activityDates fechas distintas de actividad (cualquier orden)
     * @return streakWeeks, weekActiveDays (L→D) y weekActiveCount
     */
    public WeeklyConstancyResponse calculateWeeklyConstancy(List<LocalDate> activityDates) {
        if (activityDates == null || activityDates.isEmpty()) {
            return new WeeklyConstancyResponse(0, EMPTY_WEEK_DAYS, 0);
        }

        LocalDate today = LocalDate.now();
        IsoWeek currentWeek = IsoWeek.of(today);

        Map<IsoWeek, Set<LocalDate>> daysByWeek = new HashMap<>();
        boolean[] weekActiveDays = new boolean[7];

        for (LocalDate date : activityDates) {
            IsoWeek week = IsoWeek.of(date);
            daysByWeek.computeIfAbsent(week, ignored -> new HashSet<>()).add(date);

            if (week.equals(currentWeek)) {
                weekActiveDays[date.getDayOfWeek().getValue() - 1] = true;
            }
        }

        int weekActiveCount = 0;
        List<Boolean> weekActiveDaysList = new ArrayList<>(7);
        for (boolean active : weekActiveDays) {
            if (active) {
                weekActiveCount++;
            }
            weekActiveDaysList.add(active);
        }

        IsoWeek anchor = daysInWeek(daysByWeek, currentWeek) >= minDaysPerWeek
                ? currentWeek
                : currentWeek.minusWeeks(1);

        int streakWeeks = 0;
        IsoWeek cursor = anchor;
        while (daysInWeek(daysByWeek, cursor) >= minDaysPerWeek) {
            streakWeeks++;
            cursor = cursor.minusWeeks(1);
        }

        return new WeeklyConstancyResponse(
                streakWeeks,
                Collections.unmodifiableList(weekActiveDaysList),
                weekActiveCount
        );
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

    private static int daysInWeek(Map<IsoWeek, Set<LocalDate>> daysByWeek, IsoWeek week) {
        Set<LocalDate> days = daysByWeek.get(week);
        return days == null ? 0 : days.size();
    }

    /**
     * Semana ISO identificada por año basado en semana y número de semana.
     */
    private record IsoWeek(int weekBasedYear, int weekOfYear) {

        static IsoWeek of(LocalDate date) {
            return new IsoWeek(
                    date.get(ISO_WEEK.weekBasedYear()),
                    date.get(ISO_WEEK.weekOfWeekBasedYear())
            );
        }

        IsoWeek minusWeeks(int weeks) {
            LocalDate monday = LocalDate.of(weekBasedYear, 1, 4)
                    .with(ISO_WEEK.weekOfWeekBasedYear(), weekOfYear)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return of(monday.minusWeeks(weeks));
        }
    }

    private static class FriendBuilder {
        final String username;
        final List<FriendTimeHistoryResponse.TimeEntry> totalHistory    = new ArrayList<>();
        final List<FriendTimeHistoryResponse.TimeEntry> workoutsHistory = new ArrayList<>();
        final List<FriendTimeHistoryResponse.TimeEntry> runsHistory     = new ArrayList<>();

        FriendBuilder(String username) { this.username = username; }
    }
}
