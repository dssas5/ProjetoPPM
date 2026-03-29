package logic

import scala.collection.parallel.immutable.ParMap
import scala.collection.parallel.CollectionConverters.ImmutableMapIsParallelizable
import scala.collection.parallel.CollectionConverters.ImmutableIterableIsParallelizable
import scala.collection.parallel.CollectionConverters.MapIsParallelizable
import scala.collection.parallel.CollectionConverters.IterableIsParallelizable

def createInitialBoard(width: Int, height: Int): Board = {
  // 1. Gerar todas as combinações de (linha, coluna)
  val coords = for {
    r <- 0 until height
    c <- 0 until width
  } yield (r, c)

  // 2. Associar cada coordenada a uma cor e criar o mapa
  val boardData = coords.map { case (r, c) =>
    val stone = if ((r + c) % 2 == 0) Stone.Black else Stone.White
    ((r, c), stone)
  }.toMap

  // 3. Converter para ParMap (Obrigatório!)
  boardData.par
}

def lstOpenCoords()