import logic.*

import scala.annotation.tailrec
import scala.io.StdIn.*


object TUI {
  def main(args: Array[String]): Unit = {
    setConfigGame() match {
      case Some(config) =>
        println("___________________________________\n")
        explainCommands()

        val height = config.dimensions._1
        val width = config.dimensions._2

        val removalList = getRemovalList(height,width)

        createInitialBoard(width, height, Some(removalList(config.removalSpot - 1))) match {
          case Some((board, lstOpenCoords)) =>
            val initialGameState = GameState(board, lstOpenCoords, config.starter)
            val gameHistory: GameHistory = List(initialGameState)
            gameCycle(gameHistory, config)

          case _ =>
            println("Erro inesperado a criar o tabuleiro inicial! ln 37")
        }

      case _ =>
        println("Jogo cancelado.")
    }
  }

  // retorna as configuracoes introduzidas pelo jogador
  @tailrec
  private def setConfigGame(): Option[GameConfig] = {
    println("temporizador (em segundos):")
    val time = readLine().trim.toIntOption.getOrElse(-1) // Mais seguro que readInt!

    println("dimensoes do tabuleiro (R espaco C):")
    val formatDim = """^(\d+)\s+(\d+)$""".r
    val (row, col) = readLine().trim match {
      case formatDim(r, c) => (r.toInt, c.toInt)
      case _ => (-1, -1)
    }

    println("Quem comeca (P ou B) [Colocar um input invalido colocara como padrao as brancas]:")
    val stone = readLine().trim.toUpperCase match {
      case "P" => Stone.Black
      case _ => Stone.White
    }

    println(
      """Escolhe o numero da opcao que corresponde a localizacao das duas pecas removidas:
        |
        | 1) Canto superior esquerdo horizontal
        | 2) Canto superior esquerdo vertical
        | 3) Canto superior direito horizontal
        | 4) Canto superior direito vertical
        | 5) Canto inferior esquerdo horizontal
        | 6) Canto inferior esquerdo vertical
        | 7) Canto inferior direito horizontal
        | 8) Canto inferior direito vertical
        | 9) Centro (padrao, colocar valores invalidos ou nao colocar input escolhera este)
        | """.stripMargin)

    val removalSpot = readLine().trim match {
      case t if t.nonEmpty =>
        val num = t.toIntOption.getOrElse(9)
        if (num >= 9 || num < 1) 9 else num
      case _ => 9
    }

    val criteria = (time > 0, row > 0, col > 0)

    criteria match {
      case (true, true, true) =>
        val successText =
          s"""As configuracoes foram bem sucedidas:
             |
             |Jogador Inicial: ${stone match { case Stone.White => "Brancas" case Stone.Black => "Pretas" }}
             |Dimensao do Tabuleiro: ${row}X$col
             |Temporizador por jogada: ${timeConverterString(time)}
             |Posicao de Pecas Removidas: $removalSpot
             |""".stripMargin
        println(successText)

        // Criamos o objeto GameConfig!
        Some(GameConfig(time, (row, col), stone, removalSpot, GameMode.PvP, false))

      case _ =>
        println("Houve um problema com a configuracao: valores invalidos introduzidos. Recomecar configuracao (Y/N)?")
        println()
        readLine().trim.toUpperCase match {
          case "Y" => setConfigGame()
          case _ => None
        }
    }
  }

  private def explainCommands(): Unit = {
    println(
      """Para comecar qualquer commando introduzir "cmd" + space + comando desejado (comandos sao case insensitive):
        |
        |1) undo - voltar ao estado anterior
        |2) restart - recomecar o jogo
        |3) pvp - jogador contra jogador (padrao)
        |4) pve param - jogador contra AI (param e a dificuldade: 1-facil, 2-medio, 3-dificil)
        |5) chng-dim param1 param2 - reinicia o jogo do zero com as novas dimensoes (caso invalidas nao faz nada)
        |6) chng-time param1 - muda o temporizador por jogada
        |7) piecetrain (1 or 0) - abilita ou desabilita comer pecas inimigas em serie ( por padrao e 0)
        |""".stripMargin)
  }

