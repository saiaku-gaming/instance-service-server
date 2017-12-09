package com.valhallagame.instanceserviceserver.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.repository.InstanceRepository;

@Service
public class InstanceService {
	@Autowired
	private InstanceRepository instanceRepository;

	public Optional<Instance> getSelectedInstance(String owner) {
		return instanceRepository.getSelectedInstance(owner.toLowerCase());
	}

	public void deleteInstance(Instance local) {
		instanceRepository.delete(local);
	}

	public Optional<Instance> getInstance(String member) {
		return instanceRepository.getInstance(member);
	}

	public Optional<Instance> getHubWithLeastAmountOfPlayers(String version) {
		return Optional.empty();
	}

	public void removeInstanceFromPerson(String username) {
	}

	public void setInstance(String username) {
	
	}

	public Optional<Instance> getInstanceByPerson(String leader) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
