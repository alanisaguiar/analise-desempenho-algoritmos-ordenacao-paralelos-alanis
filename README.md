# Análise de Desempenho de Algoritmos de Ordenação Paralelos em Java
## Algoritmos Implementados

| Algoritmo | Complexidade | Paralelização |
|---|---|---|
| Merge Sort | O(n log n) | ForkJoinPool — divisão e conquista |
| Quick Sort | O(n log n) | ForkJoinPool — divisão e conquista |
| Bubble Sort | O(n²) | ExecutorService — Odd-Even Transposition |
| Counting Sort | O(n+k) | ExecutorService — contagem por faixas |

---

## Estrutura do Projeto
| Arquivo | Conceito |
|---|---|
| SerialSorting.java | Implementações seriais | 
| ParallelSorting.java | Implementações paralelas | 
| BenchmarkRunner.java | Framework de benchmark + geração de CSV | 
| AnalyzeCSV.java | Leitura e análise estatística dos resultados | 
| ChartViewer.java | Visualizador dinâmico em Java Swing | 
| Main.java | Ponto de entrada | 

---

## Requisitos

- Java 17 ou superior
- Nenhuma dependência externa — apenas bibliotecas nativas do Java

---

## Como Executar

**1. Clone o repositório**
```bash
git clone https://github.com/seu-usuario/nome-do-repositorio.git
cd nome-do-repositorio/src
```

**2. Compile**
```bash
javac -sourcepath . sorting/Main.java
```

**3. Execute**
```bash
java sorting.Main
```

Isso irá:
- Rodar o benchmark completo para todos os algoritmos
- Gerar o arquivo `csv/results.csv` com os resultados
- Abrir automaticamente o visualizador gráfico (Java Swing)

**4. Para analisar os resultados separadamente**
```bash
java sorting.benchmark.AnalyzeCSV
```

---

## Configurações do Benchmark

Definidas em `BenchmarkRunner.java`:

| Parâmetro | Valor |
|---|---|
| Tamanhos testados | 1.000 / 10.000 / 50.000 elementos |
| Amostras por configuração | 5 |
| Threads testadas | 2, 4 e 8 |
| Seed aleatória | 42 (reprodutível) |

---

## Saída Gerada

O arquivo `csv/results.csv` é gerado automaticamente com o seguinte formato:

Algorithm,Mode,Threads,Size,Sample,TimeMs

MergeSort,Serial,1,1000,1,0

MergeSort,Parallel,2,1000,1,1

---

## Visualizador Gráfico

O `ChartViewer` abre uma janela Swing com dois modos de visualização:

- **Tempo por algoritmo** = compara o tempo médio entre serial e paralelo
- **Speedup vs Serial** = exibe o ganho real de cada configuração paralela

É possível alternar o tamanho do array dinamicamente pelo menu.

---

## Resultados Principais

Para 50.000 elementos:

| Algoritmo | Serial | Melhor Paralelo | Speedup |
|---|---|---|---|
| Bubble Sort | 4324,6 ms | 2404,2 ms (8t) | 1,80× |
| Merge Sort | 12,4 ms | 9,0 ms (4t) | 1,38× |
| Quick Sort | 7,4 ms | 5,8 ms (4t) | 1,28× |
| Counting Sort | 3,0 ms | 9,6 ms (2t) | 0,31× |

> O Counting Sort paralelo é mais lento que o serial devido ao overhead de threads
> ser superior ao tempo de execução do próprio algoritmo.

---

**Alanis Aguiar Bitencourt**
Ciência da Computação | UNIFOR
Disciplina: Computação Paralela e Concorrente
