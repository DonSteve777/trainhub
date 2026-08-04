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
-- 0. Limpiar tablas (excepto users)
-- ------------------------------------------------------------
TRUNCATE TABLE comment_likes, post_likes, comments, friendships, posts RESTART IDENTITY CASCADE;

-- ------------------------------------------------------------
-- 1. Género (el endpoint de registro en bulk no lo persiste)
-- ------------------------------------------------------------
UPDATE users SET gender = 'MALE'   WHERE username IN ('pedro_alonso','marcos_gil','javier_mena','alberto_diaz','roberto_santos','carlos_martin','miguel_torres','david_romero','sergio_molina','fernando_rubio','alejandro_reyes','antonio_lara','jose_guerrero','manuel_cano','raul_pascual');
UPDATE users SET gender = 'FEMALE' WHERE username IN ('lucia_vega','sofia_ramos','nuria_pons','marta_fuentes','isabel_perez','ana_garcia','laura_jimenez','paula_navarro','elena_castro','carmen_ortiz','pilar_moreno','beatriz_herrero','rosa_medina','cristina_vidal','eva_serrano');

-- ------------------------------------------------------------
-- 2. Posts — 35 entrenamientos (5 usuarios con 2 posts)
--    total_time = suma de los 16 campos de ejercicio
-- ------------------------------------------------------------
INSERT INTO posts (
    user_id,
    running1, running2, running3, running4, running5, running6, running7, running8,
    ski_erg, sled_push, sled_pull, burpee_broad_jump, "row",
    farmers_carry, sandbag_lunges, wall_balls,
    total_time, description, category, creation_date
)
SELECT u.id,
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
JOIN users u ON u.username = v.uname;

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
    user_id, mate,
    running1, running2, running3, running4, running5, running6, running7, running8,
    ski_erg, sled_push, sled_pull, burpee_broad_jump, "row",
    farmers_carry, sandbag_lunges, wall_balls,
    total_time, description, category, creation_date
)
SELECT u.id, m.id,
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
JOIN users m ON m.username = v.mate_name;

INSERT INTO posts (
    user_id,
    running1, running2, running3, running4, running5, running6, running7, running8,
    ski_erg, sled_push, sled_pull, burpee_broad_jump, "row",
    farmers_carry, sandbag_lunges, wall_balls,
    total_time, description, category, creation_date
)
SELECT u.id,
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
JOIN users u ON u.username = v.uname;
