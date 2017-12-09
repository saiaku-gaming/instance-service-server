package com.valhallagame.instanceserviceserver.controller;

import java.io.IOException;
import java.util.Optional;

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
import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.instanceserviceserver.message.SessionAndConnectionResponse;
import com.valhallagame.instanceserviceserver.message.UsernameAndVersionParameter;
import com.valhallagame.instanceserviceserver.model.Hub;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.InstanceState;
import com.valhallagame.instanceserviceserver.service.HubService;
import com.valhallagame.instanceserviceserver.service.InstanceService;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.partyserviceclient.model.Party;

@Controller
@RequestMapping(path = "/v1/instance")
public class InstanceController {

	private static InstanceContainerServiceClient instanceContainerServiceClient = InstanceContainerServiceClient.get();
	
	@Autowired
	private InstanceService instanceService;
	
	@Autowired
	private HubService hubService;

	
	@RequestMapping(path = "/get-player-session", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> getPlayerSession(@RequestBody UsernameAndVersionParameter usernameAndVersion) throws IOException {

		String username = usernameAndVersion.getUsername();
		String version = usernameAndVersion.getVersion();

		if (usernameAndVersion.getVersion() == null || usernameAndVersion.getVersion().isEmpty()) {
			return JS.message(HttpStatus.BAD_REQUEST, "Missing game version");
		}
		
		if (username == null || username.isEmpty()) {
			return JS.message(HttpStatus.BAD_REQUEST, "Missing username");
		}
		
		PartyServiceClient partyServiceClient = PartyServiceClient.get();

		RestResponse<Party> partyResp = partyServiceClient.getParty(username);
		if (partyResp.isOk()) {
			Party party = partyResp.getResponse().get();

			Optional<Instance> insOpt = instanceService.getSelectedInstance(party.getLeader());
			if (correctVersionAndReady(insOpt, version)) {
				return getSession(username, insOpt.get());
			}
		}

		// If user is not in a party but was playing an instance that has yet
		// not died.
		Optional<Instance> insOpt = instanceService.getSelectedInstance(username);
		if (correctVersionAndReady(insOpt, version)) {
			return getSession(username, insOpt.get());
		}

		// Find the hub with the lowest numbers of players on it.
		Optional<Hub> hubOpt = hubService.getHubWithLeastAmountOfPlayers(version);
		if(hubOpt.isPresent()) {
			Optional<Instance> instOpt = Optional.ofNullable(hubOpt.get().getInstance());
			if (correctVersionAndReady(instOpt, version)) {
				return getSession(username, instOpt.get());
			} 
		}
		return JS.message(HttpStatus.NOT_FOUND, "No instance found. Please try again.");
	}

	private boolean correctVersionAndReady(Optional<Instance> insOpt, final String version) {
		return insOpt.map(ins -> {
			boolean ready = ins.getState().equals(InstanceState.READY.name());
			boolean sameVersion = ins.getVersion().equals(version);
			return ready && sameVersion;
		}).orElse(false);
	}

	private ResponseEntity<?> getSession(String username, Instance ins) throws IOException {

		RestResponse<String> playerSessionResp = instanceContainerServiceClient.createPlayerSession(username, ins.getId());
		if(playerSessionResp.isOk()) {
			String playerSession = playerSessionResp.getResponse().get();
			SessionAndConnectionResponse sac = new SessionAndConnectionResponse(ins.getAddress(), ins.getPort(), playerSession);
			instanceService.setInstance(username);
			return JS.message(HttpStatus.OK, sac);
		} else {
			return JS.message(HttpStatus.NOT_FOUND, "No player session available. Please try again.");
		}
	}

}
