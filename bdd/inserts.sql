-- ============================================================
-- SEED DATA — ejecutar con seed.ps1 DESPUÉS de docker compose up
-- ============================================================

-- select * from users;
-- select * from posts;
-- select * from friendships;
-- select * from comments;
-- select * from post_likes;
-- select * from omment_likes;

-- ------------------------------------------------------------
-- 0. Limpiar tablas (excepto users, cities y boxes)
--    Nota: cities/boxes NO se truncan aquí porque users.box_id
--    referencia boxes; un TRUNCATE ... CASCADE sobre boxes arrastraría
--    también la tabla users. En su lugar se insertan de forma idempotente
--    (INSERT ... WHERE NOT EXISTS) en el punto 0b.
-- ------------------------------------------------------------
TRUNCATE TABLE comment_likes, post_likes, comments, post_participants, friendships, posts RESTART IDENTITY CASCADE;

-- ------------------------------------------------------------
-- 0b. Ciudad y box de ejemplo (idempotente)
-- ------------------------------------------------------------
INSERT INTO cities (name)
SELECT 'Madrid'
WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Madrid');

INSERT INTO boxes (name, city_id, address)
SELECT 'CrossFit Origen', c.id, 'Calle del Deporte 12, Madrid'
FROM cities c
WHERE c.name = 'Madrid'
  AND NOT EXISTS (SELECT 1 FROM boxes WHERE name = 'CrossFit Origen');

-- ------------------------------------------------------------
-- 1. Género (el endpoint de registro en bulk no lo persiste)
-- ------------------------------------------------------------
UPDATE users SET gender = 'MALE'   WHERE username IN ('pedro_alonso','marcos_gil','javier_mena','alberto_diaz','roberto_santos','carlos_martin','miguel_torres','david_romero','sergio_molina','fernando_rubio','alejandro_reyes','antonio_lara','jose_guerrero','manuel_cano','raul_pascual');
UPDATE users SET gender = 'FEMALE' WHERE username IN ('lucia_vega','sofia_ramos','nuria_pons','marta_fuentes','isabel_perez','ana_garcia','laura_jimenez','paula_navarro','elena_castro','carmen_ortiz','pilar_moreno','beatriz_herrero','rosa_medina','cristina_vidal','eva_serrano');

-- ------------------------------------------------------------
-- 1b. Box y rol (el endpoint de registro en bulk no los persiste)
-- ------------------------------------------------------------
UPDATE users SET box_id = (SELECT id FROM boxes WHERE name = 'CrossFit Origen');
UPDATE users SET role = 'BOX_ADMIN' WHERE username = 'carlos_martin';

