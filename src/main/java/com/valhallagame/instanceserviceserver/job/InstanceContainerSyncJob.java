package com.valhallagame.instanceserviceserver.job;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.valhallagame.instanceserviceserver.service.InstanceService;

@Component
@Profile("production")
public class InstanceContainerSyncJob {

	@Autowired
	private InstanceService instanceService;

	@Scheduled(fixedRate = 60000, initialDelay = 5000)
	public void execute() {
		try {
			instanceService.syncInstances();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
