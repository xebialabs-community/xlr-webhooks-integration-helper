package com.xebialabs.xlrelease.webhooks.generator

import com.xebialabs.xlplatform.webhooks.domain.HttpRequestEvent
import com.xebialabs.xlrelease.XLReleaseScalaTest
import com.xebialabs.xlrelease.webhooks.endpoint.WebhookEndpointConfiguration.PostWebhookEndpointConfiguration
import com.xebialabs.xlrelease.webhooks.generator.types.JsonEventProcessorGeneratorConfig
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import scala.collection.JavaConverters._
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class JsonEventProcessorGeneratorTest extends XLReleaseScalaTest {

  describe("JsonEventProcessorGenerator") {
    it("should do its job") {
      def payload = Source.fromResource("simple.json").mkString
      val src = new PostWebhookEndpointConfiguration
      src.setTitle("test")
      src.path = "test"
      src.outputEventType = "xebialabs.HttpRequestEvent"

      val cfg = new JsonEventProcessorGeneratorConfig
      cfg.typePrefix = "test"
      cfg.typeName = "simple"
      cfg.inputEventType = "xebialabs.HttpRequestEvent"
      cfg.outputEventType = "xlrelease.WebhookSupportGeneratedEvent"
      cfg.source = src
      val output = JsonEventProcessorGenerator.generate(cfg,
        HttpRequestEvent(
          src,
          Map.empty[String, String].asJava,
          Map.empty[String, Array[String]].asJava,
          payload
        )
      )
      output.isSuccess shouldBe true
      val out = output.get
      val synthetic =  SyntheticGenerator.generate(out)
      val parser = ParserGenerator.generate(out)
      logger.info("synthetic.xml\n" + synthetic)
      logger.info("parser.py\n" + parser)
      synthetic.trim() shouldBe Source.fromResource("expected-synthetic.xml").mkString.trim()
      parser.trim() shouldBe Source.fromResource("expected-parser.py").mkString.trim()
    }
  }

}