-- ------------------------------------------------------------
-- 2. Amistades — red social de 47 pares
--    LEAST/GREATEST garantiza user_a_id < user_b_id
-- ------------------------------------------------------------
INSERT INTO friendships (user_a_id, user_b_id, status, requester_id)
SELECT LEAST(u1.id,u2.id), GREATEST(u1.id,u2.id), 'FRIEND', u1.id
FROM (VALUES
  ('pedro_alonso','marcos_gil'),
  ('pedro_alonso','javier_mena'),
  ('pedro_alonso','alberto_diaz'),
  ('pedro_alonso','roberto_santos'),
  ('pedro_alonso','carlos_martin'),
  ('lucia_vega','sofia_ramos'),
  ('lucia_vega','nuria_pons'),
  ('lucia_vega','marta_fuentes'),
  ('lucia_vega','isabel_perez'),
  ('lucia_vega','ana_garcia'),
  ('marcos_gil','miguel_torres'),
  ('marcos_gil','david_romero'),
  ('javier_mena','sofia_ramos'),
  ('javier_mena','sergio_molina'),
  ('sofia_ramos','laura_jimenez'),
  ('alberto_diaz','fernando_rubio'),
  ('alberto_diaz','alejandro_reyes'),
  ('roberto_santos','carlos_martin'),
  ('roberto_santos','jose_guerrero'),
  ('nuria_pons','paula_navarro'),
  ('nuria_pons','pilar_moreno'),
  ('marta_fuentes','elena_castro'),
  ('marta_fuentes','beatriz_herrero'),
  ('isabel_perez','carmen_ortiz'),
  ('isabel_perez','rosa_medina'),
  ('carlos_martin','miguel_torres'),
  ('carlos_martin','raul_pascual'),
  ('ana_garcia','laura_jimenez'),
  ('ana_garcia','cristina_vidal'),
  ('ana_garcia','eva_serrano'),
  ('miguel_torres','david_romero'),
  ('laura_jimenez','paula_navarro'),
  ('david_romero','sergio_molina'),
  ('paula_navarro','elena_castro'),
  ('sergio_molina','fernando_rubio'),
  ('elena_castro','carmen_ortiz'),
  ('fernando_rubio','alejandro_reyes'),
  ('carmen_ortiz','rosa_medina'),
  ('alejandro_reyes','antonio_lara'),
  ('pilar_moreno','beatriz_herrero'),
  ('pilar_moreno','cristina_vidal'),
  ('antonio_lara','jose_guerrero'),
  ('antonio_lara','manuel_cano'),
  ('beatriz_herrero','cristina_vidal'),
  ('jose_guerrero','raul_pascual'),
  ('rosa_medina','eva_serrano'),
  ('manuel_cano','raul_pascual')
) AS v(a, b)
JOIN users u1 ON u1.username = v.a
JOIN users u2 ON u2.username = v.b
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------

-- ------------------------------------------------------------
-- 3. Posts de box y check-ins variados (mezcla realista del feed)
--    Fechas recientes (jul–ago 2026) para que salgan arriba en el feed

-- Check-ins recientes de amigos (para mezclar arriba en el feed)
INSERT INTO posts (user_id, post_type, training_tag, description, creation_date)
SELECT u.id, 'CHECKIN', v.tag, v.descr, v.cdate
FROM (VALUES
  ('javier_mena',   'HYROX',  'Open workout casero. Buen ritmo en estaciones.', TIMESTAMP '2026-08-02 08:30:00'),
  ('lucia_vega',    'HYROX',  'Segunda simulación del mes. Wall balls más fluidas.', TIMESTAMP '2026-08-04 09:15:00'),
  ('roberto_santos','HYROX',  'Entrenamiento con marca. Cerca del PB de primavera.', TIMESTAMP '2026-08-06 07:45:00'),
  ('alberto_diaz',  'HYROX',  'Doble con buen ritmo. Transiciones más limpias.', TIMESTAMP '2026-08-07 18:00:00')
) AS v(uname, tag, descr, cdate)
JOIN users u ON u.username = v.uname;

