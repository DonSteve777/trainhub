TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE posts CASCADE;
TRUNCATE TABLE friendships CASCADE;



-- ============================================================
-- prueba: ver un comentario hecho por 93adones a un post de javier mena
-- lucia vega con id = ?? es amiga de javier mena
--    La constraint exige user_a_id < user_b_id → como los nuevos
-- ============================================================
INSERT INTO friendships (user_a_id, user_b_id, status)
SELECT 92, id, 'FRIEND'
FROM users
WHERE username IN (
  'pedro_alonso', 'marcos_gil', 'sofia_ramos', 'javier_mena',
  'nuria_pons', 'alberto_diaz', 'marta_fuentes', 'roberto_santos'
);

ALTER TABLE users ADD COLUMN notifications_last_seen_at TIMESTAMP;


-- ============================================================
-- 2. Amistades con el usuario 93adones@gmail.com
--    La constraint exige user_a_id < user_b_id → como los nuevos
-- ============================================================
INSERT INTO friendships (user_a_id, user_b_id, status)
SELECT 108, id, 'FRIEND'
FROM users
WHERE username IN (
  'lucia_vega', 'marcos_gil', 'pedro_alonso'
  
);

INSERT INTO friendships (user_a_id, user_b_id, status)
SELECT 104, id, 'FRIEND'
FROM users
WHERE username IN (
  'lucia_vega', 'marcos_gil', 'sofia_ramos', 'javier_mena',
  'nuria_pons', 'alberto_diaz', 'marta_fuentes', 'roberto_santos', 'isabel_perez'
);

INSERT INTO posts (
  user_id,
  running1, running2, running3, running4, running5, running6, running7, running8,
  ski_erg, sled_push, sled_pull, burpee_broad_jump, row, farmers_carry, sandbag_lunges, wall_balls,
  total_time, description, creation_date
)
SELECT id,
       r1, r2, r3, r4, r5, r6, r7, r8,
       w1, w2, w3, w4, w5, w6, w7, w8,
       r1+r2+r3+r4+r5+r6+r7+r8 + w1+w2+w3+w4+w5+w6+w7+w8,
       desc_text,
       cdate
FROM users
JOIN (VALUES
  ('pedro_alonso',    62, 57, 71, 66, 81, 76, 89, 84,  118, 46, 52, 179, 296, 91, 119, 149, 'Mejor marca personal en la carrera de obstáculos!',        TIMESTAMP '2026-04-10 08:00:00'),
  ('lucia_vega',      58, 53, 68, 63, 78, 73, 93, 87,  124, 44, 49, 183, 307, 88, 123, 153, 'Entrenamiento duro pero gratificante.',                    TIMESTAMP '2026-04-11 09:30:00'),
  ('marcos_gil',      64, 60, 74, 69, 84, 79, 86, 81,  115, 47, 54, 174, 293, 94, 116, 146, 'Primera vez que bajo de 27 minutos!',                      TIMESTAMP '2026-04-12 07:45:00'),
  ('sofia_ramos',     60, 55, 70, 65, 80, 75, 90, 85,  120, 45, 50, 180, 300, 90, 120, 150, 'Sesión completada con el equipo.',                         TIMESTAMP '2026-04-13 18:15:00'),
  ('javier_mena',     66, 62, 76, 71, 86, 81, 84, 79,  112, 50, 56, 170, 288, 97, 113, 143, 'Nuevo PR en ski erg. El entreno de fuerza está dando frutos.', TIMESTAMP '2026-04-14 06:30:00'),
  ('nuria_pons',      57, 52, 67, 62, 77, 72, 94, 88,  126, 43, 48, 184, 309, 87, 124, 154, 'Vuelta a la competición después de las vacaciones.',       TIMESTAMP '2026-04-15 10:00:00'),
  ('alberto_diaz',    63, 59, 73, 68, 83, 78, 87, 82,  116, 48, 53, 176, 294, 93, 117, 147, 'Las wall balls me matan, pero el tiempo total mejora.',    TIMESTAMP '2026-04-16 08:45:00'),
  ('marta_fuentes',   59, 54, 69, 64, 79, 74, 91, 86,  122, 44, 51, 181, 302, 89, 122, 152, 'Entrenamiento en solitario. Muy concentrada hoy.',         TIMESTAMP '2026-04-17 17:00:00'),
  ('roberto_santos',  65, 61, 75, 70, 85, 80, 85, 80,  113, 49, 55, 172, 290, 95, 114, 144, 'El sled pull sigue siendo mi talón de Aquiles.',           TIMESTAMP '2026-04-18 07:15:00'),
  ('isabel_perez',    61, 56, 72, 67, 82, 77, 88, 83,  119, 45, 51, 178, 298, 91, 119, 149, 'Buen día, buen humor, buen tiempo.',                       TIMESTAMP '2026-04-19 09:00:00')
) AS v(uname, r1,r2,r3,r4,r5,r6,r7,r8, w1,w2,w3,w4,w5,w6,w7,w8, desc_text, cdate)
  ON users.username = v.uname;


