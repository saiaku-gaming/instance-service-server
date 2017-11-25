package com.valhallagame.instanceserviceserver.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.repository.InstanceRepository;

@Service
public class InstanceService {
	@Autowired
	private InstanceRepository instanceRepository;

	public Instance saveInstance(Instance instance) {
		return instanceRepository.save(instance);
	}

	public Optional<Instance> getInstance(String instanceName) {
		return instanceRepository.findByInstanceName(instanceName.toLowerCase());
	}
	
	public List<Instance> getInstances(String username) {
		return instanceRepository.findByOwner(username.toLowerCase());
	}

	public void setSelectedInstance(String owner, String instanceName) {
		instanceRepository.setSelectedInstance(owner.toLowerCase(), instanceName.toLowerCase());
	}
	
	public Optional<Instance> getSelectedInstance(String owner) {
		return instanceRepository.getSelectedInstance(owner.toLowerCase());
	}

	public void deleteInstance(Instance local) {
		instanceRepository.delete(local);
	}
}