  @tailrec
  private def askInput(timeLimitSec: Int): Option[(String, (Coord2D, Coord2D), Int)] = {
    println(s"Tens $timeLimitSec segundos para jogar!")
    val startTime = System.currentTimeMillis() / 1000

    val input = scala.io.StdIn.readLine("Input: ").trim

    val endTime = System.currentTimeMillis() / 1000
    val elapsedTime = endTime - startTime

    println(s"Demoraste: ${timeConverterString(elapsedTime.toInt)}")

    if (elapsedTime > timeLimitSec) {
      println("TEMPO ESGOTADO!")
      None
    } else {
      val patternPlayInput = """^([a-zA-Z]+)(\d+)\s+([a-zA-Z]+)(\d+)$""".r
      input match {
        case t if t.startsWith("cmd") =>
          Some((s"${t.substring(3).trim}", ((-1, -1), (-1, -1)), (timeLimitSec - elapsedTime).toInt))

        case patternPlayInput(c1, r1, c2, r2) =>
          val row1 = r1.toInt
          val row2 = r2.toInt
          val col1 = columnLettersToIndex(c1)
          val col2 = columnLettersToIndex(c2)
          Some(("MOVE", ((row1, col1), (row2, col2)), (timeLimitSec - elapsedTime).toInt))

        case _ =>
          println("FORMATO INCORRETO! FORMATO E: LN LN")
          askInput((timeLimitSec - elapsedTime).toInt)
      }
    }
  }

  @tailrec
  private def askPlayerValidInput(gameHistory: GameHistory, timeLimitSec: Int, config: GameConfig): Option[PlayerAction] = {
    val currentGame = gameHistory.head
    val (board, player, listOpenCoords) = (currentGame.board, currentGame.currentPlayer, currentGame.openCoords)

    askInput(timeLimitSec) match {
      case Some(("MOVE", move, remainingTime)) =>
        if (play(board = board, player = player, coordFrom = move._1, coordTo = move._2, lstOpenCoords = listOpenCoords)._1.isDefined) {
          Some(PlayerAction.Move(move._1, move._2))
        } else {
          println("Movimento ilegal! Peca errada, movimento invalido ou salto sobre o vazio.")
          askPlayerValidInput(gameHistory, remainingTime, config)
        }

      case Some((comandoTexto, _, remainingTime)) =>
        val tokens = comandoTexto.toLowerCase.split("\\s+").toList

        tokens match {
          case List("undo") => Some(PlayerAction.Undo)
          case List("restart") => Some(PlayerAction.Restart)

          case List("chng-dim", rStr, cStr) =>
            (rStr.toIntOption, cStr.toIntOption) match {
              case (Some(r), Some(c)) if r > 0 && c > 0 => Some(PlayerAction.ChngDim(r, c))
              case _ =>
                println("Dimensoes invalidas! Introduz apenas numeros inteiros positivos.")
                askPlayerValidInput(gameHistory, remainingTime, config)
            }

          case List("chng-time", time) =>
            time.toIntOption match {
              case Some(t) if t > 0 => Some( PlayerAction.ChngTime(t))
              case _ =>
                println("tempo invalido! Introduz apenas um numero inteiro positivo de segundos")
                askPlayerValidInput(gameHistory, remainingTime, config)
            }

          case List("pve", diffStr) =>
            diffStr.toIntOption match {
              case Some(diff) if diff >= 1 && diff <= 3 => Some(PlayerAction.Pve(diff))
              case _ =>
                println("Dificuldade invalida! Escolhe 1, 2 ou 3.")
                askPlayerValidInput(gameHistory, remainingTime, config)
            }

          case List("pvp") => Some(PlayerAction.Pvp)

          case List("piecetrain", state) =>
            state.toIntOption match {
              case Some(1) => Some(PlayerAction.PieceTrain(true))
              case Some(0) => Some(PlayerAction.PieceTrain(false))
              case _ =>
                println("Estado invalido introduzido! 1 ou 0")
                askPlayerValidInput(gameHistory, remainingTime, config)
            }


          case _ =>
            println("Comando nao reconhecido ou parametros incorretos.")
            askPlayerValidInput(gameHistory, remainingTime, config)
        }

      case None =>
        None
    }
  }


