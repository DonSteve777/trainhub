-- ============================================================
-- SEED DATA — ejecutar con seed.ps1 (Windows) o seed.sh (bash) DESPUÉS de docker compose up
-- ============================================================

-- select * from users;
-- select * from posts;
-- select * from friendships;
-- select * from comments;
-- select * from post_likes;
-- select * from comment_likes;

-- ------------------------------------------------------------
-- 0. Limpiar tablas (excepto users, cities y boxes)
--    Nota: cities/boxes NO se truncan aquí porque users.box_id
--    referencia boxes; un TRUNCATE ... CASCADE sobre boxes arrastraría
--    también la tabla users. En su lugar se insertan de forma idempotente
--    (INSERT ... WHERE NOT EXISTS) en el punto 0b.
-- ------------------------------------------------------------
TRUNCATE TABLE comment_likes, post_likes, comments, post_participants, friendships, posts,
               goal_marks, goal_participants, goals RESTART IDENTITY CASCADE;

-- ------------------------------------------------------------
-- 0b. Ciudades y boxes (idempotente)
-- ------------------------------------------------------------
INSERT INTO cities (name)
SELECT 'Madrid'
WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Madrid');

INSERT INTO cities (name)
SELECT 'Barcelona'
WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Barcelona');

INSERT INTO cities (name)
SELECT 'Valencia'
WHERE NOT EXISTS (SELECT 1 FROM cities WHERE name = 'Valencia');

INSERT INTO boxes (name, city_id, address)
SELECT 'CrossFit Origen', c.id, 'Calle del Deporte 12, Madrid'
FROM cities c
WHERE c.name = 'Madrid'
  AND NOT EXISTS (SELECT 1 FROM boxes WHERE name = 'CrossFit Origen');

INSERT INTO boxes (name, city_id, address)
SELECT 'Box Norte BCN', c.id, 'Carrer de la Indústria 45, Barcelona'
FROM cities c
WHERE c.name = 'Barcelona'
  AND NOT EXISTS (SELECT 1 FROM boxes WHERE name = 'Box Norte BCN');

INSERT INTO boxes (name, city_id, address)
SELECT 'Hyrox Valencia Center', c.id, 'Avenida del Puerto 88, Valencia'
FROM cities c
WHERE c.name = 'Valencia'
  AND NOT EXISTS (SELECT 1 FROM boxes WHERE name = 'Hyrox Valencia Center');

-- ------------------------------------------------------------
-- 1. Género (el endpoint de registro en bulk no lo persiste)
-- ------------------------------------------------------------
UPDATE users SET gender = 'MALE'   WHERE username IN ('pedro_alonso','marcos_gil','javier_mena','alberto_diaz','roberto_santos','carlos_martin','miguel_torres','david_romero','sergio_molina','fernando_rubio','alejandro_reyes','antonio_lara','jose_guerrero','manuel_cano','raul_pascual');
UPDATE users SET gender = 'FEMALE' WHERE username IN ('lucia_vega','sofia_ramos','nuria_pons','marta_fuentes','isabel_perez','ana_garcia','laura_jimenez','paula_navarro','elena_castro','carmen_ortiz','pilar_moreno','beatriz_herrero','rosa_medina','cristina_vidal','eva_serrano');

-- ------------------------------------------------------------
-- 1b. Boxes, roles y marca de notificaciones (demo pedro_alonso)
--    Madrid / CrossFit Origen — círculo principal de demo
--    Barcelona / Box Norte BCN — segundo admin
--    Valencia / Hyrox Valencia Center
-- ------------------------------------------------------------
UPDATE users SET role = 'USER', box_id = NULL;

UPDATE users SET box_id = (SELECT id FROM boxes WHERE name = 'CrossFit Origen')
WHERE username IN (
  'pedro_alonso','lucia_vega','marcos_gil','sofia_ramos','javier_mena',
  'nuria_pons','alberto_diaz','marta_fuentes','roberto_santos','isabel_perez',
  'carlos_martin','ana_garcia','miguel_torres','laura_jimenez'
);

UPDATE users SET box_id = (SELECT id FROM boxes WHERE name = 'Box Norte BCN')
WHERE username IN (
  'david_romero','paula_navarro','sergio_molina','elena_castro',
  'fernando_rubio','carmen_ortiz','alejandro_reyes','pilar_moreno'
);

UPDATE users SET box_id = (SELECT id FROM boxes WHERE name = 'Hyrox Valencia Center')
WHERE username IN (
  'antonio_lara','beatriz_herrero','jose_guerrero','rosa_medina',
  'manuel_cano','cristina_vidal','raul_pascual','eva_serrano'
);

UPDATE users SET role = 'BOX_ADMIN' WHERE username IN ('carlos_martin', 'elena_castro');

-- Notificaciones no leídas al entrar como pedro (última visita a mediados de agosto)
UPDATE users SET notifications_last_seen_at = TIMESTAMP '2026-08-15 12:00:00'
WHERE username = 'pedro_alonso';

-- ------------------------------------------------------------
-- 2. Amistades — red FRIEND + PENDING (demo pedro_alonso)
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
  ('manuel_cano','raul_pascual'),
  -- Amistades cross-box (feed multi-ciudad para pedro)
  ('pedro_alonso','david_romero'),
  ('pedro_alonso','elena_castro'),
  ('pedro_alonso','eva_serrano'),
  ('lucia_vega','paula_navarro'),
  ('marcos_gil','fernando_rubio')
) AS v(a, b)
JOIN users u1 ON u1.username = v.a
JOIN users u2 ON u2.username = v.b
ON CONFLICT DO NOTHING;

-- Amistades PENDING (demo pedro_alonso):
--   · Entrantes: nuria_pons / isabel_perez / antonio_lara → pedro (aceptar en UI)
--   · Salientes: pedro → marta_fuentes / rosa_medina (pendientes de respuesta)
INSERT INTO friendships (user_a_id, user_b_id, status, requester_id, created_at)
SELECT LEAST(u1.id,u2.id), GREATEST(u1.id,u2.id), 'PENDING', u1.id, v.cdate
FROM (VALUES
  ('nuria_pons',    'pedro_alonso', TIMESTAMP '2026-08-28 10:00:00'),
  ('isabel_perez',  'pedro_alonso', TIMESTAMP '2026-08-30 16:30:00'),
  ('antonio_lara',  'pedro_alonso', TIMESTAMP '2026-09-01 09:15:00'),
  ('pedro_alonso',  'marta_fuentes', TIMESTAMP '2026-08-29 11:00:00'),
  ('pedro_alonso',  'rosa_medina',   TIMESTAMP '2026-09-01 18:00:00'),
  ('laura_jimenez', 'sofia_ramos',   TIMESTAMP '2026-08-27 14:00:00'),
  ('manuel_cano',   'javier_mena',   TIMESTAMP '2026-08-31 08:00:00')
) AS v(requester, target, cdate)
JOIN users u1 ON u1.username = v.requester
JOIN users u2 ON u2.username = v.target
ON CONFLICT DO NOTHING;

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

