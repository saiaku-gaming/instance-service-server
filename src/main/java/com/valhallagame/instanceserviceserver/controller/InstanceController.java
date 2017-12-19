package com.valhallagame.instanceserviceserver.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.valhallagame.common.JS;
import com.valhallagame.common.RestResponse;
import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.instancecontainerserviceclient.message.QueuePlacementDescription;
import com.valhallagame.instanceserviceserver.message.ActivateInstanceParameter;
import com.valhallagame.instanceserviceserver.message.GetDungeonConnectionParameter;
import com.valhallagame.instanceserviceserver.message.GetHubParameter;
import com.valhallagame.instanceserviceserver.message.GetRelevantDungeonsParameter;
import com.valhallagame.instanceserviceserver.message.InstancePlayerLoginParameter;
import com.valhallagame.instanceserviceserver.message.InstancePlayerLogoutParameter;
import com.valhallagame.instanceserviceserver.message.SessionAndConnectionResponse;
import com.valhallagame.instanceserviceserver.message.StartDungeonParameter;
import com.valhallagame.instanceserviceserver.message.UpdateInstanceStateParameter;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Hub;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.InstanceState;
import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import com.valhallagame.instanceserviceserver.service.DungeonService;
import com.valhallagame.instanceserviceserver.service.HubService;
import com.valhallagame.instanceserviceserver.service.InstanceService;
import com.valhallagame.instanceserviceserver.service.QueuePlacementService;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.partyserviceclient.model.PartyMemberResponse;
import com.valhallagame.partyserviceclient.model.PartyResponse;

@Controller
@RequestMapping(path = "/v1/instance")
public class InstanceController {

	private static InstanceContainerServiceClient instanceContainerServiceClient = InstanceContainerServiceClient.get();
	private static PartyServiceClient partyServiceClient = PartyServiceClient.get();

	@Autowired
	private InstanceService instanceService;

	@Autowired
	private HubService hubService;

	@Autowired
	private DungeonService dungeonService;

	@Autowired
	private QueuePlacementService queuePlacementService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@RequestMapping(path = "/get-hub", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> getHub(@RequestBody GetHubParameter input) throws IOException {
		Optional<Hub> optHub = hubService.getHubWithLeastAmountOfPlayers(input.getVersion(), input.getUsername());
		if (!optHub.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "No instance found. Please try again.");
		}

		return getSession(input.getUsername(), optHub.get().getInstance());
	}

	@RequestMapping(path = "/get-dungeon-connection", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> getDungeonConnection(@RequestBody GetDungeonConnectionParameter input) throws IOException {

		String version = input.getVersion();

		Optional<Instance> instanceOpt = instanceService.getInstance(input.getGameSessionId());
		if (!instanceOpt.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "No instance found.");
		}
		Instance instance = instanceOpt.get();
		Optional<Dungeon> dungeonOpt = dungeonService.getDungeonByInstance(instance);
		if (!dungeonOpt.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "That game session does not seem to be a dungeon!");
		}

		int dungeonId = dungeonOpt.get().getId();
		String username = input.getUsername();

