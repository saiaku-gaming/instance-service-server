DROP TABLE selected_instance;

ALTER TABLE instance DROP COLUMN player_count;

CREATE TABLE instance_member (
	instance_member_id SERIAL NOT NULL,
	instance_id INTEGER,
	username TEXT
);

ALTER TABLE dungeon RENAME COLUMN owner TO owner_username;
ALTER TABLE dungeon ALTER COLUMN owner_username DROP NOT NULL;
ALTER TABLE dungeon ADD COLUMN owner_party_id INTEGER;
ALTER TABLE dungeon ADD COLUMN creator_username TEXT NOT NULL;