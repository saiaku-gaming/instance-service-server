package com.valhallagame.instanceserviceserver.job;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import com.valhallagame.instancecontainerserviceclient.message.QueuePlacementDescription;
import com.valhallagame.instanceserviceserver.model.Dungeon;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.model.InstanceState;
import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import com.valhallagame.instanceserviceserver.model.QueuePlacementStatus;
import com.valhallagame.instanceserviceserver.service.DungeonService;
import com.valhallagame.instanceserviceserver.service.InstanceService;
import com.valhallagame.instanceserviceserver.service.QueuePlacementService;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.partyserviceclient.model.PartyMemberResponse;
import com.valhallagame.partyserviceclient.model.PartyResponse;

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

		RestResponse<List<QueuePlacementDescription>> queuePlacementInfo = instanceContainerServiceClient
				.getQueuePlacementInfo(allQueuePlacements.stream().map(qp -> qp.getId()).collect(Collectors.toList()));

		if (!queuePlacementInfo.isOk()) {
			return;
		}

		Map<String, QueuePlacementDescription> collect = queuePlacementInfo.getResponse().get().stream()
				.collect(Collectors.toMap(qpd -> qpd.getId(), Function.identity()));

		for (QueuePlacement queuePlacement : allQueuePlacements) {
			QueuePlacementDescription queuePlacementDescription = collect.get(queuePlacement.getId());

			if (queuePlacementDescription.getStatus().equals(QueuePlacementStatus.FULFILLED.name())) {
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

				RestResponse<PartyResponse> party = partyServiceClient.getParty(queuePlacement.getQueuerUsername());

				if (party.isOk()) {
					dungeon.setOwnerPartyId(party.getResponse().get().getId());
				} else {
					dungeon.setOwnerUsername(queuePlacement.getQueuerUsername());
				}

				dungeon.setCreatorUsername(queuePlacement.getQueuerUsername());

				dungeon = dungeonService.saveDungeon(dungeon);

				queuePlacementService.deleteQueuePlacement(queuePlacement);

				if (party.isOk()) {
					for (PartyMemberResponse member : party.getResponse().get().getPartyMembers()) {
						rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
								RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(),
								new NotificationMessage(member.getDisplayUsername().toLowerCase(), "Dungeon active!"));
					}
				} else {
					rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.INSTANCE.name(),
							RabbitMQRouting.Instance.DUNGEON_ACTIVE.name(),
							new NotificationMessage(queuePlacement.getQueuerUsername(), "Dungeon active!"));
				}
			}
		}
	}
}
