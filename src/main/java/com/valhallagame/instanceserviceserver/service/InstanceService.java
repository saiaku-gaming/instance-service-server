package com.valhallagame.instanceserviceserver.service;

import com.valhallagame.common.RestResponse;
import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.instanceserviceserver.model.Instance;
import com.valhallagame.instanceserviceserver.repository.InstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class InstanceService {

    private static final Logger logger = LoggerFactory.getLogger(InstanceService.class);

    private static ConcurrentMap<String, Instant> developmentInstances = new ConcurrentHashMap<>();

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private InstanceContainerServiceClient instanceContainerServiceClient;

    public Instance saveInstance(Instance instance) {
        return instanceRepository.save(instance);
    }

    public void deleteInstance(Instance local) {
        logger.info("Deleting instance {}", local.getId());
        developmentInstances.remove(local.getId());
        instanceRepository.delete(local);
    }

    private void deleteInstance(String instanceId) {
        logger.info("Deleting instance {}", instanceId);
        developmentInstances.remove(instanceId);
        instanceRepository.delete(instanceId);
    }

    public Optional<Instance> getInstance(String id) {
        return instanceRepository.findInstanceById(id);
    }

    public List<Instance> getAllInstances() {
        return instanceRepository.findAll();
    }

    public Optional<Instance> findInstanceByMember(String username) {
        return instanceRepository.findInstanceByMembers(username);
    }

    public void syncInstances() throws IOException {
        RestResponse<List<String>> gameSessionsResp = instanceContainerServiceClient.getGameSessions();
        Optional<List<String>> gameSessionsOpt = gameSessionsResp.get();
        if (!gameSessionsOpt.isPresent()) {
            logger.error("Unable to get all game sessions from instance container service");
            return;
        }

        List<String> allInstancesIds = getAllInstances().stream().map(Instance::getId).collect(Collectors.toList());
        List<String> gameSessionIds = gameSessionsOpt.get();
        allInstancesIds.removeAll(gameSessionIds);

        for (String instanceId : allInstancesIds) {
            deleteInstance(instanceId);
        }
    }

    public void createLocalInstance(String gameSessionId, String address, int port, String level, String state, String version) {
        Instance localInstance = new Instance();
        localInstance.setId(gameSessionId);
        localInstance.setAddress(address);
        localInstance.setPort(port);
        localInstance.setLevel(level);
        localInstance.setState(state);
        localInstance.setVersion(version);

        saveInstance(localInstance);

        developmentInstances.put(gameSessionId, Instant.now());
    }

    public void removeOldDevelopmentInstances() {
        for (Map.Entry<String, Instant> entry : developmentInstances.entrySet()) {
            if (entry.getValue().plus(1, ChronoUnit.HOURS).isBefore(Instant.now())) {
                deleteInstance(entry.getKey());
            }
        }
    }
}
