# Sistema de Rotas Multimodais (Dijkstra e A*)

Sistema em Python para recomendacao de rotas entre origem e destino com multiplos modais, otimizando **tempo**, **custo** e **risco** de forma conjunta.

Este projeto possui:
- motor de calculo em Python (`main.py`)
- API FastAPI no backend (`backend/`)
- aplicativo Android nativo com Kotlin e Jetpack Compose (`android/`)

---

## 1. Visao Geral

O sistema transforma o problema de rota em um problema de caminho minimo em um **grafo multimodal**.

Cada aresta representa um deslocamento possivel com um modal especifico (walk, bike, car, moto, bus, uber_car, uber_moto), e recebe um peso multiobjetivo:

```text
peso = 0.5 * tempo_norm + 0.3 * custo_norm + 0.2 * risco_norm
```

O algoritmo escolhido (`dijkstra` ou `astar`) percorre esse grafo buscando o menor custo acumulado.

---

## 2. Motivo do projeto

Este projeto existe para transformar um problema real de mobilidade urbana em um problema formal de algoritmos.
Em Recife, a escolha de rota  nao depende so de distancia: envolve tempo, custo e seguranca. A proposta foi
mostrar como esses criterios podem ser modelados em um grafo multimodal, comparando Dijkstra e A* de forma
controlada e com dados reais (OSM, GTFS e CSVs locais).

Pontos-chave do motivo:
- aproximar teoria de analise de algoritmos do cotidiano urbano
- permitir comparacao clara entre metodos de busca em grafos
- avaliar trade-offs entre rapidez, custo e risco em uma mesma funcao objetivo
- criar uma base tecnica reutilizavel para outras cidades e outros modais

---

## 3. Rota unica (escopo do estudo)

O estudo foi concebido com **uma rota principal** para manter o experimento controlado: O deslocamento entre pontos estratégicos da cidade, como o Cine São Luiz, na rua da Aurora, no centro, e a Faculdade Nova
Roma, na rua Padre Carapuceiro, em Boa Viagem. Isso permite comparar algoritmos e pesos
com o minimo de variaveis externas.

No sistema, essa rota unica nao fica "presa" no codigo. Ela e definida no `input.json`:

```json
{
  "origem": [-8.062742, -34.8739],
  "destino": [-8.117809, -34.900231]
}
```

Ou seja:
- a rota unica do estudo e o par origem/destino escolhido para o experimento
- a ferramenta permite trocar a rota para novos cenarios sem alterar o codigo

---

## 4. Como Rodar

### 4.1 Backend (API)

No diretorio raiz do projeto:

```powershell
pip install -r requirements.txt
pip install -r backend/requirements.txt
python -m uvicorn backend.app.main:app --reload --port 8000
```

Endpoints principais:
- `GET /api/health`
- `GET /api/route`
- `POST /api/calculate`
- `POST /api/options`

### 4.2 App Android

O app mobile consome a API local do backend. O fluxo usado no desenvolvimento e:

```powershell
cd android
.\gradlew.bat assembleDebug
```

Depois, instalar o APK no emulador ou dispositivo conectado.

---

## 5. Entrada e Saida

### 5.1 Entrada (`input.json`)

Exemplo:

```json
{
  "origem": [-8.062742, -34.8739],
  "destino": [-8.117809, -34.900231],
  "modo_inicial": "walk",
  "algoritmo": "dijkstra",
  "restricao_modal": "bus_com_acesso"
}
```

Campos:
- `origem`: `[lat, lon]`
- `destino`: `[lat, lon]`
- `modo_inicial`: `walk|bike|car|moto|bus|uber_car|uber_moto`
- `algoritmo`: `dijkstra|astar` (default do motor: `dijkstra`)
- `restricao_modal`:
  - `walk`, `bike`, `car`, `moto`, `bus`, `uber_car`, `uber_moto`
  - `bus_com_acesso` (permite walk + bus com regra de uso de onibus)

### 5.2 Saida (`output.json`)

Estrutura retornada:

```json
{
  "edges": [
    {
      "modo": "walk",
      "origem": [-8.06, -34.87],
      "destino": [-8.07, -34.88],
      "distancia": 0.41,
      "tempo": 0.08,
      "custo": 0.0,
      "risco": 0.003,
      "peso": 0.12
    }
  ],
  "segments": [
    {
      "modo": "walk",
      "tempo": 0.32,
      "distancia": 1.63,
      "custo": 0.0,
      "risco_medio": 0.004
    }
  ],
  "resumo": {
    "tempo_total": 1.7232,
    "custo_total": 0.0,
    "distancia_total": 8.6158,
    "risco_medio": 0.003,
    "velocidade_media_total": 5.0
  }
}
```

