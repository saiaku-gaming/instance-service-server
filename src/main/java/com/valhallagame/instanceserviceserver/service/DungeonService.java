package com.valhallagame.instanceserviceserver.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.repository.DungeonRepository;

@Service
public class DungeonService {

	@Autowired
	private DungeonRepository dungeonRepository;

	public Dungeon saveDungeon(Dungeon dungeon) {
		return dungeonRepository.save(dungeon);
	}

	public void deleteDungeon(Dungeon dungeon) {
		dungeonRepository.delete(dungeon);
	}

	public Optional<Dungeon> getDungeonByOwner(String owner) {
		return dungeonRepository.findDungeonByOwner(owner);
	}

	public Optional<Dungeon> getDungeonByInstance(Instance instance) {
		return dungeonRepository.findDungeonByInstanceId(instance.getId());
	}
}
