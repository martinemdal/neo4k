package net.emdal.tank.clause

import net.emdal.tank.*

interface GraphClause : Clause {

  /**
   * Builds a Cypher query part for a node definition on the form:
   * ([alias]:[labels] { [properties] })
   *
   * @return A [Clause] containing the previously built query and context plus the one generated by
   * this node function.
   */
  fun <T : Node, C : Clause> C.node(alias: String = "", vararg labels: T, properties: T.() -> String? = { null }) =
    this.apply {
      query = query + "($alias:${concatenate(labels)}${properties(labels, properties)})"
    }

  /**
   * Builds a Cypher query part for a node definition on the form:
   * (:[labels] { [properties] })
   *
   * @return A [Clause] containing the previously built query and context plus the one generated by
   * this node function.
   */
  fun <T : Node, C : Clause> C.node(vararg labels: T, block: T.() -> String? = { null }): C = this.apply {
    query = query + "(:${concatenate(labels)}${properties(labels, block)})"
  }

  /**
   * Builds a Cypher query part for a node definition on the form:
   * ([alias] { [properties] })
   *
   * @return A [Clause] containing the previously built query and context plus the one generated by
   * this node function.
   */
  fun <C : Clause> C.node(alias: String = "", block: () -> String? = { null }): C = this.apply {
    query = query + "($alias${properties(block)})"
  }

  /**
   * Builds a Cypher query part for a relationship definition on the form:
   * ([alias] { [properties] })
   *
   * @return A [Clause] containing the previously built query and context plus the one generated by
   * this [relationship] function.
   */
  fun <C : Clause> C.relationship(alias: String = "", block: () -> String? = { null }): C = this.apply {
    query = query + "-[$alias${properties(block)}]->"
  }

  /**
   * Builds a Cypher query part for a relationship definition on the form:
   * -[[alias]:[types] { [properties] }]->
   *
   * @return A [Clause] containing the previously built query and context plus the one generated by
   * this [relationship] function.
   */
  fun <T : Relationship, C : Clause> C.relationship(
    alias: String = "",
    vararg types: T,
    block: T.() -> String? = { null }
  ): C = this.apply {
    query = query + "-[$alias:${concatenate(types)}${properties(types, block)}]->"
  }

  /**
   * Builds a Cypher query part for a relationship definition on the form:
   * -[:[types] { [properties] }]->
   *
   * @return A [Clause] containing the previously built query and context plus the one generated by
   * this function.
   */
  fun <T : Relationship, C : Clause> C.relationship(vararg types: T, block: T.() -> String? = { null }): C =
    this.apply {
      query = query + "-[:${concatenate(types)}${properties(types, block)}]->"
    }

  private fun <T : Node> concatenate(labels: Array<out T>) = labels.map(
    Node::label
  ).joinToString(":")

  private fun <T : Relationship> concatenate(types: Array<out T>) = types.map(
    Relationship::type
  ).joinToString("|")

  private fun properties(block: () -> String?): String = block().body()

  private fun <T : Entity> properties(entities: Array<out T>, block: T.() -> String?) =
    entities.first().block().body()

  private fun String?.body(): String = this?.let { " { $it }" } ?: ""

  infix fun StringProperty.eq(value: String): String {
    return """${this.propertyName}: "$value""""
  }

  infix fun IntProperty.eq(value: Int): String {
    return """${this.propertyName}: $value"""
  }
}