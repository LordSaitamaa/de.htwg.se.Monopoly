package de.htwg.se.Monopoly.controller

import de.htwg.se.Monopoly.model.{SpecialField, Street}

class Context {
  var state: State = _
  state = new StartState()

  def setPlayer(): Unit = {
    state.setPlayer(this)
  }

  def nextPlayer(): Unit = {
    state.nextPlayer(this)
  }

  def rollDice(controller: Controller): Unit = {
    state.handleField(this, controller)
  }

  def setState(s: State): Unit = {
    state = s
  }
}

trait State {
  def setPlayer(context: Context) {}
  def nextPlayer(context: Context) {}
  def handleField(context: Context, controller: Controller) {}
}

class StartState() extends State {
  override def setPlayer(context: Context): Unit = {
    context.setState(new NextPlayerState)
  }
}

class NextPlayerState() extends State {
  override def handleField(context: Context, controller: Controller): Unit = {
    val players = controller.players
    controller.actualField match {
      case s: Street =>
        if (s.owner.isEmpty) {
          context.setState(new BuyStreet)
        } else if (players(controller.currentPlayerIndex).index.equals(s.owner.orNull.index)) {
          context.setState(new NextPlayerState)
        } else {
          context.setState(new PayOtherPlayer)
        }
      case sp: SpecialField =>
        if (sp.index == 0) {
          context.setState(new LandedOnGo)
        } else if (sp.index == 10){
          context.setState(new VisitJail)
        } else if (sp.index == 20){
          context.setState(new FreeParking)
        } else if (sp.index == 30) {
          context.setState(new GoToJail)
        }
      case _ => context.setState(new NextPlayerState)
    }
  }
}

class BuyStreet() extends State {
  override def nextPlayer(context: Context): Unit = {
    context.setState(new NextPlayerState)
  }
}

class PayOtherPlayer() extends State {
  override def nextPlayer(context: Context): Unit = {
    context.setState(new NextPlayerState)
  }
}

class GoToJail() extends State {
  override def nextPlayer(context: Context): Unit = {
    context.setState(new NextPlayerState)
  }
}

class LandedOnGo() extends State {
  override def nextPlayer(context: Context): Unit = {
    context.setState(new NextPlayerState)
  }
}

class VisitJail() extends State {
  override def nextPlayer(context: Context): Unit = {
    context.setState(new NextPlayerState)
  }
}

class FreeParking() extends State {
  override def nextPlayer(context: Context): Unit = {
    context.setState(new NextPlayerState)
  }
}


