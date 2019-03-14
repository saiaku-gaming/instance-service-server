package com.valhallagame.instanceserviceserver.repository;

import com.valhallagame.instanceserviceserver.model.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HubRepository extends JpaRepository<Hub, Integer> {
	@Query(value = "" +
			" SELECT h.*" +
			" FROM hub h " +
			"	JOIN instance i ON(i.instance_id = h.instance_id)" +
			"	JOIN (" +
			"		SELECT instance.instance_id, COUNT(im.instance_member_id) AS player_count" +
			"		FROM instance" +
			"			JOIN hub ON (instance.instance_id = hub.instance_id)" +
			"			LEFT JOIN instance_member im ON(instance.instance_id = im.instance_id)" +
			"		WHERE instance.version = :version GROUP BY instance.instance_id ORDER BY player_count ASC LIMIT 1" +
			"	) AS q ON (q.instance_id = i.instance_id)" +
			" WHERE i.state IN ('ACTIVE', 'STARTING')" +
			" LIMIT 1"
			, nativeQuery = true)
	Optional<Hub> getHubWithLeastAmountOfPlayers(@Param("version") String version);
}