---

## 6. Pipeline de Calculo (Passo a Passo)

O `main.py` executa este fluxo:

1. Le `input.json`
2. Carrega CSVs de dados (risco, velocidade, custos)
3. Calcula perfis de risco por modal
4. Configura calculadora de custo
5. Busca clima (Open-Meteo) e calcula fatores (`rain_factor`, `tide_factor`)
6. Carrega ou constroi o grafo multimodal (OSMnx + cache)
7. Integra GTFS de onibus quando disponivel
8. Encontra no de origem e no de destino
9. Calcula parametros de normalizacao min-max
10. Executa `dijkstra` ou `astar`
11. Reconstrui arestas e segmentos
12. Salva `output.json`

---

## 7. Como o Sistema Faz o Calculo

### 7.1 Funcao objetivo (multiobjetivo)

Para cada aresta candidata:

```text
tempo_base = distancia_km / velocidade_kmh
tempo_final = tempo_base * rain_factor * tide_factor

custo = funcao_por_modal(modal, distancia_km, tempo_min, velocidade, clima)

risco_base = risco_do_modal_ajustado_por_clima
risco = risco_base * distancia_km / 10

tempo_norm = normalize(tempo_final)
custo_norm = normalize(custo)
risco_norm = normalize(risco)

peso = 0.5 * tempo_norm + 0.3 * custo_norm + 0.2 * risco_norm
```

Resumo:
- **tempo** aumenta com distancia, chuva e mare
- **custo** depende do modal (tarifa, combustivel ou regra Uber)
- **risco** vem da combinacao crime + acidente, ajustado por clima

### 7.2 Normalizacao

Como tempo, custo e risco estao em escalas diferentes, o projeto aplica Min-Max:

```text
norm = (valor - min) / (max - min)
```

Isso evita que uma metrica domine a decisao so por ter numeros maiores.

---

## 8. Dijkstra x A* (como explicar)

### 8.1 Estado de busca

Nos dois algoritmos, o estado e:

```text
(node_id, modal_atual, used_bus)
```

- `node_id`: no do grafo
- `modal_atual`: modal usado para chegar naquele estado
- `used_bus`: flag para validar cenarios com obrigatoriedade de bus

### 8.2 Dijkstra

O Dijkstra usa apenas custo acumulado real:

```text
prioridade = g(n)
```

- garante otimo para pesos nao negativos
- tende a expandir mais estados
- usado como padrao do projeto

### 8.3 A*

O A* usa custo acumulado + heuristica:

```text
prioridade = f(n) = g(n) + h(n)
```

Heuristica implementada:
- distancia em linha reta entre no atual e destino
- dividida pela maior velocidade disponivel
- normalizada para a mesma escala

Na pratica:
- normalmente visita menos estados que Dijkstra
- costuma ser mais rapido
- mantem qualidade de rota quando a heuristica e bem comportada

### 8.4 Quando usar cada um

- Use `dijkstra` quando quiser comportamento mais conservador e baseline.
- Use `astar` quando quiser reduzir tempo de busca em grafos grandes.

---

## 9. Regras de Modais e Restricoes

### 9.1 Trocas de modal

Quando a viagem inicia em `walk`, o motor permite embarcar em:
- `bus`, `uber_car`, `uber_moto`, `bike`

Tambem permite descer para `walk` ao final.

### 9.2 `bus_com_acesso`

Modo especial para onibus com acesso/egresso a pe:
- modais permitidos: `walk` e `bus`
- rota so e valida se usar `bus` em algum trecho
- limite por aresta de caminhada: `0.5 km`
- penalidade no peso para aresta walk: `x1.4`

---

## 10. GTFS e Onibus

Quando os arquivos GTFS estao presentes em `data/bus_gtfs`, o sistema integra:
- `stops.txt`
- `stop_times.txt`
- `trips.txt`
- `routes.txt`

Com isso, o grafo inclui:
- nos de parada
- arestas de bus entre paradas consecutivas
- conexao de rua <-> parada por walk

Se faltar parte do GTFS, o sistema usa fallback para manter o calculo funcionando.

---

## 11. API para Recalculo

### 11.1 `POST /api/calculate`

Recalcula a rota com os parametros enviados e retorna o novo `output.json`.

Payload exemplo:

```json
{
  "origem": [-8.062742, -34.8739],
  "destino": [-8.117809, -34.900231],
  "modo_inicial": "walk",
  "algoritmo": "astar",
  "restricao_modal": "bus_com_acesso"
}
```

### 11.2 `POST /api/options`

