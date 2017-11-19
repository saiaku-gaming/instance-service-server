CREATE TABLE character (
    owner TEXT NOT NULL,
    character_name TEXT PRIMARY KEY,
    display_character_name TEXT NOT NULL,
    chest_item TEXT NOT NULL,
    mainhand_armament TEXT NOT NULL,
    off_hand_armament TEXT NOT NULL
);

CREATE TABLE selected_character (
    owner TEXT PRIMARY KEY,
    character_name TEXT NOT NULL REFERENCES character (character_name) ON DELETE CASCADE
)