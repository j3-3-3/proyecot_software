-- ============================================================
-- SEED DATA for Sistema de Gestión Logística
-- BCrypt hash for "password123":
-- $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjZAzkv8c.
-- ============================================================

-- Usuarios
INSERT INTO usuario (nombre, apellidos, email, password, rol, activo)
VALUES
  ('Admin', 'Sistema', 'admin@logisys.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjZAzkv8c.', 'ADMIN', true),
  ('Carlos', 'García López', 'operario@logisys.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjZAzkv8c.', 'OPERARIO', true),
  ('María', 'Martínez Ruiz', 'repartidor@logisys.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjZAzkv8c.', 'REPARTIDOR', true),
  ('Juan', 'Pérez Sánchez', 'cliente@logisys.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjZAzkv8c.', 'CLIENTE', true),
  ('Ana', 'González Díaz', 'ana.cliente@logisys.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjZAzkv8c.', 'CLIENTE', true),
  ('Pedro', 'López Fernández', 'pedro.repartidor@logisys.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LjZAzkv8c.', 'REPARTIDOR', true)
ON CONFLICT (email) DO NOTHING;

-- Vehiculos
INSERT INTO vehiculo (matricula, marca, modelo, tipo, disponible, activo)
VALUES
  ('1234-ABC', 'Mercedes', 'Sprinter', 'FURGONETA', true, true),
  ('5678-DEF', 'Ford', 'Transit', 'FURGONETA', true, true),
  ('9012-GHI', 'Renault', 'Master', 'CAMION', false, true),
  ('3456-JKL', 'Volkswagen', 'Crafter', 'FURGONETA', true, true)
ON CONFLICT (matricula) DO NOTHING;

-- Productos
INSERT INTO producto (nombre, descripcion, categoria, cantidad_stock, ubicacion, precio_unitario, activo)
VALUES
  ('Palet de Madera Estándar', 'Palet de madera 1200x800mm', 'MATERIALES', 150, 'Almacén A - Estantería 1', 12.50, true),
  ('Caja de Cartón Grande', 'Caja resistente 60x40x40cm', 'EMBALAJE', 500, 'Almacén A - Estantería 2', 2.80, true),
  ('Film Estirable', 'Rollo de film para paletizar 500m', 'EMBALAJE', 80, 'Almacén B - Estantería 1', 15.00, true),
  ('Cinta de Embalaje', 'Pack 6 rollos cinta marrón 50m', 'EMBALAJE', 200, 'Almacén B - Estantería 2', 8.40, true),
  ('Esquinero de Cartón', 'Protector ángulo 50x50x200cm', 'PROTECCIÓN', 300, 'Almacén A - Estantería 3', 0.95, true),
  ('Espuma Protectora 2cm', 'Plancha espuma PE 200x100cm', 'PROTECCIÓN', 120, 'Almacén C - Estantería 1', 22.00, true),
  ('Caja Isotérmica', 'Caja con aislamiento térmico 40L', 'ESPECIAL', 40, 'Almacén C - Estantería 2', 35.00, true),
  ('Precinto de Seguridad', 'Pack 100 precintos numerados', 'SEGURIDAD', 60, 'Almacén B - Estantería 3', 18.50, true)
ON CONFLICT DO NOTHING;
