package com.trainhub.backend.util;

import com.trainhub.backend.enums.AccountStatus;
import com.trainhub.backend.enums.PostType;
import com.trainhub.backend.enums.TrainingTag;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.model.User;

import java.time.LocalDateTime;

/**
 * Factorías de datos de prueba reutilizables entre tests.
 */
public final class TestFixtures {

    private TestFixtures() {}

    public static User aUser(Integer id, String username) {
        User user = new User("user" + id + "@test.com", username, "hash", AccountStatus.ACTIVE);
        user.setId(id);
        user.setPhotoUrl(null);
        return user;
    }

    public static Post aPost(Integer id, User user, LocalDateTime creationDate) {
        Post post = new Post();
        post.setId(id);
        post.setUser(user);
        post.setPostType(PostType.CHECKIN);
        post.setTrainingTag(TrainingTag.HYROX);
        post.setCreationDate(creationDate);
        return post;
    }

    /** Fila simulada del GROUP BY que devuelven countByPostIds */
    public static Object[] likeRow(Integer postId, long count) {
        return new Object[]{postId, count};
    }

    public static Object[] commentRow(Integer postId, long count) {
        return new Object[]{postId, count};
    }
}
