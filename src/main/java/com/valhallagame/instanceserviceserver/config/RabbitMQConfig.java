package com.valhallagame.instanceserviceserver.config;

import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.common.rabbitmq.RabbitSender;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class RabbitMQConfig {

	private RabbitTemplate rabbitTemplate;

    public RabbitMQConfig(@Lazy RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

	@Bean
	public DirectExchange instanceExchange() {
		return new DirectExchange(RabbitMQRouting.Exchange.INSTANCE.name());
	}

	@Bean
	public Jackson2JsonMessageConverter jacksonConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public DirectExchange partyExchange() {
		return new DirectExchange(RabbitMQRouting.Exchange.PARTY.name());
	}

	@Bean
	public Queue partyCreatedQueue() {
		return new Queue("partyCreatedQueue");
	}


	@Bean
	public Queue partyInviteAcceptedQueue() {
		return new Queue("partyInviteAcceptedQueue");
	}

	@Bean
	public Binding bindingPartyCreated(DirectExchange partyExchange, Queue partyCreatedQueue) {
		return BindingBuilder.bind(partyCreatedQueue).to(partyExchange).with(RabbitMQRouting.Party.CREATED);
	}

	@Bean
	public Binding bindingPartyInviteAccepted(DirectExchange partyExchange, Queue partyInviteAcceptedQueue) {
		return BindingBuilder.bind(partyInviteAcceptedQueue).to(partyExchange).with(RabbitMQRouting.Party.ACCEPT_INVITE);
	}

	@Bean
	public SimpleRabbitListenerContainerFactory containerFactory() {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setMessageConverter(jacksonConverter());
		return factory;
	}

	@Bean
	public RabbitSender rabbitSender() {
		return new RabbitSender(rabbitTemplate);
	}
}