-- CHECKIN: varios usuarios / tags
INSERT INTO posts (user_id, post_type, training_tag, description, creation_date)
SELECT u.id, 'CHECKIN', v.tag, v.descr, v.cdate
FROM (VALUES
  ('pedro_alonso',  'CARRERA', 'Rodaje suave de 8km antes de la sesión de Hyrox. Sensaciones muy buenas.',           TIMESTAMP '2026-05-04 07:00:00'),
  ('lucia_vega',    'FUERZA',  'Sesión de fuerza en el box: sentadilla, press banca y peso muerto.',                TIMESTAMP '2026-05-09 18:30:00'),
  ('marcos_gil',    'HYROX',   'Simulación corta de estaciones. Ski + sled + wall balls. Piernas hechas papilla.', TIMESTAMP '2026-07-28 07:15:00'),
  ('javier_mena',   'CLASE',   'Clase de technique con el coach. Mucho foco en farmers carry.',                    TIMESTAMP '2026-07-29 19:00:00'),
  ('sofia_ramos',   'CARRERA', 'Intervalos 8x400. Ritmo alto, recuperación activa.',                               TIMESTAMP '2026-07-30 08:00:00'),
  ('alberto_diaz',  'FUERZA',  'Empuje: press militar + fondos. Buenas sensaciones de hombro.',                    TIMESTAMP '2026-07-31 18:45:00'),
  ('roberto_santos','DESCANSO_ACTIVO', 'Movilidad y core. Día de descarga después del open workout.',                      TIMESTAMP '2026-08-01 09:30:00'),
  ('lucia_vega',    'HYROX',   'Circuito HYROX express en el box. 4 estaciones a tope.',                           TIMESTAMP '2026-08-02 10:00:00'),
  ('nuria_pons',    'CLASE',   'Primera clase de la semana. Motivación al máximo.',                                TIMESTAMP '2026-08-03 07:45:00'),
  ('miguel_torres', 'CARRERA', 'Fartlek por el parque. 45 minutos sin mirar el reloj.',                            TIMESTAMP '2026-08-04 07:00:00'),
  ('ana_garcia',    'FUERZA',  'Peso muerto + hip thrust. Volumen moderado.',                                      TIMESTAMP '2026-08-05 18:20:00'),
  ('pedro_alonso',  'HYROX',   'Ensayo de transición entre estaciones. Cada segundo cuenta.',                      TIMESTAMP '2026-08-06 08:10:00'),
  ('marcos_gil',    'DESCANSO_ACTIVO', 'Sesión de movilidad y foam roller. Recuperación activa.',                          TIMESTAMP '2026-08-07 20:00:00'),
  ('javier_mena',   'CARRERA', 'Tempo run 6km. Ritmo cómodo-rápido, buen feeling.',                                TIMESTAMP '2026-08-08 07:30:00')
) AS v(uname, tag, descr, cdate)
JOIN users u ON u.username = v.uname;

-- Constancia semanal de demo (javier_mena): ≥3 días/semana en W29–W32
-- Semana actual (W32, 3–9 ago 2026): L–J + S → dots parciales y racha visible en el feed
INSERT INTO posts (user_id, post_type, training_tag, description, creation_date)
SELECT u.id, 'CHECKIN', v.tag, v.descr, v.cdate
FROM (VALUES
  -- W29 (13–19 jul)
  ('javier_mena', 'FUERZA',  'Fuerza: sentadilla + press. Semana de volumen.',           TIMESTAMP '2026-07-14 18:30:00'),
  ('javier_mena', 'HYROX',   'Circuito corto de estaciones en el box.',                  TIMESTAMP '2026-07-15 07:15:00'),
  ('javier_mena', 'CARRERA', 'Rodaje 7km zona 2.',                                       TIMESTAMP '2026-07-17 07:00:00'),
  -- W30 (20–26 jul)
  ('javier_mena', 'CLASE',   'Clase de technique. Farmers y lunges.',                    TIMESTAMP '2026-07-21 19:00:00'),
  ('javier_mena', 'FUERZA',  'Peso muerto + hip thrust.',                                TIMESTAMP '2026-07-22 18:45:00'),
  ('javier_mena', 'CARRERA', 'Intervalos 6x400.',                                        TIMESTAMP '2026-07-24 08:00:00'),
  -- W31 (27 jul–2 ago) — Jul 29 ya existe arriba; sumamos 2 más para llegar a N
  ('javier_mena', 'HYROX',   'Ski + sled + wall balls. Piernas hechas.',                 TIMESTAMP '2026-07-28 07:20:00'),
  ('javier_mena', 'DESCANSO_ACTIVO', 'Movilidad y core. Descarga activa.',                       TIMESTAMP '2026-07-31 09:00:00'),
  -- W32 (3–9 ago) — Aug 8 ya existe; dots L M X J · · S
  ('javier_mena', 'FUERZA',  'Empuje: press militar + fondos.',                          TIMESTAMP '2026-08-03 18:30:00'),
  ('javier_mena', 'CARRERA', 'Fartlek 40 min por el parque.',                            TIMESTAMP '2026-08-04 07:10:00'),
  ('javier_mena', 'HYROX',   'Simulación de transiciones entre estaciones.',             TIMESTAMP '2026-08-05 08:00:00'),
  ('javier_mena', 'CLASE',   'Clase matinal. Buen feeling de piernas.',                  TIMESTAMP '2026-08-06 07:40:00')
) AS v(uname, tag, descr, cdate)
JOIN users u ON u.username = v.uname;

