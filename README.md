# OptiRout Android

Documentacao tecnica da base Android nativa do OptiRout, implementada com Kotlin e Jetpack Compose.

## Visao geral

Este modulo Android funciona como a camada mobile do projeto. Ele nao executa o algoritmo de roteamento localmente; em vez disso, envia os parametros da consulta para o backend Python/FastAPI e renderiza o resultado retornado pela API.

A primeira entrega da base concentra tres pontos:

- estrutura inicial do app Android
- tela principal com mapa, selecao de modal e resumo da rota
- integracao direta com a API de calculo existente

## Stack utilizada

- Kotlin 2.0.21
- Android Gradle Plugin 8.5.2
- Jetpack Compose
- Material 3
- Navigation Compose
- Coil para carregamento de tiles/imagens
- Coroutines para execucao assincrona
- HTTP nativo com HttpURLConnection para chamar o backend

## Estrutura do app

O ponto de entrada da aplicacao eh a [MainActivity](app/src/main/java/com/optirout/MainActivity.kt) e a arvore principal de UI eh montada pela composable [OptiRoutApp](app/src/main/java/com/optirout/OptiRoutApp.kt).

Fluxo atual:

1. [MainActivity](app/src/main/java/com/optirout/MainActivity.kt) abre a aplicacao e aplica o tema.
2. [OptiRoutTheme](app/src/main/java/com/optirout/ui/theme/Theme.kt) define a paleta visual.
3. [OptiRoutApp](app/src/main/java/com/optirout/OptiRoutApp.kt) renderiza a tela principal.
4. O usuario escolhe um modal e dispara o calculo.
5. O app envia a requisicao para o backend FastAPI e atualiza a interface com o retorno.

## Como o app funciona

### 1. Tela principal

A tela principal atual e composta por:

- topo com o nome do app e menu lateral via botao de menu
- mapa da rota com zoom e pan
- card de resumo com modal, custo, tempo medio e distancia total
- lista de segmentos da rota, quando o backend devolve dados segmentados
- botao para executar o calculo multimodal

### 2. Selecao de modal

O app trabalha com uma lista fixa de modais suportados, incluindo multimodal, a pe, bicicleta, carro, moto, onibus e variantes de Uber. Essa selecao define dois campos de negocio enviados ao backend:

- modo_inicial
- restricao_modal, quando aplicavel

A selecao atual abre um dialog com opcoes predefinidas.

### 3. Chamado ao backend

Quando o usuario toca em "Calcular rota multimodal", o app monta um payload JSON com:

- origem fixa
- destino fixo
- modo inicial escolhido
- restricao de modal, se existir

O backend esperado esta em `http://10.0.2.2:8000/api/calculate`, que e o endereco usado pelo emulador Android para acessar o servidor local da maquina de desenvolvimento.

### 4. Processamento da resposta

A resposta da API pode conter:

- resumo com tempo_total, custo_total e distancia_total
- segments com o detalhamento por trecho
- route_points para desenhar a rota no mapa
- edges como fallback para reconstruir os pontos da rota, caso route_points nao venha preenchido

O app converte esses dados em estado de tela e atualiza os cards de resumo e de segmentos.

### 5. Renderizacao do mapa

O mapa atual nao usa SDK nativo de mapas. Ele eh montado manualmente com tiles da Carto e desenha a rota sobreposta em Canvas.

O comportamento atual inclui:

- calculo de projeção geografica com base nos pontos da rota
- carregamento de tiles via Coil
- desenho do traçado da rota em duas camadas de cor para melhorar contraste
- marcadores de origem e destino
- controles de zoom na propria tela

## Contrato com o backend

### Request

O envio atual usa JSON com a seguinte estrutura conceitual:

- origem: [latitude, longitude]
- destino: [latitude, longitude]
- modo_inicial: string
- restricao_modal: string opcional

### Response

O app espera uma resposta com estrutura semelhante a:

- resumo
	- tempo_total
	- custo_total
	- distancia_total
- segments
	- servico, meio ou modo
	- tempo
	- custo
	- distancia
- route_points ou edges

Se a API nao devolver pontos prontos, o app tenta reconstruir a geometria a partir dos segmentos/arestas.

## Tema e interface

O tema visual esta centralizado em [Theme.kt](app/src/main/java/com/optirout/ui/theme/Theme.kt) e usa uma paleta clara/escura propria. A interface foi pensada para uma primeira versao funcional, com foco em leitura de rota e acesso rapido ao calculo.

## Estados atuais da tela

O estado da tela e controlado com Compose state local. Hoje o app acompanha, entre outros:

- modal selecionado
- status de calculo em andamento
- visibilidade de dialog de modal
- visibilidade do dialog explicativo
- valor gasto, tempo medio e distancia total
- lista de segmentos
- lista de pontos da rota

## Dependencias e ambiente

O modulo usa Java 17 e compileSdk/targetSdk 35. A comunicacao com o backend depende de permissao de internet e de cleartext habilitado no manifest para desenvolvimento local.

## Melhorias ja mapeadas

As proximas evolucoes que ja estao sendo consideradas sao:

- melhoria de arquitetura, separando melhor UI, estado, acesso a dados e regra de negocio
- melhoria do input/modal de selecao, substituindo a experiencia atual por um componente mais intuitivo e robusto
- criacao de uma tela inicial/empty state para evitar que o app abra exibindo dados vazios ou placeholders pouco informativos

## Observacoes tecnicas

- o backend Python continua sendo o motor de calculo principal
- a base Android foi criada para isolar a experiencia mobile da camada de algoritmo
- a implementacao atual privilegia validar o fluxo ponta a ponta antes de uma reorganizacao maior da arquitetura

## Como executar

Em um ambiente Android padrao, a compilacao pode ser feita pelo Gradle do modulo `android`.

Exemplo:

```bash
./gradlew assembleDebug
```

Para testar o fluxo completo, o backend FastAPI precisa estar rodando localmente na porta 8000.
