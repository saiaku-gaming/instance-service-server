package com.valhallagame.instanceserviceserver.rabbitmq;

import java.util.List;
import java.util.Optional;

import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.service.DungeonService;

@Component
public class DungeonConsumer {

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

		if(partyId == null || username == null){
			return;
		}

		List<Dungeon> dungeons = dungeonService.findActiveDungeonsByPartyId(partyId);
		if(dungeons.isEmpty()){
			return;
		}

		dungeons.sort((a, b) -> b.getId().compareTo(a.getId()));
		Dungeon dungeon = dungeons.get(0);

		NotificationMessage notificationMessage = new NotificationMessage(username, "Queue placement fulfilled!");
		notificationMessage.addData("dungeonId", dungeon.getId());
		notificationMessage.addData("dungeon", dungeon);
		notificationMessage = new NotificationMessage(username,	"Member joined party with active dungeon!");

		rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
				RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(), notificationMessage);

	}
}
