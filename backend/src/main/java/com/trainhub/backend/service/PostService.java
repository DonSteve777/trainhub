package com.trainhub.backend.service;

import com.trainhub.backend.dto.request.NewCheckinRequest;
import com.trainhub.backend.dto.request.NewBoxPostRequest;
import com.trainhub.backend.dto.response.BoxWodSummaryResponse;
import com.trainhub.backend.dto.response.StreakResponse;
import com.trainhub.backend.dto.response.WeeklyConstancyResponse;
import com.trainhub.backend.enums.PostType;
import com.trainhub.backend.enums.Role;
import com.trainhub.backend.enums.TrainingTag;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.PostRepository;
import com.trainhub.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final List<TrainingTag> EMPTY_WEEK_TAGS = Collections.unmodifiableList(
            Arrays.asList(new TrainingTag[7])
    );

    /**
     * Día de actividad con su tipo de entrenamiento (como máximo uno por fecha).
     */
    public record ActivityDay(LocalDate date, TrainingTag tag) {}

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
     * Crea un check-in de entrenamiento.
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

        if (request.getWodPostId() != null) {
            Post wod = postRepository.findById(request.getWodPostId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "WOD no encontrado"));

            if (wod.getPostType() != PostType.BOX_WOD) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El post indicado no es un WOD de box");
            }

            Integer userBoxId = user.getBox() != null ? user.getBox().getId() : null;
            Integer wodBoxId = wod.getBox() != null ? wod.getBox().getId() : null;
            if (userBoxId == null || wodBoxId == null || !userBoxId.equals(wodBoxId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El WOD no pertenece al box del usuario");
            }

            if (postRepository.existsByUserIdAndWodPostId(userId, wod.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya has hecho check-in de este WOD");
            }

            post.setWodPost(wod);
        }

        return postRepository.save(post);
    }

    /**
     * Lista WODs recientes del box del usuario para el selector de check-in.
     *
     * @param userId id del usuario autenticado
     * @return resumen de hasta 10 WODs (más recientes primero); lista vacía sin box
     */
    public List<BoxWodSummaryResponse> listRecentBoxWods(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + userId));

        if (user.getBox() == null) {
            return List.of();
        }

        LocalDateTime since = LocalDateTime.of(2000, 1, 1, 0, 0);
        return postRepository
                .findRecentBoxWods(user.getBox().getId(), since, PageRequest.of(0, 10))
                .stream()
                .map(wod -> new BoxWodSummaryResponse(
                        wod.getId(),
                        wod.getTitle(),
                        wod.getDescription(),
                        wod.getTrainingTag(),
                        wod.getCreationDate()
                ))
                .collect(Collectors.toList());
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
        List<LocalDate> activityDates = mapActivityDays(postRepository.findActivityDaysForUser(userId))
                .stream()
                .map(ActivityDay::date)
                .toList();
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
     * @return semanas consecutivas, tags L–D de la semana ISO actual y conteo
     */
    public WeeklyConstancyResponse getWeeklyConstancy(Integer userId) {
        return calculateWeeklyConstancy(mapActivityDays(postRepository.findActivityDaysForUser(userId)));
    }

    public static ActivityDay toActivityDay(Object dateValue, Object tagValue, Object postTypeValue) {
        return new ActivityDay(toLocalDate(dateValue), resolveTrainingTag(tagValue, postTypeValue));
    }

    private static List<ActivityDay> mapActivityDays(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<ActivityDay> days = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            days.add(toActivityDay(row[0], row[1], row[2]));
        }
        return days;
    }

    private static TrainingTag resolveTrainingTag(Object tagValue, Object postTypeValue) {
        TrainingTag tag = null;
        if (tagValue instanceof TrainingTag trainingTag) {
            tag = trainingTag;
        } else if (tagValue instanceof String s && !s.isBlank()) {
            tag = TrainingTag.valueOf(s);
        }
        if (tag != null) {
            return tag;
        }
        return TrainingTag.OTRO;
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.util.Date utilDate) {
            return new Date(utilDate.getTime()).toLocalDate();
        }
        throw new IllegalArgumentException("No se puede convertir a LocalDate: " + value);
    }

    /**
     * Calcula constancia semanal a partir de días de actividad (CHECKIN).
     * <p>
     * Semanas ISO (L–D). Una semana cuenta si tiene ≥ {@code minDaysPerWeek} días distintos.
     * Si la semana actual aún no llega al mínimo, la racha se ancla en la anterior.
     *
     * @param activityDays días de actividad con tag (cualquier orden; ≤1 por fecha)
     * @return streakWeeks, weekDayTags (L→D) y weekActiveCount
     */
    public WeeklyConstancyResponse calculateWeeklyConstancy(List<ActivityDay> activityDays) {
        if (activityDays == null || activityDays.isEmpty()) {
            return new WeeklyConstancyResponse(0, EMPTY_WEEK_TAGS, 0);
        }

        LocalDate today = LocalDate.now();
        IsoWeek currentWeek = IsoWeek.of(today);

        Map<IsoWeek, Set<LocalDate>> daysByWeek = new HashMap<>();
        TrainingTag[] weekDayTags = new TrainingTag[7];

        for (ActivityDay day : activityDays) {
            LocalDate date = day.date();
            IsoWeek week = IsoWeek.of(date);
            daysByWeek.computeIfAbsent(week, ignored -> new HashSet<>()).add(date);

            if (week.equals(currentWeek)) {
                weekDayTags[date.getDayOfWeek().getValue() - 1] = day.tag();
            }
        }

        int weekActiveCount = 0;
        List<TrainingTag> weekDayTagsList = new ArrayList<>(7);
        for (TrainingTag tag : weekDayTags) {
            if (tag != null) {
                weekActiveCount++;
            }
            weekDayTagsList.add(tag);
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
                Collections.unmodifiableList(weekDayTagsList),
                weekActiveCount
        );
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

}