-- Check-ins recientes (fin ago – sep 2026) + usuarios infrautilizados (demo feed pedro)
INSERT INTO posts (user_id, post_type, training_tag, description, creation_date)
SELECT u.id, 'CHECKIN', v.tag, v.descr, v.cdate
FROM (VALUES
  -- Círculo Madrid / amigos de pedro
  ('pedro_alonso',  'HYROX',  'Simulación completa Open. Transiciones a 55–60s.',           TIMESTAMP '2026-08-25 08:00:00'),
  ('pedro_alonso',  'FUERZA', 'Sentadilla + press. Bloque de fuerza de cara a septiembre.', TIMESTAMP '2026-08-28 18:30:00'),
  ('pedro_alonso',  'CARRERA', 'Rodaje 10km zona 2. Piernas frescas tras el descanso.',     TIMESTAMP '2026-09-01 07:15:00'),
  ('lucia_vega',    'HYROX',  'Doble estación: ski + wall balls. Buen ritmo.',              TIMESTAMP '2026-08-26 09:00:00'),
  ('lucia_vega',    'CLASE',  'Clase técnica de farmers. Grip al límite.',                  TIMESTAMP '2026-08-31 19:00:00'),
  ('marcos_gil',    'HYROX',  'Partner WOD con Pedro. Ski + sled a tope.',                  TIMESTAMP '2026-08-27 07:30:00'),
  ('javier_mena',   'FUERZA', 'Deadlift 5x3. Sensaciones sólidas.',                         TIMESTAMP '2026-08-29 18:45:00'),
  ('sofia_ramos',   'CARRERA', 'Intervalos 10x200. Ritmo de carrera.',                      TIMESTAMP '2026-08-30 08:00:00'),
  ('alberto_diaz',  'HYROX',  'Circuito express 5 estaciones.',                             TIMESTAMP '2026-08-28 07:00:00'),
  ('roberto_santos','CLASE',  'Open floor del sábado. Mucha gente.',                        TIMESTAMP '2026-08-30 10:00:00'),
  ('ana_garcia',    'FUERZA', 'Hip thrust + RDL. Volumen alto.',                            TIMESTAMP '2026-08-27 18:20:00'),
  ('miguel_torres', 'CARRERA', 'Fartlek 50 min. Calor de agosto.',                           TIMESTAMP '2026-08-26 07:00:00'),
  ('laura_jimenez', 'HYROX',  'Wall balls + burpee BJ. Cap en 12 min.',                     TIMESTAMP '2026-08-29 09:30:00'),
  ('isabel_perez',  'DESCANSO_ACTIVO', 'Movilidad y foam. Día de descarga.',                TIMESTAMP '2026-08-31 20:00:00'),
  -- Box Norte BCN (amigos cross-box)
  ('david_romero',  'HYROX',  'Primera simulación en Box Norte. Ambiente top.',             TIMESTAMP '2026-08-27 18:00:00'),
  ('david_romero',  'CARRERA', 'Tempo 8km por el parque de la Ciutadella.',                 TIMESTAMP '2026-09-01 07:45:00'),
  ('paula_navarro', 'FUERZA', 'Empuje: press banca + fondos.',                              TIMESTAMP '2026-08-28 19:00:00'),
  ('sergio_molina', 'HYROX',  'SkiErg 2k + sled. Piernas hechas.',                          TIMESTAMP '2026-08-29 07:30:00'),
  ('elena_castro',  'CLASE',  'WOD del box publicado. Buena asistencia.',                   TIMESTAMP '2026-08-30 09:00:00'),
  ('fernando_rubio','CARRERA', 'Rodaje suave 7km. Recuperación activa.',                    TIMESTAMP '2026-08-31 08:00:00'),
  ('carmen_ortiz',  'FUERZA', 'Sentadilla frontal 5x5.',                                    TIMESTAMP '2026-08-28 18:30:00'),
  ('alejandro_reyes','HYROX', 'Transiciones: row → wall balls → farmers.',                  TIMESTAMP '2026-09-01 07:00:00'),
  ('pilar_moreno',  'CLASE',  'Technique day: lunges y carry.',                             TIMESTAMP '2026-08-26 19:15:00'),
  -- Valencia
  ('antonio_lara',  'HYROX',  'Sesión en Hyrox Valencia. Sled push fuerte.',                TIMESTAMP '2026-08-28 08:00:00'),
  ('beatriz_herrero','CARRERA', 'Intervalos en pista. 8x400.',                              TIMESTAMP '2026-08-29 07:30:00'),
  ('jose_guerrero', 'FUERZA', 'Peso muerto + pull-ups.',                                    TIMESTAMP '2026-08-30 18:00:00'),
  ('rosa_medina',   'HYROX',  'Circuito de estaciones en el center.',                       TIMESTAMP '2026-08-31 09:00:00'),
  ('manuel_cano',   'DESCANSO_ACTIVO', 'Yoga y movilidad 40 min.',                          TIMESTAMP '2026-08-27 20:00:00'),
  ('cristina_vidal','CLASE',  'Primera clase de la semana en Valencia.',                    TIMESTAMP '2026-09-01 07:45:00'),
  ('raul_pascual',  'HYROX',  'Simulación corta: 4 estaciones.',                            TIMESTAMP '2026-08-29 18:30:00'),
  ('eva_serrano',   'CARRERA', 'Rodaje 9km. Preparando el 5k sub-25.',                      TIMESTAMP '2026-08-30 07:00:00'),
  ('eva_serrano',   'HYROX',  'Wall balls unbroken 50. Casi!',                              TIMESTAMP '2026-09-01 18:00:00'),
  ('nuria_pons',    'FUERZA', 'Press militar + remo. Hombro estable.',                      TIMESTAMP '2026-08-28 18:00:00'),
  ('marta_fuentes', 'HYROX',  'Partner con Elena (remoto). Buena motivación.',              TIMESTAMP '2026-08-29 10:00:00')
) AS v(uname, tag, descr, cdate)
JOIN users u ON u.username = v.uname;

