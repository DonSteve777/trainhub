-- Migración: Muro del WOD (check-in → BOX_WOD)
-- Ejecutar si la BD ya existía antes de schema.sql con wod_post_id.

ALTER TABLE posts
  ADD COLUMN IF NOT EXISTS wod_post_id INT;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'fk_posts_wod'
  ) THEN
    ALTER TABLE posts
      ADD CONSTRAINT fk_posts_wod
      FOREIGN KEY (wod_post_id) REFERENCES posts (id) ON DELETE SET NULL;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_posts_wod_post_id ON posts (wod_post_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_posts_user_wod_checkin
  ON posts (user_id, wod_post_id)
  WHERE wod_post_id IS NOT NULL;
