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
-- 2. Posts — 35 entrenamientos (5 usuarios con 2 posts)
--    total_time = suma de los 16 campos de ejercicio
-- ------------------------------------------------------------
INSERT INTO posts (
    user_id, post_type, box_id,
    running1, running2, running3, running4, running5, running6, running7, running8,
    ski_erg, sled_push, sled_pull, burpee_broad_jump, "row",
    farmers_carry, sandbag_lunges, wall_balls,
    total_time, description, category, creation_date
)
SELECT u.id, 'RESULT', b.id,
       v.r1,v.r2,v.r3,v.r4,v.r5,v.r6,v.r7,v.r8,
       v.ski,v.push,v.pull,v.burp,v.row_t,v.farm,v.sand,v.wall,
       v.r1+v.r2+v.r3+v.r4+v.r5+v.r6+v.r7+v.r8+v.ski+v.push+v.pull+v.burp+v.row_t+v.farm+v.sand+v.wall,
       v.descr, v.cat, v.cdate
FROM (VALUES
  ('pedro_alonso',     62,57,71,66,81,76,89,84,  118,46,52,179,296,91,119,149, 'Mejor marca personal. El sled pull sigue mejorando.',             'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-10 08:00:00'),
  ('pedro_alonso',     63,58,72,67,82,77,88,83,  116,47,53,178,295,91,119,149, 'Segunda semana seguida bajando de 27:30.',                        'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-24 07:45:00'),
  ('lucia_vega',       58,53,68,63,78,73,93,87,  124,44,49,183,307,88,123,153, 'Entrenamiento duro pero gratificante.',                           'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-11 09:30:00'),
  ('lucia_vega',       57,52,67,62,77,72,94,88,  125,43,48,185,310,87,125,155, 'Nuevo PB en wall balls. ¡Muy contenta!',                          'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-25 08:15:00'),
  ('marcos_gil',       63,58,73,68,83,78,87,82,  115,47,54,174,293,94,116,146, 'Primera vez que bajo de 27 minutos!',                             'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-12 07:45:00'),
  ('marcos_gil',       62,58,72,67,82,77,88,83,  116,47,53,175,293,93,117,147, 'Consistencia. Semana muy sólida.',                                'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-26 08:00:00'),
  ('sofia_ramos',      60,55,70,65,80,75,90,85,  120,45,50,180,300,90,120,150, 'Sesión completada con el equipo.',                                'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-13 18:15:00'),
  ('javier_mena',      66,62,76,71,86,81,84,79,  112,50,56,170,288,97,113,143, 'Nuevo PR en ski erg. El entrenamiento de fuerza da frutos.',      'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-14 06:30:00'),
  ('javier_mena',      65,61,75,70,85,80,85,80,  111,50,55,169,287,96,112,142, 'Mejorando la técnica en sled push.',                             'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-28 06:45:00'),
  ('nuria_pons',       57,52,67,62,77,72,94,88,  126,43,48,184,309,87,124,154, 'Vuelta a la competición después de las vacaciones.',              'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-15 10:00:00'),
  ('alberto_diaz',     63,59,73,68,83,78,87,82,  116,48,53,176,294,93,117,147, 'Las wall balls me matan, pero el tiempo total mejora.',           'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-16 08:45:00'),
  ('marta_fuentes',    59,54,69,64,79,74,91,86,  122,44,51,181,302,89,122,152, 'Entrenamiento en solitario. Muy concentrada hoy.',                'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-17 17:00:00'),
  ('roberto_santos',   65,61,75,70,85,80,85,80,  113,49,55,172,290,95,114,144, 'El sled pull sigue siendo mi talón de Aquiles.',                  'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-18 07:15:00'),
  ('isabel_perez',     61,56,72,67,82,77,88,83,  119,45,51,178,298,91,119,149, 'Buen día, buen humor, buen tiempo.',                             'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-19 09:00:00'),
  ('carlos_martin',    60,56,70,65,80,75,90,85,  120,47,51,177,298,92,120,151, 'Primera competición oficial. Muy satisfecho.',                    'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-20 08:30:00'),
  ('carlos_martin',    59,55,69,64,79,74,91,86,  121,45,50,178,298,91,120,151, 'Mejorando semana a semana.',                                      'INDIVIDUAL_MALE',   TIMESTAMP '2026-05-01 07:30:00'),
  ('ana_garcia',       62,57,72,67,82,77,88,83,  118,46,52,177,297,91,118,148, 'Entrenamiento matutino. El cuerpo responde bien.',                'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-21 07:00:00'),
  ('miguel_torres',    67,63,77,72,87,82,83,78,  110,51,57,168,286,98,112,142, 'El running me sale muy bien hoy.',                               'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-22 09:00:00'),
  ('laura_jimenez',    64,59,74,69,84,79,86,81,  114,48,54,174,292,94,116,146, 'Debut en Hyrox. Resultado mejor de lo esperado!',                'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-22 10:30:00'),
  ('david_romero',     61,57,71,66,81,76,89,84,  117,46,53,175,295,91,118,148, 'Entrenamiento con Carlos. Muy buen ritmo.',                       'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-23 08:00:00'),
  ('paula_navarro',    65,60,75,70,85,80,85,80,  113,49,55,172,290,95,114,144, 'Buena sesión. Mejoro en burpees.',                               'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-23 09:15:00'),
  ('sergio_molina',    64,60,74,69,84,79,86,81,  114,48,54,174,292,94,117,146, 'Entrenamiento nocturno. Ambiente increíble.',                     'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-24 20:00:00'),
  ('elena_castro',     63,58,73,68,83,78,87,82,  116,47,53,176,294,93,117,147, 'Concentración máxima. Resultado personal.',                       'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-25 08:30:00'),
  ('fernando_rubio',   62,58,72,67,82,77,88,83,  116,47,53,176,294,92,118,148, 'Técnica mejorada en farmers carry.',                             'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-26 07:45:00'),
  ('carmen_ortiz',     66,61,76,71,86,81,84,79,  111,50,56,171,288,97,112,142, 'Cada semana un paso más.',                                        'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-27 09:00:00'),
  ('alejandro_reyes',  59,55,69,64,79,74,91,86,  121,45,51,180,300,89,121,151, 'El trabajo de fuerza se nota en el sled.',                        'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-28 08:00:00'),
  ('pilar_moreno',     58,53,68,63,78,73,93,87,  125,44,49,184,308,88,124,154, 'Semana brutal. El esfuerzo vale la pena.',                        'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-29 10:00:00'),
  ('antonio_lara',     65,61,75,70,85,80,85,80,  112,50,55,171,289,96,113,143, 'Mejorar en row es mi objetivo del mes.',                          'INDIVIDUAL_MALE',   TIMESTAMP '2026-04-30 07:00:00'),
  ('beatriz_herrero',  60,55,70,65,80,75,90,85,  121,45,50,181,301,90,121,151, 'Temporada en alza. Muy contenta.',                               'INDIVIDUAL_FEMALE', TIMESTAMP '2026-04-30 09:30:00'),
  ('jose_guerrero',    63,59,73,68,83,78,87,82,  115,48,53,175,294,92,118,148, 'Debut. Nervios pero buen resultado.',                             'INDIVIDUAL_MALE',   TIMESTAMP '2026-05-01 08:00:00'),
  ('rosa_medina',      62,57,72,67,82,77,88,83,  119,46,52,178,298,91,119,149, 'Nuevo mes, nuevas metas.',                                        'INDIVIDUAL_FEMALE', TIMESTAMP '2026-05-01 09:00:00'),
  ('manuel_cano',      68,64,78,73,88,83,82,77,  109,52,58,166,284,99,111,141, 'El sprinting es mi punto fuerte.',                               'INDIVIDUAL_MALE',   TIMESTAMP '2026-05-02 07:30:00'),
  ('cristina_vidal',   64,59,74,69,84,79,86,81,  115,48,54,174,292,94,116,146, 'Gran sesión. El trabajo constante da sus frutos.',               'INDIVIDUAL_FEMALE', TIMESTAMP '2026-05-02 08:45:00'),
  ('raul_pascual',     61,57,71,66,81,76,89,84,  118,46,52,179,296,90,119,149, 'Semana cargada pero el resultado merece la pena.',                'INDIVIDUAL_MALE',   TIMESTAMP '2026-05-03 07:15:00'),
  ('eva_serrano',      67,62,77,72,87,82,83,78,  110,51,57,168,286,98,112,142, 'Me sorprendo a mí misma cada entreno.',                          'INDIVIDUAL_FEMALE', TIMESTAMP '2026-05-03 09:00:00')
) AS v(uname, r1,r2,r3,r4,r5,r6,r7,r8, ski,push,pull,burp,row_t,farm,sand,wall, descr,cat,cdate)
JOIN users u ON u.username = v.uname
JOIN boxes b ON b.name = 'CrossFit Origen';

