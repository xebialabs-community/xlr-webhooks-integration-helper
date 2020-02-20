package com.xebialabs.xlrelease.webhooks.generator.domain

import com.xebialabs.deployit.plugin.api.reflect.PropertyKind
import com.xebialabs.xlrelease.webhooks.generator.domain.PropertyDef.referenceKinds

case class PropertyDef(name: PropertyName,
                       kind: PropertyKind,
                       reference: Option[String]) {
  require(
    (referenceKinds.contains(kind) && reference.isDefined) ||
      (!referenceKinds.contains(kind) && reference.isEmpty)
  )

  override def toString: String = {
    val ref = reference.map(" [" + _ + "]").getOrElse("")
    s"PropertyDef(${name.toString}, ${kind.name()})$ref"
  }

}

object PropertyDef {
  lazy val referenceKinds = Set(PropertyKind.CI, PropertyKind.SET_OF_CI, PropertyKind.LIST_OF_CI)


}
