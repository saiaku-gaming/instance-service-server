package com.valhallagame.instanceserviceserver.job;

import com.valhallagame.instanceserviceserver.service.InstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Profile("production")
public class InstanceContainerSyncJob {
	
	private static final Logger logger = LoggerFactory.getLogger(InstanceContainerSyncJob.class);

	@Autowired
	private InstanceService instanceService;

	@Value("${spring.application.name}")
	private String appName;

	@Scheduled(fixedRate = 60000, initialDelay = 5000)
	public void execute() {
		MDC.put("service_name", appName);
		MDC.put("request_id", UUID.randomUUID().toString());

		try {
			instanceService.syncInstances();
		} catch (IOException e) {
			logger.error("Failed sync", e);
		} finally {
			MDC.clear();
		}
	}

}