-- ------------------------------------------------------------
-- 3. Amistades — red social de 47 pares
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
-- 4. Comentarios
-- ------------------------------------------------------------
INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'lucia_vega'),
       '¡Qué pasada! Enhorabuena por el PR.',
       TIMESTAMP '2026-04-10 09:15:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'pedro_alonso' AND p.creation_date = TIMESTAMP '2026-04-10 08:00:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'marcos_gil'),
       'Ese sled pull ya se nota que está mejorando, ¡sigue así!',
       TIMESTAMP '2026-04-10 10:00:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'pedro_alonso' AND p.creation_date = TIMESTAMP '2026-04-10 08:00:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'sofia_ramos'),
       'El ski erg siempre ha sido tu fuerte. ¡Brutal resultado!',
       TIMESTAMP '2026-04-14 07:30:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'javier_mena' AND p.creation_date = TIMESTAMP '2026-04-14 06:30:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'pedro_alonso'),
       '¡Bien hecho! A ver si coincidimos en el próximo.',
       TIMESTAMP '2026-04-14 08:00:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'javier_mena' AND p.creation_date = TIMESTAMP '2026-04-14 06:30:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'marta_fuentes'),
       'Wall balls... no hay nada peor jaja. ¡Bien!',
       TIMESTAMP '2026-04-25 09:00:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'lucia_vega' AND p.creation_date = TIMESTAMP '2026-04-25 08:15:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'nuria_pons'),
       '¿Otro PB? ¡Qué animal!',
       TIMESTAMP '2026-04-25 10:30:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'lucia_vega' AND p.creation_date = TIMESTAMP '2026-04-25 08:15:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'pedro_alonso'),
       'Primera competición y ya con ese tiempo. ¡Impresionante!',
       TIMESTAMP '2026-04-20 09:30:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'carlos_martin' AND p.creation_date = TIMESTAMP '2026-04-20 08:30:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'roberto_santos'),
       '¡Bienvenido al club!',
       TIMESTAMP '2026-04-20 10:00:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'carlos_martin' AND p.creation_date = TIMESTAMP '2026-04-20 08:30:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'pedro_alonso'),
       '¡Por fin! Te dije que podías bajar de 27.',
       TIMESTAMP '2026-04-12 08:30:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'marcos_gil' AND p.creation_date = TIMESTAMP '2026-04-12 07:45:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'javier_mena'),
       '¡Máquina!',
       TIMESTAMP '2026-04-12 09:00:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'marcos_gil' AND p.creation_date = TIMESTAMP '2026-04-12 07:45:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'ana_garcia'),
       '¡Debut con nota! Mucho por delante.',
       TIMESTAMP '2026-04-22 11:00:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'laura_jimenez' AND p.creation_date = TIMESTAMP '2026-04-22 10:30:00';