Executa varios cenarios (walk_only, bike_only, car_only etc.), calcula score comparativo e retorna ranking das opcoes.

---

## 12. Estrutura de Pastas (resumo)

```text
android/           app Android nativo com Kotlin/Compose
backend/           API FastAPI e endpoints locais
graph/             construcao e cache do grafo multimodal
loaders/           carga de CSV, clima e dados auxiliares
routing/           Dijkstra, A* e reconstrucao da rota
data/              dados de entrada do motor
documentation/     planos, guias e material de apoio
main.py            orquestrador do motor
input.json         entrada da simulacao
output.json        saida gerada pela execucao
```

---

## 13. Dependencias

Python:
- `osmnx`, `networkx`
- `pandas`, `numpy`, `geopandas`
- `requests`, `folium`
- `fastapi`, `uvicorn`, `pydantic` (backend)

Android:
- `kotlin`
- `jetpack compose`
- `coil` para carregamento dos tiles do mapa

---

## 14. Troubleshooting Rapido

1. Erro `No module named pandas`
   - instalar dependencias com `pip install -r requirements.txt`

2. Erro `uvicorn nao reconhecido`
   - usar `python -m uvicorn backend.app.main:app --reload --port 8000`

3. Front sem resposta de calculo
   - confirmar backend ativo na porta 8000
   - testar `GET /api/health`

4. Avisos de arquivos em `data/` ausentes
   - o sistema possui fallback em varios pontos, mas com dados completos o resultado fica mais realista

---

## 15. API (contrato e fluxo)

Base URL local: `http://localhost:8000`

### Endpoints

- `GET /api/health`
  - verifica se o backend esta vivo e se `input.json`/`output.json` existem
- `GET /api/route`
  - retorna o ultimo `output.json`
- `POST /api/calculate`
  - dispara o calculo do motor (`main.py`) e retorna o novo resultado
- `POST /api/options`
  - executa cenarios predefinidos e retorna ranking de opcoes

### Regras principais do backend

- `input.json` e `output.json` ficam na raiz do projeto.
- o backend executa `main.py` via subprocess e captura `stdout/stderr`.
- o ranking usa os mesmos pesos do motor: tempo 0.5, custo 0.3, risco 0.2.

---

## 16. Como o grafo e usado

O grafo multimodal e um `MultiDiGraph` (NetworkX), onde:

- Nos: intersecoes das ruas (OSM) e paradas de onibus (GTFS).
- Arestas: deslocamentos por modal, com atributos de distancia, velocidade, custo, risco e peso.

Regras importantes:

- As arestas sao replicadas para modais (`walk`, `bike`, `car`, `moto`, `uber_car`, `uber_moto`).
- `bus` entra via GTFS como arestas entre paradas consecutivas.
- A* e Dijkstra trabalham no mesmo grafo e na mesma funcao de custo.

---

## 17. Tecnologias usadas

Backend e motor:

- Python 3
- FastAPI + Uvicorn
- NetworkX
- OSMnx
- Pandas, NumPy, GeoPandas
- Requests

Dados:

- OpenStreetMap (OSMnx)
- GTFS (stops, stop_times, trips, routes)
- CSVs locais (crime, acidentes, velocidade, custos, alagamento)

---

## 18. O que pode ser reutilizado e melhorado

### Reutilizar

- Motor multimodal (`main.py` + `graph/` + `routing/`)
- API para consumo do motor (`backend/`)
- Contrato de entrada/saida via JSON (`input.json` e `output.json`)
- Cache de grafo multimodal (`output/.graph_cache/`)

### Melhorar

- Integrar cache do base_graph OSMnx no fluxo principal (ja existe utilitario).
- Consolidar uso de `weather_factors.csv` e `tide_factors.csv` no lugar de regra fixa.
- Aplicar `flood_risk_streets.csv` diretamente no peso final da aresta.
- Criar testes automatizados para custo, risco, normalizacao e algoritmos.
- Definir benchmarks e comparar Dijkstra x A* em diferentes cenarios.

---

## 19. Documentos de apoio

- [documentation/ARCHITECTURE.md](documentation/ARCHITECTURE.md)
- [documentation/FUNCIONAMENTO_ATUAL.md](documentation/FUNCIONAMENTO_ATUAL.md)
- [documentation/GUIA_IMPLEMENTACAO_MELHORIAS.md](documentation/GUIA_IMPLEMENTACAO_MELHORIAS.md)
- [documentation/QUICKSTART.md](documentation/QUICKSTART.md)
- [documentation/WEB_QUICKSTART.md](documentation/WEB_QUICKSTART.md)
- [documentation/SUMMARY.md](documentation/SUMMARY.md)

---

#
