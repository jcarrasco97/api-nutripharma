-- ============================================================================
-- data.sql — Migración Legacy (erp_legacy_temp) → Nuevo Sistema (nutripharma_v2)
-- Generado el 2026-05-24
-- ============================================================================
-- REGLAS APLICADAS:
--   1. Solo nutricionistas: CRISTINA SANCHEZ, PACO MALDONADO GARCIA,
--      ANA DEL MORAL, OUMAIMA.
--   2. Farmacias vinculadas ÚNICAMENTE a esas 4 nutricionistas (campo
--      clientes.idNutricionista del legacy).
--   3. Catálogo completo de productos del legacy.
--   4. Todos los nombres transformados a Title Case.
--   5. Se omiten tablas no relacionadas (pacientes, facturas, albaranes, etc.).
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 1. ROLES
-- ============================================================================
-- Fuente: tabla propia del nuevo sistema (no existe en legacy).
-- Se insertan los roles base que el sistema Spring Boot necesita.

INSERT INTO `roles` (`id`, `nombre`) VALUES
  (1, 'ROLE_ADMIN'),
  (2, 'ROLE_NUTRICIONISTA'),
  (3, 'ROLE_FARMACIA'),
  (4, 'ROLE_SUPERADMIN');

-- ============================================================================
-- 1.5 CONFIGURACION GLOBAL
-- ============================================================================
INSERT INTO `configuracion_global` (`id`, `limite_monedero`) VALUES (1, 80.00);


-- ============================================================================
-- 2. USUARIOS
-- ============================================================================
-- Fuente: generados para dar soporte a las entidades nutricionista y farmacia.
-- El legacy NO tiene tabla de usuarios; los emails se toman de
-- nutricionistas.email / clientes.email cuando existen.
-- Password por defecto = BCrypt (coste 10) de 'Nutripharma2026!'.
-- IDs 1-4 → nutricionistas, IDs 5-38 → farmacias.

