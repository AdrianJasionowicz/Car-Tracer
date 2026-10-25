# CarTracker

Small app to check cars in company.

Prosta aplikacja okienkowa na Windows do pilnowania serwisu aut: przebiegu, wymian oleju, hamulców, przeglądów, OC itp.

## Funkcje

- **Auta**: marka, model, osoba odpowiedzialna, opis, przebieg.
- **Historia wydarzeń**: wymiany, naprawy, aktualizacje przebiegu (data, przebieg, opis).
- **Średni przebieg**: liczony automatycznie z odczytów (km/dzień, km/miesiąc) oraz szacowany przebieg na dziś.
- **Przypomnienia**: np. „Wymiana oleju co 15 000 km lub 12 miesięcy”. Aplikacja przelicza kilometry na prognozowaną datę na podstawie średniego przebiegu i bierze termin, który wypada wcześniej.
- **Przeliczanie po każdym wpisie**: po dodaniu przebiegu pokazuje, jak różni się od prognozy, aktualizuje średnią i terminy wszystkich przypomnień. Wydarzenie o tej samej nazwie co przypomnienie oznacza je jako wykonane.
- **Powiadomienia Windows**: ikona w zasobniku i powiadomienia o terminach, które minęły albo się zbliżają. Zamknięcie okna chowa aplikację do zasobnika.

## Technologie

Java 21, Spring Boot, Spring Data JPA, H2 (baza w pliku), JavaFX, Lombok, Maven.

## Uruchomienie

Wymagany JDK 21.

```bash
mvnw spring-boot:run
```

albo w IntelliJ uruchom klasę `CarTrackerApplication`.

## Plik .exe

```bat
build-exe.bat
```

Skrypt buduje `dist\CarTracker\CarTracker.exe` z wbudowaną Javą (na docelowym komputerze nie trzeba instalować Javy), tworzy skrót na pulpicie i opcjonalnie dodaje aplikację do autostartu. Przy autostarcie aplikacja uruchamia się z argumentem `--tray`, czyli od razu schowana w zasobniku.

## Dane

Baza zapisuje się w `%USERPROFILE%\.carTracker\`. Żeby zrobić kopię zapasową, wystarczy skopiować ten folder przy zamkniętej aplikacji.

## Testy

```bash
mvnw test
```
