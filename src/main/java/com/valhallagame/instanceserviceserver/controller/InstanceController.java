package com.valhallagame.instanceserviceserver.controller;

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
import com.valhallagame.instanceserviceserver.message.UsernameAndVersionParameter;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.InstanceState;
import com.valhallagame.instanceserviceserver.service.InstanceService;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.partyserviceclient.model.Party;

@Controller
@RequestMapping(path = "/v1/instance")
public class InstanceController {

	@Autowired
	private InstanceService instanceService;

	@RequestMapping(path = "/get-selected-instance", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> getSelectedInstance(@RequestBody UsernameAndVersionParameter usernameAndVersion) {

		String username = usernameAndVersion.getUsername();
		String version = usernameAndVersion.getVersion();

		Optional<Instance> selectedInstance = instanceService.getSelectedInstance(username);

		if (usernameAndVersion.getVersion() == null || usernameAndVersion.getVersion().isEmpty()) {
			return JS.message(HttpStatus.BAD_REQUEST, "Missing game version");
		}

		PartyServiceClient partyServiceClient = PartyServiceClient.get();

		RestResponse<Party> partyResp = partyServiceClient.getParty(username);
		if (partyResp.isOk()) {
			Party party = partyResp.getResponse().get();

			Optional<Instance> insOpt = instanceService.getInstanceByPerson(party.getLeader());
			if (insOpt.isPresent()) {
				Instance ins = insOpt.get();
				if (ins.getState().equals(InstanceState.READY.name()) && ins.getVersion().equals(version)) {
					return JS.message(HttpStatus.OK, ins);
				}
			}
		}

		// If user is not in a party but was playing an instance that has yet
		// not died.
		Optional<Instance> insOpt = instanceService.getInstanceByPerson(username);
		if (insOpt.isPresent()) {
			Instance hub = insOpt.get();
			if (hub.getState().equals(InstanceState.READY.name()) && hub.getVersion().equals(version)) {
				return JS.message(HttpStatus.OK, insOpt.get());
			}
		}

		// Find the hub with the lowest numbers of players on it.
		Optional<Instance> hubOpt = instanceService.getHubWithLeastAmountOfPlayers(version);

		if (hubOpt.isPresent()) {
			return JS.message(HttpStatus.OK, hubOpt.get());
		} else {
			return JS.message(HttpStatus.NOT_FOUND, "No instance selected");
		}
	}

}
