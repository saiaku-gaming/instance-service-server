package com.valhallagame.instanceserviceserver.repository;

import com.valhallagame.instanceserviceserver.model.Dungeon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DungeonRepository extends JpaRepository<Dungeon, Integer> {
	@Query(value = "SELECT * FROM dungeon WHERE instance_id = :instanceId", nativeQuery = true)
	Optional<Dungeon> findDungeonByInstanceId(@Param("instanceId") String instanceId);

	@Query(value = "SELECT * FROM dungeon d JOIN instance i ON(i.instance_id = d.instance_id) WHERE d.owner_party_id = :partyId AND i.state IN ('ACTIVE')", nativeQuery = true)
	List<Dungeon> findActiveDungeonsByPartyId(@Param("partyId") Integer partyId);

	@Query(value = "SELECT * FROM dungeon d JOIN instance i ON(i.instance_id = d.instance_id) WHERE d.owner_party_id = :partyId AND i.state IN ('ACTIVE', 'STARTING') AND i.version = :version", nativeQuery = true)
	List<Dungeon> findRelevantDungeonsByPartyId(@Param("partyId") Integer partyId, @Param("version") String version);

	@Query(value = "SELECT * FROM dungeon d JOIN instance i ON (i.instance_id = d.instance_id) WHERE owner_username = :username AND i.state NOT IN ('FINISHING', 'FINISHED')", nativeQuery = true)
	Optional<Dungeon> findDungeonByOwnerUsername(@Param("username") String username);

	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM dungeon d JOIN instance i ON(i.instance_id = d.instance_id) WHERE d.dungeon_id = :dungeonId AND d.owner_username = :username AND i.state IN ('ACTIVE') AND i.version = :version", nativeQuery = true)
	boolean canAccessDungeon(@Param("username") String username, @Param("dungeonId") int dungeonId, @Param("version") String version);

	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM dungeon d JOIN instance i ON(i.instance_id = d.instance_id) WHERE d.dungeon_id = :dungeonId AND d.owner_party_id = :partyId AND i.state IN ('ACTIVE') AND i.version = :version", nativeQuery = true)
	boolean canAccessDungeon(@Param("partyId") Integer partyId, @Param("dungeonId") int dungeonId, @Param("version") String version);

	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM dungeon d JOIN instance i ON(i.instance_id = d.instance_id) WHERE d.owner_party_id = :partyId AND i.state != 'FINISHING'", nativeQuery = true)
	boolean hasNonFinishingDungeon(@Param("partyId") Integer partyId);

	@Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM dungeon d JOIN instance i ON(i.instance_id = d.instance_id) WHERE d.owner_username = :username AND i.state != 'FINISHING'", nativeQuery = true)
	boolean hasNonFinishingDungeon(@Param("username") String username);
}
