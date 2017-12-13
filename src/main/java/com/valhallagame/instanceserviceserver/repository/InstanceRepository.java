package com.valhallagame.instanceserviceserver.repository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.valhallagame.instanceserviceserver.model.Instance;

public interface InstanceRepository extends JpaRepository<Instance, String> {

	@Transactional
	@Modifying
	@Query(value = "INSERT INTO selected_instance (username, instance_id) " + " VALUES (:username, :instance_id)"
			+ "ON CONFLICT (username) DO UPDATE SET instance_id = :instance_id", nativeQuery = true)
	public void setSelectedInstance(@Param("username") String username, @Param("instance_id") String instanceId);

	@Query(value = "SELECT i.* from instance i join selected_instance si USING (instance_id) where si.username = :username AND i.version = :version", nativeQuery = true)
	public Optional<Instance> getSelectedInstance(@Param("username") String username, @Param("version") String version);

	public Optional<Instance> findInstanceById(String id);

}
