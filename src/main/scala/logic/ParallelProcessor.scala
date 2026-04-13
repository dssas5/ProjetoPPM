package logic

import scala.annotation.tailrec
import scala.collection.parallel.CollectionConverters.*
///////
/*
 T1 -Responsavel por gerar uma coordenada aleatoria
  para a proxima jogada a partir da lista de posições livres fornecidas
 */

case class MyRandom (seed: Long) {
  def nextInt: (Int, MyRandom) = {
    val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val nextRandom = MyRandom(newSeed)
    val n = (newSeed >>> 16).toInt
    (n, nextRandom)
  }
}


def randomMove(lstOpenCoords: List[Coord2D], rand: MyRandom):(Coord2D, MyRandom)  = {
  val (n, rng) = rand.nextInt
  val index = (n & Int.MaxValue) % lstOpenCoords.length
  (lstOpenCoords(index), rng)
}
///////
/*
 T2 -responsavel por devolver um novo tabuleiro movendo a pedra
indicada (coordFrom) para a coordenada fornecida (coordTo), se a mesma
representar uma jogada valida e None caso contrario. Para alem do tabuleiro, e
devolvida a nova lista de coordenadas livres
 */


/*
 Inicializa o tabuleiro com pedras alternadas e remove um par adjacente inicial.
 Por default (caso removedPair seja None), remove as duas pedras centrais.
 Caso seja fornecido um par, valida se é uma jogada de canto permitida.
 Devolve Some((Board, inicialOpenCoords)) em caso de sucesso, ou None se o par a remover for invalido.
 */

def createInitialBoard(width: Int, height: Int, removedPair: Option[(Coord2D,Coord2D)]): Option[(Board, List[Coord2D])] = {
  val coords = for {
    r <- 0 until height
    c <- 0 until width
  } yield (r, c)

  val boardData = coords.map { case (r, c) =>
    val stone = if ((r + c) % 2 == 0) Stone.Black else Stone.White
    ((r, c), stone)
  }.toMap
  val centerR = height / 2
  val centerC = width / 2

  val removedCoord1 = (centerR - 1, centerC - 1)
  val removedCoord2 = (centerR - 1, centerC)
  removedPair match {
    case Some(value) =>

      val corners = List (
      ((0, 0), (0, 1) ),
      ((0, 0), (1, 0) ),
      ((height - 1, 0), (height - 1, 1) ),
      ((height - 1, 0), (height - 2, 0) ),
      ((0, width - 2), (0, width - 1) ),
      ((0, width - 1), (1, width - 1) ),
      ((height - 1, width - 1), (height - 1, width - 2) ),
      ((height - 1, width - 1), (height - 2, width - 1) ),

      )
      val initialPossibleRemovals = (removedCoord1, removedCoord2) :: corners
      val validRemoval = initialPossibleRemovals.exists(par =>
        par == value || par == (value._2, value._1))
      if (validRemoval)
        val finalBoard = boardData - value._1 - value._2
        Some(finalBoard.par, List(value._1, value._2))
      else
        None

    case _ =>
      val finalBoard = boardData - removedCoord1 - removedCoord2
      val initialOpenCoords = List(removedCoord1, removedCoord2)
      Some(finalBoard.par, initialOpenCoords)
  }






}

def play(board: Board, player: Stone, coordFrom: Coord2D, coordTo: Coord2D, lstOpenCoords: List[Coord2D]): (Option[Board], List[Coord2D]) = {

  val (isValidJump, midPiece) = canPieceMoveToPos(board, player, coordTo, coordFrom)

  val conditions= (
    lstOpenCoords.contains(coordTo),
    board.get(coordFrom).contains(player),
    isValidJump
  )

  conditions match {
    case (true, true, true) =>

      val finalBoard = (board - midPiece - coordFrom) + (coordTo -> player)
      val finalOpenCoords = coordFrom :: midPiece :: lstOpenCoords.filterNot(_ == coordTo)
      (Some(finalBoard), finalOpenCoords)

    case _ =>
      (None, lstOpenCoords)
  }
}
/*
  Devolve as coordenadas adjacentes separadas por 2 de distancia
 */
private def getSpacedSurroundings(coord:Coord2D):List[Coord2D] = {
  List(
    (coord._1 - 2, coord._2), // Cima
    (coord._1 + 2, coord._2), // Baixo
    (coord._1, coord._2 - 2), // Esquerda
    (coord._1, coord._2 + 2) // Direita
  )
}

