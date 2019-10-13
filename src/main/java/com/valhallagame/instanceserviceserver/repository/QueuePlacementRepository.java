package com.valhallagame.instanceserviceserver.repository;

import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QueuePlacementRepository extends JpaRepository<QueuePlacement, String> {
	Optional<QueuePlacement> findQueuePlacementByQueuerUsername(String queuerUsername);

	List<QueuePlacement> findQueuePlacementsByMapName(String mapName);

    @Modifying
    @Query(value = "DELETE FROM queue_placement WHERE queuer_username = :username", nativeQuery = true)
    int deleteQueuePlacementByQueuerUsername(@Param("username") String username);
}
