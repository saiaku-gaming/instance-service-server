package com.valhallagame.instanceserviceserver.job;

import com.valhallagame.instanceserviceserver.service.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("development")
public class DevelopmentCleanupJob {

    @Autowired
    private InstanceService instanceService;

    @Scheduled(fixedRate = 60000L, initialDelay = 5000L)
    public void execute() {
        instanceService.removeOldDevelopmentInstances();
    }
}