INSERT INTO posts (
  user_id,
  running1, running2, running3, running4, running5, running6, running7, running8,
  ski_erg, sled_push, sled_pull, burpee_broad_jump, row, farmers_carry, sandbag_lunges, wall_balls,
  total_time, description, creation_date
)
SELECT id,
       r1, r2, r3, r4, r5, r6, r7, r8,
       w1, w2, w3, w4, w5, w6, w7, w8,
       r1+r2+r3+r4+r5+r6+r7+r8 + w1+w2+w3+w4+w5+w6+w7+w8,
       desc_text,
       cdate
FROM users
JOIN (VALUES
  ('pedro_alonso',    62, 57, 71, 66, 81, 76, 89, 84,  118, 46, 52, 179, 296, 91, 119, 149, 'Mejor marca personal en la carrera de obstáculos!',        TIMESTAMP '2026-04-10 08:00:00'),
  ('lucia_vega',      58, 53, 68, 63, 78, 73, 93, 87,  124, 44, 49, 183, 307, 88, 123, 153, 'Entrenamiento duro pero gratificante.',                    TIMESTAMP '2026-04-11 09:30:00'),
  ('marcos_gil',      64, 60, 74, 69, 84, 79, 86, 81,  115, 47, 54, 174, 293, 94, 116, 146, 'Primera vez que bajo de 27 minutos!',                      TIMESTAMP '2026-04-12 07:45:00'),
  ('sofia_ramos',     60, 55, 70, 65, 80, 75, 90, 85,  120, 45, 50, 180, 300, 90, 120, 150, 'Sesión completada con el equipo.',                         TIMESTAMP '2026-04-13 18:15:00'),
  ('javier_mena',     66, 62, 76, 71, 86, 81, 84, 79,  112, 50, 56, 170, 288, 97, 113, 143, 'Nuevo PR en ski erg. El entreno de fuerza está dando frutos.', TIMESTAMP '2026-04-14 06:30:00'),
  ('nuria_pons',      57, 52, 67, 62, 77, 72, 94, 88,  126, 43, 48, 184, 309, 87, 124, 154, 'Vuelta a la competición después de las vacaciones.',       TIMESTAMP '2026-04-15 10:00:00'),
  ('alberto_diaz',    63, 59, 73, 68, 83, 78, 87, 82,  116, 48, 53, 176, 294, 93, 117, 147, 'Las wall balls me matan, pero el tiempo total mejora.',    TIMESTAMP '2026-04-16 08:45:00'),
  ('marta_fuentes',   59, 54, 69, 64, 79, 74, 91, 86,  122, 44, 51, 181, 302, 89, 122, 152, 'Entrenamiento en solitario. Muy concentrada hoy.',         TIMESTAMP '2026-04-17 17:00:00'),
  ('roberto_santos',  65, 61, 75, 70, 85, 80, 85, 80,  113, 49, 55, 172, 290, 95, 114, 144, 'El sled pull sigue siendo mi talón de Aquiles.',           TIMESTAMP '2026-04-18 07:15:00'),
  ('isabel_perez',    61, 56, 72, 67, 82, 77, 88, 83,  119, 45, 51, 178, 298, 91, 119, 149, 'Buen día, buen humor, buen tiempo.',                       TIMESTAMP '2026-04-19 09:00:00')
) AS v(uname, r1,r2,r3,r4,r5,r6,r7,r8, w1,w2,w3,w4,w5,w6,w7,w8, desc_text, cdate)
  ON users.username = v.uname;

