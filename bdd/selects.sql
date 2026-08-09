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

