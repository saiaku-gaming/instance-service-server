package com.valhallagame.instanceserviceserver.rabbitmq;

import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.service.DungeonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DungeonConsumer {

	private static final Logger logger = LoggerFactory.getLogger(DungeonConsumer.class);

	@Autowired
	private DungeonService dungeonService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

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

	/**
	 * This is here to re-trigger the dungeon active to new players joining a party.
	 * If there is several dungeons it will only get the latest created.
	 */
	@RabbitListener(queues = "#{partyInviteAcceptedQueue.name}")
	public void receivePartyInviteAccepted(NotificationMessage message) {
		Integer partyId = (Integer) message.getData().get("partyId");
		String username = (String) message.getData().get("newUsername");

		// newUsername is the player that got the invite, we only want to send
		// new dungeon data to that user.
		if(!message.getUsername().equals(username)){
			return;
		}

		if(partyId == null){
			return;
		}

		List<Dungeon> dungeons = dungeonService.findActiveDungeonsByPartyId(partyId);
		if(dungeons.isEmpty()){
			return;
		}

		dungeons.sort((a, b) -> b.getId().compareTo(a.getId()));
		Dungeon dungeon = dungeons.get(0);

		logger.info("Sending new partymember {} a dungeon {}", username, dungeon);

		NotificationMessage notificationMessage = new NotificationMessage(username, "Member joined party with active dungeon!");
		notificationMessage.addData("dungeon", dungeon);
		rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
				RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(), notificationMessage);

	}
}
