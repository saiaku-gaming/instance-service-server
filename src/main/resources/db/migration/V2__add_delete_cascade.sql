ALTER TABLE hub DROP CONSTRAINT hub_instance_id_fkey;

ALTER TABLE hub ADD CONSTRAINT hub_instance_id_fkey FOREIGN KEY (instance_id) REFERENCES instance (instance_id) ON DELETE CASCADE;