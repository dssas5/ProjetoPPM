import javafx.fxml.FXML
import javafx.scene.control.{Button, ComboBox, Label, TextField}
import javafx.scene.layout.{AnchorPane, GridPane, HBox, StackPane}
import javafx.scene.shape.Circle
import javafx.scene.paint.Color
import logic.*
import logic.GameMode.*
import javafx.animation.{Animation, KeyFrame, Timeline}
import javafx.application.Platform
import javafx.util.Duration
import javafx.scene.input.{KeyCode, KeyEvent}

class Controller {

  @FXML
  private var startMenu: StackPane = _
  @FXML
  private var starterCombo: ComboBox[String] = _
  @FXML
  private var timeInput: TextField = _
  @FXML
  private var removalInput: ComboBox[Int] = _

  @FXML
  private var winnerOverlay: StackPane = _
  @FXML
  private var clockLabel: Label = _
  @FXML
  private var winnerLabel: Label = _
  @FXML
  private var boardGrid: GridPane = _
  @FXML
  private var lowerBar: HBox = _
  @FXML
  private var playerLabel: Label = _
  @FXML
  private var coverPane: AnchorPane = _

  private var isOnTrain = false

  private var config: GameConfig =
    GameConfig(
      timeLimit = 60,
      dimensions = (6, 6),
      starter = Stone.Black,
      removalSpot = 9,
      mode = GameMode.PvP,
      pieceTrainEnabled = false
    )

  private var timeLeft: Int = config.timeLimit
  private var clockTimeline: Timeline = _

  private var gameHistory: GameHistory = Nil

  private var selectedFrom: Option[Coord2D] = None

  private var aiPathHistory: List[Option[List[Coord2D]]] = Nil

  private var rng = MyRandom(System.currentTimeMillis())

  @FXML
  def initialize(): Unit = {

    Platform.runLater(() => {
      coverPane.requestFocus()
    })

    starterCombo.getItems.addAll("Pretas", "Brancas")
    starterCombo.setValue("Pretas")

    timeInput.setText(config.timeLimit.toString)
    removalInput.getItems.addAll(1,2,3,4,5,6,7,8,9)
    removalInput.setValue(9)

    if winnerOverlay != null then
      hideWinner()

    if startMenu != null then
      startMenu.setVisible(true)
      startMenu.setManaged(true)
  }

  @FXML
  def onStartGameClicked(): Unit = {
    val chosenStarter =
      starterCombo.getValue match {
        case "Brancas" => Stone.White
        case _ => Stone.Black
      }

    val chosenTime =
      try {
        val value = timeInput.getText.trim.toInt
        if value > 0 then value else config.timeLimit
      } catch {
        case _: NumberFormatException => config.timeLimit
      }


    val chosenRemoval = removalInput.getValue


    config = config.copy(
      starter = chosenStarter,
      timeLimit = chosenTime,
      removalSpot = chosenRemoval
    )

    startMenu.setVisible(false)
    startMenu.setManaged(false)

    startNewGame()
  }

  private def startNewGame(): Unit = {
    val rows = config.dimensions._1
    val cols = config.dimensions._2

    val removalList = getRemovalList(rows, cols)

    createInitialBoard(
      width = cols,
      height = rows,
      removedPair = Some(removalList(config.removalSpot - 1))
    ) match {
      case Some((board, openCoords)) =>
        val initialState =
          GameState(
            board = board,
            openCoords = openCoords,
            currentPlayer = config.starter
          )

        gameHistory = List(initialState)
        selectedFrom = None
        aiPathHistory = List(None)
        isOnTrain = false
        hideWinner()
        drawBoard()
        startClock()



      case None =>
        println("Nao foi possivel criar tabuleiro")
    }
  }

  private def currentState: GameState =
    gameHistory.head