-- ============================================================
-- 4. Dos posts extra para el usuario nuria.pons@example.com id 96
-- ============================================================
INSERT INTO posts (
  user_id,
  running1, running2, running3, running4, running5, running6, running7, running8,
  ski_erg, sled_push, sled_pull, burpee_broad_jump, row, farmers_carry, sandbag_lunges, wall_balls,
  total_time, description, creation_date
) VALUES
  (96,
   56, 51, 66, 61, 76, 71, 96, 90,
   128, 42, 47, 186, 312, 85, 126, 156,
   56+51+66+61+76+71+96+90 + 128+42+47+186+312+85+126+156,
   'Segunda sesión de la semana. El cuerpo responde bien.',
   TIMESTAMP '2026-04-20 08:00:00'),
  (96,
   54, 49, 64, 59, 74, 69, 98, 92,
   130, 41, 46, 188, 315, 84, 128, 158,
   54+49+64+59+74+69+98+92 + 130+41+46+188+315+84+128+158,
   'Mejorando en burpee broad jump, flojeo en ski erg.',
   TIMESTAMP '2026-04-21 07:30:00');

-- ============================================================
-- 5. Dos posts extra para el usuario javier.mena@example.com
-- ============================================================
INSERT INTO posts (
  user_id,
  running1, running2, running3, running4, running5, running6, running7, running8,
  ski_erg, sled_push, sled_pull, burpee_broad_jump, row, farmers_carry, sandbag_lunges, wall_balls,
  total_time, description, creation_date
) VALUES
  (95,
   63, 58, 73, 68, 83, 78, 87, 82,
   116, 47, 53, 177, 295, 92, 117, 147,
   63+58+73+68+83+78+87+82 + 116+47+53+177+295+92+117+147,
   'Consistencia ante todo. Semana muy sólida.',
   TIMESTAMP '2026-04-22 09:15:00'),
  (95,
   65, 60, 75, 70, 85, 80, 85, 80,
   113, 49, 55, 173, 291, 95, 114, 144,
   65+60+75+70+85+80+85+80 + 113+49+55+173+291+95+114+144,
   'Último entrenamiento antes de la competición del domingo.',
   TIMESTAMP '2026-04-23 06:45:00');

-- ============================================================
-- 6. Like a un post de javier.mena@example.com
-- ============================================================
INSERT INTO post_likes (post_id, user_id, liked_at)
VALUES (, 7, NOW());



