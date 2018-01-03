package com.valhallagame.instanceserviceserver.job;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.valhallagame.common.RestResponse;
import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.instancecontainerserviceclient.model.QueuePlacementDescriptionData;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.InstanceState;
import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import com.valhallagame.instanceserviceserver.model.QueuePlacementStatus;
import com.valhallagame.instanceserviceserver.service.DungeonService;
import com.valhallagame.instanceserviceserver.service.InstanceService;
import com.valhallagame.instanceserviceserver.service.QueuePlacementService;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.partyserviceclient.model.PartyData;
import com.valhallagame.partyserviceclient.model.PartyMemberData;

@Component
public class QueuePlacementJob {

	@Autowired
	private QueuePlacementService queuePlacementService;

	@Autowired
	private DungeonService dungeonService;

	@Autowired
	private InstanceService instanceService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	private static InstanceContainerServiceClient instanceContainerServiceClient = InstanceContainerServiceClient.get();
	private static PartyServiceClient partyServiceClient = PartyServiceClient.get();

	@Scheduled(fixedRate = 1000, initialDelay = 1000)
	public void execute() throws IOException {
		List<QueuePlacement> allQueuePlacements = queuePlacementService.getAllQueuePlacements();

		if (allQueuePlacements.isEmpty()) {
			return;
		}

		RestResponse<List<QueuePlacementDescriptionData>> queuePlacementInfoResp = instanceContainerServiceClient
				.getQueuePlacementInfo(
						allQueuePlacements.stream().map(QueuePlacement::getId).collect(Collectors.toList()));
		Optional<List<QueuePlacementDescriptionData>> queuePlacementInfoOpt = queuePlacementInfoResp.get();

		if (!queuePlacementInfoOpt.isPresent()) {
			return;
		}

		Map<String, QueuePlacementDescriptionData> collect = queuePlacementInfoOpt.get().stream()
				.collect(Collectors.toMap(QueuePlacementDescriptionData::getId, Function.identity()));

		for (QueuePlacement queuePlacement : allQueuePlacements) {
			QueuePlacementDescriptionData queuePlacementDescription = collect.get(queuePlacement.getId());

			if (queuePlacementDescription.getStatus().equals(QueuePlacementStatus.FULFILLED.name())) {
				saveInstanceAndNotify(queuePlacement, queuePlacementDescription);
			}
		}
	}

	private void saveInstanceAndNotify(QueuePlacement queuePlacement,
			QueuePlacementDescriptionData queuePlacementDescription) throws IOException {
		Instance instance = new Instance();
		instance.setId(queuePlacementDescription.getGameSessionId());
		instance.setAddress(queuePlacementDescription.getAddress());
		instance.setLevel(queuePlacement.getMapName());
		instance.setPort(queuePlacementDescription.getPort());
		instance.setState(InstanceState.ACTIVE.name());
		instance.setVersion(queuePlacement.getVersion());

		instance = instanceService.saveInstance(instance);

		Dungeon dungeon = new Dungeon();
		dungeon.setInstance(instance);

		RestResponse<PartyData> partyResp = partyServiceClient.getParty(queuePlacement.getQueuerUsername());
		Optional<PartyData> partyOpt = partyResp.get();
		if (partyOpt.isPresent()) {
			dungeon.setOwnerPartyId(partyOpt.get().getId());
		} else {
			dungeon.setOwnerUsername(queuePlacement.getQueuerUsername());
		}

		dungeon.setCreatorUsername(queuePlacement.getQueuerUsername());

		dungeonService.saveDungeon(dungeon);

		queuePlacementService.deleteQueuePlacement(queuePlacement);

		if (partyOpt.isPresent()) {
			for (PartyMemberData member : partyOpt.get().getPartyMembers()) {
				NotificationMessage notificationMessage = new NotificationMessage(
						member.getDisplayUsername().toLowerCase(), "Queue placement fulfilled!");

				notificationMessage.addData("dungeonId", dungeon.getId());
				notificationMessage.addData("queueId", queuePlacement.getId());

				rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
						RabbitMQRouting.Instance.QUEUE_PLACEMENT_FULFILLED.name(), notificationMessage);

				notificationMessage = new NotificationMessage(member.getDisplayUsername().toLowerCase(),
						"Dungeon active!");

				notificationMessage.addData("dungeon", dungeon);

				rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
						RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(), notificationMessage);
			}
		} else {
			NotificationMessage notificationMessage = new NotificationMessage(queuePlacement.getQueuerUsername(),
					"Queue placement fulfilled!");

			notificationMessage.addData("dungeonId", dungeon.getId());
			notificationMessage.addData("queueId", queuePlacement.getId());

			rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
					RabbitMQRouting.Instance.QUEUE_PLACEMENT_FULFILLED.name(), notificationMessage);

			notificationMessage = new NotificationMessage(queuePlacement.getQueuerUsername(), "Dungeon active!");

			notificationMessage.addData("dungeon", dungeon);

			rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
					RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(), notificationMessage);
		}
	}
}
