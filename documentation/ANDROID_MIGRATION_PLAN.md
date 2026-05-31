# Plano de Migracao Android do OptiRout

Este documento registra a primeira fase da migracao do projeto web para Android nativo com Kotlin.

## Diretriz principal

- manter o motor Python e o backend FastAPI existentes
- substituir a interface web por um app Android em Jetpack Compose
- centralizar a experiencia do usuario no mobile

## Estrutura atual da migracao

- `android/`: novo projeto Android nativo
- `backend/`: camada de API e processamento mantida em Python
- `main.py`: motor matematico e calculo das rotas

## Contrato de integracao esperado

O app Android deve consumir os mesmos recursos expostos pelo backend atual:

- `GET /api/health`
- `GET /api/route`
- `POST /api/calculate`
- `POST /api/options`

## Primeira entrega mobile

- tela inicial com foco em mapa
- resumo da rota fixa do estudo
- selecao visual de modais
- comparacao basica entre Dijkstra e A*

## Proximos passos

1. criar camadas de dados, dominio e apresentacao no Android
2. integrar Retrofit ao backend local
3. desenhar a tela de mapa com marcadores e polylines
4. exportar resultados de tempo, custo e risco no app
