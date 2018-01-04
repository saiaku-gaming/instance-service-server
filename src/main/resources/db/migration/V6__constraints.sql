DELETE FROM instance_member;
DELETE FROM dungeon;
ALTER TABLE instance_member ADD UNIQUE (username);
ALTER TABLE dungeon ADD UNIQUE (owner_username);
