package de.htwg.se.Monopoly.model

import scala.collection.mutable

case class Board(fields: Vector[Field]) {
  def this() = this(Variable.START_BOARD)

  def getField(player: Player, newPosition: Int): Field = {
    fields(newPosition).actOnPlayer(player)
  }

  override def toString: String = {
    val boardString = new mutable.StringBuilder("")
    boardString ++= "%-6s %-25s %-10s %-5s %-5s %-20s\n".format("index", "name", "type", "price", "rent", "owner")
    for (i <- 0 to 39) {
      fields(i) match {
        case Street(index, name, neighbourhoodTypes, price, rent, owner) =>
          boardString ++= "%-6s %-25s %-10s %-5s %-5s %-20s\n".format(index, name, neighbourhoodTypes, price, rent, owner)
        case ChanceCard(index, name, getMoney, giveMoney, otherPlayerIndex) =>
          boardString ++= "%-6s %-25s\n".format(index, name)
        case Tax(index, name, taxAmount) =>
          boardString ++= "%-6s %-25s %14s\n".format(index, name, taxAmount)
        case SpecialField(index, name) =>
          boardString ++= "%-6s %-25s\n".format(index, name)
      }
    }
    boardString.toString()
  }
}
