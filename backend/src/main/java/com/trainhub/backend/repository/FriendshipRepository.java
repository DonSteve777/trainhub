package com.trainhub.backend.repository;

import com.trainhub.backend.enums.FriendshipStatus;
import com.trainhub.backend.model.Friendship;
import com.trainhub.backend.model.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.id.userAId = :userAId AND f.id.userBId = :userBId)
               OR (f.id.userAId = :userBId AND f.id.userBId = :userAId)
            """)
    Optional<Friendship> findBetween(
            @Param("userAId") Integer userAId,
            @Param("userBId") Integer userBId);

    @Query("""
            SELECT COUNT(f) > 0 FROM Friendship f
            WHERE f.status = :status
            AND (
                (f.id.userAId = :userAId AND f.id.userBId = :userBId)
                OR
                (f.id.userAId = :userBId AND f.id.userBId = :userAId)
            )
            """)
    boolean areFriends(
            @Param("userAId") Integer userAId,
            @Param("userBId") Integer userBId,
            @Param("status") FriendshipStatus status);
}
