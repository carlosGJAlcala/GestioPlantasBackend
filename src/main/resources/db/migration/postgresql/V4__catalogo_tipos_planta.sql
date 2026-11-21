-- Catálogo inicial de tipos de planta para que una BD nueva sea usable desde el primer día.
-- Humedad = % de humedad del sustrato tal como la calcula el ESP32 (4095 seco → 0 %, 800 saturado → 100 %),
-- luz en lux y temperatura en °C. Son rangos orientativos: ajustarlos desde la web tras unas semanas de datos
-- (la métrica «riegosManuales» de /riego/maceta/{id}/resumen indica si el mínimo de humedad se queda corto).
INSERT INTO tipoplanta (nombreplanta, descripcion,
                        nivelhumedadnecesariominimo, nivelhumedadnecesariomaximo,
                        nivelluxnecesariominimo, nivelluxnecesariomaximo,
                        niveltemperaturanecesariominimo, niveltemperaturanecesariomaximo,
                        estaciones, zonaorigen) VALUES
('Tomate',     'Hortaliza de fruto. Riego regular y profundo; la irregularidad provoca rajado y podredumbre apical.',
               60, 80, 15000, 60000, 15, 30, 'Primavera, Verano', 'Sudamérica'),
('Pimiento',   'Hortaliza de fruto exigente en calor. No tolera encharcamiento.',
               55, 75, 15000, 50000, 18, 30, 'Primavera, Verano', 'América Central'),
('Lechuga',    'Hoja de ciclo corto. Con calor y sequía se espiga y amarga.',
               60, 80, 10000, 30000, 7, 24, 'Otoño, Invierno, Primavera', 'Mediterráneo'),
('Fresa',      'Raíz superficial: el sustrato no debe secarse del todo, pero el fruto se pudre con exceso de agua.',
               60, 80, 15000, 40000, 10, 26, 'Primavera, Verano', 'Europa / América'),
('Albahaca',   'Aromática anual. Sensible al frío y al encharcamiento.',
               50, 70, 10000, 40000, 18, 30, 'Primavera, Verano', 'India'),
('Perejil',    'Aromática bienal de semisombra.',
               50, 75, 8000, 30000, 10, 25, 'Todo el año', 'Mediterráneo'),
('Hierbabuena','Aromática vigorosa, agradece humedad constante y semisombra.',
               60, 85, 5000, 25000, 12, 28, 'Primavera, Verano, Otoño', 'Europa'),
('Romero',     'Aromática mediterránea leñosa. Riego escaso: dejar secar entre riegos.',
               25, 45, 15000, 60000, 5, 32, 'Todo el año', 'Mediterráneo'),
('Suculenta',  'Cactus y crasas. Riego muy espaciado; el exceso de agua es la primera causa de muerte.',
               10, 30, 20000, 60000, 10, 35, 'Primavera, Verano', 'Zonas áridas'),
('Potus',      'Planta de interior de luz indirecta. Regar cuando se seque la capa superior.',
               40, 60, 1000, 10000, 15, 29, 'Todo el año', 'Sudeste asiático');
