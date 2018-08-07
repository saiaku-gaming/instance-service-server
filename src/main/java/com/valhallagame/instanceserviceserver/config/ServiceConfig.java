package com.valhallagame.instanceserviceserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.valhallagame.common.DefaultServicePortMappings;
import com.valhallagame.instancecontainerserviceclient.InstanceContainerServiceClient;
import com.valhallagame.partyserviceclient.PartyServiceClient;
import com.valhallagame.personserviceclient.PersonServiceClient;

@Configuration
@Profile({"production", "development"})
public class ServiceConfig {
	@Bean
	public PartyServiceClient partyServiceClient() {
		PartyServiceClient.init("http://party-service:" + DefaultServicePortMappings.PARTY_SERVICE_PORT);
		return PartyServiceClient.get();
	}

	@Bean
	public PersonServiceClient personServiceClient() {
		PersonServiceClient.init("http://person-service:" + DefaultServicePortMappings.PERSON_SERVICE_PORT);
		return PersonServiceClient.get();
	}

	@Bean
	public InstanceContainerServiceClient instanceContainerServiceClient() {
		InstanceContainerServiceClient.init(
				"http://instance-container-service:" + DefaultServicePortMappings.INSTANCE_CONTAINER_SERVICE_PORT);
		return InstanceContainerServiceClient.get();
	}
}
