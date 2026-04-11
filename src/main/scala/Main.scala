import logic.*
import logic.Stone.White

import scala.annotation.tailrec

object Main {
  def main(args: Array[String]): Unit = {

    val openCoordsForTest: List[Coord2D] =
      (for {
        x <- 0 until 10
        y <- 0 until 10
      } yield (x, y)).toList

    println("T1:Teste para RandomMove")

    loop(openCoordsForTest, MyRandom(System.currentTimeMillis()), 0, 10)
    println("________")
    println("T2-a:Teste inicializacao do tabuleiro")
    println()
    val (board,lstOpenCoords) = createInitialBoard(8,8)
    showBoard(board,8,8)
    println()
    print(s"Coordenadas vazias:$lstOpenCoords")
    println()
    println("________")
    println("T2-b:Teste primeira Jogada valida das brancas")
    val rng =  MyRandom(System.currentTimeMillis())
    val (board2, lstOpenCoords2) = play(board = board, player = White, coordFrom = (1,4), coordTo = (3,4), lstOpenCoords = lstOpenCoords)
    println()
    board2 match {
      case Some(board2) => showBoard(board2, 8, 8)
        println()
        print(s"Coordenadas vazias:$lstOpenCoords2")
      case None =>
    }
    println()
    println("________")
    println("T2-b:Teste segunda Jogada valida das pretas")
    println()
    board2 match {
      case Some(board2) =>
        val (board3, lstOpenCoords3) = play(board = board2, player = Stone.Black, coordFrom = (4, 4), coordTo = (2, 4), lstOpenCoords = lstOpenCoords2)
        board3 match{
          case Some(board3) => showBoard(board3,8,8)
            println()
            println(s"Coordenadas vazias:$lstOpenCoords3")
            println()
            println("________")
            println("T2-c:Teste jogada invalida brancas")
            val (board4, lstOpenCoords4) = play(board = board3, player = Stone.Black, coordFrom = (5, 4), coordTo = (3, 4), lstOpenCoords = lstOpenCoords3)
              board4 match {
              case Some(board4) => showBoard(board4,8,8)
              case None =>
                println()
                println("Erro")
                println()
                println("________")
                println("T3:Teste 2 jogadas aleatoria branca e depois preta")
                println()
                val (board5, rng2, lstOpenCoords5, coordPlayed) = playRandomly(board = board3, r = rng, player = Stone.White, lstOpenCoords = lstOpenCoords3, f = randomMove)
                board5 match{
                  case Some(board5) =>showBoard(board5,8,8)
                    println()
                    println(s"Coordenadas vazias:$lstOpenCoords5")
                    println(s"Movimento feito:$coordPlayed")
                    println()
                    val (board6, rng3, lstOpenCoords6, coordPlayed2) = playRandomly(board = board5, r = rng2, player = Stone.Black, lstOpenCoords = lstOpenCoords5, f = randomMove)
                    board6 match {
                      case Some(board6) => showBoard(board6, 8, 8)
                        println()
                        println(s"Coordenadas vazias:$lstOpenCoords5")
                        println(s"Movimento feito:$coordPlayed2")
                        println()
                    }
                }
            }
          case None =>

        }

      case None =>
    }
  }

  @tailrec
  private def loop(lstOpenCoords: List[Coord2D], rnd: MyRandom, counter: Int, limit: Int): Unit = {
    if (counter >= limit) ()
    else {
      val (nextCoord, nextRnd) = logic.randomMove(lstOpenCoords, rnd)

      println(s"${counter + 1}: $nextCoord")

      loop(lstOpenCoords, nextRnd, counter + 1, limit)
    }
  }

  private def showBoard(board:Board, width: Int, height: Int): Unit = {

    for (r <- 0 until height) {
      for (c <- 0 until width) {


        val symbol = board.get((r, c)) match {
          case Some(Stone.Black) => "B"
          case Some(Stone.White) => "W"
          case None => "." // Casa vazia
        }

        print(s"$symbol ")
      }
      println()
    }
  }
}