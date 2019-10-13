package com.valhallagame.instanceserviceserver.service;

import com.valhallagame.common.RestResponse;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.repository.DungeonRepository;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.partyserviceclient.model.PartyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DungeonService {
	private static final Logger logger = LoggerFactory.getLogger(DungeonService.class);

	@Autowired
	private DungeonRepository dungeonRepository;

	@Autowired
	private QueuePlacementService queuePlacementService;

	@Autowired
	private PartyServiceClient partyServiceClient;

	public Dungeon saveDungeon(Dungeon dungeon) {
		logger.info("Saving dungeon {}", dungeon);
		return dungeonRepository.save(dungeon);
	}

	public Optional<Dungeon> getDungeonByInstance(Instance instance) {
		logger.info("Getting dungeon with instance {}", instance);
		return instance == null ? Optional.empty() : dungeonRepository.findDungeonByInstanceId(instance.getId());
	}

	public boolean canCreateDungeon(String username) throws IOException {
		logger.info("Can create dungeon for user {}", username);
		RestResponse<PartyData> partyResp = partyServiceClient.getParty(username);

		Optional<PartyData> partyOpt = partyResp.get();
		if (partyOpt.isPresent() && (!partyOpt.get().getLeader().getDisplayUsername().equalsIgnoreCase(username) || dungeonRepository.hasNonFinishingDungeon(partyOpt.get().getId()))) {
			return false;
		}

		if(dungeonRepository.hasNonFinishingDungeon(username)) {
			return false;
		}

		return !queuePlacementService.getQueuePlacementFromQueuer(username).isPresent();
	}

	public List<Dungeon> findActiveDungeonsByPartyId(int partyId) {
		logger.info("find active dungeons for party {}", partyId);
		return dungeonRepository.findActiveDungeonsByPartyId(partyId);
	}

	public List<Dungeon> getRelevantDungeonsFromParty(PartyData party, String version) {
		logger.info("Getting relevant dungeons for party {} version {}", party, version);
		return dungeonRepository.findRelevantDungeonsByPartyId(party.getId(), version);
	}

	public Optional<Dungeon> getDungeonFromOwnerUsername(String username) {
		logger.info("Getting dungeon from user {}", username);
		return dungeonRepository.findDungeonByOwnerUsername(username);
	}

	public boolean canAccessDungeon(String username, int dungeonId, String version) {
		logger.info("Can user {} access dungeon {} version {}", username, dungeonId, version);
		return dungeonRepository.canAccessDungeon(username, dungeonId, version);
	}

	public boolean canAccessDungeon(Integer partyId, int dungeonId, String version) {
		logger.info("Can party {}, access dungeon {} version {}", partyId, dungeonId, version);
		return dungeonRepository.canAccessDungeon(partyId, dungeonId, version);
	}
}