  private def drawBoard(): Unit = {
    addButtons()
    boardGrid.getChildren.clear()

    val rows = config.dimensions._1
    val cols = config.dimensions._2

    val validPaths = {
      getAllValidTurnPaths(
        currentState.board,
        currentState.currentPlayer,
        currentState.openCoords,
        config.pieceTrainEnabled
      )

    }
    val currentAiPath = aiPathHistory.headOption.flatten
    for row <- 0 until rows do
      for col <- 0 until cols do

        val coord = (row, col)

        val cell = new StackPane()
        cell.setPrefSize(100, 100)

        cell.setStyle("-fx-border-color: black; -fx-background-color: burlywood;")

        if( currentAiPath.exists(_.contains(coord))) {
          cell.setStyle("-fx-border-color: black; -fx-background-color: #a851a8;")
        }
        if (selectedFrom.isDefined && validPaths.exists(path => selectedFrom.contains(path.head) && path.drop(2).contains((row, col)))) {
          cell.setStyle("-fx-border-color: black; -fx-background-color: orange;")
        }
        if (selectedFrom.isDefined && validPaths.exists(path => selectedFrom.contains(path.head) && path.take(2).contains((row, col)))) {
          cell.setStyle("-fx-border-color: black; -fx-background-color: #dcdc6a;")
        }
        if (selectedFrom.contains((row, col))) {
          cell.setStyle("-fx-border-color: black; -fx-background-color: rgba(0,0,255,0.78);")
        }


        currentState.board.get(coord) match {
          case Some(Stone.Black) =>
            val piece = new Circle(22)
            piece.setFill(Color.BLACK)
            piece.setMouseTransparent(true)
            cell.getChildren.add(piece)


          case Some(Stone.White) =>
            val piece = new Circle(22)
            piece.setFill(Color.WHITE)
            piece.setStroke(Color.BLACK)
            piece.setMouseTransparent(true)
            cell.getChildren.add(piece)

          case None =>
            ()
        }

        cell.setOnMouseClicked(_ => handleCellClick(coord))

        boardGrid.add(cell, col, row)
  }

  private def handleCellClick(coord: Coord2D): Unit = {
    val state = currentState
    // logica movimento em pieceTrain
    if isOnTrain then {
      selectedFrom match {
        case Some(from) =>

          // Tocar peca em comboio termina piecetrain
          if coord == from then {
            endTrainTurn()
            return
          }


          if state.board.get(coord).contains(state.currentPlayer) then {
            println("Durante o pieceTrain só podes jogar com a mesma peça.")
            return
          }

          val validTrainPaths =
            validPathsFrom(
              state.board,
              state.currentPlayer,
              state.openCoords,
              from
            ).filter(path => path.last == coord)

          if validTrainPaths.isEmpty then {
            println("Movimento invalido durante o pieceTrain.")
            return
          }

          val chosenPath = validTrainPaths.sortWith(_.length < _.length).head

          playSequence(
            board = state.board,
            player = state.currentPlayer,
            path = chosenPath,
            openCoords = state.openCoords
          ) match {
            case (Some(newBoard), newOpenCoords) =>
              val newPiecePosition = chosenPath.last

              val canContinue = trainCanContinue(newBoard, state.currentPlayer, newOpenCoords, newPiecePosition)

              val nextPlayer =
                if canContinue then state.currentPlayer
                else otherPlayer(state.currentPlayer)

              val nextState = GameState(board = newBoard, openCoords = newOpenCoords, currentPlayer = nextPlayer)

              // Todos os movimentos em pieceTrain contam como 1, ent trocar a head
              gameHistory = nextState :: gameHistory.tail
              aiPathHistory = None :: aiPathHistory.tail

              if canContinue then {
                isOnTrain = true
                selectedFrom = Some(newPiecePosition)
                drawBoard()
              } else {
                isOnTrain = false
                selectedFrom = None
                drawBoard()

                config.mode match {
                  case PvE(t) =>
                    doAiMove(nextState, t)
                    startClock()

                  case _ =>
                    startClock()
                }
              }

              drawBoard()

              if playerWon(newBoard, state.currentPlayer, newOpenCoords) then {
                val whoWon =
                  state.currentPlayer match {
                    case Stone.Black => "Pretas"
                    case Stone.White => "Brancas"
                  }

                showWinner(s"As $whoWon Ganharam!")
              }

            case (None, _) =>
              println("Movimento invalido durante o pieceTrain.")
          }

        case None =>
          isOnTrain = false
          selectedFrom = None
          drawBoard()
      }


    }
    // logica movimento normal
    else {
      selectedFrom match {
        case None =>
          selectedFrom =
            if state.board.get(coord).contains(state.currentPlayer) then Some(coord)
            else None

          drawBoard()

        case Some(from) =>
          if state.board.get(coord).contains(state.currentPlayer) then {
            selectedFrom = Some(coord)
            drawBoard()
          } else {
            val to = coord

            val validPaths =
              getAllValidTurnPaths(state.board, state.currentPlayer, state.openCoords, config.pieceTrainEnabled).filter(path => path.head == from && path.last == to)

            if validPaths.isEmpty then {
              println("Movimento invalido")
              selectedFrom = None
              drawBoard()
              return
            }

            val chosenPath = validPaths.sortWith(_.length < _.length).head

            playSequence(board = state.board, player = state.currentPlayer, path = chosenPath, openCoords = state.openCoords) match {
              case (Some(newBoard), newOpenCoords) =>
                val movedPieceNewPosition = chosenPath.last

                val canStartTrain = trainCanContinue(newBoard, state.currentPlayer, newOpenCoords, movedPieceNewPosition)

                val nextPlayer = //se puder continuar a jogar com a peca, n trocar jogador
                  if canStartTrain then state.currentPlayer
                  else otherPlayer(state.currentPlayer)

                val nextState = GameState(board = newBoard, openCoords = newOpenCoords, currentPlayer = nextPlayer)

                gameHistory = nextState :: gameHistory
                aiPathHistory = None :: aiPathHistory

                if canStartTrain then {
                  isOnTrain = true
                  selectedFrom = Some(movedPieceNewPosition)
                } else {
                  isOnTrain = false
                  selectedFrom = None
                  startClock()
                }

                drawBoard()

                config.mode match {
                  case PvE(t) =>
                    if playerWon(newBoard, state.currentPlayer, newOpenCoords) then {
                      val whoWon =
                        state.currentPlayer match {
                          case Stone.Black => "Pretas(Jogador)"
                          case Stone.White => "Brancas(Jogador)"
                        }

                      showWinner(s"As $whoWon Ganharam!")
                    } else if !canStartTrain then {
                      doAiMove(nextState, t)
                      startClock()
                    }

                  case _ =>
                    if playerWon(newBoard, state.currentPlayer, newOpenCoords) then {
                      val whoWon =
                        state.currentPlayer match {
                          case Stone.Black => "Pretas"
                          case Stone.White => "Brancas"
                        }

                      showWinner(s"As $whoWon Ganharam!")
                    }
                }

              case (None, _) =>
                println("Movimento invalido")
                selectedFrom = None
                drawBoard()
            }
          }
      }
    }
  }

