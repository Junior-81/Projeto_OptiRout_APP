# OptiRout — Android Client

Aplicativo Android para visualização de rotas multimodais calculadas pelo serviço de otimização OptiRout. O app consome a API REST do backend, exibe o resultado em mapa interativo e detalha o trajeto por segmento de transporte.

> Este repositório contém exclusivamente o cliente Android. O serviço de cálculo de rotas é mantido em repositório separado.

---

## Requisitos

| Item | Versão |
|---|---|
| Android mínimo | 8.0 (API 26) |
| Android Studio | Hedgehog ou superior |
| JDK | 17 |
| Google Maps API Key | obrigatória |

---

## Configuração

1. Clone o repositório
2. Crie ou edite `local.properties` na raiz e adicione:
   ```properties
   MAPS_API_KEY=sua_chave_aqui
   ```
3. Adicione a chave no manifest.xml (linha 17)
   ```xml
   <meta-data
      android:name="com.google.android.geo.API_KEY"
      android:value="" />
   ```
4. Certifique-se de que o backend OptiRout está rodando:
   - **Emulador:** `http://10.0.2.2:8000` (padrão)
   - **Dispositivo físico:** edite `BASE_URL` em `data/network/ApiClient.kt` com o IP da máquina
5. Compile via Android Studio ou:
   ```bash
   ./gradlew assembleDebug
   ```

---

## Bibliotecas e versões

### Android / Jetpack

| Biblioteca | Versão |
|---|---|
| Compose BOM | 2024.12.01 |
| Compose Material 3 | via BOM |
| Compose UI | via BOM |
| Material Icons Extended | via BOM |
| Activity Compose | 1.9.3 |
| Navigation Compose | 2.8.5 |
| Lifecycle ViewModel Compose | 2.8.7 |
| Lifecycle Runtime KTX | 2.8.7 |
| Core KTX | 1.13.1 |
| Google Material | 1.12.0 |

### Rede

| Biblioteca | Versão |
|---|---|
| Retrofit | 2.11.0 |
| Retrofit Gson Converter | 2.11.0 |
| OkHttp Logging Interceptor | 4.12.0 |

### Mapas

| Biblioteca | Versão |
|---|---|
| Maps Compose | 4.3.3 |
| Play Services Maps | 19.0.0 |

### UI / Animação

| Biblioteca | Versão |
|---|---|
| Lottie Compose | 6.4.0 |

### Kotlin / Build

| Item | Versão |
|---|---|
| Kotlin | 2.0.21 |
| Kotlinx Coroutines Android | 1.7.3 |
| Android Gradle Plugin | 8.5.2 |
| Gradle | 9.0.0 |
| compileSdk / targetSdk | 35 |

---

## Estrutura do projeto

```
app/src/main/java/com/optirout/
├── MainActivity.kt
├── OptiRoutApp.kt
├── data/
│   ├── model/       RouteModels.kt — TransportMode, RouteResponse, Edge, Segment, Summary
│   ├── network/     ApiClient.kt, RouteApiService.kt
│   └── repository/  RouteRepository.kt
├── ui/
│   ├── navigation/  NavGraph.kt — rotas + animações de transição
│   ├── screens/     HomeScreen, LoadingScreen, RouteMapScreen, ErrorScreen
│   └── theme/       Theme.kt
└── viewmodel/       RouteViewModel.kt — StateFlow compartilhado
```

---

## Documentação

| Arquivo | Conteúdo |
|---|---|
| [`documentation/architecture.md`](documentation/architecture.md) | Diagrama de camadas, fluxo de estado, modelo de dados |
| [`documentation/screens.md`](documentation/screens.md) | Detalhamento de cada tela e fluxo de navegação |