INSERT INTO comments (post_id, user_id, content, creation_date)
SELECT p.id,
       (SELECT id FROM users WHERE username = 'cristina_vidal'),
       '¡Gran sesión! Nos vemos en el próximo.',
       TIMESTAMP '2026-05-02 09:30:00'
FROM posts p JOIN users u ON p.user_id = u.id
WHERE u.username = 'eva_serrano' AND p.creation_date = TIMESTAMP '2026-05-03 09:00:00';

-- ------------------------------------------------------------
-- 5. Likes en posts
-- ------------------------------------------------------------
INSERT INTO post_likes (post_id, user_id)
SELECT p.id, (SELECT id FROM users WHERE username = v.liker)
FROM posts p
JOIN users u ON p.user_id = u.id
JOIN (VALUES
  ('pedro_alonso',  TIMESTAMP '2026-04-10 08:00:00', 'lucia_vega'),
  ('pedro_alonso',  TIMESTAMP '2026-04-10 08:00:00', 'marcos_gil'),
  ('pedro_alonso',  TIMESTAMP '2026-04-10 08:00:00', 'javier_mena'),
  ('pedro_alonso',  TIMESTAMP '2026-04-10 08:00:00', 'carlos_martin'),
  ('javier_mena',   TIMESTAMP '2026-04-14 06:30:00', 'pedro_alonso'),
  ('javier_mena',   TIMESTAMP '2026-04-14 06:30:00', 'sofia_ramos'),
  ('javier_mena',   TIMESTAMP '2026-04-14 06:30:00', 'alberto_diaz'),
  ('lucia_vega',    TIMESTAMP '2026-04-11 09:30:00', 'sofia_ramos'),
  ('lucia_vega',    TIMESTAMP '2026-04-11 09:30:00', 'nuria_pons'),
  ('lucia_vega',    TIMESTAMP '2026-04-11 09:30:00', 'marta_fuentes'),
  ('lucia_vega',    TIMESTAMP '2026-04-11 09:30:00', 'isabel_perez'),
  ('lucia_vega',    TIMESTAMP '2026-04-25 08:15:00', 'ana_garcia'),
  ('lucia_vega',    TIMESTAMP '2026-04-25 08:15:00', 'marta_fuentes'),
  ('marcos_gil',    TIMESTAMP '2026-04-12 07:45:00', 'pedro_alonso'),
  ('marcos_gil',    TIMESTAMP '2026-04-12 07:45:00', 'javier_mena'),
  ('carlos_martin', TIMESTAMP '2026-04-20 08:30:00', 'pedro_alonso'),
  ('carlos_martin', TIMESTAMP '2026-04-20 08:30:00', 'roberto_santos'),
  ('carlos_martin', TIMESTAMP '2026-04-20 08:30:00', 'miguel_torres'),
  ('carlos_martin', TIMESTAMP '2026-04-20 08:30:00', 'raul_pascual'),
  ('nuria_pons',    TIMESTAMP '2026-04-15 10:00:00', 'lucia_vega'),
  ('nuria_pons',    TIMESTAMP '2026-04-15 10:00:00', 'marta_fuentes'),
  ('ana_garcia',    TIMESTAMP '2026-04-21 07:00:00', 'lucia_vega'),
  ('ana_garcia',    TIMESTAMP '2026-04-21 07:00:00', 'laura_jimenez'),
  ('laura_jimenez', TIMESTAMP '2026-04-22 10:30:00', 'ana_garcia'),
  ('laura_jimenez', TIMESTAMP '2026-04-22 10:30:00', 'sofia_ramos'),
  ('miguel_torres', TIMESTAMP '2026-04-22 09:00:00', 'marcos_gil'),
  ('miguel_torres', TIMESTAMP '2026-04-22 09:00:00', 'carlos_martin')
) AS v(owner, cdate, liker)
  ON u.username = v.owner AND p.creation_date = v.cdate
