package com.valhallagame.instanceserviceserver.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.valhallagame.instanceserviceserver.model.Hub;

public interface HubRepository extends JpaRepository<Hub, Integer> {
	@Query(value = "SELECT h.* FROM hub h JOIN instance i ON(i.instance_id = h.instance_id) WHERE i.version = :version AND i.state = 'ACTIVE' ORDER BY i.player_count ASC LIMIT 1", nativeQuery = true)
	Optional<Hub> getHubWithLeastAmountOfPlayers(@Param("version") String version);
}
