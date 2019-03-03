package com.valhallagame.instanceserviceserver.rabbitmq;

import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.service.DungeonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DungeonConsumer {

	private static final Logger logger = LoggerFactory.getLogger(DungeonConsumer.class);

	@Autowired
	private DungeonService dungeonService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Value("${spring.application.name}")
	private String appName;

	@RabbitListener(queues = "#{partyCreatedQueue.name}")
    @Transactional
	public void receivePartyCreated(NotificationMessage message) {
		MDC.put("service_name", appName);
		MDC.put("request_id", message.getData().get("requestId") != null ? (String)message.getData().get("requestId") : UUID.randomUUID().toString());

		logger.info("Received Party Created with message {}", message);

		try {
			Optional<Dungeon> dungeonOpt = dungeonService.getDungeonFromOwnerUsername(message.getUsername());
			if (dungeonOpt.isPresent()) {
				Dungeon dungeon = dungeonOpt.get();
				dungeon.setOwnerPartyId((Integer) message.getData().get("partyId"));
				dungeon.setOwnerUsername(null);
				dungeonService.saveDungeon(dungeon);
				logger.info("Received party created and a dungeon was present {}", message);
				String secondPartyMember = (String) message.getData().get("secondPartyMember");
				if (secondPartyMember != null && dungeon.getInstance().getState().equals("ACTIVE")) {
					logger.info("Sending new partymember {} a dungeon {}", secondPartyMember, dungeon);
					NotificationMessage notificationMessage = new NotificationMessage(secondPartyMember, "Member joined newly created party with active dungeon!");
					notificationMessage.addData("dungeon", dungeon);
					rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
							RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(), notificationMessage);
				}
			}
		} finally {
			MDC.clear();
		}
	}

	/**
	 * This is here to re-trigger the dungeon active to new players joining a party.
	 * If there is several dungeons it will only get the latest created.
	 */
	@RabbitListener(queues = "#{partyInviteAcceptedQueue.name}")
    @Transactional
	public void receivePartyInviteAccepted(NotificationMessage message) {
		MDC.put("service_name", appName);
		MDC.put("request_id", message.getData().get("requestId") != null ? (String)message.getData().get("requestId") : UUID.randomUUID().toString());

        logger.info("Party invite recorded {}", message);

        try {
			Integer partyId = (Integer) message.getData().get("partyId");
			String username = (String) message.getData().get("newUsername");

			// newUsername is the player that got the invite, we only want to send
			// new dungeon data to that user.
			if(!message.getUsername().equals(username)){
				logger.info("Not interested in this person {}", message);
				return;
			}

			if(partyId == null){
				logger.error("Could not find partyId? Strange {}", message);
				return;
			}

			List<Dungeon> dungeons = dungeonService.findActiveDungeonsByPartyId(partyId);
			if(dungeons.isEmpty()){
				logger.info("This party has no dungeon and therefor nothing to react to! {}", message);
				return;
			}

			dungeons.sort((a, b) -> b.getId().compareTo(a.getId()));
			Dungeon dungeon = dungeons.get(0);

			logger.info("Sending new partymember {} a dungeon {}", username, dungeon);

			NotificationMessage notificationMessage = new NotificationMessage(username, "Member joined party with active dungeon!");
			notificationMessage.addData("dungeon", dungeon);
			rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
					RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(), notificationMessage);
		} finally {
        	MDC.clear();
		}
	}
}
