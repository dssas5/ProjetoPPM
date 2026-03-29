package logic

import scala.collection.parallel.immutable.ParMap

// Definições obrigatórias do enunciado:
type Coord2D = (Int, Int) // (linha, coluna) [cite: 78]

enum Stone: // Cores das pedras [cite: 81]
  case Black, White

type Board = ParMap[Coord2D, Stone] // O tabuleiro é um mapa paralelo

case class MyRandom (seed: Long) {
  def nextInt: (Int, MyRandom) = {
    val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val nextRandom = MyRandom(newSeed)
    val n = (newSeed >>> 16).toInt
    (n, nextRandom)
  }
}