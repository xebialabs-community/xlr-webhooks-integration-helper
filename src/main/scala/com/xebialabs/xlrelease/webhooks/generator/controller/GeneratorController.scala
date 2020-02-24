package com.xebialabs.xlrelease.webhooks.generator.controller

import com.xebialabs.xlrelease.webhooks.generator.{JsonEventProcessorGenerator, ParserGenerator, SyntheticGenerator}
import com.xebialabs.xlrelease.webhooks.generator.types.{JsonEventProcessorGeneratorConfig, WebhookSupportGeneratedEvent}
import grizzled.slf4j.Logging
import org.springframework.web.bind.annotation.{PostMapping, RequestBody, RequestParam, RestController}
import spray.json._

@RestController
class GeneratorController extends Logging {

  @PostMapping(Array("/generate"))
  def generate(@RequestParam("typePrefix") prefix: String,
               @RequestParam("typeName") name: String,
               @RequestBody payload: String): Unit = {

    val config = JsonEventProcessorGeneratorConfig(prefix, name)
    JsonEventProcessorGenerator.generate(config, payload).map {
      finalState =>
        val output = new WebhookSupportGeneratedEvent
        logger.info(s"output of ${payload.parseJson.prettyPrint}:")
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
