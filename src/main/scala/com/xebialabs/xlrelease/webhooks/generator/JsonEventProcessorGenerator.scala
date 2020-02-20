package com.xebialabs.xlrelease.webhooks.generator

import java.time.Instant

import com.xebialabs.deployit.plugin.api.reflect.PropertyKind._
import com.xebialabs.xlplatform.webhooks.domain.HttpRequestEvent
import com.xebialabs.xlrelease.webhooks.generator.domain._
import com.xebialabs.xlrelease.webhooks.generator.types.JsonEventProcessorGeneratorConfig
import grizzled.slf4j.Logging
import org.joda.time.DateTime
import spray.json._

import scala.util.{Failure, Success, Try}

/* TODO:
  optimization:
    avoid double/triple passes
 */
object JsonEventProcessorGenerator extends Logging {

  type CFG = JsonEventProcessorGeneratorConfig

  def generate(config: JsonEventProcessorGeneratorConfig, event: HttpRequestEvent): Try[State] = {
    implicit val cfg: CFG = config
    event.content.parseJson match {
      case obj: JsObject =>
        Success(
          fillProperties(TypeDef(cfg.typeName, Map.empty))
            .apply(obj)
            .apply(State.empty)
        )
      case _ =>
        Failure(new IllegalArgumentException("Only works on JSON objects!"))
    }
  }

  def generateType(name: PropertyName, node: JsObject)
                  (implicit cfg: CFG): State => (State, String) = state => {
    logger.debug(s"generateType($name, <node>)")
    state.types.foreach { typeDef =>
      logger.debug(" - " + typeDef.toString)
    }
    logger.debug("---")
    state.types.collectFirst {
      case existingType if matchesShape(node, existingType).apply(state) =>
        logger.debug(s"found matching type from existing set: ${existingType.name}")
        state -> existingType.name
    }.getOrElse {
      val newName = freshName(state)
      logger.debug(s"this is a new thing, let's name it '$newName'")
      val newTypeDef = TypeDef(newName, Map.empty)
      val newState = state.copy(knownTypes = state.knownTypes + (newName -> newTypeDef))
      fillProperties(newTypeDef).apply(node).apply(newState) -> newName
    }
  }

  // either pick existing type or generate new one
  def fillProperties(tempType: TypeDef)
                    (implicit cfg: CFG): JsObject => State => State =
    node =>
      state => {
        logger.debug(s"fillProperties(${tempType.name})")
        node.fields.map {
          case (key, value) =>
            PropertyName.of(key) -> value
        }.foldLeft(state -> tempType) {
          case ((s, t), (name, value)) if !(tempType.properties contains name.name) =>
            logger.debug(s" - $name")
            val parseProperty: PartialFunction[JsValue, (State, PropertyDef)] = Seq(
              string(name),
              number(name),
              boolean(name),
              array(name),
              obj(name),
            ).map(_.apply(s)).foldLeft(PartialFunction.empty[JsValue, (State, PropertyDef)]) {
              _ orElse _
            }

            val pd: Option[(State, PropertyDef)] = value match {
              case JsNull => None
              case other => parseProperty.lift(other)
            }

            val (newState, newTypeDef) = pd.map {
              case (newState, pd) =>
                logger.debug(s"updating state for ${t.name} with ${pd}")
                newState -> t.copy(properties = t.properties + (name.name -> pd))
            }.getOrElse {
              logger.warn(s"Ignoring property '${name.key}': $value")
              s -> t
            }
            newState.addType(newTypeDef) -> newTypeDef
        }
        }._1

  def freshName: State => String = state =>
    state.main.name + state.nestedTypes.size

  // TODO: make it configurable?
  lazy val reasonablyRecentInstant: Instant = Instant.ofEpochSecond(1095379200) // 2004

  def isDate(str: JsString): Boolean = Try(DateTime.parse(str.value)).isSuccess

  def isDate(num: JsNumber): Boolean =
    num.value.isValidLong && Try(Instant.ofEpochSecond(num.value.toLongExact))
      .map(_.isAfter(reasonablyRecentInstant))
      .getOrElse(false)

  def isInteger(num: JsNumber): Boolean = num.value.isValidInt

  def isString(value: JsValue): Boolean = value match {
    case _: JsString => true
    case _ => false
  }

  def isArrayListOfStrings(arr: JsArray): Boolean =
    arr.elements.nonEmpty && arr.elements.forall(isString)

  def isArrayListOfObjects(arr: JsArray)
                          (implicit cfg: CFG): State => Boolean = state => {
    arr.elements.nonEmpty && arr.elements.toList.sliding(2).forall {
      case a :: b :: Nil => sameShape(a.asJsObject, b.asJsObject).apply(state)
      case _ => true
    }
  }

