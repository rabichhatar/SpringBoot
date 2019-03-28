package com.rabi.messaging.JMSEx1;

import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import brave.Span;
import brave.jms.JmsTracing;

@Component
public class MyReceiver {
	
	@Autowired
	JmsTracing jmsTracing;
	
	@JmsListener(destination = "mailbox", containerFactory = "jmsListenerContainerFactory")
	public void receiveMessage(final Message jsonMessage) {
		Span span = jmsTracing.nextSpan(jsonMessage);
		System.out.println("<<<<"+span.context().spanId());
		System.out.println("<<<<"+span.context().parentId());
		System.out.println("<<<<"+span.context().traceId());
		System.out.println("Received <" + jsonMessage.toString() + ">");
	}

}
