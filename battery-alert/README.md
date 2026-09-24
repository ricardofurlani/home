# Bateria 100%

App Android que avisa (som de alarme + vibração) quando o carregamento da bateria chega a 100%.

## Como funciona

- Ao abrir o app, o monitoramento é ligado. Um serviço em primeiro plano fica escutando o nível da
  bateria e mostra uma notificação fixa com a porcentagem atual.
- Quando o aparelho está no carregador e chega a 100%, dispara uma notificação de alarme que repete
  o som até você tocar em **OK, parar alerta** ou desconectar o carregador.
- O alerta toca uma vez por carga: é rearmado quando o carregador é desconectado.
- Após reiniciar o celular, o monitoramento volta sozinho (se estiver ligado).
- Botão **Testar alerta** para conferir som/vibração.
- Botão para desativar a otimização de bateria (recomendado em Xiaomi, Samsung, Motorola etc.,
  que costumam encerrar apps em segundo plano).

Requer Android 8.0 (API 26) ou superior.

## Obter o APK

O workflow `.github/workflows/battery-alert.yml` compila o app a cada push. Baixe o artefato
**bateria-100-apk** na aba *Actions* do GitHub e instale `app-release.apk` (ou `app-debug.apk`)
no celular — é preciso permitir a instalação de fontes desconhecidas.

> O `app-release.apk` é assinado com a chave de debug, adequado para uso pessoal. Para publicar na
> Play Store, configure uma keystore própria em `app/build.gradle.kts`.

## Compilar localmente

Com o Android SDK instalado (ou pelo Android Studio):

```sh
cd battery-alert
./gradlew assembleRelease
```

O ícone (`art/ic_launcher_512.png` e `app/src/main/res/mipmap-*`) foi gerado a partir da imagem T&R.
