package com.valhallagame.instanceserviceserver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valhallagame.instanceserviceserver.model.QueuePlacement;

public interface QueuePlacementRepository extends JpaRepository<QueuePlacement, String> {
	Optional<QueuePlacement> findQueuePlacementByQueuerUsername(String queuerUsername);

	List<QueuePlacement> findQueuePlacementsByMapName(String mapName);
}
