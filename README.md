# Tavin

Aplicativo para compartilhar a tela de um celular para outro usando o mesmo Wi‑Fi. O app é instalado nos dois celulares e um deles entra em modo `sender` e o outro em modo `receiver`.

## Visão geral

- `sender`: captura a tela e envia frames para o servidor
- `server`: recebe frames do emissor e repassa para o receptor
- `receiver`: recebe os frames e mostra na tela do dispositivo

## Como funciona

1. Instale o APK em ambos os celulares.
2. No celular 1 escolha `sender`.
3. No celular 2 escolha `receiver`.
4. Informe o endereço do servidor, por exemplo: `192.168.0.10:8080`.
5. Inicie a conexão e dê permissão de gravação da tela.

## Requisitos

- Android Studio + SDK 34
- Node.js 18+
- Conexão na mesma rede Wi‑Fi (idealmente)

## Estrutura do repositório

- `android/` — projeto Android em Kotlin
- `server/` — servidor WebSocket em Node.js

## Rodar o servidor

```bash
cd server
npm install
node server.js
```

O servidor escuta em:

- `ws://<IP-DO-COMPUTADOR>:8080`

## Build do APK

Abra a pasta `android/TavinApp` no Android Studio e gere o APK, ou use Gradle.

## Observação

Este projeto é para uso legítimo com consentimento explícito dos dois aparelhos e em ambiente local. Não use para monitoramento não autorizado.