  @tailrec
  private def gameCycle(gameHistory: GameHistory, config: GameConfig): Unit = {
    val currentGame = gameHistory.head
    val namePlayer = currentGame.currentPlayer match {
      case Stone.Black => "Pretas"
      case Stone.White => "Brancas"
    }

    println(s"\n--- $namePlayer a jogar! ---")
    println(boardToString(currentGame.board, config.dimensions._2, config.dimensions._1))
    val isHumanTurn = config.mode match {
      case GameMode.PvP => true
      case GameMode.PvE(_) => currentGame.currentPlayer == config.starter
    }
    if (isHumanTurn) {
      /// TURNO HUMANO

      askPlayerValidInput(gameHistory, config.timeLimit, config) match {
        case Some(PlayerAction.Move(from, to)) =>
          play(currentGame.board, currentGame.currentPlayer, from, to, currentGame.openCoords) match {
            case (Some(newBoard), newOpenCoords) =>


              val (finalBoard, finalOpenCoords) = if (config.pieceTrainEnabled) {
                askPieceTrainMoves(newBoard, newOpenCoords, currentGame.currentPlayer, to, config.timeLimit, config)
              } else {
                (newBoard, newOpenCoords)
              }


              val nextPlayer = currentGame.currentPlayer match {
                case Stone.White => Stone.Black;
                case Stone.Black => Stone.White
              }


              val nextGameState = GameState(finalBoard, finalOpenCoords, nextPlayer)
              val updatedGameHistory = nextGameState :: gameHistory

              if (playerWon(finalBoard, currentGame.currentPlayer, finalOpenCoords)) {
                println(s"\n$namePlayer venceram!\n")
                println(boardToString(finalBoard, config.dimensions._2, config.dimensions._1))
              } else {
                gameCycle(updatedGameHistory, config)
              }

            case _ =>
              println("Erro inesperado ao processar jogada.")
          }

        case Some(PlayerAction.Undo) =>
          println("Voltando atras no tempo...")
          val newHistory = if (gameHistory.length > 2) gameHistory.drop(2) else gameHistory
          gameCycle(newHistory, config)

        case Some(PlayerAction.Restart) =>
          println("A recomecar o jogo...")
          val initialHistory = List(gameHistory.last)
          gameCycle(initialHistory, config)

        case Some(PlayerAction.ChngDim(r, c)) =>
          println(s"A reiniciar com novas dimensoes ($r x $c)...")


          createInitialBoard(c,r,Some(getRemovalList(r,c)(config.removalSpot - 1))) match {
            case Some((board, newOpenCoords)) =>
              val newConfig = config.copy(dimensions = (r,c))
              val newGameHistory = List(GameState(board,newOpenCoords,config.starter))
              gameCycle(newGameHistory,newConfig)
            case _ =>
              println("Erro: Dimensoes incompativeis com a remocao escolhida. O jogo atual vai continuar.")
              gameCycle(gameHistory, config)
          }
        case Some(PlayerAction.ChngTime(t)) =>
          val newConfig = config.copy(timeLimit = t)
          gameCycle(gameHistory, newConfig)

        case Some(PlayerAction.Pve(diff)) =>
          println(s"Mudando para modo PvE (Dificuldade $diff)...")
          val newConfig = config.copy(mode = GameMode.PvE(diff))
          gameCycle(gameHistory, newConfig)

        case Some(PlayerAction.Pvp) =>
          println("Mudando para modo PvP...")
          val newConfig = config.copy(mode = GameMode.PvP)
          gameCycle(gameHistory, newConfig)

        case Some(PlayerAction.PieceTrain(en)) =>
          println(s"A ${
            if (en) {
              "ativar"
            } else {
              "desativar"
            }
          } pieceTrain ...")
          val newConfig = config.copy(pieceTrainEnabled = en)
          gameCycle(gameHistory, newConfig)

        case None =>
          println("Tempo esgotado! Passagem de turno ou fim de jogo.")

        case _ =>
          println("Erro")
      }
    }
    else {
      //// TURNO AI
      println(s"O Computador ($namePlayer) esta a pensar...")


      val difficulty = config.mode match {
        case GameMode.PvE(diff) => diff
        case _ => 1 // Fallback de seguranca
      }

      val rngInicial = MyRandom(System.currentTimeMillis())

      val (aiPathOption, _) = logic.getAiMove(
        board = currentGame.board,
        player = currentGame.currentPlayer,
        openCoords = currentGame.openCoords,
        degree = difficulty,
        pieceTrainEnabled = config.pieceTrainEnabled,
        rng = rngInicial
      )

      aiPathOption match {
        case Some((path,score)) =>

          if (score == -1) {
            println("Score da IA: [Modo Aleatorio - Sem analise de confianca]")
          } else {
            println(s"Score da IA: [$score/10 Vitorias Simuladas]")
          }
          
          val pathStrs = path.map(c => s"${getColumnLetter(c._2)}${c._1}")
          println(s"O Computador fez a seguinte rota: ${pathStrs.mkString(" -> ")}")


          playSequence(currentGame.board, currentGame.currentPlayer, path, currentGame.openCoords) match {
            case (Some(newBoard), newOpenCoords) =>
              val nextPlayer = currentGame.currentPlayer match {
                case Stone.White => Stone.Black;
                case Stone.Black => Stone.White
              }
              val nextGameState = GameState(newBoard, newOpenCoords, nextPlayer)

              if (playerWon(newBoard, currentGame.currentPlayer, newOpenCoords)) {
                println(s"\nO Computador ($namePlayer) venceu a partida!\n")
                println(boardToString(newBoard, config.dimensions._2, config.dimensions._1))
              } else {
                gameCycle(nextGameState :: gameHistory, config)
              }
            case _ => println("Erro fatal na IA.")
          }
      }

    }
  }

