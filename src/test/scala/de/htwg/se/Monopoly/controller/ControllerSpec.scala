package de.htwg.se.Monopoly.controller

import java.awt.Color

import de.htwg.se.Monopoly.controller.controllerComponent.controllerBaseImpl.{BuyStreet, Controller, GameOverState, GoToJail, NextPlayerState}
import de.htwg.se.Monopoly.model.boardComponent.boardBaseImpl
import de.htwg.se.Monopoly.model.boardComponent.boardBaseImpl.Board
import de.htwg.se.Monopoly.model.fieldComponent.fieldBaseImpl
import de.htwg.se.Monopoly.model.fieldComponent.fieldBaseImpl.{ChanceCard, Street, Tax}
import de.htwg.se.Monopoly.model.playerComponent.playerBaseImpl
import de.htwg.se.Monopoly.model.playerComponent.playerBaseImpl.Player
import de.htwg.se.Monopoly.model.{NeighbourhoodTypes, Variable}
import org.scalatest._
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ControllerSpec extends WordSpec with Matchers {

  "A Controller" when {
    val board = Board(Variable.START_BOARD)
    val controller = new Controller(board)
    "set Players" should {
      val list = Array[String]("player1 Car", "player2 Hut")
      "createNewPlayer not with difault figures" in {
        controller.setPlayers(list)
      }
    }
    "buy Street without enough money" in {
      controller.players = Vector[Player](playerBaseImpl.Player("player1", 0, 0, 0, 10, "Cat", Color.BLUE), playerBaseImpl.Player("player2", 1, 0, 0, 2, "Cat", Color.ORANGE))
      controller.context.setPlayer()
      controller.movePlayer(3)
      controller.context.state.isInstanceOf[BuyStreet] should be (true)
      controller.buyStreet()
      controller.board.fields should be (board.fields)
      controller.context.state.isInstanceOf[NextPlayerState] should be (true)
      controller.nextPlayer()
    }
    "land on street from other Player, can't pay rent" in {
      val street = Street(3, "Turmstrasse", NeighbourhoodTypes.Brown, 60, 4, playerBaseImpl.Player("player1", 0, 0, 0, 10, "Cat", Color.BLUE))
      controller.board = boardBaseImpl.Board(controller.board.fields.updated(3, street))
      controller.currentPlayerIndex should be (1)
      controller.movePlayer(3)
      controller.context.state.isInstanceOf[GameOverState] should be (true)
    }
  }
  "A second Controller" when {
    val board = boardBaseImpl.Board(Variable.START_BOARD)
    val controller = new Controller(board)
    controller.players = Vector[Player](playerBaseImpl.Player("player1", 0, 0, 0, 1500, "Cat", Color.BLUE), playerBaseImpl.Player("player2", 1, 0, 0, 1500, "Car", Color.ORANGE))
    controller.context.setPlayer()
    "land on own Street" in {
      val street = fieldBaseImpl.Street(3, "Turmstrasse", NeighbourhoodTypes.Brown, 60, 4, playerBaseImpl.Player("player1", 0, 0, 0, 1500, "Cat", Color.BLUE))
      controller.board = boardBaseImpl.Board(controller.board.fields.updated(3, street))
      controller.currentPlayerIndex should be (0)
      controller.movePlayer(3)
      controller.actualField should be (fieldBaseImpl.Street(3, "Turmstrasse", NeighbourhoodTypes.Brown, 60, 4, playerBaseImpl.Player("player1", 0, 0, 0, 1500, "Cat", Color.BLUE)))
      controller.context.state.isInstanceOf[NextPlayerState] should be (true)
      controller.nextPlayer()
    }
    "land on one of two Untility Street owned by different players" in {
      val street = fieldBaseImpl.Street(12, "Elektrizitätswerk", NeighbourhoodTypes.Utility, 150, 0, playerBaseImpl.Player("player1", 0, 0, 0, 1500, "Cat", Color.BLUE))
      controller.board = boardBaseImpl.Board(controller.board.fields.updated(12, street))
      controller.currentPlayerIndex should be (1)
      controller.movePlayer(12)
      controller.actualField should be (fieldBaseImpl.Street(12, "Elektrizitätswerk", NeighbourhoodTypes.Utility, 150, 0, playerBaseImpl.Player("player1", 0, 0, 0, 1500, "Cat", Color.BLUE)))
      controller.nextPlayer()
    }
    "land on one of two utility Streets owned by the same player" in {
      controller.currentPlayerIndex should be (0)
      controller.nextPlayer()
      val street = fieldBaseImpl.Street(28, "Wasserwerk", NeighbourhoodTypes.Utility, 150, 0, playerBaseImpl.Player("player1", 0, 0, 0, 1500, "Cat", Color.BLUE))
      controller.board = boardBaseImpl.Board(controller.board.fields.updated(28, street))
      controller.currentPlayerIndex should be (1)
      controller.movePlayer(16)
      controller.actualField should be (fieldBaseImpl.Street(28, "Wasserwerk", NeighbourhoodTypes.Utility, 150, 0, playerBaseImpl.Player("player1", 0, 0, 0, 1500, "Cat", Color.BLUE)))
      controller.nextPlayer()
    }
  }
  "A third Controller" when {
    val board = boardBaseImpl.Board(Variable.START_BOARD)
    val controller = new Controller(board)
    controller.players = Vector[Player](playerBaseImpl.Player("player1", 0, 0, 0, 1500, "Cat", Color.BLUE), playerBaseImpl.Player("player2", 1, 2, 0, 1500, "Car", Color.ORANGE))
    controller.context.setPlayer()
    "land on Tax field" in {
      controller.movePlayer(4)
      controller.actualField should be (Tax(4, "Einkommenssteuer", 200))
      controller.context.state.isInstanceOf[NextPlayerState] should be (true)
      controller.nextPlayer()
    }
    "land on 'Ereignisfeld'" in {
      controller.currentPlayerIndex should be (1)
      val chanceCards: List[ChanceCard] = List[ChanceCard](
        ChanceCard(2, "Gemeinschaftsfeld", 1, 100, 0, -1, "Ereigniskarte: Die Bank gibt dir 100$.\n"),
        ChanceCard(2, "Gemeinschaftsfeld", 2, 50, 0, -1, "Ereigniskarte: Du hast eine Fashion-Wettbewerb gewonnen.\n Erhalte 50$.\n"),
        ChanceCard(2, "Gemeinschaftsfeld", 3, -30, 30, 0, "Ereigniskarte: Gib dem anderen Spieler 30.\n"),
        ChanceCard(2, "Gemeinschaftsfeld", 4, 0, 0, -1, "Ereigniskarte: Gehe ins Gefängnis!\n"),
        ChanceCard(2, "Gemeinschaftsfeld", 3, -30, 30, 0, "Ereigniskarte: Gib dem anderen Spieler 30.\n")
      )

      for (cC <- chanceCards) {
        controller.handleChanceCard(cC)
        cC match {
          case ChanceCard(2, "Gemeinschaftsfeld", 1, 100, 0, -1, "Ereigniskarte: Die Bank gibt dir 100$.\n") =>
            controller.players(1) should be (playerBaseImpl.Player("player2", 1, 2, 0, 1600, "Car", Color.ORANGE))

          case ChanceCard(2, "Gemeinschaftsfeld", 2, 50, 0, -1, "Ereigniskarte: Du hast eine Fashion-Wettbewerb gewonnen.\n Erhalte 50$.\n") =>
            controller.players(1) should be (playerBaseImpl.Player("player2", 1, 2, 0, 1650, "Car", Color.ORANGE))

          case ChanceCard(2, "Gemeinschaftsfeld", 3, -30, 30, 0, "Ereigniskarte: Gib dem anderen Spieler 30.\n") =>
            if (controller.players(1).equals(playerBaseImpl.Player("player2", 1, 2, 0, 1620, "Car", Color.ORANGE))) {
              controller.players(1) should be (playerBaseImpl.Player("player2", 1, 2, 0, 1620, "Car", Color.ORANGE))
              controller.players(0) should be (playerBaseImpl.Player("player1", 0, 4, 0, 1330, "Cat", Color.BLUE))
              controller.context.setState(new GoToJail)
            } else {
              controller.context.state.isInstanceOf[GameOverState] should be (true)
            }

          case ChanceCard(2, "Gemeinschaftsfeld", 4, 0, 0, -1, "Ereigniskarte: Gehe ins Gefängnis!\n") =>
            controller.players(1) should be (playerBaseImpl.Player("player2", 1, 10, 2, 1620, "Car", Color.ORANGE))
            controller.context.setState(new NextPlayerState)
            controller.players = Vector[Player](playerBaseImpl.Player("player1", 0, 0, 0, 10, "Cat", Color.BLUE), playerBaseImpl.Player("player2", 1, 2, 0, 10, "Car", Color.ORANGE))
        }
      }
    }
  }
}