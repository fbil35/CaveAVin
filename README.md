# Ma Cave

Appli Android (Kotlin, Jetpack Compose) pour gérer sa cave à vin, entièrement sur le téléphone.

- **Mes bouteilles** : liste, recherche, filtres par couleur, tri par maturité, nom, millésime ou prix.
- **Ajout par photo** : on photographie l'étiquette, le texte est lu sur l'appareil (ML Kit, gratuit et hors ligne) pour remplir le nom, l'appellation, le millésime et la couleur.
- **Descriptif automatique** : un référentiel intégré d'environ 200 appellations donne la région, les cépages habituels, les accords, la période de dégustation et la température de service. Ce sont des moyennes par appellation, à ajuster selon le producteur et le millésime.
- **À l'ouverture** : l'appli propose une bouteille en stock, de préférence à son apogée, selon la couleur et la fourchette de prix choisies.
- **Sauvegarde** : menu ⋮ de l'accueil, export et import de la cave dans un fichier JSON (sans les photos).

## Installer sur le téléphone

Chaque envoi sur GitHub compile l'APK et le publie dans les [versions](../../releases).
Téléchargez `MaCave.apk` depuis le téléphone, ouvrez-le et autorisez l'installation.

## Compiler soi-même

Ouvrir le dossier dans Android Studio, ou `./gradlew assembleRelease` (JDK 17).

La clé `signature/cave.jks` est versionnée exprès : tous les APK ont la même signature, donc une
nouvelle version s'installe par-dessus l'ancienne sans effacer la cave. Elle ne protège rien d'autre :
pour une publication sur le Play Store, il faudrait une clé privée.
