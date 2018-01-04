package com.valhallagame.instanceserviceserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.personserviceclient.PersonServiceClient;

@Configuration
@Profile("default")
public class DevConfig {
	@Bean
	public PartyServiceClient partyServiceClient() {
		return PartyServiceClient.get();
	}

	@Bean
	public PersonServiceClient personServiceClient() {
		return PersonServiceClient.get();
	}

	@Bean
	public InstanceContainerServiceClient instanceContainerServiceClient() {
		return InstanceContainerServiceClient.get();
	}
}
