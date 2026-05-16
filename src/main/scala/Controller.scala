import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.{GridPane, HBox, StackPane}
import javafx.scene.shape.Circle
import javafx.scene.paint.Color
import logic.*

class Controller {

  @FXML
  private var boardGrid: GridPane = _
  @FXML
  private var lowerBar: HBox = _
  private var config: GameConfig =
    GameConfig(
      timeLimit = 60,
      dimensions = (6, 6),
      starter = Stone.Black,
      removalSpot = 9,
      mode = GameMode.PvP,
      pieceTrainEnabled = false
    )

  private var gameHistory: GameHistory = Nil

  private var selectedFrom: Option[Coord2D] = None

  @FXML
  def initialize(): Unit = {
    addButtons()
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
        drawBoard()



      case None =>
        println("Nao foi possivel criar tabuleiro")
    }
  }

  private def currentState: GameState =
    gameHistory.head

  private def drawBoard(): Unit = {
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

    for row <- 0 until rows do
      for col <- 0 until cols do

        val coord = (row, col)

        val cell = new StackPane()
        cell.setPrefSize(100, 100)


        cell.setStyle("-fx-border-color: black; -fx-background-color: burlywood;")
        if (selectedFrom.isDefined && validPaths.exists(path => selectedFrom.contains(path.head) && path.contains((row, col)))) {
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
    selectedFrom match {
      case None =>
        selectedFrom =
          if currentState.board.get(coord).contains(currentState.currentPlayer) then Some(coord)
          else None

        drawBoard()

      case Some(from) =>
        val state = currentState
        if (state.board.get(coord).contains(state.currentPlayer)){
          selectedFrom = Some(coord)
          drawBoard()
        }else {
          val to = coord
          val state = currentState

          play(
            board = state.board,
            player = state.currentPlayer,
            coordFrom = from,
            coordTo = to,
            lstOpenCoords = state.openCoords
          ) match {
            case (Some(newBoard), newOpenCoords) =>
              val nextPlayer =
                state.currentPlayer match {
                  case Stone.Black => Stone.White
                  case Stone.White => Stone.Black
                }

              val nextState =
                GameState(
                  board = newBoard,
                  openCoords = newOpenCoords,
                  currentPlayer = nextPlayer
                )

              gameHistory = nextState :: gameHistory
              selectedFrom = None
              drawBoard()

            case (None, _) =>
              println("movimento Invalido")
              selectedFrom = None
              drawBoard()
          }
        }
    }
  }

  @FXML
  def onUndoClicked(): Unit = {
    if gameHistory.length > 2 then
      gameHistory = gameHistory.drop(2)
      selectedFrom = None
      drawBoard()
    else
      println("Impossivel voltar atras")
  }

  @FXML
  def onRestartClicked(): Unit = {
    selectedFrom = None
    startNewGame()
  }
  def addButtons(): Unit = {
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
  }

}