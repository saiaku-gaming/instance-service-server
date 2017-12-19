package com.valhallagame.instanceserviceserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.valhallagame.common.DefaultServicePortMappings;

@EnableScheduling
@SpringBootApplication
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return (container -> {
			container.setPort(DefaultServicePortMappings.INSTANCE_SERVICE_PORT);
		});
	}

}
