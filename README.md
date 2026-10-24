# Harf AI: Arabic letter pronunciation assistant (MVP)

An Android app for learning the 28 Arabic letters. The student recites a letter, an AI model
checks the pronunciation (makhraj), and once the student has learned the letter, or has a doubt,
they send the recording to a human teacher (Hujur) for correction.

## Flow

0. **Language**: on first launch the user picks **বাংলা** or **English**. The EN | বাং switch on
   the Learn screen changes it at any time. All text, letter names, makhraj hints, numbers and
   dates follow the chosen language (`ui/Strings.kt`).
1. **Learn**: a progress card (ring, plus practicing / learned / Hujur-approved counts), an
   **Up next** card for the next letter to practice, filter chips (All / To practice / Learned),
   and the 28 letters in **7 lessons** of 4–5 letters, each lesson with its own progress bar.
2. **Practice a letter**: tap the mic and recite it (up to 5 s). The app records 16 kHz mono WAV,
   saves it on the phone, and sends it to the verify API.
   **Or upload audio** recorded earlier (phone voice recorder, WhatsApp voice note, MP3/M4A/OGG/WAV).
   The API only accepts mono WAV, so `audio/AudioImporter.kt` decodes the file on the phone,
   mixes it to mono, resamples it to 16 kHz WAV (first 10 s), and saves it like a normal recording.
3. The result appears as *Correct ✓* or *Try again (AI heard X)*. After **3 correct** checks the
   letter counts as **learned**.
4. **Send to Hujur**: any recording can be sent with an optional note, either from
   *"Not sure? Ask Hujur"* or from *"Send best recording to Hujur"* once the letter is learned.
   The **Share** button also sends the WAV through WhatsApp or Telegram to the student's own Hujur.
5. **Hujur tab** (PIN `1234`): the teacher plays each pending recording, writes a correction, and
   marks it *Correct* or *Needs work*.
6. **My reviews**: the student sees the Hujur's verdict and comment. A letter the Hujur marks
   *Correct* shows **Hujur ✓✓** on the grid.

## Tech

| Concern     | Choice |
|-------------|--------|
| UI          | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| Storage     | SQLite (`SQLiteOpenHelper`, `harf.db`) with `attempts` and `reviews` tables |
| Audio files | App internal storage: `files/recordings/*.wav` (private, removed on uninstall) |
| Recording   | `AudioRecord` → PCM16 16 kHz mono → WAV |
| Network     | OkHttp multipart `POST /predict` |
| Build       | AGP 9.4, Gradle 9.8, compileSdk 37, minSdk 26 |

### Verify API

```
POST https://quran-python-api.whiteground-9f1dac8d.eastasia.azurecontainerapps.io/predict
  -F audio=@sample.wav  -F target_letter=Ba
→ {"predicted_label":"Ba","confidence":0.93,"accepted":true,"target_letter":"Ba","is_correct":true}
```

The base URL and the Hujur PIN are `buildConfigField`s in `app/build.gradle.kts`.

> **Letter labels:** the API does not publish its label list. `apiLabel` values in
> `data/Letters.kt` use common transliterations (Alif, Ba, Ta, Tha, Jeem, …). Only `Ba` is confirmed;
> check the others against the model's training labels and edit that one file.

## Project layout

```
app/src/main/java/com/harf/ai/
├── HarfApp.kt              Application; wires DB + API + recordings dir
├── MainActivity.kt         Bottom nav: Learn / My reviews / Hujur, plus practice route
├── audio/
│   ├── WavRecorder.kt      Mic → WAV file, live level for the pulse animation
│   └── AudioPlayer.kt      Play/stop one recording at a time
├── ui/Strings.kt           All UI text in English and Bangla
├── data/
│   ├── AppSettings.kt      Saved language choice
│   ├── Letters.kt          28 letters (EN/BN names + makhraj), API labels, lessons
│   ├── Models.kt           Attempt, ReviewRequest, LetterProgress
│   ├── HarfDatabase.kt     SQLite schema + queries
│   ├── VerifyApi.kt        /predict client
│   └── HarfRepository.kt   Flows for the UI; background AI verification
└── ui/screens/             Home, Practice (+ViewModel), MyReviews, Hujur
```

## Build and run

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or open the folder in Android Studio and press Run.

## MVP limits and next steps

- **Hujur review happens on the same device** (PIN-gated tab) or by sharing the file. A real
  student-to-teacher flow needs a backend: upload the audio, keep a review queue per Hujur,
  and send push notifications. The `reviews` table already has the shape to sync.
- No user accounts, and the PIN is hard-coded.
- Add audio examples of the correct pronunciation for each letter.
- Add harakat (fatha, kasra, damma) and joined forms after the isolated letters.