  private def doAiMove(nextState: GameState, difficulty: Int): Unit = {
    getAiMove(nextState.board, nextState.currentPlayer, nextState.openCoords, difficulty, config.pieceTrainEnabled, rng) match {
      case (Some((aiFoundPath, rating)), newRng) =>
        rng = newRng
        playSequence(nextState.board, nextState.currentPlayer, aiFoundPath, nextState.openCoords) match {
          case (Some(seqBoard), seqLstOpenCoords) =>
            val nPlayer = nextState.currentPlayer match {
              case Stone.Black => Stone.White
              case Stone.White => Stone.Black
            }

            val nextAiState = GameState(
              board = seqBoard,
              openCoords = seqLstOpenCoords,
              currentPlayer = nPlayer
            )
            val curAiPlayer = nextState.currentPlayer
            gameHistory = nextAiState :: gameHistory
            aiPathHistory = Some(aiFoundPath) :: aiPathHistory
            drawBoard()
            if(playerWon(seqBoard,curAiPlayer,seqLstOpenCoords))
              val whoWon = curAiPlayer match{
                case Stone.Black => "Pretas(AI)"
                case Stone.White => "Brancas(AI)"
              }
              showWinner(s"As $whoWon Ganharam!")


          case _ =>
            println("AI nao conseguiu jogar")
        }
      case _ =>
        println("AI nao encontrou caminho")
    }
  }

  def onUndoClicked(): Unit = {
    if gameHistory.length > 2 then
      gameHistory = gameHistory.drop(2)
      aiPathHistory = aiPathHistory.drop(2)
      selectedFrom = None
      drawBoard()
      startClock()
    else
      println("Impossivel voltar atras")
  }

  def onRestartClicked(): Unit = {
    if clockTimeline != null then
      clockTimeline.stop()

    selectedFrom = None
    hideWinner()

    startMenu.setVisible(true)
    startMenu.setManaged(true)
  }

  def onPvpClicked(): Unit = {
    config = config.copy(mode = GameMode.PvP)
    drawBoard()
  }

  def onPveClicked(): Unit = {
    config.mode match {
      case PvP => config = config.copy(mode = PvE(1))
      case PvE(t) => config = config.copy(mode = PvE(t%3 + 1))
    }
    drawBoard()
  }

