package com.xebialabs.xlrelease.webhooks.generator.types

import com.xebialabs.deployit.plugin.api.udm.{Metadata, Property}
import com.xebialabs.xlrelease.webhooks.consumers.BaseProcessorConfiguration

import scala.beans.BeanProperty


// TODO: add validation for typePrefix and typeName!
// allow only letters (no spaces) in ASCII for both prefix and name
@Metadata
class JsonEventProcessorGeneratorConfig extends BaseProcessorConfiguration {

  @BeanProperty
  @Property
  var typePrefix: String = _

  @BeanProperty
  @Property
  var typeName: String = _

}
