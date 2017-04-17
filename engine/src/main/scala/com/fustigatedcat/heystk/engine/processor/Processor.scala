package com.fustigatedcat.heystk.engine.processor

import com.fustigatedcat.heystk.common.normalization.Normalization
import com.fustigatedcat.heystk.engine.action.Action
import com.fustigatedcat.heystk.engine.rule.Rule
import com.typesafe.config.Config
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object Processor {

  def create(config : Config) : Processor = {
    val procConfig = config.getConfig("engine.processor")
    new Processor(
      procConfig.getConfigList("rules").asScala.map(Rule.create).toList,
      procConfig.getConfigList("actions").asScala.map(c => {
        val a = Action.create(c)
        c.getString("id") -> a
      }).toMap
    )
  }

}

class Processor(rules : List[Rule], actions : Map[String, Action]) {

  val logger = LoggerFactory.getLogger(this.getClass)

  def process(normalization : Normalization) : Unit = {
    rules.find(_.process(normalization)) match {
      case Some(rule) => rule.actions.foreach(act => actions.get(act) match {
        case Some(action) => action.execute(normalization)
        case _ => logger.error("Failed to find action {}", act)
      })
    }
  }

}
