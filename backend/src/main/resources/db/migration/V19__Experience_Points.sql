-- V19: Experience Points System
-- Add experience_points column to users table for XP-based level progression

ALTER TABLE users
  ADD COLUMN experience_points INT NOT NULL DEFAULT 0;

CREATE INDEX idx_users_experience ON users (experience_points);