-- Constancia semanal de demo (marcos_gil): 3 días/semana en W29–W32 (racha 4 semanas)
-- Reutiliza Jul 28 y Aug 7 (CHECKIN) ya insertados arriba
INSERT INTO posts (user_id, post_type, training_tag, description, creation_date)
SELECT u.id, 'CHECKIN', v.tag, v.descr, v.cdate
FROM (VALUES
  -- W29 (13–19 jul)
  ('marcos_gil', 'FUERZA',  'Fuerza: sentadilla y press. Arranque de bloque.',          TIMESTAMP '2026-07-13 18:30:00'),
  ('marcos_gil', 'CARRERA', 'Rodaje 8km zona 2. Piernas frescas.',                      TIMESTAMP '2026-07-15 07:00:00'),
  ('marcos_gil', 'HYROX',   'Circuito corto: ski + sled + wall balls.',                 TIMESTAMP '2026-07-17 07:20:00'),
  -- W30 (20–26 jul)
  ('marcos_gil', 'CLASE',   'Clase de technique. Farmers y lunges.',                    TIMESTAMP '2026-07-21 19:00:00'),
  ('marcos_gil', 'FUERZA',  'Peso muerto + hip thrust. Volumen moderado.',              TIMESTAMP '2026-07-23 18:45:00'),
  ('marcos_gil', 'HYROX',   'Simulación completa de sábado. Buenas sensaciones.',       TIMESTAMP '2026-07-26 09:00:00'),
  -- W31 (27 jul–2 ago) — Jul 28 ya existe como CHECKIN
  ('marcos_gil', 'CARRERA', 'Intervalos 6x400. Ritmo alto.',                            TIMESTAMP '2026-07-30 08:00:00'),
  ('marcos_gil', 'HYROX',   'Simulación de transiciones entre estaciones.',             TIMESTAMP '2026-08-01 09:00:00'),
  -- W32 (3–9 ago) — Aug 7 ya existe; hoy (sáb 8) cierra la semana
  ('marcos_gil', 'FUERZA',  'Empuje: press militar + fondos.',                          TIMESTAMP '2026-08-04 18:30:00'),
  ('marcos_gil', 'CARRERA', 'Tempo run 6km. Buen feeling hasta hoy.',                   TIMESTAMP '2026-08-08 08:15:00')
) AS v(uname, tag, descr, cdate)
JOIN users u ON u.username = v.uname;

-- BOX_WOD
INSERT INTO posts (user_id, post_type, box_id, title, training_tag, description, creation_date)
SELECT u.id, 'BOX_WOD', b.id, v.title, v.tag, v.descr, v.cdate
FROM (VALUES
  ('WOD de la semana: Hyrox Simulation', 'HYROX',
   '8x(500m row + 40 wall balls). Series completas con 2 min de descanso entre rondas.',
   TIMESTAMP '2026-05-05 06:00:00'),
  ('WOD lunes: Engine day', 'CARRERA',
   'AMRAP 20: 400m run + 15 burpees + 20 air squats. Ritmo sostenible, sin parar.',
   TIMESTAMP '2026-07-28 06:30:00'),
  ('WOD miércoles: Strength + metcon', 'FUERZA',
   '1) Back squat 5x5  2) 4 rondas: 12 thrusters + 15 cal row + 10 pull-ups.',
   TIMESTAMP '2026-07-30 06:30:00'),
  ('WOD viernes: Partner Hyrox', 'HYROX',
   'Por parejas: 1km run + ski + sled push + burpee BJ. Un atleta trabaja, el otro recupera.',
   TIMESTAMP '2026-08-01 06:30:00'),
  ('WOD sábado: Open floor', 'OTRO',
   'Libre: elige 3 estaciones HYROX y completa 3 rondas. Coach disponible para técnica.',
   TIMESTAMP '2026-08-02 09:00:00'),
  ('WOD martes: Wall balls hell', 'HYROX',
   'E2MOM 16: 20 wall balls + 10 cal ski. Si no terminas a tiempo, el resto es descanso.',
   TIMESTAMP '2026-08-05 06:30:00'),
  ('WOD de hoy: Mixed modal', 'CLASE',
   'For time: 21-15-9 thrusters y pull-ups, luego 400m run. Cap 15 min.',
   TIMESTAMP '2026-08-07 06:30:00')
) AS v(title, tag, descr, cdate)
CROSS JOIN users u
JOIN boxes b ON b.name = 'CrossFit Origen'
WHERE u.username = 'carlos_martin';

