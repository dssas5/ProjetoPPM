package logic

import scala.collection.parallel.immutable._

// Definicoes obrigatorias do enunciado:
type Coord2D = (Int, Int)

enum Stone:
  case Black, White

type Board = ParMap[Coord2D, Stone]