-- Constancia semanal de demo (eva_serrano): ≥3 días/semana → racha visible en feed
-- Semana actual W36 (31 ago–6 sep 2026): L M X (hoy mié 2)
-- W35 (24–30 ago): ya tiene 30 ago; sumamos 2 más
-- W34 (17–23 ago): 3 días
INSERT INTO posts (user_id, post_type, training_tag, description, creation_date)
SELECT u.id, 'CHECKIN', v.tag, v.descr, v.cdate
FROM (VALUES
  -- W34 (17–23 ago)
  ('eva_serrano', 'FUERZA',  'Sentadilla + press. Arranque de bloque en Valencia.', TIMESTAMP '2026-08-18 18:30:00'),
  ('eva_serrano', 'HYROX',   'Circuito corto: ski + wall balls.',                   TIMESTAMP '2026-08-20 07:15:00'),
  ('eva_serrano', 'CARRERA', 'Tempo 6km. Buenas sensaciones.',                      TIMESTAMP '2026-08-22 08:00:00'),
  -- W35 (24–30 ago) — Aug 30 ya existe arriba
  ('eva_serrano', 'CLASE',   'Technique day: farmers y lunges.',                    TIMESTAMP '2026-08-25 19:00:00'),
  ('eva_serrano', 'FUERZA',  'Peso muerto + hip thrust.',                           TIMESTAMP '2026-08-27 18:45:00'),
  -- W36 (31 ago–6 sep) — Sep 1 (M) ya existe; L + X para ≥3 y racha
  ('eva_serrano', 'CLASE',   'Clase matinal en Hyrox Valencia. Buen feeling.',      TIMESTAMP '2026-08-31 07:40:00'),
  ('eva_serrano', 'CARRERA', 'Rodaje suave 7km. Mantener la racha de la semana.',   TIMESTAMP '2026-09-02 07:20:00')
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
   TIMESTAMP '2026-08-07 06:30:00'),
  ('WOD lunes: Engine + skill', 'CARRERA',
   'AMRAP 18: 300m run + 12 toes-to-bar + 15 wall balls.',
   TIMESTAMP '2026-08-25 06:30:00'),
  ('WOD jueves: Hyrox prep', 'HYROX',
   'Por tiempo: 1km run + 50 wall balls + 30 cal ski + 20 burpee BJ. Cap 20.',
   TIMESTAMP '2026-08-28 06:30:00'),
  ('WOD sábado: Team chipper', 'OTRO',
   'Por equipos de 3: 100 cal row + 80 thrusters + 60 pull-ups + 40 box jumps.',
   TIMESTAMP '2026-08-30 09:00:00')
) AS v(title, tag, descr, cdate)
CROSS JOIN users u
JOIN boxes b ON b.name = 'CrossFit Origen'
WHERE u.username = 'carlos_martin';

-- WODs Box Norte BCN (admin elena_castro)
INSERT INTO posts (user_id, post_type, box_id, title, training_tag, description, creation_date)
SELECT u.id, 'BOX_WOD', b.id, v.title, v.tag, v.descr, v.cdate
FROM (VALUES
  ('WOD BCN: Ski & carry', 'HYROX',
   '5 rondas: 500m ski + 40m farmers. Descansa 90s entre rondas.',
   TIMESTAMP '2026-08-27 07:00:00'),
  ('WOD BCN: Strength Friday', 'FUERZA',
   'Back squat 5x5 + metcon corto 10 min AMRAP burpees/KB swings.',
   TIMESTAMP '2026-08-29 07:00:00')
) AS v(title, tag, descr, cdate)
CROSS JOIN users u
JOIN boxes b ON b.name = 'Box Norte BCN'
WHERE u.username = 'elena_castro';

-- Anuncios BCN
INSERT INTO posts (user_id, post_type, box_id, title, description, creation_date)
SELECT u.id, 'BOX_ANNOUNCEMENT', b.id, v.title, v.descr, v.cdate
FROM (VALUES
  ('Horario ampliado en septiembre',
   'A partir del 1 de septiembre abrimos clase extra a las 21:00 de lunes a jueves.',
   TIMESTAMP '2026-08-28 11:00:00'),
  ('Open Hyrox interno BCN',
   'El 20 de septiembre simulacro Open en el box. Apúntate en recepción.',
   TIMESTAMP '2026-09-01 10:00:00')
) AS v(title, descr, cdate)
CROSS JOIN users u
JOIN boxes b ON b.name = 'Box Norte BCN'
WHERE u.username = 'elena_castro';

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

-- Participantes WOD Hyrox prep (Madrid)
INSERT INTO post_participants (post_id, user_id, joined_at)
SELECT w.id, part.id, v.joined
FROM posts w
CROSS JOIN (VALUES
  ('pedro_alonso',  TIMESTAMP '2026-08-28 07:00:00'),
  ('lucia_vega',    TIMESTAMP '2026-08-28 07:10:00'),
  ('marcos_gil',    TIMESTAMP '2026-08-28 07:20:00'),
  ('javier_mena',   TIMESTAMP '2026-08-28 07:30:00'),
  ('alberto_diaz',  TIMESTAMP '2026-08-28 08:00:00'),
  ('roberto_santos',TIMESTAMP '2026-08-28 08:15:00'),
  ('ana_garcia',    TIMESTAMP '2026-08-28 09:00:00')
) AS v(username, joined)
JOIN users part ON part.username = v.username
WHERE w.post_type = 'BOX_WOD'
  AND w.title = 'WOD jueves: Hyrox prep';

-- Check-ins vinculados al WOD Hyrox prep
INSERT INTO posts (user_id, post_type, box_id, training_tag, description, wod_post_id, creation_date)
SELECT u.id, 'CHECKIN', w.box_id, 'HYROX', v.descr, w.id, v.cdate
FROM (VALUES
  ('pedro_alonso', 'Hyrox prep hecho. Cap en 18:40. Buenas piernas.', TIMESTAMP '2026-08-28 19:00:00'),
  ('lucia_vega',   'Completado el prep del jueves. Wall balls fluidas.', TIMESTAMP '2026-08-28 19:20:00'),
  ('marcos_gil',   'Cap 19:10. Ski más limpio que la semana pasada.', TIMESTAMP '2026-08-28 19:45:00')
) AS v(uname, descr, cdate)
JOIN users u ON u.username = v.uname
JOIN posts w ON w.post_type = 'BOX_WOD' AND w.title = 'WOD jueves: Hyrox prep';

