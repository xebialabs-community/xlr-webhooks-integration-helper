package com.xebialabs.xlrelease.webhooks.generator.types

import com.xebialabs.deployit.plugin.api.udm.Property
import com.xebialabs.xlplatform.webhooks.domain.HttpRequestEvent

import scala.beans.BeanProperty

class WebhookSupportGeneratedEvent extends HttpRequestEvent {

  @BeanProperty
  @Property(label = "Event parser script")
  var script: String = _

  @BeanProperty
  @Property(label = "Event synthetic definition")
  var synthetic: String = _

}
