package com.valhallagame.instanceserviceserver.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.instanceserviceserver.model.Hub;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.repository.HubRepository;

@Service
public class HubService {
	private static final String HUB_MAP = "ValhallaMap";

	private static final int SOFT_MAX = 10;

	@Autowired
	private HubRepository hubRepository;

	@Autowired
	private InstanceService instanceService;

	public void saveHub(Hub hub) {
		hubRepository.save(hub);
	}

	public void deleteHub(Hub local) {
		hubRepository.delete(local);
	}

	public Optional<Hub> getHubWithLeastAmountOfPlayers(String version, String username) throws IOException {
		Optional<Hub> hubOpt = hubRepository.getHubWithLeastAmountOfPlayers(version);

		// Give it the old hub if we reached SOFT_MAX,
		// Give it the new hub if we could not find any old ones.
		if (!hubOpt.isPresent() || (hubOpt.isPresent() && hubOpt.get().getInstance().getPlayerCount() > SOFT_MAX)) {
			Optional<Instance> optInstance = instanceService.createInstance(HUB_MAP, version, username);
			if (optInstance.isPresent()) {
				Instance instance = optInstance.get();
				Hub hub = new Hub();
				hub.setInstance(instance);
				hub = hubRepository.save(hub);
			}
		}

		return hubOpt.isPresent() && hubOpt.get().getInstance().getState().equals("ACTIVE") ? hubOpt : Optional.empty();
	}
}