-- Participantes WOD BCN
INSERT INTO post_participants (post_id, user_id, joined_at)
SELECT w.id, part.id, v.joined
FROM posts w
CROSS JOIN (VALUES
  ('david_romero',   TIMESTAMP '2026-08-27 07:30:00'),
  ('paula_navarro',  TIMESTAMP '2026-08-27 07:45:00'),
  ('sergio_molina',  TIMESTAMP '2026-08-27 08:00:00'),
  ('fernando_rubio', TIMESTAMP '2026-08-27 08:15:00')
) AS v(username, joined)
JOIN users part ON part.username = v.username
WHERE w.post_type = 'BOX_WOD'
  AND w.title = 'WOD BCN: Ski & carry';

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
-- 4. Comentarios (check-ins + posts recientes de pedro)
-- ------------------------------------------------------------
INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id, u.id, v.content, v.cdate
FROM (VALUES
  ('marcos_gil',  TIMESTAMP '2026-07-28 07:15:00', 'lucia_vega',    'Que sesion mas dura! Respeto total.', TIMESTAMP '2026-07-28 10:00:00'),
  ('javier_mena', TIMESTAMP '2026-07-29 19:00:00', 'pedro_alonso',  'Esa clase de technique marca la diferencia.', TIMESTAMP '2026-07-29 20:30:00'),
  ('sofia_ramos', TIMESTAMP '2026-07-30 08:00:00', 'marcos_gil',    'Esos intervalos se notan luego en la carrera.', TIMESTAMP '2026-07-30 12:00:00'),
  ('lucia_vega',  TIMESTAMP '2026-08-02 10:00:00', 'javier_mena',   'Circuito express y con muy buenas sensaciones.', TIMESTAMP '2026-08-02 11:15:00'),
  ('pedro_alonso',TIMESTAMP '2026-08-06 08:10:00', 'alberto_diaz',  'Las transiciones son oro puro.', TIMESTAMP '2026-08-06 09:00:00'),
  ('javier_mena', TIMESTAMP '2026-08-08 07:30:00', 'marcos_gil',    'Buen tempo. A por la siguiente semana.', TIMESTAMP '2026-08-08 09:00:00'),
  -- Comentarios recientes (notif para pedro)
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'lucia_vega',   'Esa simulación se ve sólida. ¡A por el Open!', TIMESTAMP '2026-08-25 09:30:00'),
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'marcos_gil',   'Transiciones de crack. Yo aún estoy a 70s.', TIMESTAMP '2026-08-25 11:00:00'),
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'javier_mena',  'Brutal. Quedamos a simular juntos?', TIMESTAMP '2026-08-25 12:15:00'),
  ('pedro_alonso', TIMESTAMP '2026-08-28 18:30:00', 'roberto_santos','Ese bloque de fuerza se nota luego en el Hyrox.', TIMESTAMP '2026-08-28 20:00:00'),
  ('pedro_alonso', TIMESTAMP '2026-09-01 07:15:00', 'david_romero', '10km zona 2 es oro. Yo hice 8 por Ciutadella.', TIMESTAMP '2026-09-01 10:00:00'),
  ('pedro_alonso', TIMESTAMP '2026-09-01 07:15:00', 'eva_serrano',  'Buen rodaje! Yo preparando el 5k sub-25.', TIMESTAMP '2026-09-01 11:30:00'),
  ('lucia_vega',   TIMESTAMP '2026-08-26 09:00:00', 'pedro_alonso', 'Qué buen ritmo en wall balls.', TIMESTAMP '2026-08-26 10:00:00'),
  ('marcos_gil',   TIMESTAMP '2026-08-27 07:30:00', 'pedro_alonso', 'Partner WOD de los buenos. Repetimos?', TIMESTAMP '2026-08-27 09:00:00'),
  ('david_romero', TIMESTAMP '2026-08-27 18:00:00', 'pedro_alonso', 'Bienvenido al Box Norte! Cuando pasas por BCN?', TIMESTAMP '2026-08-27 19:00:00'),
  ('eva_serrano',  TIMESTAMP '2026-09-01 18:00:00', 'pedro_alonso', '50 unbroken es bestia. Felicidades!', TIMESTAMP '2026-09-01 19:00:00'),
  ('alberto_diaz', TIMESTAMP '2026-08-28 07:00:00', 'sofia_ramos',  'Circuito express y sin parar. Respeto.', TIMESTAMP '2026-08-28 12:00:00'),
  ('elena_castro', TIMESTAMP '2026-08-30 09:00:00', 'paula_navarro','Gran asistencia al WOD. El box vibra.', TIMESTAMP '2026-08-30 11:00:00')
) AS v(author, pdate, commenter, content, cdate)
JOIN users a ON a.username = v.author
JOIN posts p ON p.user_id = a.id AND p.creation_date = v.pdate
JOIN users u ON u.username = v.commenter;

-- ------------------------------------------------------------
-- 5. Likes en posts
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
  ('alberto_diaz',TIMESTAMP '2026-08-07 18:00:00', 'pedro_alonso'),
  -- Likes recientes en posts de pedro (notif)
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'lucia_vega'),
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'marcos_gil'),
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'javier_mena'),
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'alberto_diaz'),
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'roberto_santos'),
  ('pedro_alonso', TIMESTAMP '2026-08-28 18:30:00', 'lucia_vega'),
  ('pedro_alonso', TIMESTAMP '2026-08-28 18:30:00', 'carlos_martin'),
  ('pedro_alonso', TIMESTAMP '2026-09-01 07:15:00', 'marcos_gil'),
  ('pedro_alonso', TIMESTAMP '2026-09-01 07:15:00', 'david_romero'),
  ('pedro_alonso', TIMESTAMP '2026-09-01 07:15:00', 'eva_serrano'),
  ('lucia_vega',   TIMESTAMP '2026-08-26 09:00:00', 'pedro_alonso'),
  ('marcos_gil',   TIMESTAMP '2026-08-27 07:30:00', 'pedro_alonso'),
  ('david_romero', TIMESTAMP '2026-09-01 07:45:00', 'pedro_alonso'),
  ('eva_serrano',  TIMESTAMP '2026-09-01 18:00:00', 'pedro_alonso'),
  ('javier_mena',  TIMESTAMP '2026-08-29 18:45:00', 'pedro_alonso'),
  ('sofia_ramos',  TIMESTAMP '2026-08-30 08:00:00', 'ana_garcia'),
  ('elena_castro', TIMESTAMP '2026-08-30 09:00:00', 'david_romero'),
  ('sergio_molina',TIMESTAMP '2026-08-29 07:30:00', 'javier_mena')
) AS v(author, pdate, liker)
JOIN users a ON a.username = v.author
JOIN posts p ON p.user_id = a.id AND p.creation_date = v.pdate
JOIN users u ON u.username = v.liker;

