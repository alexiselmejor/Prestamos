/*
  Migración del Asistente Inteligente OTI y mantenimiento preventivo.
  Ejecutar una sola vez (es idempotente) sobre la base prestamos_equipos.
  No elimina ni modifica registros existentes.
*/
USE [prestamos_equipos];
GO

IF OBJECT_ID(N'dbo.mantenimientos', N'U') IS NULL
BEGIN
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
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_mantenimientos_equipo_fecha' AND object_id=OBJECT_ID('dbo.mantenimientos'))
  CREATE INDEX ix_mantenimientos_equipo_fecha ON dbo.mantenimientos(equipo_id, fecha_registro DESC);
GO

IF OBJECT_ID(N'dbo.ia_conversaciones', N'U') IS NULL
BEGIN
  CREATE TABLE dbo.ia_conversaciones (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_ia_conversaciones PRIMARY KEY,
    usuario_id INT NOT NULL,
    titulo NVARCHAR(150) NOT NULL CONSTRAINT df_ia_conversaciones_titulo DEFAULT N'Consulta al asistente',
    fecha_creacion DATETIME2(0) NOT NULL CONSTRAINT df_ia_conversaciones_creacion DEFAULT SYSDATETIME(),
    fecha_actualizacion DATETIME2(0) NOT NULL CONSTRAINT df_ia_conversaciones_actualizacion DEFAULT SYSDATETIME(),
    CONSTRAINT fk_ia_conversaciones_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios(id) ON DELETE CASCADE
  );
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_ia_conversaciones_usuario' AND object_id=OBJECT_ID('dbo.ia_conversaciones'))
  CREATE INDEX ix_ia_conversaciones_usuario ON dbo.ia_conversaciones(usuario_id, fecha_actualizacion DESC);
GO

IF OBJECT_ID(N'dbo.ia_mensajes', N'U') IS NULL
BEGIN
  CREATE TABLE dbo.ia_mensajes (
    id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_ia_mensajes PRIMARY KEY,
    conversacion_id BIGINT NOT NULL,
    rol VARCHAR(20) NOT NULL,
    contenido NVARCHAR(MAX) NOT NULL,
    fecha DATETIME2(0) NOT NULL CONSTRAINT df_ia_mensajes_fecha DEFAULT SYSDATETIME(),
    CONSTRAINT ck_ia_mensajes_rol CHECK (rol IN ('user','assistant')),
    CONSTRAINT fk_ia_mensajes_conversacion FOREIGN KEY (conversacion_id) REFERENCES dbo.ia_conversaciones(id) ON DELETE CASCADE
  );
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='ix_ia_mensajes_conversacion' AND object_id=OBJECT_ID('dbo.ia_mensajes'))
  CREATE INDEX ix_ia_mensajes_conversacion ON dbo.ia_mensajes(conversacion_id, id DESC);
GO

PRINT 'Migración IA OTI completada. No se eliminaron datos existentes.';
GO
