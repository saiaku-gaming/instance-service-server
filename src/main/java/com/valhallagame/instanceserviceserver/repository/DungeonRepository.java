package com.valhallagame.instanceserviceserver.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.valhallagame.instanceserviceserver.model.Dungeon;

public interface DungeonRepository extends JpaRepository<Dungeon, Integer> {
	Optional<Dungeon> findDungeonByOwner(String owner);

	@Query(value = "SELECT * FROM dungeon WHERE instance_id = :instanceId", nativeQuery = true)
	Optional<Dungeon> findDungeonByInstanceId(@Param("instanceId") String instanceId);
}
