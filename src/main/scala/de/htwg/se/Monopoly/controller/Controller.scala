package de.htwg.se.Monopoly.controller

import de.htwg.se.Monopoly.controller.GameStatus._
import de.htwg.se.Monopoly.model._
import de.htwg.se.Monopoly.util.{Observable, UndoManager}

import scala.collection.mutable

class Controller(var board: Board, var players: Vector[Player] = Vector()) extends Observable{

  var gameStatus: GameStatus = IDLE
  private val undoManager = new UndoManager
  var currentPlayerIndex: Int = 0
  var actualField: Field = SpecialField(0, "Los")
  var context = new Context()

  def setPlayers(player: Vector[Player]): Unit = {
    context.setPlayer()
    players = player
    notifyObservers
  }

  def rollDice(): Unit = {
    undoManager.doStep(new RollDiceCommand(this))
  }

  def movePlayer(rolledEyes: Int): Field = {
    val actualPlayer = players(currentPlayerIndex)
    if (actualPlayer.inJail != 0) {
      decrementJailCounter(actualPlayer)
    } else {
      setPlayer(actualPlayer, rolledEyes)
    }
  }

  def decrementJailCounter(p: Player): Field = {
    players = players.updated(p.index, p.decrementJailCounter())
    board.fields(p.currentPosition)
  }

  def setPlayer(p: Player, n: Int): Field = {
    players = players.updated(p.index, p.setPosition(p.currentPosition + n))
    val field = board.getField(players(currentPlayerIndex),
      players(currentPlayerIndex).currentPosition)
    actualField = field
    context.rollDice(this)
    print("\n You landed on " + field + "\n")
    field match {
      case s: Street => handleStreet(s)
      case c: ChanceCard => handleChanceCard(c)
      case sp: SpecialField => handleSpecialField(sp)
      case t: Tax => handleTax(t)
    }
    field
  }

  def handleStreet(s: Street): Unit = {
    context.state match {
      case _: NextPlayerState =>
        nextPlayer()
      case _: BuyStreet =>
        notifyObservers
      case _: PayOtherPlayer =>
        players = players.updated(currentPlayerIndex, players(currentPlayerIndex).decrementMoney(s.rent))
        players = players.updated(s.owner.index, s.owner.incrementMoney(s.rent))
        nextPlayer()
      case _ =>
    }
  }

  def handleChanceCard(c: ChanceCard) : Unit = {
    players = players.updated(currentPlayerIndex, players(currentPlayerIndex).incrementMoney(c.getMoney))
    if (c.otherPlayerIndex != -1) {
      players = players.updated(c.otherPlayerIndex, players(c.otherPlayerIndex).incrementMoney(c.giveMoney))
    }
    nextPlayer()
  }

  def handleSpecialField(sp: SpecialField): Unit = {
    context.state match {
      case _: LandedOnGo =>
      case _: VisitJail =>
      case _: FreeParking =>
      case _: GoToJail =>
        players = players.updated(currentPlayerIndex, players(currentPlayerIndex).goToJail())
    }
    nextPlayer()
  }

  def handleTax(t: Tax): Unit = {
    players = players.updated(currentPlayerIndex, players(currentPlayerIndex).decrementMoney(t.taxAmount))
    nextPlayer()
  }

  def nextPlayer(): Unit = {
    if (currentPlayerIndex + 1 < players.length) {
      currentPlayerIndex = currentPlayerIndex + 1} else {currentPlayerIndex = 0}
    context.nextPlayer()
    notifyObservers
  }

  def buyStreet(): Unit = {
    undoManager.doStep(new BuyCommand(this))
  }

  def undo: Unit = {
    undoManager.undoStep
    notifyObservers
  }

  def redo: Unit = {
    undoManager.redoStep
    notifyObservers
  }

  def gameToString: String = {
    val string = new mutable.StringBuilder("")
    string ++= board.toString
    string ++= "\nPlayers:\n%-6s %-25s %-10s %-5s\n".format("index", "name", "money", "position")
    for (p <- players) {
      string ++= "%-6s %-25s %-10s %-5s\n".format(p.index, p.name, p.money, p.currentPosition)
    }
    string.toString()
  }
}
