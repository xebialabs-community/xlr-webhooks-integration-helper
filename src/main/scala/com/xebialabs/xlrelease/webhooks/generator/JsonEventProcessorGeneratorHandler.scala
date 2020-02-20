package com.xebialabs.xlrelease.webhooks.generator

import com.xebialabs.deployit.plugin.api.reflect.Type
import com.xebialabs.xlplatform.webhooks.domain.HttpRequestEvent
import com.xebialabs.xlplatform.webhooks.events.domain.Event
import com.xebialabs.xlplatform.webhooks.events.handlers.EventProcessor
import com.xebialabs.xlplatform.webhooks.queue.JmsEventPublisher
import com.xebialabs.xlrelease.webhooks.consumers.BaseConsumer
import com.xebialabs.xlrelease.webhooks.generator.types.{JsonEventProcessorGeneratorConfig, WebhookSupportGeneratedEvent}
import grizzled.slf4j.Logging
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import spray.json._

@Component
class JsonEventProcessorGeneratorHandler @Autowired()(val jmsTemplate: JmsTemplate, @Value("${xl.queue.queueName}") val queueName: String)
  extends EventProcessor[Event, WebhookSupportGeneratedEvent, JsonEventProcessorGeneratorConfig]
    with JmsEventPublisher[WebhookSupportGeneratedEvent, JsonEventProcessorGeneratorConfig]
    with BaseConsumer[JsonEventProcessorGeneratorConfig]
    with Logging {

  def consumerConfigType: Type = Type.valueOf(classOf[JsonEventProcessorGeneratorConfig])

  def map(config: JsonEventProcessorGeneratorConfig, event: Event): WebhookSupportGeneratedEvent = {
    val request = event.asInstanceOf[HttpRequestEvent]
    JsonEventProcessorGenerator.generate(config, request).map {
      finalState =>
        val output = new WebhookSupportGeneratedEvent
        logger.info(s"output of ${request.content.parseJson.prettyPrint}:")
        output.synthetic = SyntheticGenerator.generate(finalState)
        output.script = ParserGenerator.generate(finalState)
        logger.info("*** synthetic ***")
        logger.info("\n" + output.synthetic)
        logger.info("\n*** parser ***")
        logger.info("\n" + output.script)
        output
    }.get
  }

}
