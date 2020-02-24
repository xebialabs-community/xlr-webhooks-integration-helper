package com.xebialabs.xlrelease.webhooks.generator

import com.xebialabs.xlrelease.webhooks.generator.types.JsonEventProcessorGeneratorConfig
import grizzled.slf4j.Logging
import org.junit.runner.RunWith
import org.scalatest.{FunSpecLike, Matchers}
import org.scalatestplus.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class JsonEventProcessorGeneratorTest extends FunSpecLike
  with Matchers
  with Logging {

  describe("JsonEventProcessorGenerator") {
    it("should do its job") {
      def payload: String = Source.fromResource("simple.json").mkString

      val cfg = new JsonEventProcessorGeneratorConfig
      cfg.typePrefix = "test"
      cfg.typeName = "simple"
      val output = JsonEventProcessorGenerator.generate(cfg, payload)
      output.isSuccess shouldBe true
      val out = output.get
      val synthetic = SyntheticGenerator.generate(out)
      val parser = ParserGenerator.generate(out)
      logger.info("synthetic.xml\n" + synthetic)
      logger.info("parser.py\n" + parser)
      synthetic.trim() shouldBe Source.fromResource("expected-synthetic.xml").mkString.trim()
      parser.trim() shouldBe Source.fromResource("expected-parser.py").mkString.trim()
    }
  }

}
