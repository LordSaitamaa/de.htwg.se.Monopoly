package de.htwg.se.Monopoly.aview

import de.htwg.se.Monopoly.controller.{BuyStreet, Controller, NextPlayerState, StartState}
import de.htwg.se.Monopoly.util.Observer

class Tui(controller: Controller) extends Observer {

  controller.add(this)
  var numPlayer: Int = 0

  def processInputLine(input: String): Unit = {
    val pattern = "p (.*)".r
    input match {
      case "help" =>
        printf("%-10s%s\n%-10s%s\n", "e", "exit", "p", "new Players")
      case "e" => print("exit Game\n")
      case "z" => controller.undo
      case "y" => controller.redo
      case "d" =>
        if (controller.context.state.isInstanceOf[NextPlayerState]) {
          controller.rollDice()
        }
      case pattern(input) =>
        if (controller.context.state.isInstanceOf[StartState]) {
          setPlayers(input.toString)
        }
      case "J" =>
        if (controller.context.state.isInstanceOf[BuyStreet]) {
          controller.buyStreet()
        }
      case "N" =>
        if (controller.context.state.isInstanceOf[BuyStreet]) {
          controller.nextPlayer()
        }
      case _ => print("Kein Pattern matching!")
    }
  }

  def setPlayers(input: String): Unit = {
    val list = input.split(" ")
    controller.setPlayers(list)
  }

  override def update: Boolean = {
    var output = ""
    if(controller.context.state.isInstanceOf[NextPlayerState]) {
      print(controller.gameToString)
    }
    output += "Aktueller Spieler: " + controller.currentPlayerIndex + "\n"
    print(output)
    true
  }
}