-- ------------------------------------------------------------
-- 5b. Likes en comentarios (comment_likes) — notif COMMENT_LIKE para pedro
-- ------------------------------------------------------------
INSERT INTO comment_likes (comment_id, user_id)
SELECT c.id, u.id
FROM (VALUES
  -- Likes al comentario de pedro en el post de javier (julio)
  ('javier_mena', TIMESTAMP '2026-07-29 19:00:00', 'pedro_alonso', 'lucia_vega'),
  ('javier_mena', TIMESTAMP '2026-07-29 19:00:00', 'pedro_alonso', 'marcos_gil'),
  -- Likes a comentarios de pedro en posts ajenos (ago–sep)
  ('lucia_vega',   TIMESTAMP '2026-08-26 09:00:00', 'pedro_alonso', 'lucia_vega'),
  ('lucia_vega',   TIMESTAMP '2026-08-26 09:00:00', 'pedro_alonso', 'javier_mena'),
  ('marcos_gil',   TIMESTAMP '2026-08-27 07:30:00', 'pedro_alonso', 'marcos_gil'),
  ('marcos_gil',   TIMESTAMP '2026-08-27 07:30:00', 'pedro_alonso', 'sofia_ramos'),
  ('david_romero', TIMESTAMP '2026-08-27 18:00:00', 'pedro_alonso', 'david_romero'),
  ('eva_serrano',  TIMESTAMP '2026-09-01 18:00:00', 'pedro_alonso', 'eva_serrano'),
  ('eva_serrano',  TIMESTAMP '2026-09-01 18:00:00', 'pedro_alonso', 'lucia_vega'),
  -- Likes a comentarios de otros en posts de pedro
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'lucia_vega',  'pedro_alonso'),
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'lucia_vega',  'javier_mena'),
  ('pedro_alonso', TIMESTAMP '2026-08-25 08:00:00', 'marcos_gil',  'pedro_alonso'),
  ('pedro_alonso', TIMESTAMP '2026-09-01 07:15:00', 'david_romero','pedro_alonso')
) AS v(post_author, pdate, commenter, liker)
JOIN users pa ON pa.username = v.post_author
JOIN posts p ON p.user_id = pa.id AND p.creation_date = v.pdate
JOIN users cu ON cu.username = v.commenter
JOIN comments c ON c.post_id = p.id AND c.user_id = cu.id
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
  ('alberto_diaz',TIMESTAMP '2026-08-07 13:00:00'),
  ('pedro_alonso',TIMESTAMP '2026-08-08 09:00:00'),
  ('lucia_vega',  TIMESTAMP '2026-08-08 10:00:00'),
  ('eva_serrano', TIMESTAMP '2026-08-09 08:00:00')
) AS v(username, joined)
JOIN users part ON part.username = v.username
WHERE p.post_type = 'BOX_CHALLENGE'
  AND p.creation_date = TIMESTAMP '2026-08-07 11:00:00';

-- Más participantes reto 5k sub-25
INSERT INTO post_participants (post_id, user_id, joined_at)
SELECT p.id, part.id, v.joined
FROM posts p
CROSS JOIN (VALUES
  ('sofia_ramos',   TIMESTAMP '2026-08-05 09:00:00'),
  ('miguel_torres', TIMESTAMP '2026-08-06 07:00:00'),
  ('eva_serrano',   TIMESTAMP '2026-08-07 08:00:00'),
  ('david_romero',  TIMESTAMP '2026-08-08 10:00:00')
) AS v(username, joined)
JOIN users part ON part.username = v.username
WHERE p.post_type = 'BOX_CHALLENGE'
  AND p.creation_date = TIMESTAMP '2026-08-01 08:00:00'
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------
-- 8. Objetivos (goals / goal_participants / goal_marks)
--    Usuarios: pedro_alonso (owner principal) + amigos
-- ------------------------------------------------------------
INSERT INTO goals (title, description, metric_label, target_value, unit, direction, weeks, deadline, status, created_by_user_id, created_at)
SELECT 'Sub 90 en Hyrox Open',
       'Bajar de 1h 30m en la próxima carrera Open.',
       'Tiempo total', 5400, 'TIME', 'LOWER', 15,
       TIMESTAMPTZ '2026-09-15 23:59:59+00', 'ACTIVE', u.id,
       TIMESTAMP '2026-06-01 10:00:00'
FROM users u WHERE u.username = 'pedro_alonso';

INSERT INTO goals (title, description, metric_label, target_value, unit, direction, weeks, deadline, status, created_by_user_id, created_at)
SELECT '100 kg en deadlift',
       'Alcanzar 100 kg en peso muerto convencional.',
       'Carga máxima', 100, 'KG', 'HIGHER', 13,
       TIMESTAMPTZ '2026-10-01 23:59:59+00', 'ACTIVE', u.id,
       TIMESTAMP '2026-07-01 09:00:00'
FROM users u WHERE u.username = 'pedro_alonso';

INSERT INTO goals (title, description, metric_label, target_value, unit, direction, weeks, deadline, status, created_by_user_id, created_at)
SELECT 'SkiErg 1000 m en 3:30',
       'Marca de referencia en SkiErg para la estación.',
       'SkiErg 1000 m', 210, 'TIME', 'LOWER', 8,
       TIMESTAMPTZ '2026-05-01 23:59:59+00', 'ACHIEVED', u.id,
       TIMESTAMP '2026-03-01 10:00:00'
FROM users u WHERE u.username = 'pedro_alonso';

