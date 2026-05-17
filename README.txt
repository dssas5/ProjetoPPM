====================================================================
Projeto de Programacao Multiparadigma 2025/2026 - Jogo Konane
Parte 1: Camada de Negocio (Scala)
====================================================================

GRUPO: JG7
MEMBROS:
- 129820 - Dinis Sousa
- 129851 - Duarte Oliveira
- 129830 - Flavio Santos

SOBRE O PROJETO:
Este ficheiro contem a implementacao da Parte 1 (Camada de Negocio) do jogo de tabuleiro Konane. O desenvolvimento foi inteiramente realizado na linguagem Scala, cumprindo rigorosamente os requisitos da programacao funcional pura para as tarefas T1, T2, T3 e T4 solicitadas na primeira entrega.

COMO EXECUTAR:
A bateria de testes principal encontra-se no objeto `Main` (pacote `logic`). A sua execucao corre testes sequenciais as varias tarefas, demonstrando inicializacoes (centrais e de cantos), rejeicoes de inputs invalidos, jogadas deterministicas (T2) e jogadas baseadas em aleatoriedade contida (T3).

CONSIDERACOES TECNICAS E FATORES DE VALORIZACAO APLICADOS:
No desenvolvimento desta camada, foi dada prioridade a qualidade do codigo e aos principios funcionais avancados exigidos pelo enunciado:

1. Funcoes Puras e Ausencia de Side-Effects:
   A tarefa T4 foi desenhada estritamente como uma funcao pura (`boardToString`). Em vez de imprimir diretamente na consola, ela gera e devolve uma unica `String` multilinhas imutavel formatada via `.mkString`, isolando os side-effects (I/O) do nucleo de negocio. A principio, a funcao nao era pura, anteriormente chamada de `showBoard`, pois possuia prints no( ou seja, causava eventos colaterais).

2. Recursividade de Cauda (@tailrec):
   Na tarefa T3, os algoritmos de iteracao e pesquisa (ex: `GiveRandomPlay`) recorrem a funcoes de ordem superior e `@tailrec` para garantir a exploracao e contracao do espaco de jogadas livres sem recorrer a ciclos iterativos imperativos (`while`/`var`), evitando cenarios de StackOverflow.

3. Pattern Matching Extensivo:
   Utilizacao exaustiva de Pattern Matching para processamento logico multiplo e desconstrucao segura de tipos `Option`, substituindo a utilizacao de condicionais imperativas padrao.

4. Resiliencia aos Limites do Sistema:
   A interface de visualizacao da matriz (`getColumnLetter`) implementa um algoritmo recursivo de rotulacao de colunas estilo folha de calculo (A..Z, AA..ZZ), o que garante suporte visual perfeito e continuo para tabuleiros de dimensao "variavel", por maiores que sejam.

5. Imutabilidade e Funcoes de Ordem Superior:
   Ausencia total do uso de variaveis mutaveis (`var`) ou `return`. O mapeamento do dominio faz uso massivo de funcoes como `.map`, `.filterNot` e `.exists`, bem como passagem de funcoes-parametro (como a `f` na funcao `playRandomly`).
====================================================================
Atualizacao: Jogo Completo (GUI, TUI e Inteligencia Artificial)
====================================================================

NOVAS FUNCIONALIDADES E ATUALIZACOES:
Este projeto foi expandido para incluir a versao interativa e completa do jogo Konane, suportando agora as seguintes funcionalidades:

1. Interfaces de Utilizador (GUI e TUI):
   - GUI (JavaFX): Interface grafica completa e interativa, com capa menu, selecao de jogadas com o rato, visualizacao de caminhos validos e posicoes jogadas pela AI e um temporizador integrado, para além de botoes 'undo', 'restart', e outros.
   - TUI: Interface de linha de comandos interativa que permite jogar via consola, suportando comandos avancados como 'undo', 'restart', 'chng-dim', 'chng-time', entre outros.

2. Modos de Jogo:
   - Jogador vs Jogador (PvP).
   - Jogador vs Computador (PvE) com 3 niveis de dificuldade. A Inteligencia Artificial utiliza simulacoes de partidas para avaliar e escolher a melhor jogada possivel.

3. Mecanicas Avancadas (Piece Train):
   - Possibilidade de realizar capturas multiplas (saltos em serie/comboio) num unico turno. Esta mecanica pode ser ativada ou desativada nas configuracoes do jogo.

4. Configuracoes Dinamicas:
   - Personalizacao do tempo limite por jogada, dimensoes do tabuleiro ( no caso da TUI), jogador inicial (Pretas ou Brancas) e localizacao do par de pecas a remover no inicio da partida (cantos ou centro).
   - Funcionalidade de historico que permite voltar atras nas jogadas (Undo) ou reiniciar a partida (Restart).


COMO EXECUTAR (NOVAS INTERFACES):
- Para iniciar a Interface Grafica (GUI): Executar o objeto FxApp contido no ficheiro GUI.scala.
- Para iniciar a Interface de Texto (TUI): Executar o objeto TUI contido no ficheiro TUI.scala.