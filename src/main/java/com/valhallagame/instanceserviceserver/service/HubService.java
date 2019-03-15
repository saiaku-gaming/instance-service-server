package com.valhallagame.instanceserviceserver.service;

import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.instancecontainerserviceclient.model.FleetData;
import com.valhallagame.instanceserviceserver.model.Hub;
import com.valhallagame.instanceserviceserver.model.QueuePlacement;
import com.valhallagame.instanceserviceserver.repository.HubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Service
public class HubService {
    private static final Logger logger = LoggerFactory.getLogger(HubService.class);
    public static final String HUB_MAP = "ValhallaMap";

    private static final int SOFT_MAX = 10;

    @Autowired
    private HubRepository hubRepository;

    @Autowired
    private QueuePlacementService queuePlacementService;

    @Autowired
    private InstanceContainerServiceClient instanceContainerServiceClient;

    public Hub saveHub(Hub hub) {
        logger.info("Saving hub {}", hub);
        return hubRepository.save(hub);
    }

    public Optional<Hub> getHubWithLeastAmountOfPlayers(String version, String username) throws IOException {
        logger.info("Getting hub with least amount of players for user {} version {}", username, version);
        Optional<Hub> hubOpt = hubRepository.getHubWithLeastAmountOfPlayers(version);

        // Give it the old hub if we reached SOFT_MAX,
        // Give it the new hub if we could not find any old ones.
        if (!hubOpt.isPresent() || (hubOpt.get().getInstance().getPlayerCount() > SOFT_MAX)) {
            List<QueuePlacement> queuePlacementsFromMapName = queuePlacementService
                    .getQueuePlacementsFromMapName(HUB_MAP)
                    .stream()
                    .filter(qp -> qp.getVersion().equals(version))
                    .filter(qp -> qp.getQueuerUsername().equals(username))
                    .collect(Collectors.toList());
            if (queuePlacementsFromMapName.isEmpty()) {
                queuePlacementService.queueForInstance(version, HUB_MAP, username);

				/*
		            TODO: we need to some architectural overhaul here and start handling fleet capacity manually!
		                  Until then, this is a ugly fix to make sure that there is a fleet alive when we queue up.
		        */
                if (!hubOpt.isPresent()) {
                    instanceContainerServiceClient
                            .getFleets()
                            .get()
                            .flatMap(fleetData -> fleetData
                                    .stream()
                                    .filter(fleetData2 -> fleetData2.getVersion().equals(version))
                                    .map(FleetData::getFleetId)
                                    .findAny()
                            ).ifPresent(fleetId -> {
                        try {
                            instanceContainerServiceClient.updateFleetCapacity(fleetId, 1, 1, 1);
                            // Reset capacity 15 minutes later when the user have had time to join
                            TimerTask task = new TimerTask() {
                                public void run() {
                                    try {
                                        instanceContainerServiceClient.updateFleetCapacity(fleetId, 0, 1, null);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            Timer timer = new Timer("Timer");
                            timer.schedule(task, 15 * 1000 * 60);
                        } catch (IOException e) {
                            logger.error("failed to kickstart hub", e);
                        }
                    });
                }

            }
        }

        return hubOpt.isPresent() && hubOpt.get().getInstance().getState().equals("ACTIVE") ? hubOpt : Optional.empty();
    }
}
