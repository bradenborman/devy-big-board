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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_verified (verified),
    INDEX idx_position (position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Drafts table
CREATE TABLE IF NOT EXISTS drafts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) UNIQUE NOT NULL,
    draft_name VARCHAR(255),
    status VARCHAR(50) DEFAULT 'completed',
    participant_count INT DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    INDEX idx_uuid (uuid),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Draft picks table
CREATE TABLE IF NOT EXISTS draft_picks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    draft_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    pick_number INT NOT NULL,
    picked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_draft_id (draft_id),
    INDEX idx_player_id (player_id),
    FOREIGN KEY (draft_id) REFERENCES drafts(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Player assets table (for headshots)
CREATE TABLE IF NOT EXISTS player_assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_player_asset (player_id),
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
