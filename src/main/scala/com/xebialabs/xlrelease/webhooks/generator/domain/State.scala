package com.xebialabs.xlrelease.webhooks.generator.domain

import com.xebialabs.xlrelease.webhooks.generator.JsonEventProcessorGenerator.CFG
import grizzled.slf4j.Logging

object State {
  def empty(implicit cfg: CFG): State = State(TypeDef(cfg.typeName, Map.empty), Map.empty)
}

case class State(main: TypeDef,
                 knownTypes: Map[String, TypeDef])
                (implicit val cfg: CFG) extends Logging {

  def nestedTypes: Set[TypeDef] = knownTypes.values.toSet

  def types: Set[TypeDef] = nestedTypes + main

  def addType(typeDef: TypeDef): State = {
    if (main.name == typeDef.name) {
      copy(main = typeDef)
    } else {
      copy(knownTypes = knownTypes + (typeDef.name -> typeDef))
    }
  }

}
