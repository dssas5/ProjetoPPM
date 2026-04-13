import logic.*



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

    createInitialBoard(8, 8, None) match {
      case Some((board, lstOpenCoords)) =>
        println(boardToString(board, 8, 8))
        println()
        print(s"Coordenadas vazias:$lstOpenCoords")
        println()
        println("________")
        println("T2-b:Teste primeira Jogada valida das brancas")

        val rng = MyRandom(System.currentTimeMillis())
        val (board2Option, lstOpenCoords2) = play(board = board, player = Stone.White, coordFrom = (1, 4), coordTo = (3, 4), lstOpenCoords = lstOpenCoords)
        println()

        board2Option match {
          case Some(board2) =>
            print(boardToString(board2, 8, 8))
            println()
            print(s"Coordenadas vazias:$lstOpenCoords2")
            println()
            println("________")
            println("T2-b:Teste segunda Jogada valida das pretas")
            println()

            val (board3Option, lstOpenCoords3) = play(board = board2, player = Stone.Black, coordFrom = (4, 4), coordTo = (2, 4), lstOpenCoords = lstOpenCoords2)

            board3Option match {
              case Some(board3) =>
                print(boardToString(board3, 8, 8))
                println()
                println(s"Coordenadas vazias:$lstOpenCoords3")
                println()
                println("________")
                println("T2-c:Teste jogada invalida brancas")



                val (board4Option, lstOpenCoords4) = play(board = board3, player = Stone.Black, coordFrom = (5, 4), coordTo = (3, 4), lstOpenCoords = lstOpenCoords3)

                board4Option match {
                  case Some(board4) =>
                    print(boardToString(board4, 8, 8))
                  case None =>
                    println()
                    println("Erro (Esperado: Jogada Invalida)")
                    println()
                    println("________")
                    println("T2-d: Testes de Inicializacao com Cantos (Tabuleiro 8x8)")
                    println()

                    // As 8 combinacoes possiveis para um tabuleiro 8x8
                    val validCorners8x8 = List(
                      ((0, 0), (0, 1)), ((0, 0), (1, 0)),
                      ((7, 0), (7, 1)), ((7, 0), (6, 0)),
                      ((0, 6), (0, 7)), ((0, 7), (1, 7)),
                      ((7, 7), (7, 6)), ((7, 7), (6, 7))
                    )

                    validCorners8x8.foreach { cornerPair =>
                      createInitialBoard(8, 8, Some(cornerPair)) match {
                        case Some(a) =>
                          println("====")
                          println(s"[Sucesso] Tabuleiro inicializado corretamente removendo: $cornerPair")
                          print(boardToString(a._1,8,8))

                          println()
                        case None =>
                          println("====")
                          println(s"[FALHA] O sistema rejeitou um canto valido: $cornerPair")
                      }
                      println("====")
                    }

                    println()
                    println("________")
                    println("T2-e: Teste de Inicializacao Invalida")
                    println()

                    // Uma dupla de pecas adjacentes, mas que NAO estao nem num canto nem no centro perfeito
                    val invalidPair = ((2, 2), (2, 3))

                    println(s"A tentar inicializar com remocao invalida: $invalidPair...")

                    createInitialBoard(8, 8, Some(invalidPair)) match {
                      case Some(_) =>
                        println("====")
                        println(s"[FALHA] O sistema aceitou uma remocao que viola as regras!")
                      case None =>
                        println("====")
                        println(s"[Sucesso] O sistema bloqueou a jogada invalida e devolveu None perfeitamente.")
                    }
                    println("====")
                    println("________")
                    println("T3:Teste 2 jogadas aleatorias branca e depois preta")
                    println()

                    val (board5Option, rng2, lstOpenCoords5, coordPlayed) = playRandomly(board = board3, r = rng, player = Stone.White, lstOpenCoords = lstOpenCoords3, f = randomMove)

                    board5Option match {
                      case Some(board5) =>
                        print(boardToString(board5, 8, 8))
                        println()
                        println(s"Coordenadas vazias:$lstOpenCoords5")
                        println(s"Movimento feito:$coordPlayed")
                        println()

                        val (board6Option, rng3, lstOpenCoords6, coordPlayed2) = playRandomly(board = board5, r = rng2, player = Stone.Black, lstOpenCoords = lstOpenCoords5, f = randomMove)

                        board6Option match {
                          case Some(board6) =>
                            print(boardToString(board6, 8, 8))
                            println()
                            println(s"Coordenadas vazias:$lstOpenCoords6")
                            println(s"Movimento feito:$coordPlayed2")
                            println()
                          case None => // nada
                        }
                      case None => // nada
                    }
                }
              case None => println("Erro na segunda jogada")
            }
          case None => println("Erro na primeira jogada")
        }
      case None => println("Erro: Tabuleiro inicial nao gerado!")
    }

    println()
    println("________")
    println("T4:Teste limites do tabuleiro de tamanho variavel ")
    println()

    val num1 = 30
    val num2 = 30

    createInitialBoard(num1, num2, None) match {
      case Some((testBoardSize, list)) =>
        print(boardToString(testBoardSize, num1, num2))
      case None =>
        println("Erro a inicializar o tabuleiro pequeno!")
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
}