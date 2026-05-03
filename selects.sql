SELECT * FROM posts;
SELECT * FROM users;
SELECT * FROM friendships;
SELECT * FROM comment_likes;


-- ============================================================
-- posts de un usuario
-- ============================================================
SELECT *
FROM posts
WHERE user_id = 108
ORDER BY creation_date DESC;

-- ============================================================
-- historico de un usuario
-- ============================================================

SELECT
    p.total_time,
    p.running1, p.running2, p.running3, p.running4,
    p.running5, p.running6, p.running7, p.running8,
    p.ski_erg, p.sled_push, p.sled_pull, p.burpee_broad_jump,
    p.row, p.farmers_carry, p.sandbag_lunges, p.wall_balls,
    p.creation_date
FROM posts p
WHERE p.user_id = 107
ORDER BY p.creation_date ASC;


-- ============================================================
-- 1. Mis Likes a post 
-- ============================================================
SELECT * FROM post_likes WHERE user_id = 101;


-- ============================================================
-- 0. Mis amigos
-- 101 -> 93adones@
-- 95  ->  javier.mena@example.com
-- 96  -> nuria.pons@example.com
-- ============================================================
SELECT u.id, u.username, u.email, f.status
FROM friendships f
JOIN users u
  ON u.id = CASE
              WHEN f.user_a_id = 104 THEN f.user_b_id
              ELSE f.user_a_id
            END
WHERE f.user_a_id = 104
   OR f.user_b_id = 104;

