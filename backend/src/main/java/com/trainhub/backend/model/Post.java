package com.trainhub.backend.model;

import com.trainhub.backend.enums.PostType;
import com.trainhub.backend.enums.TrainingTag;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Entidad que representa una publicación en el sistema.
 * Mapeada a la tabla "posts" de la base de datos.
 */
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private PostType postType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    private Box box;

    @Column(name = "title", length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_tag", length = 20)
    private TrainingTag trainingTag;

    @Column(name = "challenge_deadline")
    private OffsetDateTime challengeDeadline;

    @Column(name = "description")
    private String description;

    /** WOD de box al que se vincula un CHECKIN (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wod_post_id")
    private Post wodPost;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    public Post() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public Box getBox() { return box; }
    public void setBox(Box box) { this.box = box; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public TrainingTag getTrainingTag() { return trainingTag; }
    public void setTrainingTag(TrainingTag trainingTag) { this.trainingTag = trainingTag; }

    public OffsetDateTime getChallengeDeadline() { return challengeDeadline; }
    public void setChallengeDeadline(OffsetDateTime challengeDeadline) { this.challengeDeadline = challengeDeadline; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Post getWodPost() { return wodPost; }
    public void setWodPost(Post wodPost) { this.wodPost = wodPost; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
}