ON CONFLICT DO NOTHING;

-- ------------------------------------------------------------
-- 6. Posts en pareja (DOUBLES)
-- ------------------------------------------------------------
INSERT INTO posts (
    user_id, mate, post_type, box_id,
    running1, running2, running3, running4, running5, running6, running7, running8,
    ski_erg, sled_push, sled_pull, burpee_broad_jump, "row",
    farmers_carry, sandbag_lunges, wall_balls,
    total_time, description, category, creation_date
)
SELECT u.id, m.id, 'RESULT', b.id,
       v.r1,v.r2,v.r3,v.r4,v.r5,v.r6,v.r7,v.r8,
       v.ski,v.push,v.pull,v.burp,v.row_t,v.farm,v.sand,v.wall,
       v.r1+v.r2+v.r3+v.r4+v.r5+v.r6+v.r7+v.r8+v.ski+v.push+v.pull+v.burp+v.row_t+v.farm+v.sand+v.wall,
       v.descr, v.cat, v.cdate
FROM (VALUES
  ('pedro_alonso',   'marcos_gil',      65,60,74,69,84,79,91,86,  120,48,54,181,298,93,121,151, 'Primera vez en pareja. ¡Brutal sinergia!',                       'DOUBLES_MALE',   TIMESTAMP '2026-05-01 08:00:00'),
  ('javier_mena',    'alberto_diaz',    67,62,76,71,86,81,93,88,  122,50,56,183,300,95,123,153, 'Mejor tiempo en dobles masculino hasta la fecha.',               'DOUBLES_MALE',   TIMESTAMP '2026-05-02 07:30:00'),
  ('carlos_martin',  'roberto_santos',  64,59,73,68,83,78,90,85,  119,47,53,180,297,92,120,150, 'Bien coordinados en el sled. Repetiremos.',                      'DOUBLES_MALE',   TIMESTAMP '2026-05-03 08:15:00'),
  ('miguel_torres',  'david_romero',    66,61,75,70,85,80,92,87,  121,49,55,182,299,94,122,152, 'Nos hemos complementado perfectamente en el ski erg.',           'DOUBLES_MALE',   TIMESTAMP '2026-05-04 07:00:00'),
  ('lucia_vega',     'sofia_ramos',     60,55,70,65,80,75,95,89,  126,46,51,185,309,90,125,155, 'Dobles femenino por primera vez. ¡Muy satisfechas!',             'DOUBLES_FEMALE', TIMESTAMP '2026-05-01 09:30:00'),
  ('nuria_pons',     'marta_fuentes',   61,56,71,66,81,76,94,88,  125,45,50,184,308,89,124,154, 'El burpee broad jump fue el punto fuerte del equipo.',           'DOUBLES_FEMALE', TIMESTAMP '2026-05-02 10:00:00'),
  ('ana_garcia',     'laura_jimenez',   59,54,69,64,79,74,96,90,  127,47,52,186,310,91,126,156, 'Gran entrenamiento. Mañana repetimos apuntando al podio.',       'DOUBLES_FEMALE', TIMESTAMP '2026-05-03 09:00:00'),
  ('pedro_alonso',   'lucia_vega',      63,58,72,67,82,77,92,87,  120,47,53,182,300,92,122,152, 'Mixto con mucha energía. El wall balls voló.',                   'DOUBLES_MIXED',  TIMESTAMP '2026-05-05 08:00:00'),
  ('javier_mena',    'nuria_pons',      65,60,74,69,84,79,91,86,  122,49,55,183,301,93,123,153, 'Ritmo constante de principio a fin. Top resultado mixto.',       'DOUBLES_MIXED',  TIMESTAMP '2026-05-06 07:45:00'),
  ('carlos_martin',  'elena_castro',    64,59,73,68,83,78,90,85,  121,48,54,181,299,93,121,151, 'Entrenamiento mixto muy completo. El row fue espectacular.',     'DOUBLES_MIXED',  TIMESTAMP '2026-05-07 08:30:00')
) AS v(uname, mate_name, r1,r2,r3,r4,r5,r6,r7,r8, ski,push,pull,burp,row_t,farm,sand,wall, descr,cat,cdate)
JOIN users u ON u.username = v.uname
JOIN users m ON m.username = v.mate_name
JOIN boxes b ON b.name = 'CrossFit Origen';

