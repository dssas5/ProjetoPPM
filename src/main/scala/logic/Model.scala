package logic

import scala.collection.parallel.immutable._

// Definições obrigatórias do enunciado:
type Coord2D = (Int, Int)

enum Stone:
  case Black, White

type Board = ParMap[Coord2D, Stone]
