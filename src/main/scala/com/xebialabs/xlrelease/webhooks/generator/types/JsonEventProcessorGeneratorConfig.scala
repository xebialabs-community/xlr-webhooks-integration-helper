package com.xebialabs.xlrelease.webhooks.generator.types

import com.xebialabs.deployit.plugin.api.udm.Metadata

import scala.beans.BeanProperty


// TODO: add validation for typePrefix and typeName!
// allow only letters (no spaces) in ASCII for both prefix and name
@Metadata
class JsonEventProcessorGeneratorConfig {

  @BeanProperty
  var typePrefix: String = _

  @BeanProperty
  var typeName: String = _

}

object JsonEventProcessorGeneratorConfig {
  def apply(typePrefix: String, typeName: String): JsonEventProcessorGeneratorConfig = {
    val config = new JsonEventProcessorGeneratorConfig()
    config.setTypePrefix(typePrefix)
    config.setTypeName(typeName)
    config
  }
}