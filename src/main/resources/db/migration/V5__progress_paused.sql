ALTER TABLE player_progress
    DROP CONSTRAINT player_progress_status_check,
    ADD CONSTRAINT player_progress_status_check CHECK (status IN ('IN_PROGRESS', 'PAUSED', 'DEAD', 'FINISHED'));
