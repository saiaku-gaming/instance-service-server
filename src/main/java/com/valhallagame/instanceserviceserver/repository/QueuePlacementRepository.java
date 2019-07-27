package com.valhallagame.instanceserviceserver.repository;

import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueuePlacementRepository extends JpaRepository<QueuePlacement, String> {
	Optional<QueuePlacement> findQueuePlacementByQueuerUsername(String queuerUsername);

	List<QueuePlacement> findQueuePlacementsByMapName(String mapName);

    int deleteQueuePlacementByQueuerUsername(String username);
}
