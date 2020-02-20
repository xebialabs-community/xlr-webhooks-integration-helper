package com.xebialabs.xlrelease.webhooks.generator

import com.xebialabs.deployit.plugin.api.reflect.PropertyKind
import com.xebialabs.xlrelease.webhooks.generator.JsonEventProcessorGenerator.CFG
import com.xebialabs.xlrelease.webhooks.generator.domain.{PropertyDef, State, TypeDef}

object ParserGenerator {

  lazy val parserHeader: String =
    s"""|import json
        |import time
        |
        |global CI
        |global input
        |global config
        |
        |def parseDate(date):
        |    if (isinstance(date, int)):
        |        return time.strftime('%Y-%m-%dT%H:%M:%SZ', time.localtime(date))
        |    else:
        |        return date
        |
        |""".stripMargin

  def generate(state: State): String = {
    import state._
    s"""|${parserHeader}
        |${nestedTypes.map(generate).mkString("", "\n", "\n")}
        |${generate(main)}
        |output = ${parseCi(main.name, "json.loads(input.content)")}
        |""".stripMargin
  }

  def generate(typeDef: TypeDef)
              (implicit cfg: CFG): String = {
    import typeDef._
    s"""|def parse_$name(data):
        |    if data:
        |        return CI("${cfg.typePrefix}.$name", {
        |${properties.values.map(generate).mkString("", ",\n", "")}
        |        })
        |    else:
        |        return None
        |""".stripMargin
  }

  def generate(propertyDef: PropertyDef)
              (implicit cfg: CFG): String = {
    import propertyDef._
    val data = s"data['${name.key}'] if '${name.key}' in data else None"
    val value: String = reference.map {
      referencedTypeName =>
        kind match {
          case PropertyKind.CI =>
            parseCi(referencedTypeName, data)
          case PropertyKind.LIST_OF_CI | PropertyKind.SET_OF_CI =>
            s"map(lambda x: ${parseCi(referencedTypeName, "x")}, $data)"
          case other =>
            throw new IllegalStateException(s"Error generating parser for ${cfg.typePrefix}.${name}.${name.name}: ${other.name()} is not a reference kind.")
        }
    }.getOrElse {
      if (propertyDef.kind.eq(PropertyKind.DATE)) {
        s"parseDate($data)"
      } else {
        data
      }
    }
    s"""            '${name.name}': $value"""
  }

  def parseCi(referencedTypeName: String, arg: String): String =
    s"parse_${referencedTypeName}($arg)"

}
