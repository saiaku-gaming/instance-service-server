package com.valhallagame.instanceserviceserver.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.common.RestResponse;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.InstanceState;
import com.valhallagame.instanceserviceserver.repository.DungeonRepository;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.partyserviceclient.model.PartyResponse;

@Service
public class DungeonService {

	@Autowired
	private DungeonRepository dungeonRepository;

	@Autowired
	private QueuePlacementService queuePlacementService;

	private PartyServiceClient partyServiceClient = PartyServiceClient.get();

	public Dungeon saveDungeon(Dungeon dungeon) {
		return dungeonRepository.save(dungeon);
	}

	public void deleteDungeon(Dungeon dungeon) {
		dungeonRepository.delete(dungeon);
	}

	public Optional<Dungeon> getDungeonByInstance(Instance instance) {
		return dungeonRepository.findDungeonByInstanceId(instance.getId());
	}

	public boolean canCreateDungeon(String username) throws IOException {
		RestResponse<PartyResponse> party = partyServiceClient.getParty(username);

		if (party.isOk()
				&& !party.getResponse().get().getLeader().getDisplayUsername().toLowerCase().equals(username)) {
			return false;
		}

		Optional<Dungeon> optDungeon = dungeonRepository.findDungeonByCreatorUsername(username);
		if (!optDungeon.isPresent()) {
			return true;
		}

		if (optDungeon.get().getInstance().getState().equals(InstanceState.STARTING.name())) {
			return false;
		}

		if (queuePlacementService.getQueuePlacementFromQueuer(username).isPresent()) {
			return false;
		}

		return true;
	}

	public List<Dungeon> getRelevantDungeonsFromParty(PartyResponse party, String version) {
		return dungeonRepository.findRelevantDungeonsByPartyId(party.getId(), version);
	}

	public Optional<Dungeon> getDungeonFromOwnerUsername(String username) {
		return dungeonRepository.findDungeonByOwnerUsername(username);
	}
}
