# Bar POS - Android App

Bar-/POS-system bygget som native Android-app i Kotlin med Jetpack Compose.
Bar-navnet er konfigurerbart via admin-panelet.

## Funktioner

- **POS Salgsside** - Vælg medlem, tilføj produkter til kurv, registrer køb på konto
- **Saldostyring** - Hvert medlem har en løbende saldo der vises i realtid
- **MobilePay QR-betaling** - Genererer QR-kode med MobilePay deep link for nem betaling
- **Købshistorik** - Se alle transaktioner med filtrering og detaljer
- **Admin panel** med PIN-login:
  - Brugerstyring (opret, rediger, deaktiver medlemmer)
  - Produktstyring (produkter og kategorier)
  - Salgsstatistik (omsætning, top-produkter, salg per kategori/medlem)
  - Indstillinger (bar-navn, MobilePay-nummer, PIN-kode, CSV-eksport)

## Teknologi

- Kotlin + Jetpack Compose (Material 3)
- Room database (lokal SQLite)
- MVVM arkitektur
- Landscape-only layout optimeret til Samsung Galaxy Tab A9+ (11")

## Kom i gang

1. Åbn projektet i Android Studio
2. Synkroniser Gradle
3. Kør appen på tablet eller emulator
4. Standard admin PIN-kode: `1234`

## Første opsætning

1. Log ind som admin (tandhjul-ikon -> PIN: 1234)
2. Gå til **Indstillinger** og sæt bar-navn og MobilePay-nummer
3. Gå til **Produkter** og opret kategorier (fx "Øl", "Sodavand", "Snacks")
4. Tilføj produkter med navn og pris
5. Gå til **Brugere** og opret klubmedlemmer
6. Nu er appen klar til brug!

## Betalingsflow

1. Vælg et medlem på POS-skærmen
2. Tilføj produkter til kurven
3. Tryk "Tilføj til konto" - beløbet trækkes fra medlemmets saldo
4. Når medlemmet vil betale: Tryk "Betal saldo"
5. En QR-kode vises som medlemmet scanner med sin telefon
6. MobilePay åbner med korrekt beløb
7. Bartenderen bekræfter betalingen med "Gennemført"
8. Saldoen nulstilles
