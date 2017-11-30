package com.valhallagame.instanceserviceserver.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.valhallagame.instanceserviceserver.model.Instance;

public interface InstanceRepository extends JpaRepository<Instance, Integer> {
	public Optional<Instance> findByInstanceName(String instanceName);
	
	public List<Instance> findByOwner(String owner);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO selected_instance (owner, instance_name) "
    		+ " VALUES (:owner, :instance_name)"
    		+ "ON CONFLICT (owner) DO UPDATE SET instance_name = :instance_name", nativeQuery = true)
	public void setSelectedInstance(@Param("owner") String owner, @Param("instance_name")  String instanceName);
    
    @Query(value = "SELECT c.* from instance c join selected_instance sc USING (owner, instance_name) where sc.owner = :owner", nativeQuery = true)
	public Optional<Instance> getSelectedInstance(@Param("owner") String owner);

	public Optional<Instance> getInstance(String member);
}
