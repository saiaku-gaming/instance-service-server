package com.valhallagame.instanceserviceserver.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import com.valhallagame.instanceserviceserver.repository.QueuePlacementRepository;

@Service
public class QueuePlacementService {

	@Autowired
	private QueuePlacementRepository queuePlacementRepository;

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
}
