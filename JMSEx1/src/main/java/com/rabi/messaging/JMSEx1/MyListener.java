package com.rabi.messaging.JMSEx1;

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import brave.Span;
import brave.Tracer.SpanInScope;
import brave.Tracing;
import brave.jms.JmsTracing;

import com.google.gson.Gson;

@Component
public class MyListener {

	@Autowired
	JmsTracing jmsTracing;

	@Autowired
	Tracing tracing;

	@JmsListener(destination = "inbound.queue")
	@SendTo("outbound.queue")
	public String receiveMessage(final Message jsonMessage) throws JMSException {

		Span span = jmsTracing.nextSpan(jsonMessage).name("process").start();
		System.out.println(">>>>"+span.context().spanId());
		try (SpanInScope ws = tracing.tracer().withSpanInScope(span)) {
			System.out.println(">>>>"+span.context().spanId());
			System.out.println(">>>>"+span.context().parentId());
			System.out.println(">>>>"+span.context().traceId());
			String messageData = null;
			System.out.println("Received message "
					+ jsonMessage.getStringProperty("b3"));
			System.out.println("Received message "
					+ jsonMessage.getStringProperty("spanId"));
			System.out.println("Received message "
					+ jsonMessage.getStringProperty("spanid"));
			String response = null;
			if (jsonMessage instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) jsonMessage;
				messageData = textMessage.getText();
				Map map = new Gson().fromJson(messageData, Map.class);
				response = "Hello " + map.get("name");
			}
			return response;
		} catch (RuntimeException | Error e) {
			span.error(e); // make sure any error gets into the span before it
							// is finished
			throw e;
		} finally {
			span.finish(); // ensure the span representing this processing
							// completes.
		}

	}
}
