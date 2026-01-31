-- SQL Migration for PIN Authentication Feature
-- Run these statements to add PIN authentication to live drafts

-- Add PIN column to drafts table
ALTER TABLE drafts 
ADD COLUMN pin VARCHAR(4) DEFAULT NULL 
COMMENT '4-digit PIN for draft authentication';

-- Add is_verified column to draft_participants table
ALTER TABLE draft_participants 
ADD COLUMN is_verified BOOLEAN NOT NULL DEFAULT FALSE 
COMMENT 'Whether participant has been verified with PIN (creators are auto-verified)';

-- Add index for faster PIN lookups
CREATE INDEX idx_drafts_pin ON drafts(pin);