-- CHECKIN vinculados al WOD (sigue existiendo el vínculo; el muro muestra participantes)
UPDATE posts p
SET wod_post_id = w.id,
    box_id = w.box_id
FROM posts w, users u
WHERE w.post_type = 'BOX_WOD'
  AND w.title = 'WOD de hoy: Mixed modal'
  AND p.post_type = 'CHECKIN'
  AND p.user_id = u.id
  AND u.username = 'marcos_gil'
  AND p.creation_date = TIMESTAMP '2026-08-07 20:00:00';

INSERT INTO posts (user_id, post_type, box_id, training_tag, description, wod_post_id, creation_date)
SELECT u.id, 'CHECKIN', w.box_id, v.tag, v.descr, w.id, v.cdate
FROM (VALUES
  ('pedro_alonso', 'CLASE',  'Hecho el Mixed modal del box. Cap en 14:20.', TIMESTAMP '2026-08-07 19:15:00'),
  ('lucia_vega',   'HYROX',  'Mixed modal con Lucia. Buenas sensaciones.',  TIMESTAMP '2026-08-07 19:45:00'),
  ('javier_mena',  'CLASE',  'WOD Mixed modal completado. Cap justo.',      TIMESTAMP '2026-08-07 18:30:00')
) AS v(uname, tag, descr, cdate)
JOIN users u ON u.username = v.uname
JOIN posts w ON w.post_type = 'BOX_WOD' AND w.title = 'WOD de hoy: Mixed modal';

-- Participantes del WOD de hoy (muro RSVP)
INSERT INTO post_participants (post_id, user_id, joined_at)
SELECT w.id, part.id, v.joined
FROM posts w
CROSS JOIN (VALUES
  ('marcos_gil',   TIMESTAMP '2026-08-07 07:00:00'),
  ('lucia_vega',   TIMESTAMP '2026-08-07 07:15:00'),
  ('pedro_alonso', TIMESTAMP '2026-08-07 07:30:00'),
  ('javier_mena',  TIMESTAMP '2026-08-07 08:00:00')
) AS v(username, joined)
JOIN users part ON part.username = v.username
WHERE w.post_type = 'BOX_WOD'
  AND w.title = 'WOD de hoy: Mixed modal';

-- BOX_ANNOUNCEMENT
INSERT INTO posts (user_id, post_type, box_id, title, description, creation_date)
SELECT u.id, 'BOX_ANNOUNCEMENT', b.id, v.title, v.descr, v.cdate
FROM (VALUES
  ('Nuevo horario de clases',
   'A partir del lunes, la clase de las 19:00 pasa a las 19:30. ¡Gracias por vuestra paciencia!',
   TIMESTAMP '2026-05-06 09:00:00'),
  ('Cerrado el 15 de agosto',
   'El box permanecerá cerrado el viernes 15 por festivo. Reabrimos el sábado a las 09:00.',
   TIMESTAMP '2026-07-29 12:00:00'),
  ('Open day para amig@s',
   'El domingo 10 trae a un amigo gratis a la clase de las 11:00. Cupo limitado: avisa en recepción.',
   TIMESTAMP '2026-08-03 10:00:00'),
  ('Camisetas del box disponibles',
   'Ya podéis encargar la camiseta oficial 2026 en recepción. Plazo hasta el 20 de agosto.',
   TIMESTAMP '2026-08-04 16:00:00'),
  ('Cambio de coach en la clase de las 07:00',
   'Durante dos semanas, Laura sustituye a Miguel en la clase matinal. ¡Misma energía!',
   TIMESTAMP '2026-08-06 08:00:00')
) AS v(title, descr, cdate)
CROSS JOIN users u
JOIN boxes b ON b.name = 'CrossFit Origen'
WHERE u.username = 'carlos_martin';

