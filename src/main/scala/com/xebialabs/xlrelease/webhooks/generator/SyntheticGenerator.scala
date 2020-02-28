package com.xebialabs.xlrelease.webhooks.generator

import com.xebialabs.deployit.plugin.api.reflect.PropertyKind
import com.xebialabs.xlrelease.webhooks.generator.JsonEventProcessorGenerator.CFG
import com.xebialabs.xlrelease.webhooks.generator.domain.PropertyDef.referenceKinds
import com.xebialabs.xlrelease.webhooks.generator.domain.{PropertyDef, State, TypeDef}

object SyntheticGenerator {

  lazy val syntheticHeader: String =
    """|<synthetic xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       |           xmlns="http://www.xebialabs.com/deployit/synthetic"
       |           xsi:schemaLocation="http://www.xebialabs.com/deployit/synthetic synthetic.xsd">
       |""".stripMargin
  lazy val syntheticFooter: String = """</synthetic>"""


  def generate(state: State): String = {
    import state._
    s"""|$syntheticHeader
        |${generateMain(main)}
        |${nestedTypes.map(generateNested).mkString("", "\n\n", "\n")}
        |$syntheticFooter
        |""".stripMargin
  }

  def generateMain(typeDef: TypeDef)
                  (implicit cfg: CFG): String =
    generateType(nested = false)(typeDef)

  def generateNested(typeDef: TypeDef)
                    (implicit cfg: CFG): String =
    generateType(nested = true)(typeDef)


  def generateType(nested: Boolean)
                  (typeDef: TypeDef)
                  (implicit cfg: CFG): String = {
    import typeDef._
    val superType = if (nested) "udm.BaseConfigurationItem" else "events.Event"
    s"""|    <type type="${cfg.typePrefix}.$name" extends="$superType">
        |${properties.values.map(generateProperty).mkString("\n")}
        |    </type>""".stripMargin
  }

  def generateProperty(propertyDef: PropertyDef)
                      (implicit cfg: CFG): String = {
    import propertyDef._
    val referencedType = reference.map {
      typeRefName =>
        s""" referenced-type="${cfg.typePrefix}.$typeRefName""""
    }.getOrElse("")

    val nested = if (kind == PropertyKind.CI) {
      """ nested="true""""
    } else if (referenceKinds.contains(kind)) {
      """ as-containment="true""""
    } else {
      ""
    }
    s"""        <property name="${name.name}" kind="${kind.name().toLowerCase}"$referencedType$nested/>"""
  }

}
