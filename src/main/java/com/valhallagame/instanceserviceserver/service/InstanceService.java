package com.valhallagame.instanceserviceserver.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.common.RestResponse;
import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.InstanceState;
import com.valhallagame.instanceserviceserver.repository.InstanceRepository;

@Service
public class InstanceService {
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
		RestResponse<String> createInstance = instanceContainerServiceClient.createInstance(level, version, creatorId);
		if (createInstance.isOk()) {
			String gameSessionId = createInstance.getResponse().get();

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
		RestResponse<List<String>> responseGameSessions = instanceContainerServiceClient.getGameSessions();
		if (!responseGameSessions.isOk()) {
			System.err.println("Unable to get all game sessions from instance container service");
			return;
		}

		List<String> allInstancesIds = getAllInstances().stream().map(i -> i.getId()).collect(Collectors.toList());
		List<String> gameSessionIds = responseGameSessions.getResponse().get();
		allInstancesIds.removeAll(gameSessionIds);

		for (String instanceId : allInstancesIds) {
			deleteInstance(instanceId);
		}
	}
}