-- Goal EXPIRED (plazo pasado sin lograr)
INSERT INTO goals (title, description, metric_label, target_value, unit, direction, weeks, deadline, status, created_by_user_id, created_at)
SELECT '5k sub-22 en junio',
       'Correr 5 km por debajo de 22 minutos antes del verano.',
       'Tiempo 5k', 1320, 'TIME', 'LOWER', 6,
       TIMESTAMPTZ '2026-06-30 23:59:59+00', 'EXPIRED', u.id,
       TIMESTAMP '2026-05-15 10:00:00'
FROM users u WHERE u.username = 'pedro_alonso';

-- Goal unidad REPS
INSERT INTO goals (title, description, metric_label, target_value, unit, direction, weeks, deadline, status, created_by_user_id, created_at)
SELECT '50 pull-ups unbroken',
       'Encadenar 50 pull-ups sin soltar la barra.',
       'Pull-ups seguidas', 50, 'REPS', 'HIGHER', 10,
       TIMESTAMPTZ '2026-10-15 23:59:59+00', 'ACTIVE', u.id,
       TIMESTAMP '2026-08-01 11:00:00'
FROM users u WHERE u.username = 'lucia_vega';

-- Goal unidad METERS
INSERT INTO goals (title, description, metric_label, target_value, unit, direction, weeks, deadline, status, created_by_user_id, created_at)
SELECT 'Farmers carry 400 m',
       'Completar 400 m de farmers carry sin soltar (peso categoría).',
       'Distancia unbroken', 400, 'METERS', 'HIGHER', 8,
       TIMESTAMPTZ '2026-09-30 23:59:59+00', 'ACTIVE', u.id,
       TIMESTAMP '2026-08-10 09:00:00'
FROM users u WHERE u.username = 'javier_mena';

-- Participantes objetivo Hyrox
INSERT INTO goal_participants (goal_id, user_id, is_owner, started_at, ends_at)
SELECT g.id, u.id, v.is_owner, v.started_at, v.ends_at
FROM goals g
CROSS JOIN (VALUES
  ('pedro_alonso', true,  TIMESTAMPTZ '2026-06-01 10:00:00+00', TIMESTAMPTZ '2026-09-15 23:59:59+00'),
  ('lucia_vega',   false, TIMESTAMPTZ '2026-06-08 17:00:00+00', TIMESTAMPTZ '2026-09-15 23:59:59+00'),
  ('marcos_gil',   false, TIMESTAMPTZ '2026-06-10 19:00:00+00', TIMESTAMPTZ '2026-09-15 23:59:59+00'),
  ('sofia_ramos',  false, TIMESTAMPTZ '2026-06-12 16:00:00+00', TIMESTAMPTZ '2026-09-15 23:59:59+00'),
  ('javier_mena',  false, TIMESTAMPTZ '2026-08-20 10:00:00+00', TIMESTAMPTZ '2026-09-15 23:59:59+00'),
  ('alberto_diaz', false, TIMESTAMPTZ '2026-08-22 18:00:00+00', TIMESTAMPTZ '2026-09-15 23:59:59+00'),
  ('eva_serrano',  false, TIMESTAMPTZ '2026-08-25 09:00:00+00', TIMESTAMPTZ '2026-09-15 23:59:59+00')
) AS v(username, is_owner, started_at, ends_at)
JOIN users u ON u.username = v.username
WHERE g.title = 'Sub 90 en Hyrox Open';

-- Participantes deadlift
INSERT INTO goal_participants (goal_id, user_id, is_owner, started_at, ends_at)
SELECT g.id, u.id, v.is_owner, v.started_at, v.ends_at
FROM goals g
CROSS JOIN (VALUES
  ('pedro_alonso', true,  TIMESTAMPTZ '2026-07-01 09:00:00+00', TIMESTAMPTZ '2026-10-01 23:59:59+00'),
  ('javier_mena',  false, TIMESTAMPTZ '2026-07-05 11:00:00+00', TIMESTAMPTZ '2026-10-01 23:59:59+00'),
  ('marcos_gil',   false, TIMESTAMPTZ '2026-08-18 19:00:00+00', TIMESTAMPTZ '2026-10-01 23:59:59+00'),
  ('roberto_santos', false, TIMESTAMPTZ '2026-08-26 10:00:00+00', TIMESTAMPTZ '2026-10-01 23:59:59+00')
) AS v(username, is_owner, started_at, ends_at)
JOIN users u ON u.username = v.username
WHERE g.title = '100 kg en deadlift';

-- Participantes SkiErg
INSERT INTO goal_participants (goal_id, user_id, is_owner, started_at, ends_at)
SELECT g.id, u.id, v.is_owner, v.started_at, v.ends_at
FROM goals g
CROSS JOIN (VALUES
  ('pedro_alonso', true,  TIMESTAMPTZ '2026-03-01 10:00:00+00', TIMESTAMPTZ '2026-05-01 23:59:59+00'),
  ('lucia_vega',   false, TIMESTAMPTZ '2026-03-10 17:00:00+00', TIMESTAMPTZ '2026-05-01 23:59:59+00')
) AS v(username, is_owner, started_at, ends_at)
JOIN users u ON u.username = v.username
WHERE g.title = 'SkiErg 1000 m en 3:30';

-- Participantes 5k EXPIRED
INSERT INTO goal_participants (goal_id, user_id, is_owner, started_at, ends_at)
SELECT g.id, u.id, v.is_owner, v.started_at, v.ends_at
FROM goals g
CROSS JOIN (VALUES
  ('pedro_alonso', true,  TIMESTAMPTZ '2026-05-15 10:00:00+00', TIMESTAMPTZ '2026-06-30 23:59:59+00'),
  ('sofia_ramos',  false, TIMESTAMPTZ '2026-05-20 08:00:00+00', TIMESTAMPTZ '2026-06-30 23:59:59+00'),
  ('miguel_torres',false, TIMESTAMPTZ '2026-05-22 07:00:00+00', TIMESTAMPTZ '2026-06-30 23:59:59+00')
) AS v(username, is_owner, started_at, ends_at)
JOIN users u ON u.username = v.username
WHERE g.title = '5k sub-22 en junio';

