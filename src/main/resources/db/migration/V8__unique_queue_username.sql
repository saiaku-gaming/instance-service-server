DELETE
FROM queue_placement
WHERE TRUE;
ALTER TABLE queue_placement
    ADD UNIQUE (queuer_username);
