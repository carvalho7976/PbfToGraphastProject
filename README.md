##Engenharia de Software - Universidade Federal do Ceará - Campus Quixadá

*Trabalho de Conclusão de Curso submetido à Coordenação   do   Curso   Bacharelado   em Engenharia   de   Software   da   Universidade Federal do Ceará como requisito parcial para obtenção   do   grau   de   Bacharel.   Área   deconcentração: Computação.*

#### Este projeto visa extrair redes de ruas a partir de dados do [OpenStreetMap](http://www.openstreetmap.org/)
O projeto gera um grafo para framework [Graphast](http://www.graphast.org/). 

#### Observações:
A classe Reader dentro do pacote br.ufc.quixada.tcc.Reader contém o método execute() que cria e retorna um grafo a partir do formato PBF. 
O projeto consome uma quantidade razoavel de memória, crescendo a medida que o tamanho/numero de nós de uma região aumenta. 

