package com.xebialabs.xlrelease.webhooks.generator.domain

import com.xebialabs.deployit.plugin.api.reflect.PropertyKind
import com.xebialabs.xlrelease.webhooks.generator.types.JsonEventProcessorGeneratorConfig


case class PropertyName private(name: String)
                               (val key: String = name) {
  override def toString: String = {
    if (name != key) {
      s"$name <- $key"
    } else {
      name
    }
  }
}

object PropertyName {
  lazy val reserved: Set[String] = Set(
    "type", "id", "name"
  )

  def of(key: String): PropertyName =
    if (reserved contains key) {
      new PropertyName(name = s"_$key")(key = key)
    } else {
      new PropertyName(name = key)(key = key)
    }

  implicit class PropertyNameOps(val _name: PropertyName) extends AnyVal {
    def property(kind: PropertyKind, nestedTypeRef: Option[String])
                (implicit cfg: JsonEventProcessorGeneratorConfig): PropertyDef =
      PropertyDef(_name, kind, nestedTypeRef)
    def property(kind: PropertyKind)
                (implicit cfg: JsonEventProcessorGeneratorConfig): PropertyDef =
      property(kind, None)

  }
}