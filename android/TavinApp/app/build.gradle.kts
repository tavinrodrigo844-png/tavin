# Tavin

Aplicativo para compartilhar a tela de um celular para outro em rede local, com consentimento explícito dos dois aparelhos.

## Como funciona

- Celular A: usa o modo `sender`
- Celular B: usa o modo `receiver`
- Servidor: fica no mesmo Wi‑Fi e repassa os frames

## Requisitos

- Android Studio
- SDK Android 34
- Node.js 18+
- Dois celulares na mesma rede Wi‑Fi

## Iniciar o servidor

```bash
cd server
npm install
node server.js
```

O servidor fica em:

```text
ws://SEU_IP_LOCAL:8080
```

Exemplo:

```text
ws://192.168.0.10:8080
```

## Rodar no Android

1. Abra a pasta `android/TavinApp` no Android Studio.
2. Sincronize o projeto.
3. Conecte um celular para depuração.
4. No app:
   - escolha `Sender` no celular 1
   - escolha `Receiver` no celular 2
   - informe o IP do servidor
   - pressione `Conectar`
   - no celular 1, pressione `Iniciar`
   - aceite a permissão de gravação de tela

## Observação legal

Este projeto é para uso legítimo, local e com consentimento explícito dos dois dispositivos. Não use para monitoramento não autorizado.

## Futuras melhorias

- Login por código de compartilhamento
- Gravação em MP4 local
- Suporte a múltiplos receivers
- Compressão mais eficiente
- UI refinada