INSERT INTO `usuarios` (`id`, `activo`, `email`, `password`) VALUES
  -- Nutricionistas
  (1,  1, 'cristina.sanchez@mynutripharma.com',     '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),
  (2,  1, 'paco.maldonado@mynutripharma.com',       '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),
  (3,  1, 'ana.delmoral@mynutripharma.com',          '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),
  (4,  1, 'oumaima@mynutripharma.com',               '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),
  -- Farmacias (una por farmacia/cliente del legacy)
  -- Nutri 41 (Cristina Sanchez) — 16 farmacias
  (5,  1, 'farmacia.centromedicoretamarina@nutripharma.com',  '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=36
  (6,  1, 'farmacia.chafarinas@nutripharma.com',              '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=65
  (7,  1, 'farmacia.sanar@nutripharma.com',                   '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=67
  (8,  1, 'farmacia.caparrosyreina@nutripharma.com',          '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=73
  (9,  1, 'farmacia.bulevard@nutripharma.com',                '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=75
  (10, 1, 'farmacia.elparque@nutripharma.com',                '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=77
  (11, 1, 'farmacia.carlosfernandez@nutripharma.com',         '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=80
  (12, 1, 'farmacia.mariamullor@nutripharma.com',             '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=82
  (13, 1, 'farmacia.valverde@nutripharma.com',                '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=88
  (14, 1, 'farmacia.garciabalcazar@nutripharma.com',          '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=89
  (15, 1, 'farmacia.gador@nutripharma.com',                   '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=91
  (16, 1, 'farmacia.viator@nutripharma.com',                  '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=93
  (17, 1, 'farmacia.losllanos@nutripharma.com',               '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=102
  (18, 1, 'farmacia.silviasoler@nutripharma.com',             '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=110
  (19, 1, 'farmacia.garciarodriguez@nutripharma.com',         '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=111
  (20, 1, 'farmacia.consultasonlinealmeria@nutripharma.com',  '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=112
  -- Nutri 10 (Paco Maldonado) — 4 farmacias
  (21, 1, 'farmacia.franciscaluna@nutripharma.com',           '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=16
  (22, 1, 'farmacia.consultaalmeria@nutripharma.com',         '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=57
  (23, 1, 'farmacia.bolaazul@nutripharma.com',                '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=71
  (24, 1, 'farmacia.castelar@nutripharma.com',                '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=98
  (25, 1, 'farmacia.mariadeona@nutripharma.com',              '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=99
  -- Nutri 36 (Ana del Moral) — 7 farmacias
  (26, 1, 'farmacia.mariadelpozo@nutripharma.com',            '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=5
  (27, 1, 'farmacia.carmencaroeismann@nutripharma.com',       '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=9
  (28, 1, 'farmacia.montoro@nutripharma.com',                 '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=60
  (29, 1, 'farmacia.isabelvilar@nutripharma.com',             '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=94
  (30, 1, 'farmacia.callehuelva5@nutripharma.com',            '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=96
  (31, 1, 'farmacia.elenacobo@nutripharma.com',               '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=101
  (32, 1, 'farmacia.nutripharmaana@nutripharma.com',          '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=104
  -- Nutri 37 (Oumaima) — 6 farmacias
  (33, 1, 'farmacia.inesgomez@nutripharma.com',               '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=3
  (34, 1, 'farmacia.jaimesanchez@nutripharma.com',            '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=4
  (35, 1, 'farmacia.gonzalezsauci@nutripharma.com',           '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=10
  (36, 1, 'farmacia.antoniogimenez@nutripharma.com',          '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=49
  (37, 1, 'farmacia.buendia@nutripharma.com',                 '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe'),   -- legacy idCliente=100
  (38, 1, 'farmacia.nutripharmaoumaima@nutripharma.com',      '$2a$10$b5qNKyJyLu2.wOZFJdXWiORx7N7i4rVIh2yJYmlKC1ANHLfJbzJSe');   -- legacy idCliente=108

-- ============================================================================
-- 3. USUARIO_ROL  (asignación de roles)
-- ============================================================================

INSERT INTO `usuario_rol` (`usuario_id`, `rol_id`) VALUES
  -- Nutricionistas → ROLE_NUTRICIONISTA (id=2)
  (1, 2), (2, 2), (3, 2), (4, 2),
  -- Farmacias → ROLE_FARMACIA (id=3)
  (5, 3), (6, 3), (7, 3), (8, 3), (9, 3), (10, 3),
  (11, 3), (12, 3), (13, 3), (14, 3), (15, 3), (16, 3),
  (17, 3), (18, 3), (19, 3), (20, 3), (21, 3), (22, 3),
  (23, 3), (24, 3), (25, 3), (26, 3), (27, 3), (28, 3),
  (29, 3), (30, 3), (31, 3), (32, 3), (33, 3), (34, 3),
  (35, 3), (36, 3), (37, 3), (38, 3);

-- ============================================================================
-- 4. NUTRICIONISTAS
-- ============================================================================
-- Fuente: legacy tabla `nutricionistas` (idNutricionista 41, 10, 36, 37).
-- Mapeo:
--   legacy.nombre        → nuevo.nombre + apellidos (split manual)
--   legacy.horas         → nuevo.horas_contrato_mensual
--   legacy.telefonoPersonal → nuevo.telefono
--   legacy.cancelado     → nuevo.activo (invertido: cancelado=0 → activo=1)

INSERT INTO `nutricionistas` (`id`, `activo`, `apellidos`, `borrado_por`, `fecha_baja`, `horas_contrato_mensual`, `nombre`, `telefono`, `usuario_id`) VALUES
  (1, 1, 'Sanchez',           NULL, NULL, 30, 'Cristina',  '000000000', 1),   -- legacy idNutricionista=41, horas=30
  (2, 1, 'Maldonado Garcia',  NULL, NULL, 36, 'Paco',      '685935858', 2),   -- legacy idNutricionista=10, horas=36, tel del legacy comerciales
  (3, 1, 'Del Moral',         NULL, NULL, 10, 'Ana',       '000000000', 3),   -- legacy idNutricionista=36, horas=10
  (4, 1, '',                  NULL, NULL, 12, 'Oumaima',   '000000000', 4);   -- legacy idNutricionista=37, horas=12

-- ============================================================================
-- 5. FARMACIAS
-- ============================================================================
-- Fuente: legacy tabla `clientes` + `direcciones`.
-- Mapeo:
--   clientes.nombre      → farmacias.nombre (Title Case)
--   clientes.cifnif       → farmacias.cif
--   direcciones.direccion+poblacion+cp → farmacias.direccion (concatenado)
--   clientes.liquidacion  → farmacias.saldo_virtual
--   clientes.telefono1    → farmacias.telefono
-- Nota: `es_provincia_local` se infiere: Almería=true, resto=false.
-- Nota: `porcentaje_comision` se pone a 0 por defecto (no hay campo equivalente claro en legacy).
-- Nota: Algunos CIF del legacy son claramente erróneos (ej: 'PASEO ESPAÑA 36').
--       Se mantienen tal cual para no perder datos; corregir manualmente.

INSERT INTO `farmacias` (`id`, `activo`, `borrado_por`, `cif`, `direccion`, `es_provincia_local`, `fecha_baja`, `nombre`, `porcentaje_comision`, `saldo_virtual`, `usuario_id`, `telefono`) VALUES
  -- === Farmacias de CRISTINA SANCHEZ (legacy nutricionista id=41) ===
  -- legacy idCliente=36, nombre='CENTRO MEDICO RETAMARINA', dir=Paseo del Toyo 138, Retamar 04131
  (1,  1, NULL, 'SIN-CIF-36',   'Paseo Del Toyo 138, Retamar, 04131',                         1, NULL, 'Centro Medico Retamarina',             0, 0.00,   5,  NULL),
  -- legacy idCliente=65, nombre='CHAFARINAS (CAÑADA)', dir vacía
  (2,  1, NULL, 'SIN-CIF-65',   NULL,                                                          1, NULL, 'Chafarinas (Cañada)',                   0, 0.00,   6,  NULL),
  -- legacy idCliente=67, nombre='SANUR EMERGENCIAS, S. L.', cif='B-04681805'
  (3,  1, NULL, 'B-04681805',   'Avd. Cabo De Gata 36, Almería, 04007',                        1, NULL, 'Sanur Emergencias S.L.',                0, 0.00,   7,  NULL),
  -- legacy idCliente=73, nombre='FARMACIA CAPARROS Y REINA', cif='E04627030'
  (4,  1, NULL, 'E04627030',    'Avd. Daza 122, Santa María Del Águila, 04710',                 0, NULL, 'Farmacia Caparros Y Reina',             0, 392.05,  8,  '950580501'),
  -- legacy idCliente=75, nombre='FARMACIA BULEVARD', dir=Avd. Bulevard 195, El Ejido
  (5,  1, NULL, 'SIN-CIF-75',   'Avd. Bulevard 195, El Ejido, 04700',                          0, NULL, 'Farmacia Bulevard',                     0, 0.00,   9,  NULL),
  -- legacy idCliente=77, nombre='FARMACIA EL PARQUE', cif='27250985X'
  (6,  1, NULL, '27250985X',    'Calle El Parque 18, Níjar, 04100',                             0, NULL, 'Farmacia El Parque',                    0, 0.00,  10,  '616862701'),
  -- legacy idCliente=80, nombre='CARLOS FERNÁNDEZ FUENTES', cif='75711988J'
  (7,  1, NULL, '75711988J',    'C/ Pilarica 9, Almeria',                                       1, NULL, 'Carlos Fernández Fuentes',              0, 0.00,  11,  '625851403'),
  -- legacy idCliente=82, nombre='MARIA MULLOR SORIANO', cif='27224480R'
  (8,  1, NULL, '27224480R',    'Carretera Del Mamí 81',                                        1, NULL, 'Maria Mullor Soriano',                  0, 0.00,  12,  '950226385'),
  -- legacy idCliente=88, nombre='FARMACIA VALVERDE', cif='75266436V'
  (9,  1, NULL, '75266436V',    'C/ Profesor Tierno Galván 21, Huércal De Almería, 04230',      1, NULL, 'Farmacia Valverde',                     0, 0.00,  13,  '950303183'),
  -- legacy idCliente=89, nombre='FARMACIA GARCÍA BALCÁZAR', cif='E04624813'
  (10, 1, NULL, 'E04624813',    'C/ Sierra De Gredos 29, Almería, 04009',                       1, NULL, 'Farmacia García Balcázar',              0, 0.00,  14,  '950315052'),
  -- legacy idCliente=91, nombre='FARMACIA GÁDOR', cif='53711409S'
  (11, 1, NULL, '53711409S',    'C/ La Paz 2, Gádor, 04560',                                    0, NULL, 'Farmacia Gádor',                        0, 0.00,  15,  NULL),
  -- legacy idCliente=93, nombre='FARMACIA VIATOR', cif='45590917-H'
  (12, 1, NULL, '45590917H',    'C/ Plaza Constitucion 3, Viator',                               1, NULL, 'Farmacia Viator',                       0, 0.00,  16,  NULL),
  -- legacy idCliente=102, nombre='FARMACIA LOS LLANOS', dir=C/ Potasio 7, Loma Cabrera 04120
  (13, 1, NULL, 'SIN-CIF-102',  'C/ Potasio 7, Loma Cabrera, 04120',                            1, NULL, 'Farmacia Los Llanos',                   0, 0.00,  17,  NULL),
  -- legacy idCliente=110, nombre='FARMACIA SILVIA SOLER (CRISTINA)', dir=Puebla de Vicar
  (14, 1, NULL, 'SIN-CIF-110',  'Puebla De Vicar',                                              0, NULL, 'Farmacia Silvia Soler (Cristina)',       0, -78.86, 18,  NULL),
  -- legacy idCliente=111, nombre='FARMACIA GARCIA RODRIGUEZ', cif='E04564050'
  (15, 1, NULL, 'E04564050',    'Carretera Nijar 204, La Cañada, Almería, 04120',               1, NULL, 'Farmacia Garcia Rodriguez',              0, 0.00,  19,  '950291781'),
  -- legacy idCliente=112, nombre='CONSULTAS ONLINE ALMERÍA', cp=04001
  (16, 1, NULL, 'SIN-CIF-112',  'Almería, 04001',                                               1, NULL, 'Consultas Online Almería',               0, 0.00,  20,  NULL),

  -- === Farmacias de PACO MALDONADO GARCIA (legacy nutricionista id=10) ===
  -- legacy idCliente=16, nombre='FRANCISCA LUNA BOTIAS', cif='PASEO ESPAÑA 36' (dato erróneo, es dirección)
  (17, 1, NULL, 'SIN-CIF-16',   'Paseo España 36, Jaen, 23009',                                 0, NULL, 'Francisca Luna Botias',                 0, 0.00,  21,  '953256254'),
  -- legacy idCliente=57, nombre='CONSULTA ALMERÍA', dir=Paseo de Almería 45, Almería
  (18, 1, NULL, 'SIN-CIF-57',   'Paseo De Almería 45, Almería, 04001',                          1, NULL, 'Consulta Almería',                      0, 0.00,  22,  NULL),
  -- legacy idCliente=71, nombre='FARMACIA BOLA AZUL', cif='27507622J'
  (19, 1, NULL, '27507622J',    'Carretera Ronda 325, Almería, 04009',                           1, NULL, 'Farmacia Bola Azul',                    0, 0.00,  23,  NULL),
  -- legacy idCliente=98, nombre='FARMACIA CASTELAR', cif='26471744D'
  (20, 1, NULL, '26471744D',    'Pasaje De La Iglesia 1, Castelar, 03690',                       0, NULL, 'Farmacia Castelar',                     0, 0.00,  24,  NULL),
  -- legacy idCliente=99, nombre='MARÍA DE OÑA (VEGA DE ACA)', cif='75260040S'
  (21, 1, NULL, '75260040S',    'C/ Casares S/N, 04007',                                         1, NULL, 'María De Oña (Vega De Aca)',             0, 0.00,  25,  NULL),

  -- === Farmacias de ANA DEL MORAL (legacy nutricionista id=36) ===
  -- legacy idCliente=5, nombre='MARIA Y MARIA DEL CARMEN DEL POZO', cif='E23673510'
  (22, 1, NULL, 'E23673510',    'Avenida De Andalucia 8, Linares, 23700',                        0, NULL, 'Maria Y Maria Del Carmen Del Pozo',     0, 0.00,  26,  NULL),
  -- legacy idCliente=9, nombre='CARMEN VICTORIA CARO EISMAN', cif='26221831Z'
  (23, 1, NULL, '26221831Z',    'C/ Marques De Linares Nº 44, Linares, 23700',                   0, NULL, 'Carmen Victoria Caro Eisman',           0, 0.00,  27,  NULL),
  -- legacy idCliente=60, nombre='Mª AMPARO MONTORO MASA Y MANUEL TALLÓN RUÍZ C.B.', cif='E23700990'
  (24, 1, NULL, 'E23700990',    'Corredera San Fernando 26, Úbeda, 23400',                       0, NULL, 'Mª Amparo Montoro Masa Y Manuel Tallón Ruíz C.B.', 0, 0.00, 28, '953750332'),
  -- legacy idCliente=94, nombre='ISABEL VILAR ZAMORA [SABIOTE]', cif='26469373F'
  (25, 1, NULL, '26469373F',    'C/ San Miguel N 3, Sabiote, 23410',                             0, NULL, 'Isabel Vilar Zamora (Sabiote)',          0, 0.00,  29,  NULL),
  -- legacy idCliente=96, nombre='FARMACIA CALLE HUELVA 5', cif='26459071D'
  (26, 1, NULL, '26459071D',    'C/ Huelva 5, Ubeda, 23400',                                     0, NULL, 'Farmacia Calle Huelva 5',               0, 0.00,  30,  NULL),
  -- legacy idCliente=101, nombre='ELENA COBO - RUS', sin CIF
  (27, 1, NULL, 'SIN-CIF-101',  'C/ Huelva 2, Rus, 23430',                                      0, NULL, 'Elena Cobo - Rus',                      0, 0.00,  31,  NULL),
  -- legacy idCliente=104, nombre='NUTRIPHARMA ANA', dir='Jaen'
  (28, 1, NULL, 'SIN-CIF-104',  'Jaen',                                                          0, NULL, 'Nutripharma Ana',                       0, 0.00,  32,  NULL),

  -- === Farmacias de OUMAIMA (legacy nutricionista id=37) ===
  -- legacy idCliente=3, nombre='INES GOMEZ SERRANO', cif='29777739F'
  (29, 1, NULL, '29777739F',    'Diego Moron 10, Huelva, 21005',                                  0, NULL, 'Ines Gomez Serrano',                    0, 0.00,  33,  NULL),
  -- legacy idCliente=4, nombre='JAIME SANCHEZ PEREZ', cif='29796587H'
  (30, 1, NULL, '29796587H',    'C/ Antonio Delgado 14, Huelva, 21007',                           0, NULL, 'Jaime Sanchez Perez',                   0, 0.00,  34,  NULL),
  -- legacy idCliente=10, nombre='GONZALEZ SAUCI CB', sin CIF
  (31, 1, NULL, 'SIN-CIF-10',   'C/ Huelva 11, San Juan Del Puerto, 21610',                       0, NULL, 'Gonzalez Sauci Cb',                     0, 0.00,  35,  NULL),
  -- legacy idCliente=49, nombre='ANTONIO GIMENEZ BOCETA', cif='28554723Q'
  (32, 1, NULL, '28554723Q',    'Plaza España 4, Huelva, 21003',                                   0, NULL, 'Antonio Gimenez Boceta',                0, 0.00,  36,  '959251499'),
  -- legacy idCliente=100, nombre='FARMACIA BUENDIA', cif='29757100F'
  (33, 1, NULL, '29757100F',    'Avd. Adoratrices 62, Huelva, 21004',                              0, NULL, 'Farmacia Buendia',                      0, 0.00,  37,  NULL),
  -- legacy idCliente=108, nombre='NUTRIPHARMA OUMAIMA', sin CIF
  (34, 1, NULL, 'SIN-CIF-108',  'Huelva',                                                          0, NULL, 'Nutripharma Oumaima',                   0, 0.00,  38,  NULL);

-- ============================================================================
-- 6. NUTRICIONISTA_FARMACIA  (tabla de relación N:M)
-- ============================================================================
-- Fuente: legacy clientes.idNutricionista → mapeo a nuevos IDs.
-- kilometros: extraído de legacy clientes.kilometros (varchar, convertido a int).

INSERT INTO `nutricionista_farmacia` (`id`, `kilometros`, `farmacia_id`, `nutricionista_id`) VALUES
  -- Cristina Sanchez (nutri id=1) → farmacias 1-16
  (1,  18, 1,  1),   -- Centro Medico Retamarina
  (2,   9, 2,  1),   -- Chafarinas (Cañada)
  (3,   6, 3,  1),   -- Sanur Emergencias S.L.
  (4,  21, 4,  1),   -- Farmacia Caparros Y Reina
  (5,  25, 5,  1),   -- Farmacia Bulevard
  (6,  32, 6,  1),   -- Farmacia El Parque
  (7,   0, 7,  1),   -- Carlos Fernández Fuentes (km vacío → 0)
  (8,   5, 8,  1),   -- Maria Mullor Soriano
  (9,  22, 9,  1),   -- Farmacia Valverde
  (10, 14, 10, 1),   -- Farmacia García Balcázar
  (11, 12, 11, 1),   -- Farmacia Gádor
  (12,  4, 12, 1),   -- Farmacia Viator
  (13, 10, 13, 1),   -- Farmacia Los Llanos
  (14, 28, 14, 1),   -- Farmacia Silvia Soler (Cristina)
  (15,  0, 15, 1),   -- Farmacia Garcia Rodriguez (km vacío → 0)
  (16,  0, 16, 1),   -- Consultas Online Almería (km vacío → 0)

  -- Paco Maldonado Garcia (nutri id=2) → farmacias 17-21
  (17,  0, 17, 2),   -- Francisca Luna Botias
  (18,  0, 18, 2),   -- Consulta Almería
  (19, 13, 19, 2),   -- Farmacia Bola Azul
  (20,  0, 20, 2),   -- Farmacia Castelar
  (21,  0, 21, 2),   -- María De Oña (Vega De Aca)

  -- Ana del Moral (nutri id=3) → farmacias 22-28
  (22, 33, 22, 3),   -- Maria Y Maria Del Carmen Del Pozo
  (23, 33, 23, 3),   -- Carmen Victoria Caro Eisman
  (24,  0, 24, 3),   -- Mª Amparo Montoro Masa Y Manuel Tallón Ruíz C.B.
  (25, 12, 25, 3),   -- Isabel Vilar Zamora (Sabiote)
  (26,  0, 26, 3),   -- Farmacia Calle Huelva 5
  (27, 12, 27, 3),   -- Elena Cobo - Rus
  (28,  0, 28, 3),   -- Nutripharma Ana

  -- Oumaima (nutri id=4) → farmacias 29-34
  (29, 44, 29, 4),   -- Ines Gomez Serrano
  (30, 47, 30, 4),   -- Jaime Sanchez Perez
  (31, 47, 31, 4),   -- Gonzalez Sauci Cb
  (32, 50, 32, 4),   -- Antonio Gimenez Boceta
  (33, 46, 33, 4),   -- Farmacia Buendia
  (34,  0, 34, 4);   -- Nutripharma Oumaima

-- ============================================================================
-- 7. PRODUCTOS
-- ============================================================================
-- Fuente: legacy tabla `productos`.
-- Mapeo:
--   productos.producto      → productos.nombre_producto (Title Case, limpiando prefijos numéricos)
--   productos.referencia    → productos.referencia
--   productos.acronimo      → productos.acronimo ('gra'→'gra', 'peq'→'peq', ''→'gra')
--   productos.acronimo      → productos.categoria ('gra'→'GRANDE', 'peq'→'PEQUENO')
--   productos.precio        → productos.pvf
--   productos.precioPVP     → productos.pvp
--   productos.idIva         → productos.iva (1→21.00, 2→8.00, 3→4.00, 4→0.00)
--   activo: productos marcados (ND) se ponen activo=0, resto activo=1.
--   hay_existencias: productos con '(SIN STOCK)' → 0, resto → 1.
-- Nota: referencias duplicadas ('00220', '00106') — se desambiguan con sufijo.

INSERT INTO `productos` (`id`, `activo`, `acronimo`, `borrado_por`, `categoria`, `fecha_baja`, `hay_existencias`, `iva`, `nombre_producto`, `pvf`, `pvp`, `referencia`) VALUES
  -- Productos activos (sin marca ND) — catálogo vigente
  (1,  1, 'gra', NULL, 'GRANDE',  NULL, 1, 8.00, 'Reduabdo Plus Forte (60 Caps)',              14.93, 23.75, '00104'),
  (3,  1, 'gra', NULL, 'GRANDE',  NULL, 1, 8.00, 'Ob-Fat 30 (60 Caps)',                        15.71, 25.00, '00106'),
  (17, 1, 'peq', NULL, 'PEQUENO', NULL, 1, 8.00, 'Circuvein (30 Caps)',                         8.80, 14.00, '00220'),
  (18, 1, 'peq', NULL, 'PEQUENO', NULL, 1, 8.00, 'Ob-Fat 30 (30 Caps)',                         9.43, 15.00, '00206'),
  (19, 1, 'peq', NULL, 'PEQUENO', NULL, 1, 8.00, 'Flaci Grass (30 Caps)',                       8.49, 13.50, '00212'),
  (20, 1, 'peq', NULL, 'PEQUENO', NULL, 1, 8.00, 'Reduabdo Plus Forte (30 Caps)',               8.77, 13.95, '00214'),
  (21, 1, 'peq', NULL, 'PEQUENO', NULL, 1, 8.00, 'Infusion Ob8 (20 Sobres)',                    2.67,  4.25, '00106B'),
  (22, 1, 'peq', NULL, 'PEQUENO', NULL, 1, 8.00, 'Melatodream (30 Caps)',                       8.80, 14.00, '00221'),
  (23, 1, 'peq', NULL, 'PEQUENO', NULL, 1, 8.00, 'Laxaloe Plus (30 Caps)',                      7.54, 12.00, '00092'),
  (24, 1, 'peq', NULL, 'PEQUENO', NULL, 1, 8.00, 'Retenliquid Plus (250 Ml)',                   6.92, 11.00, '00226'),
  (25, 1, 'peq', NULL, 'PEQUENO', NULL, 1, 8.00, 'Hepatodren Triple Acción (250 Ml)',           7.86, 12.50, '00225'),
  (26, 1, 'peq', NULL, 'PEQUENO', NULL, 1, 8.00, 'Calory Block (30 Caps)',                      7.86, 12.50, '00210'),
  (28, 1, 'gra', NULL, 'GRANDE',  NULL, 1, 8.00, 'Mealnight Sustitutive (Chocolate)',          18.83, 29.95, '00228'),
  (29, 1, 'gra', NULL, 'GRANDE',  NULL, 1, 8.00, 'Mealnight Sustitutive (Fresa-Nata)',         18.83, 29.95, '00227'),
  (30, 1, 'gra', NULL, 'GRANDE',  NULL, 1, 8.00, 'Mealnight Sustitutive (Vainilla-Canela)',    18.83, 29.95, '00229'),
  (31, 1, 'gra', NULL, 'GRANDE',  NULL, 1, 8.00, 'Colagen Pharma (330g - Frutos Rojos)',       12.25, 19.50, '00230'),

  -- Productos descatalogados / sin stock
  (2,  0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Celulit Control Advance 60 Caps',           10.31, 15.76, 'ND-002'),
  (4,  0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Flaci Grass (60 Caps) - Sin Stock',         15.08, 24.00, '00102'),
  (5,  0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Calory Block (60 Caps)',                     12.57, 20.00, '00101'),
  (6,  0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Anxiestop (60 Caps)',                        12.42, 19.75, '00103'),
  (7,  0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Retenliquid Plus (500 Ml)',                  11.63, 18.50, '00059'),
  (8,  0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Hepatodren Triple Accion (500 Ml)',          11.78, 18.75, '00091'),
  (9,  0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Vitality Complex (60 Caps)',                  8.11, 12.90, '00081'),
  (10, 0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Tabacoff Forte 60 Caps',                    20.90, 29.44, 'ND-010'),
  (11, 0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Osseolife (90 Caps)',                         9.33, 14.85, '00079'),
  (12, 0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Laxaloe Plus 60 Caps',                       8.74, 12.31, 'ND-012'),
  (13, 0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Corsano 90 Perlas',                         10.45, 14.72, 'ND-013'),
  (14, 0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Meal Night 1 Kg (Choc)',                    24.44, 33.95, 'ND-014'),
  (15, 0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Colagen Pharma Plus 400 Gr Limon',          14.76, 19.94, 'ND-015'),
  (16, 0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Reducol Max (30 Caps)',                       8.14, 12.95, '00220B'),
  (27, 0, 'peq', NULL, 'PEQUENO', NULL, 0, 8.00, 'Anxiestop (30 Caps)',                         7.86, 12.50, '00213'),
  (32, 0, 'peq', NULL, 'PEQUENO', NULL, 0, 8.00, 'Celulit Control Advance (30 Caps) - Sin Stock', 8.49, 13.50, '00224'),
  (33, 0, 'gra', NULL, 'GRANDE',  NULL, 1, 0.00, 'Z.Extra',                                     1.00,  1.00, 'ZEXTRA'),
  (34, 0, 'gra', NULL, 'GRANDE',  NULL, 0, 8.00, 'Anxiestop (60 Caps) Duplicado',             12.42, 19.75, '10103'),
  (35, 0, 'peq', NULL, 'PEQUENO', NULL, 0, 8.00, 'Psicoactive (30 Caps)',                       7.86, 12.50, '10213');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- FIN DE LA MIGRACIÓN
-- ============================================================================
-- NOTAS IMPORTANTES:
--   • Todos los usuarios comparten la contraseña temporal 'Nutripharma2026!'
--     (hash BCrypt real, coste 10). Forzar cambio en el primer inicio de sesión.
--   • Los CIF marcados como 'SIN-CIF-XX' necesitan ser completados manualmente.
--     El CIF del legacy idCliente=16 ('PASEO ESPAÑA 36') era claramente una
--     dirección copiada por error.
--   • porcentaje_comision se ha dejado a 0 para todas las farmacias porque el
--     legacy no tiene un campo equivalente directo.
--   • Los productos marcados (ND) se han insertado con activo=0 para preservar
--     históricos, pero no aparecerán en el catálogo activo.
--   • Las referencias duplicadas en el legacy ('00220' para ids 16 y 17, '00106'
--     para ids 3 y 21) se han desambiguado con sufijo 'B' (ej: '00220B', '00106B').