-- BOX_CHALLENGE
INSERT INTO posts (user_id, post_type, box_id, title, challenge_deadline, description, creation_date)
SELECT u.id, 'BOX_CHALLENGE', b.id, v.title, v.deadline::timestamptz, v.descr, v.cdate
FROM (VALUES
  ('Reto de mayo: 100 wall balls seguidas',
   '2026-05-31 23:59:00+02',
   'Quien complete 100 wall balls sin soltar la pelota se lleva una camiseta del box. ¡Apuntaos!',
   TIMESTAMP '2026-05-07 08:00:00'),
  ('Reto de agosto: 5k sub-25',
   '2026-08-31 23:59:00+02',
   'Corre 5km por debajo de 25 minutos (o tu marca personal). Sube foto del reloj en comentarios.',
   TIMESTAMP '2026-08-01 08:00:00'),
  ('Reto team: 10.000 cal colectivas',
   '2026-08-20 23:59:00+02',
   'Entre todos los apuntados sumamos 10.000 calorías en ski/row. Cada uno aporta lo que pueda.',
   TIMESTAMP '2026-08-03 09:00:00'),
  ('Reto técnica: unbroken farmers',
   '2026-09-15 23:59:00+02',
   '200m farmers carry sin soltar. Peso según categoría. Coach valida en clase.',
   TIMESTAMP '2026-08-07 11:00:00')
) AS v(title, deadline, descr, cdate)
CROSS JOIN users u
JOIN boxes b ON b.name = 'CrossFit Origen'
WHERE u.username = 'carlos_martin';

-- ------------------------------------------------------------
-- ------------------------------------------------------------
-- 4. Comentarios (sobre check-ins)
-- ------------------------------------------------------------
INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id, u.id, v.content, v.cdate
FROM (VALUES
  ('marcos_gil',  TIMESTAMP '2026-07-28 07:15:00', 'lucia_vega',    'Que sesion mas dura! Respeto total.', TIMESTAMP '2026-07-28 10:00:00'),
  ('javier_mena', TIMESTAMP '2026-07-29 19:00:00', 'pedro_alonso',  'Esa clase de technique marca la diferencia.', TIMESTAMP '2026-07-29 20:30:00'),
  ('sofia_ramos', TIMESTAMP '2026-07-30 08:00:00', 'marcos_gil',    'Esos intervalos se notan luego en la carrera.', TIMESTAMP '2026-07-30 12:00:00'),
  ('lucia_vega',  TIMESTAMP '2026-08-02 10:00:00', 'javier_mena',   'Circuito express y con muy buenas sensaciones.', TIMESTAMP '2026-08-02 11:15:00'),
  ('pedro_alonso',TIMESTAMP '2026-08-06 08:10:00', 'alberto_diaz',  'Las transiciones son oro puro.', TIMESTAMP '2026-08-06 09:00:00'),
  ('javier_mena', TIMESTAMP '2026-08-08 07:30:00', 'marcos_gil',    'Buen tempo. A por la siguiente semana.', TIMESTAMP '2026-08-08 09:00:00')
) AS v(author, pdate, commenter, content, cdate)
JOIN users a ON a.username = v.author
JOIN posts p ON p.user_id = a.id AND p.creation_date = v.pdate
JOIN users u ON u.username = v.commenter;