  def isArrayListOfCIs(name: PropertyName, arr: JsArray)
                      (implicit cfg: CFG): State => Option[String] = state => {
    if (isArrayListOfObjects(arr).apply(state)) {
      Some(generateType(name, arr.elements.head.asJsObject).apply(state)._2)
    } else {
      None
    }
  }

  def matchesShape(node: JsObject, typeDef: TypeDef)
                  (implicit cfg: CFG): State => Boolean = state => {

    node.fields.map {
      case (k, v) =>
        PropertyName.of(k) -> v
    }.forall {
      case (name, va) =>
        (va, typeDef.properties.get(name.name)) match {
          case (_: JsBoolean, Some(PropertyDef(`name`, BOOLEAN, None))) => true
          case (sa: JsString, Some(PropertyDef(`name`, DATE, None))) => isDate(sa)
          case (sa: JsString, Some(PropertyDef(`name`, STRING, None))) => !isDate(sa)
          case (na: JsNumber, Some(PropertyDef(`name`, DATE, None))) => isDate(na)
          case (na: JsNumber, Some(PropertyDef(`name`, INTEGER, None))) => !isDate(na)
          case (JsNull, None) => true
          case (aa: JsArray, Some(PropertyDef(`name`, LIST_OF_STRING, None))) => isArrayListOfStrings(aa)
          case (aa: JsArray, Some(PropertyDef(`name`, LIST_OF_CI, Some(nestedTypeName)))) =>
            isArrayListOfCIs(name, aa).apply(state).contains(nestedTypeName)
          case (oa: JsObject, Some(PropertyDef(`name`, CI, Some(nestedTypeName)))) =>
            state.nestedTypes.find(_.name == nestedTypeName).exists { nestedTypeDef =>
              matchesShape(oa, nestedTypeDef).apply(state)
            }
          case _ =>
            false
        }
    }
  }

  def sameShape(a: JsObject, b: JsObject)
               (implicit cfg: CFG): State => Boolean = state => {
    logger.debug("sameShape(..., ...)")
    (a.fields.keySet == b.fields.keySet) && {
      a.fields.map {
        case (k, v) =>
          PropertyName.of(k) -> v
      }.forall {
        case (name, va) =>
          (va, b.fields(name.key)) match {
            case (_: JsBoolean, _: JsBoolean) => true
            case (sa: JsString, sb: JsString) => (isDate(sa) && isDate(sb)) || (!isDate(sa) && !isDate(sb))
            case (na: JsNumber, nb: JsNumber) => isInteger(na) && isInteger(nb)
            case (JsNull, JsNull) => true
            case (aa: JsArray, ab: JsArray) =>
              (isArrayListOfStrings(aa) && isArrayListOfStrings(ab)) || (
                isArrayListOfCIs(name, aa).apply(state).nonEmpty && isArrayListOfCIs(name, ab).apply(state).nonEmpty
                )
            case (oa: JsObject, ob: JsObject) =>
              sameShape(oa, ob).apply(state)
            case (_, _) =>
              false
          }
      }
    }
  }


  type PF = State => PartialFunction[JsValue, (State, PropertyDef)]

  def boolean(name: PropertyName)
             (implicit cfg: CFG): PF = s => {
    case _: JsBoolean =>
      s -> name.property(BOOLEAN)
  }

  def string(name: PropertyName)
            (implicit cfg: CFG): PF = s => {
    case d: JsString if isDate(d) =>
      s -> name.property(DATE)
    case _: JsString =>
      s -> name.property(STRING)
  }

  def number(name: PropertyName)
            (implicit cfg: CFG): PF = s => {
    case n: JsNumber if isDate(n) =>
      s -> name.property(DATE)
    case n: JsNumber if isInteger(n) =>
      s -> name.property(INTEGER)
  }

  def obj(name: PropertyName)
         (implicit cfg: CFG): PF = s => {
    case obj: JsObject =>
      val (newState, typeName) = generateType(name, obj).apply(s)
      newState -> name.property(CI, Some(typeName))
  }

  def array(name: PropertyName)
           (implicit cfg: CFG): PF = s => {
    case arr: JsArray if arr.elements.forall {
      case JsString(_) => true
      case _ => false
    } =>
      s -> name.property(LIST_OF_STRING)
    case arr: JsArray if arr.elements.forall {
      case JsObject(_) => true
      case _ => false
    } && arr.elements.toList.sliding(2).forall {
      case a :: b :: Nil => sameShape(a.asJsObject, b.asJsObject).apply(s)
      case _ => true
    } =>
      val (newState, typeName) = generateType(name, arr.elements.head.asJsObject).apply(s)
      val typeDef = newState.types.find(_.name == typeName).get
      newState -> name.property(LIST_OF_CI, Some(typeName))
  }

}
