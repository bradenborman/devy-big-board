-- Migration: Rename image_url column to filename and extract just the filename
-- Run this manually on your Railway MySQL database

-- First, add the new column
ALTER TABLE player_assets ADD COLUMN filename VARCHAR(255);

-- Extract just the filename from the full URL and populate the new column
-- This handles URLs like: https://storage.railway.app/bucket/players/headshots/uuid.jpg
-- And extracts just: uuid.jpg
UPDATE player_assets 
SET filename = SUBSTRING_INDEX(image_url, '/', -1)
WHERE image_url IS NOT NULL;

-- Make the new column NOT NULL
ALTER TABLE player_assets MODIFY COLUMN filename VARCHAR(255) NOT NULL;

-- Drop the old column
ALTER TABLE player_assets DROP COLUMN image_url;
