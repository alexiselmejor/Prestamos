-- =====================================================================
--  Sistema de Préstamo de Equipos - Base de datos SQL Server
--  Compatible con SQL Server 2019+ (probado con 2022/2025).
--  Se puede ejecutar varias veces: solo crea lo que no existe.
--
--  Con sqlcmd:
--    sqlcmd -S localhost,1433 -E -C -i database\prestamos_equipos.sql
--  (la aplicación también lo ejecuta sola al arrancar si faltan las tablas)
-- =====================================================================

IF DB_ID(N'prestamos_equipos') IS NULL
    CREATE DATABASE prestamos_equipos;
GO

USE prestamos_equipos;
GO

IF OBJECT_ID(N'dbo.usuarios', N'U') IS NULL
CREATE TABLE dbo.usuarios (
  id              INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_usuarios PRIMARY KEY,
  nombre          NVARCHAR(100) NOT NULL,
  dni             VARCHAR(8)    NULL,
  celular         VARCHAR(9)    NULL,
  email           NVARCHAR(100) NOT NULL CONSTRAINT ux_usuarios_email UNIQUE,
  password_hash   VARCHAR(255)  NOT NULL,
  rol             VARCHAR(20)   NOT NULL CONSTRAINT df_usuarios_rol DEFAULT 'usuario',
  estado          TINYINT       NOT NULL CONSTRAINT df_usuarios_estado DEFAULT 1,
  fecha_registro  DATETIME2(0)  NULL CONSTRAINT df_usuarios_fecha DEFAULT SYSDATETIME(),
  CONSTRAINT ck_usuarios_rol CHECK (rol IN ('admin','encargado','usuario'))
);
GO
-- El DNI es opcional: el índice único solo cuenta los que tienen valor
-- (en SQL Server un UNIQUE normal admite un único NULL).
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ux_usuarios_dni')
    CREATE UNIQUE INDEX ux_usuarios_dni ON dbo.usuarios(dni) WHERE dni IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_usuarios_rol')
    CREATE INDEX idx_usuarios_rol ON dbo.usuarios(rol);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_usuarios_celular')
    CREATE INDEX idx_usuarios_celular ON dbo.usuarios(celular);
GO

