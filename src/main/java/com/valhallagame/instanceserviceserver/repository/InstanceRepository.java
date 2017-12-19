package com.valhallagame.instanceserviceserver.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valhallagame.instanceserviceserver.model.Instance;

public interface InstanceRepository extends JpaRepository<Instance, String> {
	public Optional<Instance> findInstanceById(String id);
}
