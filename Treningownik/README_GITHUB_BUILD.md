# Jak zbudować Treningownik.apk bez Android Studio

## 1. Załóż repozytorium na GitHubie
1. Wejdź na github.com i zaloguj się.
2. Kliknij **New repository**.
3. Nazwij je np. **Treningownik**.
4. Może być **Private**.
5. Kliknij **Create repository**.

## 2. Wrzuć projekt
Najprościej przez stronę GitHuba:
1. W nowym repozytorium kliknij **uploading an existing file** / **Add file → Upload files**.
2. Wgraj **całą zawartość folderu Treningownik**, łącznie z ukrytym folderem `.github`.
3. Kliknij **Commit changes**.

> Ważne: plik `.github/workflows/build-apk.yml` musi znaleźć się w repozytorium dokładnie w tej ścieżce.

## 3. Uruchom budowanie APK
1. Otwórz zakładkę **Actions**.
2. Po lewej wybierz **Build APK**.
3. Kliknij **Run workflow** → **Run workflow**.
4. Otwórz uruchomiony build i poczekaj, aż wszystkie kroki będą zielone.

## 4. Pobierz APK
1. Na stronie zakończonego workflow przewiń do sekcji **Artifacts**.
2. Kliknij **Treningownik-APK**.
3. GitHub pobierze ZIP z plikiem **Treningownik.apk**.
4. Rozpakuj ZIP, przenieś APK na telefon i otwórz go.
5. Android może poprosić o zgodę na **instalowanie nieznanych aplikacji** dla przeglądarki/menedżera plików — zezwól tylko dla tego źródła na czas instalacji.

## Co robi workflow
GitHub sam uruchamia komputer w chmurze, instaluje Java 17, Android SDK i Gradle, kompiluje aplikację i zapisuje gotowy `Treningownik.apk` jako artefakt. Na Twoim komputerze nie trzeba instalować Android Studio ani Android SDK.
