CREATE TABLE queue_placement(
	queue_placement_id TEXT PRIMARY KEY,
	queuer_username TEXT NOT NULL,
	status TEXT NOT NULL,
	map_name TEXT NOT NULL,
	version TEXT NOT NULL
);