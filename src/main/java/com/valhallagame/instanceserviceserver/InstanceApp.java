package com.valhallagame.instanceserviceserver;

import com.valhallagame.common.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.valhallagame.common.DefaultServicePortMappings;

@EnableScheduling
@SpringBootApplication
public class InstanceApp {

	private static final Logger logger = LoggerFactory.getLogger(InstanceApp.class);

	public static void main(String[] args) {
		Properties.load(args, logger);
		SpringApplication.run(InstanceApp.class, args);
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return (container -> container.setPort(DefaultServicePortMappings.INSTANCE_SERVICE_PORT));
	}

}
