CREATE TABLE dungeon (
	dungeon_id SERIAL NOT NULL,
	instance_id TEXT NOT NULL REFERENCES instance (instance_id) ON DELETE CASCADE,
	owner TEXT NOT NULL
);