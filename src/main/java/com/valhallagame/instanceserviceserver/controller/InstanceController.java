package com.valhallagame.instanceserviceserver.controller;

import java.io.IOException;
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
import com.valhallagame.instanceserviceserver.message.ActivateInstanceParameter;
import com.valhallagame.instanceserviceserver.message.DeactivateInstanceParameter;
import com.valhallagame.instanceserviceserver.message.GetPlayerSessionAndConnectionParameter;
import com.valhallagame.instanceserviceserver.message.SessionAndConnectionResponse;
import com.valhallagame.instanceserviceserver.message.StartDungeonParameter;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Hub;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.InstanceState;
import com.valhallagame.instanceserviceserver.service.DungeonService;
import com.valhallagame.instanceserviceserver.service.HubService;
import com.valhallagame.instanceserviceserver.service.InstanceService;
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
	private RabbitTemplate rabbitTemplate;

	@RequestMapping(path = "/get-player-session-and-connection", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> getPlayerSessionAndConnection(@RequestBody GetPlayerSessionAndConnectionParameter input)
			throws IOException {

		String username = input.getUsername();
		String version = input.getVersion();

		if (input.getVersion() == null || input.getVersion().isEmpty()) {
			return JS.message(HttpStatus.BAD_REQUEST, "Missing game version");
		}

		if (username == null || username.isEmpty()) {
			return JS.message(HttpStatus.BAD_REQUEST, "Missing username");
		}

		RestResponse<PartyResponse> partyResp = partyServiceClient.getParty(username);
		if (partyResp.isOk()) {
			PartyResponse party = partyResp.getResponse().get();

			Optional<Instance> insOpt = instanceService.getSelectedInstance(party.getLeader().getUsername(), version);
			if (correctVersionAndActive(insOpt, version)) {
				return getSession(username, insOpt.get());
			}
		}

		// If user is not in a party but was playing an instance that has yet
		// not died.
		Optional<Instance> insOpt = instanceService.getSelectedInstance(username, version);
		if (insOpt.isPresent()) {
			if (correctVersionAndActive(insOpt, version)) {
				return getSession(username, insOpt.get());
			}

			return JS.message(HttpStatus.NOT_FOUND, "Selected instance is not active. Please try again.");
		}

		// Find the hub with the lowest numbers of players in it.
		Optional<Hub> hubOpt = hubService.getHubWithLeastAmountOfPlayers(version);
		if (hubOpt.isPresent()) {
			Optional<Instance> instOpt = Optional.ofNullable(hubOpt.get().getInstance());
			if (instOpt.isPresent()) {
				instanceService.setSelectedInstance(username, instOpt.get());
			}
			if (correctVersionAndActive(instOpt, version)) {
				return getSession(username, instOpt.get());
			}
		}
		return JS.message(HttpStatus.NOT_FOUND, "No instance found. Please try again.");
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

			instanceService.setSelectedInstance(dungeon.getOwner(), instance);

			RestResponse<PartyResponse> partyResponse = partyServiceClient.getParty(dungeon.getOwner());
			if (partyResponse.isOk()) {
				PartyResponse party = partyResponse.getResponse().get();
				for (PartyMemberResponse member : party.getPartyMembers()) {
					rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
							RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(),
							new NotificationMessage(member.getUsername(), "Dungeon active!"));
				}
			} else {
				rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
						RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(),
						new NotificationMessage(dungeon.getOwner(), "Dungeon active!"));
			}
		}

		return JS.message(HttpStatus.OK, "Activated instance with id: " + input.getGameSessionId());
	}

	@RequestMapping(path = "/deactivate-instance", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> deactivateInstance(@RequestBody DeactivateInstanceParameter input) throws IOException {
		Optional<Instance> optInstance = instanceService.getInstance(input.getGameSessionId());

		if (!optInstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Could not find instance with id: " + input.getGameSessionId());
		}

		instanceService.deleteInstance(optInstance.get());

		return JS.message(HttpStatus.OK, "Deactivated instance with id: " + input.getGameSessionId());
	}

	@RequestMapping(path = "/start-dungeon", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> startDungeon(@RequestBody StartDungeonParameter input) throws IOException {
		Optional<Dungeon> optDungeon = dungeonService.getDungeonByOwner(input.getUsername());

		if (optDungeon.isPresent()) {
			return JS.message(HttpStatus.CONFLICT, "The user already has a dungeon");
		}

		RestResponse<String> instanceCreateResponse = instanceContainerServiceClient.createInstance(input.getMap(),
				input.getVersion());

		if (!instanceCreateResponse.isOk()) {
			return JS.message(instanceCreateResponse);
		}

		Instance instance = new Instance();
		instance.setId(instanceCreateResponse.getResponse().get());
		instance.setLevel(input.getMap());
		instance.setPlayerCount(0);
		instance.setState(InstanceState.STARTING.name());
		instance.setVersion(input.getVersion());

		instance = instanceService.saveInstance(instance);

		Dungeon dungeon = new Dungeon();
		dungeon.setInstance(instance);
		dungeon.setOwner(input.getUsername());

		dungeon = dungeonService.saveDungeon(dungeon);

		return JS.message(HttpStatus.OK, "Dungeon started");
	}

	private boolean correctVersionAndActive(Optional<Instance> insOpt, final String version) {
		return insOpt.map(ins -> {
			boolean ready = ins.getState().equals(InstanceState.ACTIVE.name());
			boolean sameVersion = ins.getVersion().equals(version);
			return ready && sameVersion;
		}).orElse(false);
	}

	private ResponseEntity<?> getSession(String username, Instance instance) throws IOException {

		RestResponse<String> playerSessionResp = instanceContainerServiceClient.createPlayerSession(username,
				instance.getId());
		if (playerSessionResp.isOk()) {
			String playerSession = playerSessionResp.getResponse().get();
			SessionAndConnectionResponse sac = new SessionAndConnectionResponse(instance.getAddress(),
					instance.getPort(), playerSession);
			instanceService.setSelectedInstance(username, instance);
			return JS.message(HttpStatus.OK, sac);
		} else {
			return JS.message(HttpStatus.NOT_FOUND, "No player session available. Please try again.");
		}
	}

}