-- ------------------------------------------------------------
-- 5. Likes en posts (check-ins)
-- ------------------------------------------------------------
INSERT INTO post_likes (post_id, user_id)
SELECT p.id, u.id
FROM (VALUES
  ('marcos_gil',  TIMESTAMP '2026-07-28 07:15:00', 'pedro_alonso'),
  ('marcos_gil',  TIMESTAMP '2026-07-28 07:15:00', 'lucia_vega'),
  ('javier_mena', TIMESTAMP '2026-07-29 19:00:00', 'sofia_ramos'),
  ('lucia_vega',  TIMESTAMP '2026-08-02 10:00:00', 'marcos_gil'),
  ('pedro_alonso',TIMESTAMP '2026-08-06 08:10:00', 'javier_mena'),
  ('javier_mena', TIMESTAMP '2026-08-08 07:30:00', 'lucia_vega'),
  ('alberto_diaz',TIMESTAMP '2026-08-07 18:00:00', 'pedro_alonso')
) AS v(author, pdate, liker)
JOIN users a ON a.username = v.author
JOIN posts p ON p.user_id = a.id AND p.creation_date = v.pdate
JOIN users u ON u.username = v.liker;

-- ------------------------------------------------------------
-- 6. Participantes de retos de box
-- ------------------------------------------------------------
INSERT INTO post_participants (post_id, user_id, joined_at)
SELECT p.id, part.id, v.joined
FROM posts p
CROSS JOIN (VALUES
  ('pedro_alonso', TIMESTAMP '2026-05-07 10:00:00'),
  ('lucia_vega',   TIMESTAMP '2026-05-07 11:30:00'),
  ('marcos_gil',   TIMESTAMP '2026-05-08 08:00:00'),
  ('sofia_ramos',  TIMESTAMP '2026-05-08 09:15:00')
) AS v(username, joined)
JOIN users part ON part.username = v.username
WHERE p.post_type = 'BOX_CHALLENGE'
  AND p.creation_date = TIMESTAMP '2026-05-07 08:00:00';

INSERT INTO post_participants (post_id, user_id, joined_at)
SELECT p.id, part.id, v.joined
FROM posts p
CROSS JOIN (VALUES
  ('pedro_alonso',  TIMESTAMP '2026-08-01 10:00:00'),
  ('marcos_gil',    TIMESTAMP '2026-08-01 11:00:00'),
  ('javier_mena',   TIMESTAMP '2026-08-02 08:00:00'),
  ('lucia_vega',    TIMESTAMP '2026-08-02 12:00:00'),
  ('roberto_santos',TIMESTAMP '2026-08-03 09:00:00'),
  ('ana_garcia',    TIMESTAMP '2026-08-04 18:00:00')
) AS v(username, joined)
JOIN users part ON part.username = v.username
WHERE p.post_type = 'BOX_CHALLENGE'
  AND p.creation_date = TIMESTAMP '2026-08-01 08:00:00';

INSERT INTO post_participants (post_id, user_id, joined_at)
SELECT p.id, part.id, v.joined
FROM posts p
CROSS JOIN (VALUES
  ('pedro_alonso', TIMESTAMP '2026-08-03 10:30:00'),
  ('marcos_gil',   TIMESTAMP '2026-08-03 11:00:00'),
  ('miguel_torres',TIMESTAMP '2026-08-04 07:30:00'),
  ('sofia_ramos',  TIMESTAMP '2026-08-05 09:00:00')
) AS v(username, joined)
JOIN users part ON part.username = v.username
WHERE p.post_type = 'BOX_CHALLENGE'
  AND p.creation_date = TIMESTAMP '2026-08-03 09:00:00';

INSERT INTO post_participants (post_id, user_id, joined_at)
SELECT p.id, part.id, v.joined
FROM posts p
CROSS JOIN (VALUES
  ('javier_mena', TIMESTAMP '2026-08-07 12:00:00'),
  ('alberto_diaz',TIMESTAMP '2026-08-07 13:00:00')
) AS v(username, joined)
JOIN users part ON part.username = v.username
WHERE p.post_type = 'BOX_CHALLENGE'
  AND p.creation_date = TIMESTAMP '2026-08-07 11:00:00';