INSERT INTO posts (
    user_id, post_type, box_id,
    running1, running2, running3, running4, running5, running6, running7, running8,
    ski_erg, sled_push, sled_pull, burpee_broad_jump, "row",
    farmers_carry, sandbag_lunges, wall_balls,
    total_time, description, category, creation_date
)
SELECT u.id, 'RESULT', b.id,
       v.r1,v.r2,v.r3,v.r4,v.r5,v.r6,v.r7,v.r8,
       v.ski,v.push,v.pull,v.burp,v.row_t,v.farm,v.sand,v.wall,
       v.r1+v.r2+v.r3+v.r4+v.r5+v.r6+v.r7+v.r8+v.ski+v.push+v.pull+v.burp+v.row_t+v.farm+v.sand+v.wall,
       v.descr, v.cat, v.cdate
FROM (VALUES
  ('javier_mena', 64,60,74,69,84,79,86,81, 113,49,55,171,289,96,113,143, 'Semana de carga. Sensaciones muy buenas en el ski erg.',         'INDIVIDUAL_MALE', TIMESTAMP '2026-05-08 06:30:00'),
  ('javier_mena', 63,59,73,68,83,78,87,82, 114,48,54,172,290,95,112,142, 'El sled pull mejora cada semana. A seguir así.',                 'INDIVIDUAL_MALE', TIMESTAMP '2026-05-12 07:00:00'),
  ('javier_mena', 65,61,75,70,85,80,84,79, 111,50,56,170,288,97,113,143, 'Bajé otro segundo en total. Pequeños avances que suman.',       'INDIVIDUAL_MALE', TIMESTAMP '2026-05-16 06:45:00'),
  ('javier_mena', 62,58,72,67,82,77,88,83, 115,47,53,173,291,94,114,144, 'Entrenamiento con lluvia. El burpee fue un caos.',               'INDIVIDUAL_MALE', TIMESTAMP '2026-05-20 08:00:00'),
  ('javier_mena', 66,62,76,71,86,81,83,78, 110,51,57,168,287,98,112,142, 'Nuevo PR en row. El trabajo de espalda se nota.',               'INDIVIDUAL_MALE', TIMESTAMP '2026-05-24 06:30:00'),
  ('javier_mena', 64,60,74,69,84,79,85,80, 112,49,55,169,288,96,112,142, 'Sesión nocturna. Ambiente brutal en el box.',                   'INDIVIDUAL_MALE', TIMESTAMP '2026-05-28 20:00:00'),
  ('javier_mena', 63,59,73,68,83,78,86,81, 113,48,55,171,289,95,113,143, 'Competición. Nervioso al principio pero buen ritmo al final.',  'INDIVIDUAL_MALE', TIMESTAMP '2026-06-01 09:00:00'),
  ('javier_mena', 61,57,71,66,81,76,88,83, 116,47,53,174,292,93,115,145, 'Recuperación activa. No era el día pero lo completé.',         'INDIVIDUAL_MALE', TIMESTAMP '2026-06-05 07:30:00'),
  ('javier_mena', 65,61,75,70,85,80,84,79, 111,50,56,169,287,97,112,142, 'Vuelta al 100%. Los farmers carry se sienten fáciles ya.',      'INDIVIDUAL_MALE', TIMESTAMP '2026-06-09 06:45:00'),
  ('javier_mena', 64,60,74,69,84,79,85,80, 112,49,55,170,288,96,113,143, 'Fin de temporada. Mejor marca del año. ¡A por la siguiente!',  'INDIVIDUAL_MALE', TIMESTAMP '2026-06-13 08:00:00')
) AS v(uname, r1,r2,r3,r4,r5,r6,r7,r8, ski,push,pull,burp,row_t,farm,sand,wall, descr,cat,cdate)
JOIN users u ON u.username = v.uname
JOIN boxes b ON b.name = 'CrossFit Origen';

