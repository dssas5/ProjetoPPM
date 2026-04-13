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