  @tailrec
  private def askPieceTrainMoves(board: Board, openCoords: List[Coord2D], player: Stone, currentCoord: Coord2D, timeLimitSec: Int, config: GameConfig): (Board, List[Coord2D]) = {


    if (!hasValidJumpsFrom(board, player, currentCoord, openCoords)) {
      println("Nao tens mais saltos em serie disponiveis. Fim de turno!")
      return (board, openCoords) // Devolve o tabuleiro atualizado e acaba o turno
    }


    println(s"PieceTrain Ativo! A tua peca em $currentCoord pode saltar novamente.")
    println("Escreve a proxima coordenada de destino (ex: A3), ou escreve 'fim' para terminar o turno.")
    println(boardToString(board, config.dimensions._2, config.dimensions._1))


    val startTime = System.currentTimeMillis() / 1000
    val input = scala.io.StdIn.readLine("Destino ou 'fim': ").trim.toLowerCase
    val elapsedTime = (System.currentTimeMillis() / 1000) - startTime
    val remainingTime = timeLimitSec - elapsedTime.toInt

    if (elapsedTime > timeLimitSec) {
      println("Tempo esgotado durante o comboio! O teu turno acaba aqui.")
      return (board, openCoords)
    }

    input match {
      case "fim" =>
        println("Escolheste terminar o comboio.")
        (board, openCoords)

      case destStr =>

        val destPattern = """^([a-zA-Z]+)(\d+)$""".r
        destStr match {
          case destPattern(c1, r1) =>
            val toCoord = (r1.toInt, columnLettersToIndex(c1))


            logic.play(board, player, currentCoord, toCoord, openCoords) match {
              case (Some(newBoard), newOpenCoords) =>
                println(s"Salto em serie efetuado para $toCoord!")

                askPieceTrainMoves(newBoard, newOpenCoords, player, toCoord, remainingTime, config)

              case _ =>
                println("Salto invalido. Tenta outro destino ou escreve 'fim'.")
                askPieceTrainMoves(board, openCoords, player, currentCoord, remainingTime, config)
            }

          case _ =>
            println("Formato invalido! Usa 'fim' ou uma coordenada de destino (ex: A3).")
            askPieceTrainMoves(board, openCoords, player, currentCoord, remainingTime, config)
        }
    }
  }
}


