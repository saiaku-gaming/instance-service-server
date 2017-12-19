package com.valhallagame.instanceserviceserver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.valhallagame.instanceserviceserver.model.Dungeon;

public interface DungeonRepository extends JpaRepository<Dungeon, Integer> {
	@Query(value = "SELECT * FROM dungeon WHERE instance_id = :instanceId", nativeQuery = true)
	Optional<Dungeon> findDungeonByInstanceId(@Param("instanceId") String instanceId);

	Optional<Dungeon> findDungeonByCreatorUsername(String username);

	@Query(value = "SELECT * FROM dungeon d JOIN instance i ON(i.instance_id = d.instance_id) WHERE d.owner_party_id = :partyId AND d.state IN ('ACTIVE', 'STARTING') AND i.version = :version", nativeQuery = true)
	List<Dungeon> findRelevantDungeonsByPartyId(@Param("partyId") Integer partyId, @Param("version") String version);

	Optional<Dungeon> findDungeonByOwnerUsername(String username);
}
