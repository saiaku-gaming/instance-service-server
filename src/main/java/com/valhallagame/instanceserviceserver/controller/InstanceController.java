package com.valhallagame.instanceserviceserver.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.valhallagame.common.JS;
import com.valhallagame.common.RestResponse;
import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.instancecontainerserviceclient.model.QueuePlacementDescriptionData;
import com.valhallagame.instanceserviceclient.message.ActivateInstanceParameter;
import com.valhallagame.instanceserviceclient.message.AddLocalInstanceParameter;
import com.valhallagame.instanceserviceclient.message.GetAllPlayersInSameInstanceParameter;
import com.valhallagame.instanceserviceclient.message.GetDungeonConnectionParameter;
import com.valhallagame.instanceserviceclient.message.GetHubParameter;
import com.valhallagame.instanceserviceclient.message.GetRelevantDungeonsParameter;
import com.valhallagame.instanceserviceclient.message.InstancePlayerLoginParameter;
import com.valhallagame.instanceserviceclient.message.InstancePlayerLogoutParameter;
import com.valhallagame.instanceserviceclient.message.StartDungeonParameter;
import com.valhallagame.instanceserviceclient.message.UpdateInstanceStateParameter;
import com.valhallagame.instanceserviceclient.model.SessionAndConnectionData;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Hub;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.InstanceState;
import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import com.valhallagame.instanceserviceserver.model.RelevantDungeonsAndPlacement;
import com.valhallagame.instanceserviceserver.service.DungeonService;
import com.valhallagame.instanceserviceserver.service.HubService;
import com.valhallagame.instanceserviceserver.service.InstanceService;
import com.valhallagame.instanceserviceserver.service.QueuePlacementService;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.partyserviceclient.model.PartyData;
import com.valhallagame.partyserviceclient.model.PartyMemberData;
import com.valhallagame.personserviceclient.PersonServiceClient;
import com.valhallagame.personserviceclient.model.SessionData;

@Controller
@RequestMapping(path = "/v1/instance")
public class InstanceController {

	private static InstanceContainerServiceClient instanceContainerServiceClient = InstanceContainerServiceClient.get();
	private static PartyServiceClient partyServiceClient = PartyServiceClient.get();
	private static PersonServiceClient personServiceClient = PersonServiceClient.get();

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
	public ResponseEntity<JsonNode> getHub(@Valid @RequestBody GetHubParameter input) throws IOException {
		Optional<Hub> optHub = hubService.getHubWithLeastAmountOfPlayers(input.getVersion(), input.getUsername());
		if (!optHub.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "No instance found. Please try again.");
		}

