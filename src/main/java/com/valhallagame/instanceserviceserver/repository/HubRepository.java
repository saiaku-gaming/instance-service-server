package com.valhallagame.instanceserviceserver.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valhallagame.instanceserviceserver.model.Hub;

public interface HubRepository extends JpaRepository<Hub, Integer> {

	
	Optional<Hub> getHubWithLeastAmountOfPlayers(String version);


}