-- Participantes pull-ups REPS
INSERT INTO goal_participants (goal_id, user_id, is_owner, started_at, ends_at)
SELECT g.id, u.id, v.is_owner, v.started_at, v.ends_at
FROM goals g
CROSS JOIN (VALUES
  ('lucia_vega',   true,  TIMESTAMPTZ '2026-08-01 11:00:00+00', TIMESTAMPTZ '2026-10-15 23:59:59+00'),
  ('pedro_alonso', false, TIMESTAMPTZ '2026-08-05 18:00:00+00', TIMESTAMPTZ '2026-10-15 23:59:59+00'),
  ('ana_garcia',   false, TIMESTAMPTZ '2026-08-12 19:00:00+00', TIMESTAMPTZ '2026-10-15 23:59:59+00')
) AS v(username, is_owner, started_at, ends_at)
JOIN users u ON u.username = v.username
WHERE g.title = '50 pull-ups unbroken';

-- Participantes farmers METERS
INSERT INTO goal_participants (goal_id, user_id, is_owner, started_at, ends_at)
SELECT g.id, u.id, v.is_owner, v.started_at, v.ends_at
FROM goals g
CROSS JOIN (VALUES
  ('javier_mena',  true,  TIMESTAMPTZ '2026-08-10 09:00:00+00', TIMESTAMPTZ '2026-09-30 23:59:59+00'),
  ('pedro_alonso', false, TIMESTAMPTZ '2026-08-12 10:00:00+00', TIMESTAMPTZ '2026-09-30 23:59:59+00'),
  ('alberto_diaz', false, TIMESTAMPTZ '2026-08-15 17:00:00+00', TIMESTAMPTZ '2026-09-30 23:59:59+00'),
  ('marcos_gil',   false, TIMESTAMPTZ '2026-08-20 08:00:00+00', TIMESTAMPTZ '2026-09-30 23:59:59+00')
) AS v(username, is_owner, started_at, ends_at)
JOIN users u ON u.username = v.username
WHERE g.title = 'Farmers carry 400 m';

-- Marcas Hyrox
INSERT INTO goal_marks (goal_id, user_id, value, note, recorded_at)
SELECT g.id, u.id, v.value, v.note, v.recorded_at
FROM goals g
CROSS JOIN (VALUES
  ('pedro_alonso', 6120, 'Simulación box',            TIMESTAMPTZ '2026-06-05 18:00:00+00'),
  ('pedro_alonso', 5880, 'Mejora en wall balls',       TIMESTAMPTZ '2026-06-20 18:00:00+00'),
  ('pedro_alonso', 5640, 'PR parcial',                 TIMESTAMPTZ '2026-07-12 18:00:00+00'),
  ('pedro_alonso', 5520, 'Buena simulación',           TIMESTAMPTZ '2026-08-01 18:00:00+00'),
  ('pedro_alonso', 5460, 'Simulación Open ago',        TIMESTAMPTZ '2026-08-25 18:30:00+00'),
  ('lucia_vega',   6000, NULL,                        TIMESTAMPTZ '2026-06-08 17:00:00+00'),
  ('lucia_vega',   5700, NULL,                        TIMESTAMPTZ '2026-07-01 17:00:00+00'),
  ('lucia_vega',   5460, 'Casi en el objetivo',        TIMESTAMPTZ '2026-07-28 17:00:00+00'),
  ('marcos_gil',   6300, NULL,                        TIMESTAMPTZ '2026-06-10 19:00:00+00'),
  ('marcos_gil',   5950, NULL,                        TIMESTAMPTZ '2026-07-15 19:00:00+00'),
  ('sofia_ramos',  5550, NULL,                        TIMESTAMPTZ '2026-06-12 16:00:00+00'),
  ('sofia_ramos',  5380, '¡Objetivo logrado!',         TIMESTAMPTZ '2026-07-20 16:00:00+00'),
  ('javier_mena',  5800, 'Primera marca en el goal',   TIMESTAMPTZ '2026-08-20 10:30:00+00'),
  ('alberto_diaz', 6100, NULL,                        TIMESTAMPTZ '2026-08-22 18:30:00+00'),
  ('eva_serrano',  5650, 'Desde Valencia',             TIMESTAMPTZ '2026-08-25 09:30:00+00')
) AS v(username, value, note, recorded_at)
JOIN users u ON u.username = v.username
WHERE g.title = 'Sub 90 en Hyrox Open';

-- Marcas deadlift
INSERT INTO goal_marks (goal_id, user_id, value, note, recorded_at)
SELECT g.id, u.id, v.value, v.note, v.recorded_at
FROM goals g
CROSS JOIN (VALUES
  ('pedro_alonso', 80,   NULL,      TIMESTAMPTZ '2026-07-02 10:00:00+00'),
  ('pedro_alonso', 87.5, NULL,      TIMESTAMPTZ '2026-07-18 10:00:00+00'),
  ('pedro_alonso', 92.5, 'Muy cerca', TIMESTAMPTZ '2026-08-05 10:00:00+00'),
  ('pedro_alonso', 95,   'Casi!',    TIMESTAMPTZ '2026-08-28 19:00:00+00'),
  ('javier_mena',  90,   NULL,      TIMESTAMPTZ '2026-07-05 11:00:00+00'),
  ('javier_mena',  100,  'Hecho',    TIMESTAMPTZ '2026-07-30 11:00:00+00'),
  ('marcos_gil',   85,   NULL,      TIMESTAMPTZ '2026-08-18 19:30:00+00'),
  ('roberto_santos', 88, NULL,      TIMESTAMPTZ '2026-08-26 10:30:00+00')
) AS v(username, value, note, recorded_at)
JOIN users u ON u.username = v.username
WHERE g.title = '100 kg en deadlift';

-- Marcas SkiErg
INSERT INTO goal_marks (goal_id, user_id, value, note, recorded_at)
SELECT g.id, u.id, v.value, v.note, v.recorded_at
FROM goals g
CROSS JOIN (VALUES
  ('pedro_alonso', 245, NULL,                  TIMESTAMPTZ '2026-03-05 18:00:00+00'),
  ('pedro_alonso', 228, NULL,                  TIMESTAMPTZ '2026-03-20 18:00:00+00'),
  ('pedro_alonso', 208, 'Objetivo conseguido', TIMESTAMPTZ '2026-04-15 18:00:00+00'),
  ('lucia_vega',   235, NULL,                  TIMESTAMPTZ '2026-03-10 17:00:00+00'),
  ('lucia_vega',   215, NULL,                  TIMESTAMPTZ '2026-04-02 17:00:00+00')
) AS v(username, value, note, recorded_at)
JOIN users u ON u.username = v.username
WHERE g.title = 'SkiErg 1000 m en 3:30';

