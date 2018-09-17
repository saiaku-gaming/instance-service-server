package com.valhallagame.instanceserviceserver.service;

import com.valhallagame.common.RestResponse;
import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.instancecontainerserviceclient.model.QueuePlacementDescriptionData;
import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import com.valhallagame.instanceserviceserver.repository.QueuePlacementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class QueuePlacementService {

	Logger logger = LoggerFactory.getLogger(QueuePlacementService.class);

	@Autowired
	private QueuePlacementRepository queuePlacementRepository;

	@Autowired
	private InstanceContainerServiceClient instanceContainerServiceClient;

	public QueuePlacement saveQueuePlacement(QueuePlacement queuePlacement) {
		return queuePlacementRepository.save(queuePlacement);
	}

	public void deleteQueuePlacement(QueuePlacement queuePlacement) {
		queuePlacementRepository.delete(queuePlacement);
	}

	public List<QueuePlacement> getAllQueuePlacements() {
		return queuePlacementRepository.findAll();
	}

	public Optional<QueuePlacement> getQueuePlacementFromQueuer(String queuerUsername) {
		return queuePlacementRepository.findQueuePlacementByQueuerUsername(queuerUsername);
	}

	public List<QueuePlacement> getQueuePlacementsFromMapName(String mapName) {
		return queuePlacementRepository.findQueuePlacementsByMapName(mapName);
	}

	public Optional<QueuePlacement> queueForInstance(String version, String map, String username) throws IOException {
		RestResponse<QueuePlacementDescriptionData> createQueuePlacementResponse = instanceContainerServiceClient
				.createQueuePlacement("DungeonQueue" + version, map, version, username);

		if(!createQueuePlacementResponse.isOk()) {
			logger.error("Could not create que placement for version: {}, map: {}, username: {}. Got response {}",
					version, map, username, createQueuePlacementResponse);
			return Optional.empty();
		}

		Optional<QueuePlacementDescriptionData> createQueuePlacementOpt = createQueuePlacementResponse.get();
		if (!createQueuePlacementOpt.isPresent()) {
			logger.error("Could not create que placement for version: {}, map: {}, username: {}. Got response {}",
					version, map, username, createQueuePlacementResponse);
			return Optional.empty();
		}

		QueuePlacementDescriptionData queuePlacementDescription = createQueuePlacementOpt.get();

		QueuePlacement queuePlacement = new QueuePlacement();
		queuePlacement.setId(queuePlacementDescription.getId());
		queuePlacement.setMapName(map);
		queuePlacement.setQueuerUsername(username);
		queuePlacement.setStatus(queuePlacementDescription.getStatus());
		queuePlacement.setVersion(version);
		queuePlacement.setTimestamp(Instant.now());

		return Optional.ofNullable(saveQueuePlacement(queuePlacement));
	}

	/**
	 * Removes queue wrong version so that a updated user never tries to connect to
	 * an old server.
	 */
	public void removeOldQueues(String version, String username) {
		queuePlacementRepository.findQueuePlacementByQueuerUsername(username).ifPresent(q -> {
			if (!q.getVersion().equals(version)) {
				queuePlacementRepository.delete(q);
			}
		});
	}
}
