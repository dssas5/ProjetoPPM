import logic.*

import scala.annotation.tailrec
import scala.io.StdIn.*

object TUI {
  def main(args: Array[String]): Unit = {
    setConfigGame() match{
      case Some(config) =>
        println("___________________________________\n")
        explainCommands()
      case _ =>

    }


  }
}
// retorna as configuracoes introduzidas pelo jogador
@tailrec
def setConfigGame():Option[(Int, (Int, Int),Stone, Int)] ={
  println("temporizador(em segundos):")
  val time = readInt()
  println("dimensoes do tabuleiro(R espaco C):")
  val formatDim = """^(\d+)\s+(\d+)$""".r
  val (row, col) = readLine().trim match {
    case formatDim(r,c) =>
      (r.toInt,c.toInt)
    case _ =>
      (-1,-1)
  }
  println("Quem comeca(P ou B)[ Colocar um input invalido colocara como padrao as brancas):")
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
  val removalSpot = readLine().trim match
  { case t if t.nonEmpty =>
      val num = t.toIntOption.getOrElse(9)
      if( num >= 9 || num < 1 ) 9
      else num
    case _ =>
      9
  }

  val criteria= (
    time > 0,
    row > 0,
    col > 0

  )
  criteria match{
    case (true, true, true) =>
      val successText =
        s"""As configuracoes foram bem sucedidas:
          |
          |Jogador Inicial:${stone match{case Stone.White => "Brancas" case Stone.Black => "Pretas"}}
          |Dimensao do Tabuleiro: ${row}X$col
          |Temporizador por jogada:${timeConverterString(time)}
          |Posicao de Pecas Removidas:$removalSpot
          |""".stripMargin
      println(successText)
      Some(time,(row, col), stone, removalSpot)
    case _ =>
      println("Houve um problema com a configuracao: valores invalidos introduzidos. Recomecar configuracao(Y/N)?")
      println()
      readLine().trim.toUpperCase match {
        case "Y" => setConfigGame()
        case _ => None
      }
  }
}



def explainCommands() : Unit = {
  println(
    """Para comecar qualquer commando introduzir "cmd" + space + comando desejado(comandos sao case insensitive):
      |
      |1) undo - voltar ao estado anterior
      |2) restart - recomecar o jogo
      |3) pvp - jogador contra jogador ( padrao)
      |4) pve param - jogador contra AI( o jogador cujo n é o turno é o AI) ( param e a dificuldade: 1- facil, 2 medio, 3-dificil)
      |5) chng-dim param1 param2 - reinicia o jogo do zero mas com as novas dimensoes introduzidas( caso invalidas nao faz nada)
      |6) chng-time param1 - muda o temporizador por jogada
      |7) piecetrain (1 or 0) - abilita ou desabilita comer pecas inimigas em serie com a mesma peca se possivel
      |""".stripMargin)
  //TODO list: undo,restart, pvp, pve, chng-dim, chng-time, piecetrain



}
@tailrec
def askPlayerMove(timeLimitSec: Int): Option[((Coord2D,Coord2D), Int)] = {

  println(s"Tens $timeLimitSec segundos para jogar!")
  val startTime = System.currentTimeMillis() / 1000

  val input = scala.io.StdIn.readLine("A tua jogada: ")

  val endTime = System.currentTimeMillis() / 1000
  val elapsedTime = endTime - startTime

  println(s"Demoraste: ${timeConverterString(elapsedTime.toInt)}")

  if (elapsedTime > timeLimitSec) {
    println("TEMPO ESGOTADO! Jogada invalida.")
    None // Falhou
  } else {
    //regex LetraNumero LetraNumero
    val patternInput = """^([a-zA-Z]+)(\d+)\s+([a-zA-Z]+)(\d+)$""".r
    input.trim match{
      case patternInput(c1,r1,c2,r2) =>
        val row1 = r1.toInt
        val row2 = r2.toInt
        val col1 = columnLettersToIndex(c1)
        val col2 = columnLettersToIndex(c2)
        Some(((row1,col1),(row2,col2)),(timeLimitSec - elapsedTime).toInt)
      case _ =>
        println("FORMATO INCORRETO! FORMATO E:LN LN")
        askPlayerMove( (timeLimitSec - elapsedTime).toInt)
    }

  }
}

def gameCycle(): Unit = {

}