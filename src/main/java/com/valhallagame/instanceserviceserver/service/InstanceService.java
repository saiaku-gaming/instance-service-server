package com.valhallagame.instanceserviceserver.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.common.RestResponse;
import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.InstanceState;
import com.valhallagame.instanceserviceserver.repository.InstanceRepository;

@Service
public class InstanceService {
	
	private static final Logger logger = LoggerFactory.getLogger(InstanceService.class);
	
	@Autowired
	private InstanceRepository instanceRepository;

	private static InstanceContainerServiceClient instanceContainerServiceClient = InstanceContainerServiceClient.get();

	public Instance saveInstance(Instance instance) {
		return instanceRepository.save(instance);
	}

	public void deleteInstance(Instance local) {
		instanceRepository.delete(local);
	}

	public void deleteInstance(String instanceId) {
		instanceRepository.delete(instanceId);
	}

	public Optional<Instance> getInstance(String id) {
		return instanceRepository.findInstanceById(id);
	}

	public Optional<Instance> createInstance(String level, String version, String creatorId) throws IOException {
		RestResponse<String> gameSessionIdResp = instanceContainerServiceClient.createInstance(level, version, creatorId);
		Optional<String> gameSessionIdOpt = gameSessionIdResp.get();
		if (gameSessionIdOpt.isPresent()) {
			String gameSessionId = gameSessionIdOpt.get();

			Instance instance = new Instance();
			instance.setId(gameSessionId);
			instance.setLevel(level);
			instance.setState(InstanceState.STARTING.name());
			instance.setVersion(version);

			instance = saveInstance(instance);
			return Optional.of(instance);
		}
		return Optional.empty();
	}

	public List<Instance> getAllInstances() {
		return instanceRepository.findAll();
	}

	public Optional<Instance> findInstanceByMember(String username) {
		return instanceRepository.findInstanceByMembers(username);
	}

	public void syncInstances() throws IOException {
		RestResponse<List<String>> gameSessionsResp = instanceContainerServiceClient.getGameSessions();
		Optional<List<String>> gameSessionsOpt = gameSessionsResp.get();
		if (!gameSessionsOpt.isPresent()) {
			logger.error("Unable to get all game sessions from instance container service");
			return;
		}

		List<String> allInstancesIds = getAllInstances().stream().map(Instance::getId).collect(Collectors.toList());
		List<String> gameSessionIds = gameSessionsOpt.get();
		allInstancesIds.removeAll(gameSessionIds);

		for (String instanceId : allInstancesIds) {
			deleteInstance(instanceId);
		}
	}
}