/*
  Verificacao de que a peca na posicao coorFrom pode se movimentar para coordFrom
 */

private def canPieceMoveToPos(board: Board, player: Stone, coordTo: Coord2D, coordFrom: Coord2D): (Boolean, Coord2D) = {


  val validJumps = getSpacedSurroundings(coordFrom)

  val midPiece = ((coordTo._1 + coordFrom._1) / 2, (coordTo._2 + coordFrom._2) / 2)


  if (validJumps.contains(coordTo)) {
    val isEnemyPiece = board.contains(midPiece) && !board.get(midPiece).contains(player)
    (isEnemyPiece, midPiece)
  } else {
    (false, midPiece)
  }
}

/*
 T3- responsavel por jogar de forma aleatoria numa das
coordenadas livres do tabuleiro. Para alem do tabuleiro, e devolvida a nova lista
de coordenadas livres, novo MyRandom, e coordenada para onde foi movimentada a
pedra.
 */

  def playRandomly(board:Board,
                    r:MyRandom,
                    player:Stone,
                    lstOpenCoords:List[Coord2D],
                    f:(List[Coord2D],MyRandom)=>(Coord2D,MyRandom)
                   ):(Option[Board],MyRandom,List[Coord2D],Option[Coord2D]) = {

  val (coordFrom, coordTo, newR1) = GiveRandomPlay(board, r, player, lstOpenCoords, f)

  (coordFrom, coordTo) match { //foi encontrado uma jogada valida
    case (Some(coorF),Some(coorT)) =>
      val (newBoard, newLstOpenCoords) = play (board = board, player = player, coordFrom = coorF, coordTo = coorT, lstOpenCoords = lstOpenCoords)

      (newBoard,newR1, newLstOpenCoords, coordTo)
    case _ =>
      (None, newR1, lstOpenCoords, None)
  }
}


/*
 Devolve um movimento random valido( coorFrom , coorTo, r ), caso nao exista devolve (None,r)
 */
@tailrec
private def GiveRandomPlay(board: Board, r: MyRandom, player: Stone, lstOpenCoords: List[Coord2D], f: (List[Coord2D], MyRandom) => (Coord2D, MyRandom)): (Option[Coord2D], Option[Coord2D], MyRandom) = {

  if (lstOpenCoords.isEmpty) {
    (None, None, r)
  } else {

    val (judgedOpenCoord, newR1) = f(lstOpenCoords, r) // escolher um does espacos vazios

    val playerPiecesAround = getSpacedSurroundings(judgedOpenCoord).filter(x => board.get(x).contains(player)) // possiveis pecas a volta

    val validJumpingPieces = playerPiecesAround.filter { pieceCoord =>
      play(board, player, pieceCoord, judgedOpenCoord, lstOpenCoords)._1.isDefined
    } // filtrar dos que estao a volta quais podem ser jogados

    if (validJumpingPieces.nonEmpty) { // existem pecas que podem ser jogadas
      val (chosenPiece, finalR) = f(validJumpingPieces, newR1)
      (Some(chosenPiece), Some(judgedOpenCoord), finalR)

    } else {
      //como nao existem pecas que sejam possiveis serem jogadas, remover o espaco vazio das oportunidades e voltar a tentar
      GiveRandomPlay(board, newR1, player, lstOpenCoords.filterNot(x =>  x == judgedOpenCoord), f)
    }
  }
}

/*
 T4- representar, visualmente, as jogadas no tabuleiro na linha de comando
 */

def boardToString(board: Board, width: Int, height: Int): String = {

  val header = " " * 5 + (0 until width)
    .map(c => f"${getColumnLetter(c)}%4s")
    .mkString("")


  val rows = (0 until height).map { r =>
    val rowLabel = f"$r%5d"
    val rowCells = (0 until width).map { c =>
      val symbol = board.get((r, c)) match {
        case Some(Stone.Black) => "B"
        case Some(Stone.White) => "W"
        case _ => "."
      }
      f"$symbol%4s"
    }.mkString("")

    rowLabel + rowCells
  }.mkString("\n")


  header + "\n\n" + rows
}

/*
 Devolve a letra da coluna, uma vez de A,..., Z, AA, AB,..,ZZ,...
 */

private def getColumnLetter(col:Int):String ={
  if(col < 26)
    ('A' + col).toChar.toString
  else
    getColumnLetter((col/26) - 1) + ('A' + (col % 26)).toChar.toString
}

