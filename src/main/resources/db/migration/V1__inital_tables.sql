CREATE TABLE instance (
    owner TEXT NOT NULL,
    instance_name TEXT PRIMARY KEY,
    display_instance_name TEXT NOT NULL,
    chest_item TEXT NOT NULL,
    mainhand_armament TEXT NOT NULL,
    off_hand_armament TEXT NOT NULL
);

CREATE TABLE selected_instance (
    owner TEXT PRIMARY KEY,
    instance_name TEXT NOT NULL REFERENCES instance (instance_name) ON DELETE CASCADE
)