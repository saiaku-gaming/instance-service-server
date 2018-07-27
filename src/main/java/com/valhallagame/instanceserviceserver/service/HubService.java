package com.valhallagame.instanceserviceserver.service;

import com.valhallagame.instanceserviceserver.model.Hub;
import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import com.valhallagame.instanceserviceserver.repository.HubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HubService {
	public static final String HUB_MAP = "ValhallaMap";

	private static final int SOFT_MAX = 10;

	@Autowired
	private HubRepository hubRepository;

	@Autowired
	private QueuePlacementService queuePlacementService;

	public Hub saveHub(Hub hub) {
		return hubRepository.save(hub);
	}

	public Optional<Hub> getHubWithLeastAmountOfPlayers(String version, String username) throws IOException {
		Optional<Hub> hubOpt = hubRepository.getHubWithLeastAmountOfPlayers(version);

		// Give it the old hub if we reached SOFT_MAX,
		// Give it the new hub if we could not find any old ones.
		if (!hubOpt.isPresent() || (hubOpt.get().getInstance().getPlayerCount() > SOFT_MAX)) {
			List<QueuePlacement> queuePlacementsFromMapName = queuePlacementService
					.getQueuePlacementsFromMapName(HUB_MAP).stream().filter(qp -> qp.getVersion().equals(version))
					.collect(Collectors.toList());
			if (queuePlacementsFromMapName.isEmpty()) {
				queuePlacementService.queueForInstance(version, HUB_MAP, username);
			}
		}

		return hubOpt.isPresent() && hubOpt.get().getInstance().getState().equals("ACTIVE") ? hubOpt : Optional.empty();
	}
}
