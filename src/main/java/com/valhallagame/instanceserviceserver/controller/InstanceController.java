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

import com.valhallagame.instanceserviceserver.message.InstanceAndOwnerParameter;
import com.valhallagame.instanceserviceserver.message.InstanceNameParameter;
import com.valhallagame.instanceserviceserver.message.UsernameParameter;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.service.InstanceService;
import com.valhallagame.common.JS;

@Controller
@RequestMapping(path = "/v1/instance")
public class InstanceController {

	@Autowired
	private InstanceService instanceService;

	@RequestMapping(path = "/get", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> getInstance(@RequestBody InstanceAndOwnerParameter instanceAndOwner) {
		Optional<Instance> optinstance = instanceService.getInstance(instanceAndOwner.getInstanceName());
		if (!optinstance.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "No instance with that instance name was found!");
		}

		Instance instance = optinstance.get();
		if (!instance.getOwner().equals(instanceAndOwner.getOwner())) {
			return JS.message(HttpStatus.NOT_FOUND, "Wrong owner!");
		}
		return JS.message(HttpStatus.OK, optinstance.get());
	}

	@RequestMapping(path = "/get-all", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> getAll(@RequestBody UsernameParameter username) {
		return JS.message(HttpStatus.OK, instanceService.getInstances(username.getUsername()));
	}

	@RequestMapping(path = "/create", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> save(@RequestBody Instance instanceData) {

		String charName = instanceData.getInstanceName();
		Optional<Instance> localOpt = instanceService.getInstance(charName);
		if (!localOpt.isPresent()) {
			Instance c = new Instance();
			c.setOwner(instanceData.getOwner());
			c.setDisplayInstanceName(instanceData.getInstanceName());
			c.setInstanceName(instanceData.getInstanceName().toLowerCase());
			c.setChestItem("LeatherArmor");
			c.setMainhandArmament("Sword");
			c.setOffHandArmament("MediumShield");
			instanceService.saveInstance(c);
		} else {
			return JS.message(HttpStatus.CONFLICT, "Instance already exists.");
		}
		return JS.message(HttpStatus.OK, "OK");
	}

	@RequestMapping(path = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> delete(@RequestBody InstanceAndOwnerParameter instanceAndOwner) {
		String owner = instanceAndOwner.getOwner();
		Optional<Instance> localOpt = instanceService.getInstance(instanceAndOwner.getInstanceName());
		if (!localOpt.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Not found");
		}

		Instance local = localOpt.get();

		if (owner.equals(local.getOwner())) {
			// Randomly(ish) select a new instance as default instance if the
			// person has one.
			Optional<Instance> selectedInstanceOpt = instanceService.getSelectedInstance(owner);
			if (selectedInstanceOpt.isPresent() && selectedInstanceOpt.get().equals(local)) {
				instanceService.getInstances(owner).stream().filter(x -> !x.equals(local)).findAny().ifPresent(ch -> {
					instanceService.setSelectedInstance(owner, ch.getInstanceName());
				});
			}
			instanceService.deleteInstance(local);
			return JS.message(HttpStatus.OK, "Deleted instance");
		} else {
			return JS.message(HttpStatus.FORBIDDEN, "No access");
		}
	}

	@RequestMapping(path = "/instance-available", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> instanceAvailable(@RequestBody InstanceNameParameter input) {
		if(input.getInstanceName() == null || input.getInstanceName().isEmpty()){
			return JS.message(HttpStatus.BAD_REQUEST, "Missing instanceName field");
		}
		Optional<Instance> localOpt = instanceService.getInstance(input.getInstanceName());
		if (localOpt.isPresent()) {
			return JS.message(HttpStatus.CONFLICT, "Instance not available");
		} else {
			return JS.message(HttpStatus.OK, "Instance available");
		}
	}

	@RequestMapping(path = "/select-instance", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> selectInstance(@RequestBody InstanceAndOwnerParameter instanceAndOwner) {
		Optional<Instance> localOpt = instanceService.getInstance(instanceAndOwner.getInstanceName());
		if (!localOpt.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND,
					"Instance with name " + instanceAndOwner.getInstanceName() + " was not found.");
		} else {
			if (!localOpt.get().getOwner().equals(instanceAndOwner.getOwner())) {
				return JS.message(HttpStatus.FORBIDDEN, "You don't own that instance.");
			}
			instanceService.setSelectedInstance(instanceAndOwner.getOwner(), instanceAndOwner.getInstanceName());
			return JS.message(HttpStatus.OK, "Instance selected");
		}
	}

	@RequestMapping(path = "/get-selected-instance", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> getSelectedInstance(@RequestBody UsernameParameter username) {
		Optional<Instance> selectedInstance = instanceService.getSelectedInstance(username.getUsername());
		if (selectedInstance.isPresent()) {
			return JS.message(HttpStatus.OK, selectedInstance);
		} else {
			return JS.message(HttpStatus.NOT_FOUND, "No instance selected");
		}
	}

}