IF OBJECT_ID(N'dbo.categorias', N'U') IS NULL
CREATE TABLE dbo.categorias (
  id              INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_categorias PRIMARY KEY,
  nombre          NVARCHAR(100) NOT NULL,
  descripcion     NVARCHAR(MAX) NULL,
  fecha_creacion  DATETIME2(0)  NULL CONSTRAINT df_categorias_fecha DEFAULT SYSDATETIME()
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_categorias_nombre')
    CREATE INDEX idx_categorias_nombre ON dbo.categorias(nombre);
GO

IF OBJECT_ID(N'dbo.equipos', N'U') IS NULL
CREATE TABLE dbo.equipos (
  id                INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_equipos PRIMARY KEY,
  codigo_inventario NVARCHAR(50)  NOT NULL CONSTRAINT ux_equipos_codigo UNIQUE,
  nombre            NVARCHAR(100) NOT NULL,
  descripcion       NVARCHAR(MAX) NULL,
  categoria_id      INT           NULL,
  estado            VARCHAR(20)   NOT NULL CONSTRAINT df_equipos_estado DEFAULT 'disponible',
  ubicacion         NVARCHAR(100) NULL,
  fecha_registro    DATETIME2(0)  NULL CONSTRAINT df_equipos_fecha DEFAULT SYSDATETIME(),
  CONSTRAINT ck_equipos_estado CHECK (estado IN ('disponible','prestado','mantenimiento','dado_de_baja')),
  CONSTRAINT fk_equipos_categoria FOREIGN KEY (categoria_id) REFERENCES dbo.categorias(id) ON DELETE SET NULL
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_equipos_estado')
    CREATE INDEX idx_equipos_estado ON dbo.equipos(estado);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_equipos_categoria')
    CREATE INDEX idx_equipos_categoria ON dbo.equipos(categoria_id);
GO

IF OBJECT_ID(N'dbo.prestamos', N'U') IS NULL
CREATE TABLE dbo.prestamos (
  id                        INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_prestamos PRIMARY KEY,
  usuario_id                INT           NOT NULL,
  equipo_id                 INT           NOT NULL,
  fecha_prestamo            DATETIME2(0)  NOT NULL,
  fecha_devolucion_esperada DATETIME2(0)  NOT NULL,
  fecha_devolucion_real     DATETIME2(0)  NULL,
  estado                    VARCHAR(20)   NOT NULL CONSTRAINT df_prestamos_estado DEFAULT 'activo',
  observaciones             NVARCHAR(MAX) NULL,
  CONSTRAINT ck_prestamos_estado CHECK (estado IN ('activo','devuelto','vencido')),
  CONSTRAINT fk_prestamos_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id),
  CONSTRAINT fk_prestamos_equipo  FOREIGN KEY (equipo_id)  REFERENCES dbo.equipos(id)
);
GO
-- Un equipo no puede tener dos préstamos activos a la vez
-- (reemplaza a la columna generada `equipo_activo` de MySQL).
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ux_prestamos_equipo_activo')
    CREATE UNIQUE INDEX ux_prestamos_equipo_activo ON dbo.prestamos(equipo_id) WHERE estado = 'activo';
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_prestamos_usuario')
    CREATE INDEX idx_prestamos_usuario ON dbo.prestamos(usuario_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_prestamos_equipo')
    CREATE INDEX idx_prestamos_equipo ON dbo.prestamos(equipo_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_prestamos_estado')
    CREATE INDEX idx_prestamos_estado ON dbo.prestamos(estado);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_prestamos_fechas')
    CREATE INDEX idx_prestamos_fechas ON dbo.prestamos(fecha_prestamo, fecha_devolucion_esperada);
GO

IF OBJECT_ID(N'dbo.reservas', N'U') IS NULL
CREATE TABLE dbo.reservas (
  id             INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_reservas PRIMARY KEY,
  usuario_id     INT          NOT NULL,
  equipo_id      INT          NOT NULL,
  fecha_reserva  DATETIME2(0) NOT NULL,
  fecha_inicio   DATETIME2(0) NOT NULL,
  fecha_fin      DATETIME2(0) NOT NULL,
  estado         VARCHAR(20)  NOT NULL CONSTRAINT df_reservas_estado DEFAULT 'pendiente',
  CONSTRAINT ck_reservas_estado CHECK (estado IN ('pendiente','confirmada','cancelada','completada')),
  CONSTRAINT fk_reservas_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id),
  CONSTRAINT fk_reservas_equipo  FOREIGN KEY (equipo_id)  REFERENCES dbo.equipos(id)
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_reservas_usuario')
    CREATE INDEX idx_reservas_usuario ON dbo.reservas(usuario_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_reservas_equipo')
    CREATE INDEX idx_reservas_equipo ON dbo.reservas(equipo_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_reservas_estado')
    CREATE INDEX idx_reservas_estado ON dbo.reservas(estado);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_reservas_fechas')
    CREATE INDEX idx_reservas_fechas ON dbo.reservas(fecha_inicio, fecha_fin);
GO

IF OBJECT_ID(N'dbo.historial_movimientos', N'U') IS NULL
CREATE TABLE dbo.historial_movimientos (
  id          INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_historial PRIMARY KEY,
  equipo_id   INT           NULL,
  usuario_id  INT           NULL,
  tipo        VARCHAR(20)   NOT NULL,
  fecha       DATETIME2(0)  NOT NULL CONSTRAINT df_historial_fecha DEFAULT SYSDATETIME(),
  detalle     NVARCHAR(MAX) NULL,
  CONSTRAINT ck_historial_tipo CHECK (tipo IN ('alta','edicion','prestamo','devolucion','mantenimiento','baja')),
  CONSTRAINT fk_historial_equipo  FOREIGN KEY (equipo_id)  REFERENCES dbo.equipos(id)  ON DELETE SET NULL,
  CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id) ON DELETE SET NULL
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_historial_equipo')
    CREATE INDEX idx_historial_equipo ON dbo.historial_movimientos(equipo_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_historial_tipo')
    CREATE INDEX idx_historial_tipo ON dbo.historial_movimientos(tipo);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_historial_fecha')
    CREATE INDEX idx_historial_fecha ON dbo.historial_movimientos(fecha);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_historial_usuario')
    CREATE INDEX idx_historial_usuario ON dbo.historial_movimientos(usuario_id);
GO

IF OBJECT_ID(N'dbo.login_intentos', N'U') IS NULL
CREATE TABLE dbo.login_intentos (
  id     INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_login_intentos PRIMARY KEY,
  email  NVARCHAR(100) NOT NULL,
  ip     VARCHAR(45)   NOT NULL,
  fecha  DATETIME2(0)  NOT NULL CONSTRAINT df_login_fecha DEFAULT SYSDATETIME()
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_login_intentos')
    CREATE INDEX idx_login_intentos ON dbo.login_intentos(ip, email, fecha);
GO

IF OBJECT_ID(N'dbo.solicitudes', N'U') IS NULL
CREATE TABLE dbo.solicitudes (
  id                INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_solicitudes PRIMARY KEY,
  usuario_id        INT           NOT NULL,
  equipo_solicitado NVARCHAR(255) NOT NULL,
  fecha_prestamo    DATETIME2(0)  NOT NULL,
  fecha_devolucion  DATETIME2(0)  NOT NULL,
  oficina           NVARCHAR(255) NOT NULL,
  estado            VARCHAR(50)   NOT NULL CONSTRAINT df_solicitudes_estado DEFAULT 'pendiente',
  observaciones     NVARCHAR(MAX) NULL,
  fecha_solicitud   DATETIME2(0)  NULL CONSTRAINT df_solicitudes_fecha DEFAULT SYSDATETIME(),
  CONSTRAINT fk_solicitudes_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id)
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_solicitudes_usuario')
    CREATE INDEX idx_solicitudes_usuario ON dbo.solicitudes(usuario_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_solicitudes_estado')
    CREATE INDEX idx_solicitudes_estado ON dbo.solicitudes(estado);
GO

-- Avisos de la campana que ya leyó cada usuario (las notificaciones se calculan al vuelo).
IF OBJECT_ID(N'dbo.notificaciones_leidas', N'U') IS NULL
CREATE TABLE dbo.notificaciones_leidas (
  usuario_id   INT           NOT NULL,
  clave        NVARCHAR(120) NOT NULL,
  fecha_leida  DATETIME2(0)  NOT NULL CONSTRAINT df_notificaciones_leidas_fecha DEFAULT SYSDATETIME(),
  CONSTRAINT pk_notificaciones_leidas PRIMARY KEY (usuario_id, clave),
  CONSTRAINT fk_notificaciones_leidas_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id) ON DELETE CASCADE
);
GO

-- ---------------------------- DATOS INICIALES ----------------------------
-- Solo se cargan si la tabla está vacía.

IF NOT EXISTS (SELECT 1 FROM dbo.usuarios)
BEGIN
  SET IDENTITY_INSERT dbo.usuarios ON;
  INSERT INTO dbo.usuarios (id, nombre, email, password_hash, rol, estado, fecha_registro, dni, celular) VALUES
  (1, N'Administrador', N'admin@universidad.edu', '$2y$10$sJes8wDlIKylzFB7CSWwfet7BrZAWlb5WMk0WTG9Bv9C0mgFoXqf6', 'admin', 1, '2026-08-19T22:20:13', NULL, NULL),
  (3, N'Franco Davila Samaniego', N'franco@universidad.edu', '$2y$10$Zske/c.Ge7E4R0UzPOZLR.SmzKFd4UwV63uXQlU67EV1eSi7UH8XW', 'usuario', 1, '2026-10-09T20:45:57', '75047364', '972360990'),
  (4, N'gaspar alexis', N'gaspar@universidad.edu', '$2y$10$MBQU2jkofsH9u1Y.tl7XieSTA3dzpmDUZwylCW9NQUx7xj3qJ/GHS', 'encargado', 1, '2026-10-09T21:12:35', '12345678', '906871803');
  SET IDENTITY_INSERT dbo.usuarios OFF;
  DBCC CHECKIDENT ('dbo.usuarios', RESEED, 4);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.categorias)
BEGIN
  SET IDENTITY_INSERT dbo.categorias ON;
  INSERT INTO dbo.categorias (id, nombre, descripcion, fecha_creacion) VALUES
  (1, N'Laptops',     N'Computadoras portátiles para préstamo', '2026-08-19T22:20:13'),
  (2, N'Proyectores', N'Proyectores multimedia',                '2026-08-19T22:20:13'),
  (3, N'Tablets',     N'Tabletas electrónicas',                 '2026-08-19T22:20:13'),
  (4, N'Cámaras',     N'Cámaras fotográficas y de video',       '2026-08-19T22:20:13'),
  (5, N'Accesorios',  N'Accesorios de cómputo',                 '2026-08-19T22:20:13');
  SET IDENTITY_INSERT dbo.categorias OFF;
  DBCC CHECKIDENT ('dbo.categorias', RESEED, 5);
END
GO

-- =====================================================================
--  Módulo de IA y mantenimiento preventivo (migración idempotente)
-- =====================================================================
IF OBJECT_ID(N'dbo.mantenimientos', N'U') IS NULL
CREATE TABLE dbo.mantenimientos (
  id INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_mantenimientos PRIMARY KEY,
  equipo_id INT NOT NULL,
  usuario_id INT NULL,
  tipo VARCHAR(20) NOT NULL CONSTRAINT df_mantenimientos_tipo DEFAULT 'preventivo',
  descripcion NVARCHAR(MAX) NOT NULL,
  fecha_programada DATETIME2(0) NULL,
  fecha_inicio DATETIME2(0) NULL,
  fecha_fin DATETIME2(0) NULL,
  estado VARCHAR(20) NOT NULL CONSTRAINT df_mantenimientos_estado DEFAULT 'programado',
  observaciones NVARCHAR(MAX) NULL,
  fecha_registro DATETIME2(0) NOT NULL CONSTRAINT df_mantenimientos_fecha DEFAULT SYSDATETIME(),
  CONSTRAINT ck_mantenimientos_tipo CHECK (tipo IN ('preventivo','correctivo')),
  CONSTRAINT ck_mantenimientos_estado CHECK (estado IN ('programado','en_proceso','completado','cancelado')),
  CONSTRAINT fk_mantenimientos_equipo FOREIGN KEY (equipo_id) REFERENCES dbo.equipos(id),
  CONSTRAINT fk_mantenimientos_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id) ON DELETE SET NULL
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_mantenimientos_equipo_fecha')
  CREATE INDEX ix_mantenimientos_equipo_fecha ON dbo.mantenimientos(equipo_id, fecha_registro DESC);
GO
IF OBJECT_ID(N'dbo.ia_conversaciones', N'U') IS NULL
CREATE TABLE dbo.ia_conversaciones (
  id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_ia_conversaciones PRIMARY KEY,
  usuario_id INT NOT NULL,
  titulo NVARCHAR(150) NOT NULL CONSTRAINT df_ia_conversaciones_titulo DEFAULT N'Consulta al asistente',
  fecha_creacion DATETIME2(0) NOT NULL CONSTRAINT df_ia_conversaciones_creacion DEFAULT SYSDATETIME(),
  fecha_actualizacion DATETIME2(0) NOT NULL CONSTRAINT df_ia_conversaciones_actualizacion DEFAULT SYSDATETIME(),
  CONSTRAINT fk_ia_conversaciones_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id) ON DELETE CASCADE
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_ia_conversaciones_usuario')
  CREATE INDEX ix_ia_conversaciones_usuario ON dbo.ia_conversaciones(usuario_id, fecha_actualizacion DESC);
GO
IF OBJECT_ID(N'dbo.ia_mensajes', N'U') IS NULL
CREATE TABLE dbo.ia_mensajes (
  id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_ia_mensajes PRIMARY KEY,
  conversacion_id BIGINT NOT NULL,
  rol VARCHAR(20) NOT NULL,
  contenido NVARCHAR(MAX) NOT NULL,
  fecha DATETIME2(0) NOT NULL CONSTRAINT df_ia_mensajes_fecha DEFAULT SYSDATETIME(),
  CONSTRAINT ck_ia_mensajes_rol CHECK (rol IN ('user','assistant')),
  CONSTRAINT fk_ia_mensajes_conversacion FOREIGN KEY (conversacion_id) REFERENCES dbo.ia_conversaciones(id) ON DELETE CASCADE
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_ia_mensajes_conversacion')
  CREATE INDEX ix_ia_mensajes_conversacion ON dbo.ia_mensajes(conversacion_id, id DESC);
GO