		return getSession(input.getUsername(), optHub.get().getInstance());
	}

	@RequestMapping(path = "/get-dungeon-connection", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> getDungeonConnection(@Valid @RequestBody GetDungeonConnectionParameter input)
			throws IOException {

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

		RestResponse<PartyData> partyResp = partyServiceClient.getParty(username);
		Optional<PartyData> partyOpt = partyResp.get();
		if (partyOpt.isPresent()) {
			Integer partyId = partyOpt.get().getId();
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
	public ResponseEntity<JsonNode> getRelevantDungeons(@Valid @RequestBody GetRelevantDungeonsParameter input)
			throws IOException {

		String username = input.getUsername();
		String version = input.getVersion();

		if (input.getVersion() == null || input.getVersion().isEmpty()) {
			return JS.message(HttpStatus.BAD_REQUEST, "Missing game version");
		}

		if (username == null || username.isEmpty()) {
			return JS.message(HttpStatus.BAD_REQUEST, "Missing username");
		}

		List<Dungeon> relevantDungeons = new ArrayList<>();

		RestResponse<PartyData> partyResp = partyServiceClient.getParty(username);
		Optional<PartyData> partyOpt = partyResp.get();
		if (partyOpt.isPresent()) {
			relevantDungeons = dungeonService.getRelevantDungeonsFromParty(partyOpt.get(), version);
		} else {
			Optional<Dungeon> optDungeon = dungeonService.getDungeonFromOwnerUsername(username);
			if (optDungeon.isPresent()) {
				relevantDungeons.add(optDungeon.get());
			}
		}
		
		List<QueuePlacement> queuePlacement = new ArrayList<>();
		if (partyOpt.isPresent()) {
			PartyData partyData = partyOpt.get();
			partyData.getPartyMembers().forEach(m -> {
				String memberUsername = m.getDisplayUsername().toLowerCase();
				Optional<QueuePlacement> placementOpt = queuePlacementService.getQueuePlacementFromQueuer(memberUsername);
				if(placementOpt.isPresent()) {
					queuePlacement.add(placementOpt.get());
				}
			});
		} else {
			Optional<QueuePlacement> placementOpt = queuePlacementService.getQueuePlacementFromQueuer(username);
			if(placementOpt.isPresent()) {
				queuePlacement.add(placementOpt.get());
			}
		}
		
		RelevantDungeonsAndPlacement relevantDungeonsAndPlacement = new RelevantDungeonsAndPlacement(relevantDungeons, queuePlacement);
		return JS.message(HttpStatus.OK, relevantDungeonsAndPlacement);
	}

	@RequestMapping(path = "/activate-instance", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> activateInstance(@Valid @RequestBody ActivateInstanceParameter input)
			throws IOException {
		Optional<Instance> optInstance = instanceService.getInstance(input.getGameSessionId());

		if (!optInstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find instance with id: %s", input.getGameSessionId());
		}

		Instance instance = optInstance.get();
		instance.setState(InstanceState.ACTIVE.name());
		instance.setAddress(input.getAddress());
		instance.setPort(input.getPort());

		instance = instanceService.saveInstance(instance);

		Optional<Dungeon> optDungeon = dungeonService.getDungeonByInstance(instance);

		if (optDungeon.isPresent()) {
			Dungeon dungeon = optDungeon.get();

			RestResponse<PartyData> partyResponse = partyServiceClient.getParty(dungeon.getOwnerUsername());
			Optional<PartyData> partyOpt = partyResponse.get();
			if (partyOpt.isPresent()) {
				PartyData party = partyOpt.get();
				for (PartyMemberData member : party.getPartyMembers()) {
					NotificationMessage notificationMessage = new NotificationMessage(
							member.getDisplayUsername().toLowerCase(), "Dungeon active!");
					notificationMessage.addData("dungeon", dungeon);
					rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
							RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(), notificationMessage);
				}
			} else {
				NotificationMessage notificationMessage = new NotificationMessage(dungeon.getOwnerUsername(),
						"Dungeon active!");
				notificationMessage.addData("dungeon", dungeon);
				rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
						RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(), notificationMessage);
			}
		}

		return JS.message(HttpStatus.OK, "Activated instance with id: %s", input.getGameSessionId());
	}

	@RequestMapping(path = "/update-instance-state", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> updateInstanceState(@Valid @RequestBody UpdateInstanceStateParameter input)
			throws IOException {
		Optional<Instance> optInstance = instanceService.getInstance(input.getGameSessionId());

		if (!optInstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find instance with id: %s", input.getGameSessionId());
		}

		InstanceState state = InstanceState.valueOf(input.getState().toUpperCase());
		Instance instance = optInstance.get();

		switch (state) {
		case FINISHED:
			instanceService.deleteInstance(instance);
			Optional<Dungeon> optDungeon = dungeonService.getDungeonByInstance(instance);
			if (optDungeon.isPresent()) {
				Dungeon dungeon = optDungeon.get();
				if (dungeon.getOwnerPartyId() != null) {
					RestResponse<PartyData> party = partyServiceClient.getPartyById(dungeon.getId());
					if (party.get().isPresent()) {
						for (PartyMemberData partyMember : party.get().get().getPartyMembers()) {
							NotificationMessage notificationMessage = new NotificationMessage(
									partyMember.getDisplayUsername().toLowerCase(),
									"dungeon changed state to finished");
							notificationMessage.addData("dungeon", dungeon);
							rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
									RabbitMQRouting.Instance.DUNGEON_FINISHED.name(), notificationMessage);
						}
					}
				} else {
					NotificationMessage notificationMessage = new NotificationMessage(dungeon.getOwnerUsername(),
							"dungeon changed state to finished");
					notificationMessage.addData("dungeon", dungeon);
					rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
							RabbitMQRouting.Instance.DUNGEON_FINISHED.name(), notificationMessage);
				}
			}
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
	public ResponseEntity<JsonNode> startDungeon(@Valid @RequestBody StartDungeonParameter input) throws IOException {

		if (!dungeonService.canCreateDungeon(input.getUsername())) {
			return JS.message(HttpStatus.BAD_REQUEST, "You cannot make a dungeon");
		}

		RestResponse<QueuePlacementDescriptionData> createQueuePlacementResponse = instanceContainerServiceClient
				.createQueuePlacement("DungeonQueue" + input.getVersion(), input.getMap(), input.getVersion(),
						input.getUsername());
		Optional<QueuePlacementDescriptionData> createQueuePlacementOpt = createQueuePlacementResponse.get();
		if (!createQueuePlacementOpt.isPresent()) {
			return JS.message(createQueuePlacementResponse);
		}

		QueuePlacementDescriptionData queuePlacementDescription = createQueuePlacementOpt.get();

		QueuePlacement queuePlacement = new QueuePlacement();
		queuePlacement.setId(queuePlacementDescription.getId());
		queuePlacement.setMapName(input.getMap());
		queuePlacement.setQueuerUsername(input.getUsername());
		queuePlacement.setStatus(queuePlacementDescription.getStatus());
		queuePlacement.setVersion(input.getVersion());

		queuePlacementService.saveQueuePlacement(queuePlacement);

		RestResponse<PartyData> party = partyServiceClient.getParty(input.getUsername());

		if (party.get().isPresent()) {
			for (PartyMemberData partyMember : party.get().get().getPartyMembers()) {
				NotificationMessage notificationMessage = new NotificationMessage(
						partyMember.getDisplayUsername().toLowerCase(), "queue placement placed");
				notificationMessage.addData("queuePlacementId", queuePlacement.getId());
				notificationMessage.addData("mapName", input.getMap());
				rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
						RabbitMQRouting.Instance.DUNGEON_QUEUED.name(), notificationMessage);
			}
		} else {
			NotificationMessage notificationMessage = new NotificationMessage(input.getUsername(),
					"queue placement placed");
			notificationMessage.addData("queuePlacementId", queuePlacement.getId());
			notificationMessage.addData("mapName", input.getMap());
			rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
					RabbitMQRouting.Instance.DUNGEON_QUEUED.name(), notificationMessage);
		}

		return JS.message(HttpStatus.OK, "Dungeon started");
	}

	@RequestMapping(path = "/instance-player-login", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> instancePlayerLogin(@Valid @RequestBody InstancePlayerLoginParameter input)
			throws IOException {
		Optional<Instance> optInstance = instanceService.getInstance(input.getGameSessionId());
		if (!optInstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find instance with id: " + input.getGameSessionId());
		}
		RestResponse<SessionData> sessionResp = personServiceClient.getSessionFromToken(input.getToken());
		Optional<SessionData> sessionOpt = sessionResp.get();
		if (!sessionOpt.isPresent()) {
			return JS.message(sessionResp);
		}
		String username = sessionOpt.get().getPerson().getUsername();

		Optional<Instance> otherInstOpt = instanceService.findInstanceByMember(username);
		if(otherInstOpt.isPresent()) {
			Instance otherInstance = otherInstOpt.get();
			otherInstance.getMembers().remove(username);
			instanceService.saveInstance(otherInstance);
		}
		Instance instance = optInstance.get();
		instance.getMembers().add(username);

		instance = instanceService.saveInstance(instance);

		NotificationMessage notificationMessage = new NotificationMessage(username, "Person logged into instance");
		notificationMessage.addData("gameSessionId", instance.getId());

		rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
				RabbitMQRouting.Instance.PERSON_LOGIN.name(), notificationMessage);

		return JS.message(HttpStatus.OK, "Player added to instance");
	}

	@RequestMapping(path = "/instance-player-logout", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> instancePlayerLogout(@Valid @RequestBody InstancePlayerLogoutParameter input) {
		Optional<Instance> optInstance = instanceService.getInstance(input.getGameSessionId());

		if (!optInstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find instance with id: " + input.getGameSessionId());
		}

		Instance instance = optInstance.get();
		instance.getMembers().remove(input.getUsername());

		instanceService.saveInstance(instance);

		rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
				RabbitMQRouting.Instance.PERSON_LOGOUT.name(),
				new NotificationMessage(input.getUsername(), "Person logged out of an instance"));

		return JS.message(HttpStatus.OK, "Player removed from instance");
	}

	@RequestMapping(path = "/get-all-instances", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<JsonNode> getAllInstances() {
		return JS.message(HttpStatus.OK, instanceService.getAllInstances());
	}

	@RequestMapping(path = "/add-local-instance", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> addLocalInstance(@Valid @RequestBody AddLocalInstanceParameter input) {
		Instance localInstance = new Instance();
		localInstance.setId(input.getGameSessionId());
		localInstance.setAddress(input.getAddress());
		localInstance.setPort(input.getPort());
		localInstance.setLevel(input.getMapName());
		localInstance.setState(input.getState());
		localInstance.setVersion(input.getVersion());

		instanceService.saveInstance(localInstance);

		return JS.message(HttpStatus.OK, "Local instance added");
	}

	@RequestMapping(path = "/get-all-players-in-same-instance", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> getAllPlayersInSameInstance(
			@Valid @RequestBody GetAllPlayersInSameInstanceParameter input) {
		Optional<Instance> optInstance = instanceService.findInstanceByMember(input.getUsername());

		if (!optInstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find instance for user :" + input.getUsername());
		}

		Instance instance = optInstance.get();
		List<String> members = instance.getMembers();
		members.remove(input.getUsername());
		return JS.message(HttpStatus.OK, members);
	}

	private ResponseEntity<JsonNode> getSession(String username, Instance instance) throws IOException {

		RestResponse<String> playerSessionResp = instanceContainerServiceClient.createPlayerSession(username,
				instance.getId());
		Optional<String> sessionIdOpt = playerSessionResp.get();
		if (sessionIdOpt.isPresent()) {
			String playerSession = sessionIdOpt.get();
			SessionAndConnectionData sac = new SessionAndConnectionData(instance.getAddress(), instance.getPort(),
					playerSession);
			return JS.message(HttpStatus.OK, sac);
		} else {
			return JS.message(HttpStatus.NOT_FOUND, "No player session available. Please try again.");
		}
	}

}
