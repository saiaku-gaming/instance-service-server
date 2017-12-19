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

	@Query(value = "SELECT * FROM dungeon d JOIN instance i ON(i.instance_id = d.instance_id) WHERE d.owner_party_id = :partyId AND i.state IN ('ACTIVE', 'STARTING') AND i.version = :version", nativeQuery = true)
	List<Dungeon> findRelevantDungeonsByPartyId(@Param("partyId") Integer partyId, @Param("version") String version);

	Optional<Dungeon> findDungeonByOwnerUsername(String username);

	@Query(value = "SELECT true FROM dungeon d JOIN instance i ON(i.instance_id = d.instance_id) WHERE d.dungeon_id = :dungeonId AND d.owner_username = :username AND i.state IN ('ACTIVE') AND i.version = :version", nativeQuery = true)
	boolean canAccessDungeon(@Param("username") String username, @Param("dungeonId") int dungeonId, @Param("version") String version);

	@Query(value = "SELECT true FROM dungeon d JOIN instance i ON(i.instance_id = d.instance_id) WHERE d.dungeon_id = :dungeonId AND d.owner_party_id = :partyId AND i.state IN ('ACTIVE') AND i.version = :version", nativeQuery = true)
	boolean canAccessDungeon(@Param("partyId") Integer partyId, @Param("dungeonId") int dungeonId, @Param("version") String version);
}
