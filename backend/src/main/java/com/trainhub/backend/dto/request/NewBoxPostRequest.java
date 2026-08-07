package com.trainhub.backend.dto.request;

import com.trainhub.backend.enums.PostType;
import com.trainhub.backend.enums.TrainingTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * DTO para la solicitud de creación de contenido publicado por un box.
 */
public class NewBoxPostRequest {

    @NotNull private PostType postType;
    @NotBlank @Size(max = 150) private String title;
    @NotBlank private String description;
    private TrainingTag trainingTag;
    private OffsetDateTime challengeDeadline;

    public NewBoxPostRequest() {}

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TrainingTag getTrainingTag() { return trainingTag; }
    public void setTrainingTag(TrainingTag trainingTag) { this.trainingTag = trainingTag; }

    public OffsetDateTime getChallengeDeadline() { return challengeDeadline; }
    public void setChallengeDeadline(OffsetDateTime challengeDeadline) { this.challengeDeadline = challengeDeadline; }
}
