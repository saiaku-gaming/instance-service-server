package com.valhallagame.instanceserviceserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
		if (args.length > 0) {
			// override system properties with local properties
			try (InputStream inputStream = new FileInputStream(args[0])) {
				System.getProperties().load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		SpringApplication.run(App.class, args);
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return (container -> {
			container.setPort(DefaultServicePortMappings.INSTANCE_SERVICE_PORT);
		});
	}

}
