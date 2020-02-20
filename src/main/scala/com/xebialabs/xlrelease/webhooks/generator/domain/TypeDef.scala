package com.xebialabs.xlrelease.webhooks.generator.domain


case class TypeDef(name: String, properties: Map[String, PropertyDef]) {
  override def toString(): String =
    s"TypeDef($name, ${properties.map(entry => entry._1 + "->" + entry._2.kind.name())})"
}