-- Marcas 5k EXPIRED (no llegaron a 1320s)
INSERT INTO goal_marks (goal_id, user_id, value, note, recorded_at)
SELECT g.id, u.id, v.value, v.note, v.recorded_at
FROM goals g
CROSS JOIN (VALUES
  ('pedro_alonso',  1450, 'Salida fuerte',     TIMESTAMPTZ '2026-05-20 08:00:00+00'),
  ('pedro_alonso',  1380, 'Mejora',            TIMESTAMPTZ '2026-06-10 08:00:00+00'),
  ('pedro_alonso',  1355, 'Cerca pero no',     TIMESTAMPTZ '2026-06-28 08:00:00+00'),
  ('sofia_ramos',   1400, NULL,               TIMESTAMPTZ '2026-05-22 08:00:00+00'),
  ('miguel_torres', 1420, NULL,               TIMESTAMPTZ '2026-05-25 07:30:00+00')
) AS v(username, value, note, recorded_at)
JOIN users u ON u.username = v.username
WHERE g.title = '5k sub-22 en junio';

-- Marcas pull-ups REPS
INSERT INTO goal_marks (goal_id, user_id, value, note, recorded_at)
SELECT g.id, u.id, v.value, v.note, v.recorded_at
FROM goals g
CROSS JOIN (VALUES
  ('lucia_vega',   28, NULL,              TIMESTAMPTZ '2026-08-01 11:30:00+00'),
  ('lucia_vega',   35, 'Progreso',        TIMESTAMPTZ '2026-08-15 18:00:00+00'),
  ('lucia_vega',   42, NULL,              TIMESTAMPTZ '2026-08-28 18:00:00+00'),
  ('pedro_alonso', 30, NULL,              TIMESTAMPTZ '2026-08-05 18:30:00+00'),
  ('pedro_alonso', 38, 'Mejorando grip',  TIMESTAMPTZ '2026-08-25 19:00:00+00'),
  ('ana_garcia',   25, NULL,              TIMESTAMPTZ '2026-08-12 19:30:00+00')
) AS v(username, value, note, recorded_at)
JOIN users u ON u.username = v.username
WHERE g.title = '50 pull-ups unbroken';

-- Marcas farmers METERS
INSERT INTO goal_marks (goal_id, user_id, value, note, recorded_at)
SELECT g.id, u.id, v.value, v.note, v.recorded_at
FROM goals g
CROSS JOIN (VALUES
  ('javier_mena',  200, NULL,             TIMESTAMPTZ '2026-08-10 09:30:00+00'),
  ('javier_mena',  280, 'Mejor grip',     TIMESTAMPTZ '2026-08-22 09:00:00+00'),
  ('javier_mena',  340, NULL,             TIMESTAMPTZ '2026-08-30 09:00:00+00'),
  ('pedro_alonso', 220, NULL,             TIMESTAMPTZ '2026-08-12 10:30:00+00'),
  ('pedro_alonso', 300, 'Sin soltar',     TIMESTAMPTZ '2026-08-28 10:00:00+00'),
  ('alberto_diaz', 180, NULL,             TIMESTAMPTZ '2026-08-15 17:30:00+00'),
  ('marcos_gil',   250, NULL,             TIMESTAMPTZ '2026-08-20 08:30:00+00')
) AS v(username, value, note, recorded_at)
JOIN users u ON u.username = v.username
WHERE g.title = 'Farmers carry 400 m';

-- Posts GOAL_CREATED
INSERT INTO posts (user_id, post_type, title, description, challenge_deadline, goal_id, creation_date)
SELECT u.id, 'GOAL_CREATED', g.title, g.description, g.deadline, g.id, g.created_at
FROM goals g
JOIN users u ON u.id = g.created_by_user_id
WHERE g.title IN (
  'Sub 90 en Hyrox Open',
  '100 kg en deadlift',
  '5k sub-22 en junio',
  '50 pull-ups unbroken',
  'Farmers carry 400 m'
);

-- Posts GOAL_JOIN (feed + notificaciones para owners)
INSERT INTO posts (user_id, post_type, title, description, challenge_deadline, goal_id, creation_date)
SELECT u.id, 'GOAL_JOIN', g.title, NULL, g.deadline, g.id, v.cdate
FROM (VALUES
  ('lucia_vega',    'Sub 90 en Hyrox Open', TIMESTAMP '2026-06-08 17:05:00'),
  ('marcos_gil',    'Sub 90 en Hyrox Open', TIMESTAMP '2026-06-10 19:05:00'),
  ('sofia_ramos',   'Sub 90 en Hyrox Open', TIMESTAMP '2026-06-12 16:05:00'),
  ('javier_mena',   'Sub 90 en Hyrox Open', TIMESTAMP '2026-08-20 10:05:00'),
  ('alberto_diaz',  'Sub 90 en Hyrox Open', TIMESTAMP '2026-08-22 18:05:00'),
  ('eva_serrano',   'Sub 90 en Hyrox Open', TIMESTAMP '2026-08-25 09:05:00'),
  ('javier_mena',   '100 kg en deadlift',   TIMESTAMP '2026-07-05 11:05:00'),
  ('marcos_gil',    '100 kg en deadlift',   TIMESTAMP '2026-08-18 19:05:00'),
  ('roberto_santos','100 kg en deadlift',   TIMESTAMP '2026-08-26 10:05:00'),
  ('pedro_alonso',  '50 pull-ups unbroken', TIMESTAMP '2026-08-05 18:05:00'),
  ('ana_garcia',    '50 pull-ups unbroken', TIMESTAMP '2026-08-12 19:05:00'),
  ('pedro_alonso',  'Farmers carry 400 m',  TIMESTAMP '2026-08-12 10:05:00'),
  ('alberto_diaz',  'Farmers carry 400 m',  TIMESTAMP '2026-08-15 17:05:00'),
  ('marcos_gil',    'Farmers carry 400 m',  TIMESTAMP '2026-08-20 08:05:00'),
  ('sofia_ramos',   '5k sub-22 en junio',   TIMESTAMP '2026-05-20 08:05:00'),
  ('miguel_torres', '5k sub-22 en junio',   TIMESTAMP '2026-05-22 07:05:00')
) AS v(uname, gtitle, cdate)
JOIN users u ON u.username = v.uname
JOIN goals g ON g.title = v.gtitle;
