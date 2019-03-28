package com.rabi.messaging.JMSEx1;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import brave.Span;
import brave.Tracing;
import brave.jms.JmsTracing;

import brave.sampler.Sampler;

@SpringBootApplication
@EnableJms
public class JmsEx1Application {

	String BROKER_URL = "tcp://localhost:61616";
	String BROKER_USERNAME = "admin";
	String BROKER_PASSWORD = "admin";
	
	

	public static void main(String[] args) {
		// SpringApplication.run(JmsEx1Application.class, args);

		ConfigurableApplicationContext context = SpringApplication.run(
				JmsEx1Application.class, args);

		JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
		JmsTracing jmsTracing = context.getBean(JmsTracing.class);

		System.out.println("Sending an email message.");
		
		
		jmsTemplate.convertAndSend("mailbox", "message",new MessagePostProcessor() {
			
			@Override
			public Message postProcessMessage(Message message) throws JMSException {
				Span span = jmsTracing.nextSpan(message);
				System.out.println(">>>>"+span.context().spanId());
				System.out.println(">>>>"+span.context().parentId());
				System.out.println(">>>>"+span.context().traceId());
				return message;
			}
		});

	}

	@Bean
	public Tracing tracing() {
		return Tracing.newBuilder().localServiceName("TEST_SERVICE")
				.sampler(Sampler.create(0.0f)).build();
	}

	@Bean
	public ConnectionFactory connectionFactory(JmsTracing jmsTracing) {

		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setBrokerURL(BROKER_URL);
		connectionFactory.setPassword(BROKER_USERNAME);
		connectionFactory.setUserName(BROKER_PASSWORD);
		return jmsTracing.connectionFactory(connectionFactory);
	}

	@Bean
	public JmsTracing jmsTracing(Tracing tracing) {
		return JmsTracing.newBuilder(tracing).build();
	}

	@Bean
	public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
		JmsTemplate template = new JmsTemplate();
		template.setConnectionFactory(connectionFactory);
		return template;
	}

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
			ConnectionFactory connectionFactory) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setConcurrency("1-1");
		return factory;
	}

}
