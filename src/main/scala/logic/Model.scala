package logic

import scala.collection.parallel.immutable._

// Definicoes obrigatorias do enunciado:
type Coord2D = (Int, Int)

enum Stone:
  case Black, White

type Board = ParMap[Coord2D, Stone]

case class GameState(
                      board: Board,
                      openCoords: List[Coord2D],
                      currentPlayer: Stone
                    )
enum GameMode:
  case PvP
  case PvE(difficulty: Int)
  
case class GameConfig(
                       timeLimit: Int,
                       dimensions: (Int, Int),
                       starter: Stone,
                       removalSpot: Int,
                       mode: GameMode,
                       pieceTrainEnabled: Boolean
                     )
type GameHistory = List[GameState]

enum PlayerAction:
  case Move(from: Coord2D, to: Coord2D)
  case Undo
  case Restart
  case ChngDim(rows: Int, cols: Int)
  case ChngTime(time: Int)
  case Pvp
  case Pve(diff: Int)
  case PieceTrain(state: Boolean)