-- ------------------------------------------------------------
-- 7. Posts de box y check-ins variados (mezcla realista del feed)
--    Fechas recientes (jul–ago 2026) para que salgan arriba en el feed
-- ------------------------------------------------------------

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
  ('roberto_santos','OTRO',    'Movilidad y core. Día de descarga después del open workout.',                      TIMESTAMP '2026-08-01 09:30:00'),
  ('lucia_vega',    'HYROX',   'Circuito HYROX express en el box. 4 estaciones a tope.',                           TIMESTAMP '2026-08-02 10:00:00'),
  ('nuria_pons',    'CLASE',   'Primera clase de la semana. Motivación al máximo.',                                TIMESTAMP '2026-08-03 07:45:00'),
  ('miguel_torres', 'CARRERA', 'Fartlek por el parque. 45 minutos sin mirar el reloj.',                            TIMESTAMP '2026-08-04 07:00:00'),
  ('ana_garcia',    'FUERZA',  'Peso muerto + hip thrust. Volumen moderado.',                                      TIMESTAMP '2026-08-05 18:20:00'),
  ('pedro_alonso',  'HYROX',   'Ensayo de transición entre estaciones. Cada segundo cuenta.',                      TIMESTAMP '2026-08-06 08:10:00'),
  ('marcos_gil',    'OTRO',    'Sesión de movilidad y foam roller. Recuperación activa.',                          TIMESTAMP '2026-08-07 20:00:00'),
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
  ('javier_mena', 'OTRO',    'Movilidad y core. Descarga activa.',                       TIMESTAMP '2026-07-31 09:00:00'),
  -- W32 (3–9 ago) — Aug 8 ya existe; dots L M X J · · S
  ('javier_mena', 'FUERZA',  'Empuje: press militar + fondos.',                          TIMESTAMP '2026-08-03 18:30:00'),
  ('javier_mena', 'CARRERA', 'Fartlek 40 min por el parque.',                            TIMESTAMP '2026-08-04 07:10:00'),
  ('javier_mena', 'HYROX',   'Simulación de transiciones entre estaciones.',             TIMESTAMP '2026-08-05 08:00:00'),
  ('javier_mena', 'CLASE',   'Clase matinal. Buen feeling de piernas.',                  TIMESTAMP '2026-08-06 07:40:00')
) AS v(uname, tag, descr, cdate)
JOIN users u ON u.username = v.uname;

