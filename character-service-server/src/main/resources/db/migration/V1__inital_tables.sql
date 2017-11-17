CREATE TABLE character (
    person_id serial NOT NULL,
    character_name text NOT NULL,
    display_character_name text NOT NULL
);

CREATE TABLE selected_character {
    person_id PRIMARY KEY,
    character_name text NOT NULL REFERENCES character (character_name) ON DELETE CASCADE
}