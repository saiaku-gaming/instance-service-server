package com.valhallagame.instanceserviceserver.rabbitmq;

import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.service.DungeonService;

@Component
public class DungeonConsumer {

	@Autowired
	private DungeonService dungeonService;

	@RabbitListener(queues = "#{partyCreatedQueue.name}")
	public void receivePartyCreated(NotificationMessage message) {
		Optional<Dungeon> dungeonOpt = dungeonService.getDungeonFromOwnerUsername(message.getUsername());
		if (dungeonOpt.isPresent()) {
			Dungeon dungeon = dungeonOpt.get();
			dungeon.setOwnerPartyId((Integer) message.getData().get("partyId"));
			dungeon.setOwnerUsername(null);
			dungeonService.saveDungeon(dungeon);
		}
	}
}