-- Constancia semanal de demo (marcos_gil): 3 días/semana en W29–W32 (racha 4 semanas)
-- Reutiliza Jul 26 (RESULT), Jul 28 y Aug 7 (CHECKIN) ya insertados arriba
INSERT INTO posts (user_id, post_type, training_tag, description, creation_date)
SELECT u.id, 'CHECKIN', v.tag, v.descr, v.cdate
FROM (VALUES
  -- W29 (13–19 jul)
  ('marcos_gil', 'FUERZA',  'Fuerza: sentadilla y press. Arranque de bloque.',          TIMESTAMP '2026-07-13 18:30:00'),
  ('marcos_gil', 'CARRERA', 'Rodaje 8km zona 2. Piernas frescas.',                      TIMESTAMP '2026-07-15 07:00:00'),
  ('marcos_gil', 'HYROX',   'Circuito corto: ski + sled + wall balls.',                 TIMESTAMP '2026-07-17 07:20:00'),
  -- W30 (20–26 jul) — Jul 26 ya existe como RESULT
  ('marcos_gil', 'CLASE',   'Clase de technique. Farmers y lunges.',                    TIMESTAMP '2026-07-21 19:00:00'),
  ('marcos_gil', 'FUERZA',  'Peso muerto + hip thrust. Volumen moderado.',              TIMESTAMP '2026-07-23 18:45:00'),
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

-- RESULTADOS recientes de amigos (para mezclar tipos en el feed)
INSERT INTO posts (
    user_id, post_type, box_id,
    running1, running2, running3, running4, running5, running6, running7, running8,
    ski_erg, sled_push, sled_pull, burpee_broad_jump, "row",
    farmers_carry, sandbag_lunges, wall_balls,
    total_time, description, category, creation_date
)
SELECT u.id, 'RESULT', b.id,
       v.r1,v.r2,v.r3,v.r4,v.r5,v.r6,v.r7,v.r8,
       v.ski,v.push,v.pull,v.burp,v.row_t,v.farm,v.sand,v.wall,
       v.r1+v.r2+v.r3+v.r4+v.r5+v.r6+v.r7+v.r8+v.ski+v.push+v.pull+v.burp+v.row_t+v.farm+v.sand+v.wall,
       v.descr, v.cat, v.cdate
FROM (VALUES
  ('marcos_gil',    61,56,70,65,80,75,88,83, 115,46,52,175,290,92,118,148,
   'Simulación completa de sábado. Mejoría clara en el sled.', 'INDIVIDUAL_MALE',
   TIMESTAMP '2026-07-26 09:00:00'),
  ('javier_mena',   64,60,74,69,84,79,86,81, 112,49,55,170,288,96,114,144,
   'Open workout casero. El row me salvó el total.', 'INDIVIDUAL_MALE',
   TIMESTAMP '2026-08-02 08:30:00'),
  ('lucia_vega',    58,53,68,63,78,73,92,87, 122,44,50,182,305,89,122,152,
   'Segunda simulación del mes. Wall balls más fluidas.', 'INDIVIDUAL_FEMALE',
   TIMESTAMP '2026-08-04 09:15:00'),
  ('roberto_santos',63,58,72,67,82,77,87,82, 117,48,54,176,294,93,117,147,
   'Entrenamiento con marca. Cerca del PB de primavera.', 'INDIVIDUAL_MALE',
   TIMESTAMP '2026-08-06 07:45:00'),
  ('alberto_diaz',  62,57,71,66,81,76,89,84, 118,47,53,177,295,92,118,148,
   'Doble con buen ritmo. Transiciones más limpias.', 'INDIVIDUAL_MALE',
   TIMESTAMP '2026-08-07 18:00:00')
) AS v(uname, r1,r2,r3,r4,r5,r6,r7,r8, ski,push,pull,burp,row_t,farm,sand,wall, descr,cat,cdate)
JOIN users u ON u.username = v.uname
JOIN boxes b ON b.name = 'CrossFit Origen';

-- ------------------------------------------------------------
-- 8. Participantes de retos de box
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
