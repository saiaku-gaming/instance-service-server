package com.valhallagame.instanceserviceserver.service;

import java.io.IOException;
import java.util.Optional;

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

	public Optional<Instance> getInstance(String id) {
		return instanceRepository.findInstanceById(id);
	}

	public Optional<Instance> createInstance(String level, String version) throws IOException {
		RestResponse<String> createInstance = instanceContainerServiceClient.createInstance(level, version);
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
}