		RestResponse<PartyResponse> partyResp = partyServiceClient.getParty(username);
		if (partyResp.isOk()) {
			Integer partyId = partyResp.getResponse().get().getId();
			if (dungeonService.canAccessDungeon(partyId, dungeonId, version)) {
				return getSession(input.getUsername(), instanceOpt.get());
			}
		} else if (dungeonService.canAccessDungeon(username, dungeonId, version)) {
			return getSession(input.getUsername(), instanceOpt.get());
		}
		return JS.message(HttpStatus.BAD_REQUEST, "Nope! You cant go there");
	}

	@RequestMapping(path = "/get-relevant-dungeons", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> getRelevantDungeons(@RequestBody GetRelevantDungeonsParameter input) throws IOException {

		String username = input.getUsername();
		String version = input.getVersion();

		if (input.getVersion() == null || input.getVersion().isEmpty()) {
			return JS.message(HttpStatus.BAD_REQUEST, "Missing game version");
		}

		if (username == null || username.isEmpty()) {
			return JS.message(HttpStatus.BAD_REQUEST, "Missing username");
		}

		List<Dungeon> relevantDungeons = new ArrayList<>();

		RestResponse<PartyResponse> partyResp = partyServiceClient.getParty(username);
		if (partyResp.isOk()) {
			relevantDungeons = dungeonService.getRelevantDungeonsFromParty(partyResp.getResponse().get(), version);
		} else {
			Optional<Dungeon> optDungeon = dungeonService.getDungeonFromOwnerUsername(username);
			if (optDungeon.isPresent()) {
				relevantDungeons.add(optDungeon.get());
			}
		}

		return JS.message(HttpStatus.OK, relevantDungeons);
	}

	@RequestMapping(path = "/activate-instance", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> activateInstance(@RequestBody ActivateInstanceParameter input) throws IOException {
		Optional<Instance> optInstance = instanceService.getInstance(input.getGameSessionId());

		if (!optInstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find instance with id: " + input.getGameSessionId());
		}

		Instance instance = optInstance.get();
		instance.setState(InstanceState.ACTIVE.name());
		instance.setAddress(input.getAddress());
		instance.setPort(input.getPort());

		instance = instanceService.saveInstance(instance);

		Optional<Dungeon> optDungeon = dungeonService.getDungeonByInstance(instance);

		if (optDungeon.isPresent()) {
			Dungeon dungeon = optDungeon.get();

			RestResponse<PartyResponse> partyResponse = partyServiceClient.getParty(dungeon.getOwnerUsername());
			if (partyResponse.isOk()) {
				PartyResponse party = partyResponse.getResponse().get();
				for (PartyMemberResponse member : party.getPartyMembers()) {
					rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
							RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(),
							new NotificationMessage(member.getDisplayUsername().toLowerCase(), "Dungeon active!"));
				}
			} else {
				rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
						RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(),
						new NotificationMessage(dungeon.getOwnerUsername(), "Dungeon active!"));
			}
		}

		return JS.message(HttpStatus.OK, "Activated instance with id: " + input.getGameSessionId());
	}

	@RequestMapping(path = "/update-instance-state", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> updateInstanceState(@RequestBody UpdateInstanceStateParameter input) throws IOException {
		Optional<Instance> optInstance = instanceService.getInstance(input.getGameSessionId());

		if (!optInstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find instance with id: " + input.getGameSessionId());
		}

		InstanceState state = InstanceState.valueOf(input.getState().toUpperCase());
		Instance instance = optInstance.get();

		switch (state) {
		case FINISHED:
			instanceService.deleteInstance(instance);
			break;
		case STARTING:
			return JS.message(HttpStatus.BAD_REQUEST, "The state should never be set to STARTING from here!");
		case ACTIVE:
		case FINISHING:
			instance.setState(state.name());
		}

		return JS.message(HttpStatus.OK, "Updated state on instance with id: " + input.getGameSessionId());
	}

	@RequestMapping(path = "/start-dungeon", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> startDungeon(@RequestBody StartDungeonParameter input) throws IOException {

		if (!dungeonService.canCreateDungeon(input.getUsername())) {
			return JS.message(HttpStatus.BAD_REQUEST, "You cannot make a dungeon");
		}

		RestResponse<QueuePlacementDescription> createQueuePlacementResponse = instanceContainerServiceClient
				.createQueuePlacement("DungeonQueue" + input.getVersion(), input.getMap(), input.getVersion(), input.getUsername());

		if (!createQueuePlacementResponse.isOk()) {
			return JS.message(createQueuePlacementResponse);
		}

		QueuePlacementDescription queuePlacementDescription = createQueuePlacementResponse.getResponse().get();

		QueuePlacement queuePlacement = new QueuePlacement();
		queuePlacement.setId(queuePlacementDescription.getId());
		queuePlacement.setMapName(input.getMap());
		queuePlacement.setQueuerUsername(input.getUsername());
		queuePlacement.setStatus(queuePlacementDescription.getStatus());
		queuePlacement.setVersion(input.getVersion());

		queuePlacement = queuePlacementService.saveQueuePlacement(queuePlacement);

		return JS.message(HttpStatus.OK, "Dungeon started");
	}

	@RequestMapping(path = "/instance-player-login", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> instancePlayerLogin(@RequestBody InstancePlayerLoginParameter input) throws IOException {
		Optional<Instance> optInstance = instanceService.getInstance(input.getGameSessionId());

		if (!optInstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find instance with id: " + input.getGameSessionId());
		}

		Instance instance = optInstance.get();
		instance.getMembers().add(input.getUsername());

		instance = instanceService.saveInstance(instance);

		return JS.message(HttpStatus.OK, "Player added to instance");
	}

	@RequestMapping(path = "/instance-player-logout", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> instancePlayerLogout(@RequestBody InstancePlayerLogoutParameter input) throws IOException {
		Optional<Instance> optInstance = instanceService.getInstance(input.getGameSessionId());

		if (!optInstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find instance with id: " + input.getGameSessionId());
		}

		Instance instance = optInstance.get();
		instance.getMembers().remove(input.getUsername());

		instance = instanceService.saveInstance(instance);

		return JS.message(HttpStatus.OK, "Player removed from instance");
	}

	private ResponseEntity<?> getSession(String username, Instance instance) throws IOException {

		RestResponse<String> playerSessionResp = instanceContainerServiceClient.createPlayerSession(username,
				instance.getId());
		if (playerSessionResp.isOk()) {
			String playerSession = playerSessionResp.getResponse().get();
			SessionAndConnectionResponse sac = new SessionAndConnectionResponse(instance.getAddress(),
					instance.getPort(), playerSession);
			return JS.message(HttpStatus.OK, sac);
		} else {
			return JS.message(HttpStatus.NOT_FOUND, "No player session available. Please try again.");
		}
	}

}
