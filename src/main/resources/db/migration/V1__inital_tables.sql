CREATE TABLE instance (
    instance_id TEXT PRIMARY KEY,
    version TEXT NOT NULL,
    level TEXT NOT NULL,
    address TEXT,
    port INT,
    player_count INT NOT NULL, 
    state TEXT NOT NULL
);

CREATE TABLE selected_instance (
    username TEXT PRIMARY KEY,
    instance_id TEXT NOT NULL REFERENCES instance (instance_id) ON DELETE CASCADE
);

CREATE TABLE hub (
    hub_id SERIAL,
    instance_id TEXT REFERENCES instance (instance_id)
);
