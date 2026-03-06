# No Shout

App Android d’ús **personal** (APK privat, no publicable a Play Store) que escolta contínuament en segon pla, detecta la **teva veu** (VAD + verificació d’orador) i, si detecta que “crides” (volum alt sostingut), et envia una notificació d’alta prioritat que vibra al rellotge (Pixel Watch 3).

## Dispositius objectiu

- Telèfon: Google Pixel 7 Pro (Android 14+)
- Rellotge: Pixel Watch 3 (Wear OS). No cal app al rellotge; les notificacions del telèfon s’espeien al rellotge.

## Compilar i instal·lar

1. Obre el projecte a **Android Studio** (Ladybug o superior recomanat).
2. Deixa que sincronitzi Gradle (es generaran els wrappers si cal).
3. Connecta el Pixel 7 Pro (o emulador Android 14+) amb USB debugging activat.
4. **Build → Build Bundle(s) / APK(s) → Build APK(s)** o **Run**.
5. Per instal·lar manualment l’APK release des de terminal (després d’obrir el projecte amb Android Studio):
   ```bash
   ./gradlew assembleRelease
   ```
   L’APK estarà a `app/build/outputs/apk/release/app-release.apk`. Instal·la’l amb:
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

## Permisos

- **Microfon**: necessari per escoltar àudio.
- **Notificacions**: necessari per mostrar la notificació persistent del servei i l’alerta “Baixa el to”.
- A la primera execució l’app demanarà aquests permisos.

## Notificacions al rellotge (Pixel Watch)

- Les notificacions es configuren al **telèfon**. Assegura’t que les notificacions d’aquesta app estan activades (Ajustos → Notificacions → Baixa el to).
- Al **Pixel Watch**: Ajustos del rellotge → Notificacions → assegura’t que el telèfon pot enviar notificacions al rellotge i que no has silenciat aquesta app.
- La notificació “No Shout” és d’alta prioritat i amb vibració perquè arribi al rellotge.

## Limitacions

- **dB relatiu**: el nivell (dB) és relatiu al full-scale (referència 32768), no calibrat amb un mesurador SPL real.
- **Reconeixement**: amb el model mock, el reconeixement és heurístic (espectral). Amb un model TFLite real d’embeddings d’orador millora la precisió “només la meva veu”.

## Troubleshooting

### El servei es para (battery optimizations)

- Obre **Ajustos** a l’app → **Consells** → **Obrir ajustos de bateria**.
- Selecciona “Baixa el to” i desactiva l’optimització de bateria (o marca “Sense restriccions”).

### La notificació no arriba al rellotge

- Comprova que les notificacions de l’app estan activades al telèfon.
- Al rellotge, comprova que les notificacions del telèfon estan activades i que no has bloquejat aquesta app.
- La notificació d’alerta és **alta prioritat**; si tot està correcte, hauria d’aparèixer i vibrar al rellotge.

## Afegir un model TFLite real

1. Obtén un model TFLite d’embeddings d’orador (per exemple un model exportat des de TensorFlow/Keras o un model preentrenat compatible).
2. **Renomena'l** a `speaker_embedding.tflite` i col·loca'l a `app/src/main/assets/speaker_embedding.tflite` (la carpeta `assets` existeix).
3. Torna a compilar i instal·lar. Als logs (filtre `EmbeddingModel`) veuràs "using TFLite" o "using MockEmbeddingModel".
4. (Opcional) Si el model espera **mel-spectrogram** o una mida d'entrada diferent, adapta `TFLiteEmbeddingModel.kt` i/o `EmbeddingModelFactory.kt` (expectedInputSamples). Obre `TFLiteEmbeddingModel.kt` i adapta:
   - **Forma d’entrada**: `interpreter.getInputTensor(0).shape()` indica les dimensions esperades (p.ex. [1, 16000] per 1 s a 16 kHz, o [1, 98, 40] per mels).
   - **Preprocessament**: si el model espera mel-spectrogram, implementa la conversió (sampleRate, n_mels, window/hop) i escriu el resultat a `inputBuffer`.
   - **Sortida**: `interpreter.getOutputTensor(0).shape()` dóna la mida de l’embedding; el codi ja normalitza L2.
5. **Nota**: L'app ja fa servir TFLite si el fitxer existeix (veure `EmbeddingModelFactory.kt`). Si el model espera mel-spectrogram o una durada d'entrada diferent, adapta `TFLiteEmbeddingModel.kt` i `EmbeddingModelFactory.kt` (expectedInputSamples, per defecte 32000 = 2 s). Paràmetres a alinear amb el model:
   - **sampleRate**: 16000 Hz (o el que esperi el model).
   - **Mel bins / window / hop**: si el model usa mels, cal que coincideixin amb el preprocessament del model.
   - **Durada del segment**: `expectedInputSamples` ha de correspondre a la longitud d’entrada del model (en mostres).

## Estructura del projecte

- `audio/`: AudioRecorder, VAD, FeatureExtractor (FFT, ZCR, band energy).
- `ml/`: EmbeddingModel, EmbeddingModelFactory, MockEmbeddingModel, TFLiteEmbeddingModel, SpeakerVerifier.
- `service/`: VoiceMonitorService (Foreground Service amb tipus microphone).
- `notif/`: NotificationHelper (canals, notificació persistent, alerta “No Shout”).
- `data/`: Room (EventEntity, EventDao, AppDatabase), SettingsDataStore.
- `ui/`: MainActivity, MainViewModel, pantalles (Main, Enrollment, Settings, History), navegació.
- `tile/`: VoiceMonitorTileService (Quick Settings Tile).

## Llicència

Ús personal. No publicar a la Play Store sense adaptar política de privacitat i termes.
