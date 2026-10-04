#!/bin/bash

# Script para compilar o APK sem assinatura do app Tavin

cd android/TavinApp

# Limpar builds anteriores
./gradlew clean

# Compilar o APK de debug (sem assinatura)
./gradlew assembleDebug

# O APK será gerado em:
# app/build/outputs/apk/debug/app-debug.apk

echo "APK compilado com sucesso!"
echo "Arquivo: android/TavinApp/app/build/outputs/apk/debug/app-debug.apk"
