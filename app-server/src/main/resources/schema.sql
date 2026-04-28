-- Schema initialization for Devy BigBoard
-- This file creates tables if they don't exist
-- Spring Boot will automatically execute this on startup when spring.jpa.hibernate.ddl-auto is set to 'none' or 'validate'

-- Players table
CREATE TABLE IF NOT EXISTS players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    position VARCHAR(50) NOT NULL,
    team VARCHAR(255),
    college VARCHAR(255),
    draftyear INT,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    cfbd_player_id VARCHAR(50) DEFAULT NULL COMMENT 'collegefootballdata.com player ID for stats lookup',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_verified (verified),
    INDEX idx_position (position),
    INDEX idx_cfbd_player_id (cfbd_player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- collegefootballdata.com player profile cache
-- Mirrors the /player/search response fields
CREATE TABLE IF NOT EXISTS cfbd_player_profiles (
    cfbd_player_id VARCHAR(50) PRIMARY KEY,
    player_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    position VARCHAR(50),
    team VARCHAR(255),
    weight INT,
    height INT,
    jersey INT,
    hometown VARCHAR(255),
    team_color VARCHAR(20),
    team_color_secondary VARCHAR(20),
    fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- collegefootballdata.com season stats cache
-- Stores the long/narrow format returned by /stats/player/season
-- One row per (player, season, category, stat_type)
CREATE TABLE IF NOT EXISTS cfbd_player_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cfbd_player_id VARCHAR(50) NOT NULL,
    season INT NOT NULL,
    team VARCHAR(255),
    conference VARCHAR(100),
    position VARCHAR(50),
    category VARCHAR(50) NOT NULL COMMENT 'e.g. passing, rushing, receiving, defensive, kicking, punting, fumbles',
    stat_type VARCHAR(50) NOT NULL COMMENT 'e.g. YDS, TD, ATT, COMPLETIONS, PCT, YPA, CAR, REC, YPR',
    stat VARCHAR(50) NOT NULL,
    fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_player_season_stat (cfbd_player_id, season, category, stat_type),
    INDEX idx_cfbd_player_season (cfbd_player_id, season),
    INDEX idx_season (season)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Drafts table
CREATE TABLE IF NOT EXISTS drafts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) UNIQUE NOT NULL,
    draft_name VARCHAR(255),
    status VARCHAR(50) DEFAULT 'completed',
    participant_count INT DEFAULT 1,
    created_by VARCHAR(50),
    started_at TIMESTAMP,
    current_round INT DEFAULT 1,
    current_pick INT DEFAULT 1,
    total_rounds INT DEFAULT 10,
    is_snake_draft BOOLEAN DEFAULT FALSE,
    pin VARCHAR(4) DEFAULT NULL COMMENT '4-digit PIN for draft authentication',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    INDEX idx_uuid (uuid),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status),
    INDEX idx_drafts_pin (pin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Draft picks table
CREATE TABLE IF NOT EXISTS draft_picks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    draft_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    pick_number INT NOT NULL,
    position VARCHAR(1),
    forced_by VARCHAR(1),
    round_number INT,
    picked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_draft_id (draft_id),
    INDEX idx_player_id (player_id),
    INDEX idx_draft_round (draft_id, round_number),
    FOREIGN KEY (draft_id) REFERENCES drafts(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Player assets table (for headshots)
CREATE TABLE IF NOT EXISTS player_assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_player_asset (player_id),
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Draft participants table (for live drafts)
CREATE TABLE IF NOT EXISTS draft_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    draft_id BIGINT NOT NULL,
    position VARCHAR(1) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    is_ready BOOLEAN NOT NULL DEFAULT FALSE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether participant has been verified with PIN (creators are auto-verified)',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_draft_position (draft_id, position),
    UNIQUE KEY unique_draft_nickname (draft_id, nickname),
    INDEX idx_draft_id (draft_id),
    FOREIGN KEY (draft_id) REFERENCES drafts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
