package com.trainhub.backend.repository;

import com.trainhub.backend.enums.FriendshipStatus;
import com.trainhub.backend.model.Friendship;
import com.trainhub.backend.model.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("""
            SELECT f.requesterId, f.createdAt, u.username, u.photoUrl
            FROM Friendship f
            JOIN User u ON u.id = f.requesterId
            WHERE f.status = com.trainhub.backend.enums.FriendshipStatus.PENDING
              AND (f.id.userAId = :userId OR f.id.userBId = :userId)
              AND f.requesterId <> :userId
            ORDER BY f.createdAt DESC
            """)
    List<Object[]> findPendingRequestsForUser(@Param("userId") Integer userId);
}
