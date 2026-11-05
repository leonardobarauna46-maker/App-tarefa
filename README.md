# Tarefas & Pontos — Android

Aplicativo Android nativo em **Java**, com banco de dados local SQLite. Interface em português.

## Recursos
- Cadastro de vários familiares
- Tarefas diárias atribuídas a um familiar, com pontuação configurável
- Concluir/desfazer conclusão no mesmo dia (uma conclusão por tarefa por dia)
- Ranking de pontos acumulados de todos os familiares
- Histórico das 100 conclusões mais recentes
- Excluir tarefas sem apagar pontos já ganhos
- Funciona offline, sem conta e sem anúncios

## Como gerar e instalar o APK
1. Instale o **Android Studio** no computador: https://developer.android.com/studio
2. Extraia o ZIP e, no Android Studio, escolha **Open** e selecione a pasta `TarefasEPontos`.
3. Aguarde a sincronização do Gradle. É necessário acesso à internet para baixar os componentes na primeira vez. Se solicitado, instale **Android SDK 35** e **JDK 17** (o Android Studio normalmente inclui o JDK).
4. No menu, escolha **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. O APK estará em `app/build/outputs/apk/debug/app-debug.apk`.
6. Transfira o APK ao celular Android (Android 6.0 ou superior), abra o arquivo e autorize a instalação de apps dessa origem quando solicitado.
7. Abra **Tarefas & Pontos**, cadastre os familiares e depois as tarefas.

Alternativa: conecte um celular com depuração USB habilitada e pressione **Run** no Android Studio.

## Observações
- Os dados são armazenados apenas no aparelho; **não sincronizam entre celulares**.
- A exclusão/desinstalação do aplicativo pode apagar os dados locais. Não há exportação/backup próprio nesta versão.
- O projeto contém o código-fonte, **não um APK compilado**. A compilação deve ser feita no Android Studio.
- Não foi possível executar uma compilação Android neste ambiente; valide o build no Android Studio.
- O Gradle Wrapper não está incluído: caso o Android Studio não configure o Gradle automaticamente, instale Gradle 8.9 e configure-o em **Settings > Build, Execution, Deployment > Build Tools > Gradle**.

## Versão 1.1 — Fotos e ranking semanal
- Ao tocar **Fotografar e concluir**, o aplicativo abre a câmera. A conclusão e os pontos são registrados somente após a câmera retornar uma foto.
- As fotos das tarefas aparecem na lista do dia e no histórico; ficam armazenadas **somente neste celular**.
- Em **Família**, é possível tirar uma foto de perfil de cada integrante.
- Em **Ranking**, são mostradas a classificação geral e a da semana (segunda a domingo).
- A atualização do banco de dados preserva cadastros e pontuações da versão anterior.
- Esta versão usa a **miniatura fornecida pela câmera** (não a foto em resolução original). Não há envio à nuvem, nem verificação automática se a foto realmente mostra a tarefa concluída.
- Para atualizar o aplicativo instalado, use o mesmo `applicationId` e uma assinatura compatível. Builds de depuração de computadores diferentes podem exigir desinstalação (o que pode apagar dados locais).