-- ============================================================
-- 7. Insertar 10 posts para el usuario 108 javier mena
-- ============================================================
INSERT INTO posts (
  user_id,
  running1, running2, running3, running4, running5, running6, running7, running8,
  ski_erg, sled_push, sled_pull, burpee_broad_jump, row, farmers_carry, sandbag_lunges, wall_balls,
  total_time, description, creation_date
) VALUES
  (108, 60,55,70,65,80,75,100,95, 135,45,50,195,320,90,132,162,
   60+55+70+65+80+75+100+95+135+45+50+195+320+90+132+162,
   'Primera sesión del mes. Buena energía desde el principio.',
   TIMESTAMP '2026-04-01 07:00:00'),

  (108, 59,54,69,64,79,74,99,94, 134,44,49,193,318,89,130,160,
   59+54+69+64+79+74+99+94+134+44+49+193+318+89+130+160,
   'Ritmo constante, bien en los running splits.',
   TIMESTAMP '2026-04-03 07:15:00'),

  (108, 58,53,68,63,78,73,98,93, 133,44,48,191,316,88,129,159,
   58+53+68+63+78+73+98+93+133+44+48+191+316+88+129+159,
   'Cansancio acumulado pero no me detuve.',
   TIMESTAMP '2026-04-05 08:00:00'),

  (108, 57,52,67,62,77,72,97,92, 132,43,48,190,315,87,128,158,
   57+52+67+62+77+72+97+92+132+43+48+190+315+87+128+158,
   'Sled push mejorado respecto a la semana pasada.',
   TIMESTAMP '2026-04-07 07:30:00'),

  (108, 57,52,67,62,77,71,97,91, 131,43,47,189,313,87,127,157,
   57+52+67+62+77+71+97+91+131+43+47+189+313+87+127+157,
   'Buen día, todo fluyó bien.',
   TIMESTAMP '2026-04-09 07:00:00'),

  (108, 56,51,66,61,76,71,96,91, 130,42,47,188,312,86,126,156,
   56+51+66+61+76+71+96+91+130+42+47+188+312+86+126+156,
   'Sesión de mitad de semana. Enfocado en la técnica.',
   TIMESTAMP '2026-04-11 08:30:00'),

  (108, 56,51,66,61,76,70,96,90, 130,42,46,187,310,85,125,155,
   56+51+66+61+76+70+96+90+130+42+46+187+310+85+125+155,
   'Wall balls muy bien hoy.',
   TIMESTAMP '2026-04-13 07:00:00'),

  (108, 55,50,65,60,75,70,95,90, 129,42,46,186,309,85,125,154,
   55+50+65+60+75+70+95+90+129+42+46+186+309+85+125+154,
   'Farmers carry más ligero que nunca.',
   TIMESTAMP '2026-04-15 07:45:00'),

  (108, 55,50,65,60,75,69,95,89, 128,41,46,185,308,84,124,153,
   55+50+65+60+75+69+95+89+128+41+46+185+308+84+124+153,
   'Muy bien en row, necesito mejorar burpee.',
   TIMESTAMP '2026-04-17 08:00:00'),

  (108, 54,49,64,59,74,69,94,89, 128,41,45,184,306,84,123,152,
   54+49+64+59+74+69+94+89+128+41+45+184+306+84+123+152,
   'Semana de carga alta, el cuerpo aguanta.',
   TIMESTAMP '2026-04-19 07:30:00'),

  (108, 54,49,64,59,74,68,94,88, 127,41,45,183,305,83,122,151,
   54+49+64+59+74+68+94+88+127+41+45+183+305+83+122+151,
   'Mejora progresiva en todos los ejercicios.',
   TIMESTAMP '2026-04-21 07:00:00'),

  (108, 53,48,63,58,73,68,93,88, 126,40,44,182,303,83,121,150,
   53+48+63+58+73+68+93+88+126+40+44+182+303+83+121+150,
   'Jornada completa sin paradas.',
   TIMESTAMP '2026-04-23 08:15:00'),

  (108, 53,48,63,58,73,67,93,87, 126,40,44,181,302,82,121,149,
   53+48+63+58+73+67+93+87+126+40+44+181+302+82+121+149,
   'Sled pull costó más de lo normal.',
   TIMESTAMP '2026-04-25 07:00:00'),

  (108, 52,47,62,57,72,67,92,87, 125,40,43,180,300,82,120,148,
   52+47+62+57+72+67+92+87+125+40+43+180+300+82+120+148,
   'Buena sesión previa al descanso del fin de semana.',
   TIMESTAMP '2026-04-27 07:30:00'),

  (108, 52,47,62,57,72,66,92,86, 124,39,43,179,299,81,119,147,
   52+47+62+57+72+66+92+86+124+39+43+179+299+81+119+147,
   'Comienzo de la última semana de abril fuerte.',
   TIMESTAMP '2026-04-29 07:00:00'),

  (108, 51,47,62,57,71,66,91,86, 124,39,43,178,297,81,119,146,
   51+47+62+57+71+66+91+86+124+39+43+178+297+81+119+146,
   'Fin de mes, superé mis marcas personales.',
   TIMESTAMP '2026-04-30 08:00:00'),

  (108, 51,46,61,56,71,65,91,85, 123,39,42,177,296,80,118,145,
   51+46+61+56+71+65+91+85+123+39+42+177+296+80+118+145,
   'Arrancando mayo con ganas.',
   TIMESTAMP '2026-05-01 07:00:00'),

  (108, 50,46,61,56,70,65,90,85, 122,38,42,176,294,80,117,144,
   50+46+61+56+70+65+90+85+122+38+42+176+294+80+117+144,
   'Ritmo sostenido durante toda la sesión.',
   TIMESTAMP '2026-05-01 17:00:00'),

  (108, 50,45,60,55,70,64,90,84, 122,38,41,175,293,79,116,143,
   50+45+60+55+70+64+90+84+122+38+41+175+293+79+116+143,
   'Mejora notable en ski erg.',
   TIMESTAMP '2026-05-02 07:30:00'),

  (108, 49,45,60,55,69,64,89,84, 121,38,41,174,291,79,116,142,
   49+45+60+55+69+64+89+84+121+38+41+174+291+79+116+142,
   'Gran sesión de cierre de semana.',
   TIMESTAMP '2026-05-03 07:00:00');