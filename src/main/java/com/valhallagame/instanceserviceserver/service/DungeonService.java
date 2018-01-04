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
import com.valhallagame.partyserviceclient.model.PartyData;

@Service
public class DungeonService {

	@Autowired
	private DungeonRepository dungeonRepository;

	@Autowired
	private QueuePlacementService queuePlacementService;

	@Autowired
	private PartyServiceClient partyServiceClient;

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
		RestResponse<PartyData> partyResp = partyServiceClient.getParty(username);

		Optional<PartyData> partyOpt = partyResp.get();
		if (partyOpt.isPresent() && !partyOpt.get().getLeader().getDisplayUsername().equalsIgnoreCase(username)) {
			return false;
		}

		Optional<Dungeon> optDungeon = dungeonRepository.findDungeonByCreatorUsername(username);
		if (!optDungeon.isPresent()) {
			return true;
		}

		if (!optDungeon.get().getInstance().getState().equals(InstanceState.FINISHING.name())) {
			return false;
		}

		return !queuePlacementService.getQueuePlacementFromQueuer(username).isPresent();
	}

	public List<Dungeon> getRelevantDungeonsFromParty(PartyData party, String version) {
		return dungeonRepository.findRelevantDungeonsByPartyId(party.getId(), version);
	}

	public Optional<Dungeon> getDungeonFromOwnerUsername(String username) {
		return dungeonRepository.findDungeonByOwnerUsername(username);
	}

	public boolean canAccessDungeon(String username, int dungeonId, String version) {
		return dungeonRepository.canAccessDungeon(username, dungeonId, version);
	}

	public boolean canAccessDungeon(Integer partyId, int dungeonId, String version) {
		return dungeonRepository.canAccessDungeon(partyId, dungeonId, version);
	}
}