  def addButtons(): Unit = {
    lowerBar.setStyle("-fx-border-color: black; -fx-background-color: #e3a85d")
    lowerBar.getChildren.clear()
    val undoButton = new Button("Undo")
    undoButton.setOnAction(_ =>{
      onUndoClicked()
    })

    lowerBar.getChildren.add(undoButton)

    val restartButton = new Button("Restart")

    restartButton.setOnAction(_ =>{
      onRestartClicked()
    })
    lowerBar.getChildren.add(restartButton)

    val pvpButton = new Button("PvP")
    pvpButton.setOnAction(_ => {
      onPvpClicked()
    })

    lowerBar.getChildren.add(pvpButton)
    var pvpButtonName = config.mode match {
      case PvE(t) => if( t == 1) "PvE(facil)" else if(t == 2) "PvE(medio)" else "PvE(dificil)"
      case PvP => "PvE"
    }
    var pveButton = new Button(pvpButtonName)
    pveButton.setOnAction(_ =>{
      onPveClicked()
    })

    lowerBar.getChildren.add(pveButton)
    var pieceTrainName = if( config.pieceTrainEnabled) "pieceTrain:Ativado" else "pieceTrain:Desativado"
    var pieceTrainButton = new Button(pieceTrainName)
    pieceTrainButton.setOnAction(_ =>{
      onPieceTrainClicked()
    })
    lowerBar.getChildren.add(pieceTrainButton)

    playerLabel.setText(  currentState.currentPlayer match{
      case Stone.White => "Brancas a jogar!"
      case Stone.Black => "Pretas a jogar!"
    })

  }

  def onPieceTrainClicked(): Unit = {
    config = config.copy(pieceTrainEnabled = !config.pieceTrainEnabled)
    drawBoard()
  }

  private def showWinner(message: String): Unit = {
    if clockTimeline != null then
      clockTimeline.stop()
    winnerLabel.setText(message)
    winnerOverlay.setVisible(true)
    winnerOverlay.setManaged(true)
  }

  private def hideWinner(): Unit = {
    winnerOverlay.setVisible(false)
    winnerOverlay.setManaged(false)
  }

  private def startClock(): Unit = {
    if clockTimeline != null then
      clockTimeline.stop()

    timeLeft = config.timeLimit

    if clockLabel != null then
      clockLabel.setText(s"Tempo: $timeLeft")

    clockTimeline = new Timeline(
      new KeyFrame(
        Duration.seconds(1),
        _ => {
          timeLeft -= 1

          if clockLabel != null then
            clockLabel.setText(s"Tempo: $timeLeft")

          if timeLeft <= 0 then
            clockTimeline.stop()
            val winner =currentState.currentPlayer match {
              case Stone.White => "Pretas"
              case Stone.Black => "Brancas"
            }
            showWinner(s"$winner ganham por tempo!")
        }
      )
    )

    clockTimeline.setCycleCount(Animation.INDEFINITE)
    clockTimeline.play()
  }

  private def otherPlayer(player: Stone): Stone =
    player match {
      case Stone.Black => Stone.White
      case Stone.White => Stone.Black
    }

  private def validPathsFrom(board: Board, player: Stone, openCoords: List[Coord2D], from: Coord2D): List[List[Coord2D]] = {
    getAllValidTurnPaths(board, player, openCoords, config.pieceTrainEnabled).filter(path => path.head == from)
  }

  private def trainCanContinue(board: Board, player: Stone, openCoords: List[Coord2D], from: Coord2D): Boolean = {
    config.pieceTrainEnabled &&
      validPathsFrom(board, player, openCoords, from).nonEmpty
  }

  private def endTrainTurn(): Unit = {
    val state = currentState

    val nextState =
      state.copy(
        currentPlayer = otherPlayer(state.currentPlayer)
      )

    gameHistory = nextState :: gameHistory.tail
    selectedFrom = None
    isOnTrain = false
    aiPathHistory = None :: aiPathHistory.tail

    drawBoard()

    config.mode match {
      case PvE(t) =>
        doAiMove(nextState, t)
        startClock()

      case _ =>
        startClock()
    }
  }

  @FXML
  def onCoverKeyPressed(event: KeyEvent): Unit = {
    if event.getCode == KeyCode.ENTER then
      coverPane.setVisible(false)
      coverPane.setManaged(false)
  }
}