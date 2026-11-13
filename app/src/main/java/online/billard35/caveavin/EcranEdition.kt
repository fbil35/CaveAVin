package online.billard35.caveavin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** État du formulaire : tout est du texte tant que l'on saisit. */
class Formulaire(val base: Bouteille) {
    var nom by mutableStateOf(base.nom)
    var cuvee by mutableStateOf(base.cuvee)
    var appellation by mutableStateOf(base.appellation)
    var region by mutableStateOf(base.region)
    var millesime by mutableStateOf(base.millesime?.toString() ?: "")
    var couleur by mutableStateOf(base.couleur)
    var quantite by mutableIntStateOf(base.quantite)
    var prix by mutableStateOf(base.prix?.let { formatNombre(it) } ?: "")
    var emplacement by mutableStateOf(base.emplacement)
    var cepages by mutableStateOf(base.cepages)
    var accords by mutableStateOf(base.accords)
    var descriptif by mutableStateOf(base.descriptif)
    var notes by mutableStateOf(base.notes)
    var boireDes by mutableStateOf(base.boireDes?.toString() ?: "")
    var apogeeDebut by mutableStateOf(base.apogeeDebut?.toString() ?: "")
    var apogeeFin by mutableStateOf(base.apogeeFin?.toString() ?: "")
    var boireAvant by mutableStateOf(base.boireAvant?.toString() ?: "")
    var tempMin by mutableStateOf(base.tempMin?.toString() ?: "")
    var tempMax by mutableStateOf(base.tempMax?.toString() ?: "")
    var photo by mutableStateOf(base.photo)

    /** Vrai dès que l'utilisateur a corrigé lui-même les années ou la température : on ne les recalcule plus sans demande. */
    var degustationManuelle by mutableStateOf(base.apogeeDebut != null)

    /** Vrai tant que cépages, accords et descriptif viennent du référentiel et peuvent être remplacés. */
    private var texteAuto = base.cepages.isBlank() && base.accords.isBlank() && base.descriptif.isBlank()

    fun texteModifie() {
        texteAuto = false
    }

    /**
     * Remplit la période de dégustation, la température et le descriptif d'après
     * l'appellation, la couleur et le millésime. Sans appellation connue, on se
     * rabat sur un profil moyen pour la couleur.
     */
    fun completer(forcer: Boolean) {
        val connue = Referentiel.exacte(appellation)
        val type = connue?.type(couleur)
        val profil = type?.profil ?: Referentiel.generique(couleur)
        val depart = millesime.toIntOrNull() ?: anneeCourante()

        if (forcer || !degustationManuelle) {
            boireDes = (depart + profil.des).toString()
            apogeeDebut = (depart + profil.apogeeDebut).toString()
            apogeeFin = (depart + profil.apogeeFin).toString()
            boireAvant = (depart + profil.avant).toString()
            tempMin = profil.tempMin.toString()
            tempMax = profil.tempMax.toString()
            degustationManuelle = false
        }
        if (connue != null && (forcer || region.isBlank())) region = connue.region
        if (forcer || texteAuto) {
            cepages = type?.cepages ?: ""
            accords = type?.accords ?: ""
            descriptif = buildString {
                append("Vin ").append(couleur.libelle.lowercase())
                if (connue != null) append(" de ").append(connue.region).append(", appellation ").append(connue.nom)
                append(". Style : ").append(profil.style).append(".")
                if (connue == null) append(" Repères moyens pour cette couleur, faute d'appellation reconnue.")
            }
            texteAuto = true
        }
    }

    /** [renommer] : remplace le texte saisi par le nom officiel (choix dans les propositions). */
    fun choisirAppellation(a: Appellation, renommer: Boolean) {
        if (renommer) appellation = a.nom
        if (a.type(couleur) == null) couleur = a.types.first().couleur
        region = a.region
        completer(forcer = false)
    }

    fun versBouteille(): Bouteille = base.copy(
        nom = nom.trim(),
        cuvee = cuvee.trim(),
        appellation = appellation.trim(),
        region = region.trim(),
        millesime = millesime.trim().toIntOrNull(),
        couleur = couleur,
        quantite = quantite,
        prix = prix.trim().replace(',', '.').toDoubleOrNull(),
        emplacement = emplacement.trim(),
        cepages = cepages.trim(),
        accords = accords.trim(),
        descriptif = descriptif.trim(),
        notes = notes.trim(),
        boireDes = boireDes.trim().toIntOrNull(),
        apogeeDebut = apogeeDebut.trim().toIntOrNull(),
        apogeeFin = apogeeFin.trim().toIntOrNull(),
        boireAvant = boireAvant.trim().toIntOrNull(),
        tempMin = tempMin.trim().toIntOrNull(),
        tempMax = tempMax.trim().toIntOrNull(),
        photo = photo,
    )
}

@Composable
fun EcranEdition(id: String?, retour: () -> Unit, enregistree: (String) -> Unit) {
    val contexte = LocalContext.current
    val portee = rememberCoroutineScope()
    val form = remember(id) { Formulaire(id?.let { Depot.trouver(it) } ?: Bouteille()) }
    val creation = id == null

    var lectureEnCours by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var photoEnAttente by remember { mutableStateOf<File?>(null) }

    fun appliquer(lecture: Lecture?) {
        lectureEnCours = false
        if (lecture == null) {
            message = "Je n'ai rien pu lire sur cette photo. Remplissez les champs à la main."
            return
        }
        val trouves = mutableListOf<String>()
        if (form.nom.isBlank() && lecture.nom.isNotBlank()) {
            form.nom = lecture.nom
            trouves += "nom"
        }
        if (form.cuvee.isBlank() && lecture.cuvee.isNotBlank()) form.cuvee = lecture.cuvee
        if (form.millesime.isBlank() && lecture.millesime != null) {
            form.millesime = lecture.millesime.toString()
            trouves += "millésime"
        }
        if (lecture.couleur != null && creation) form.couleur = lecture.couleur
        if (form.appellation.isBlank()) {
            if (lecture.appellation != null) {
                form.appellation = lecture.appellation.nom
                trouves += "appellation"
            } else if (lecture.appellationLibre.isNotBlank()) {
                form.appellation = lecture.appellationLibre
                trouves += "appellation"
            }
        }
        form.completer(forcer = false)
        message = if (trouves.isEmpty()) {
            "Étiquette difficile à lire. Remplissez les champs à la main."
        } else {
            "Lu sur l'étiquette : ${trouves.joinToString(", ")}. Vérifiez et corrigez si besoin."
        }
    }

    fun traiter(fichier: File) {
        lectureEnCours = true
        message = null
        portee.launch {
            val image = withContext(Dispatchers.IO) { Depot.preparerPhoto(fichier) }
            if (image == null) {
                fichier.delete()
                lectureEnCours = false
                message = "Cette photo n'a pas pu être ouverte."
            } else {
                form.photo = fichier.name
                Etiquette.lire(image) { appliquer(it) }
            }
        }
    }

    val prendre = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { reussi ->
        val fichier = photoEnAttente
        photoEnAttente = null
        if (fichier != null) {
            if (reussi) traiter(fichier) else fichier.delete()
        }
    }
    val choisir = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val fichier = Depot.nouvellePhoto()
            val copie = runCatching {
                contexte.contentResolver.openInputStream(uri)!!.use { entree ->
                    fichier.outputStream().use { sortie -> entree.copyTo(sortie) }
                }
            }.isSuccess
            if (copie) traiter(fichier) else message = "Cette photo n'a pas pu être ouverte."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (creation) "Nouvelle bouteille" else "Modifier la fiche") },
                navigationIcon = {
                    IconButton(onClick = retour) { Icon(Icons.Default.ArrowBack, contentDescription = "Annuler") }
                },
                actions = {
                    TextButton(onClick = {
                        if (form.nom.isBlank() && form.appellation.isBlank() && form.cuvee.isBlank()) {
                            erreur = "Indiquez au moins le nom du vin ou son appellation."
                        } else {
                            val b = form.versBouteille()
                            Depot.enregistrer(b)
                            enregistree(b.id)
                        }
                    }) { Text("Enregistrer") }
                },
            )
        },
    ) { marges ->
        Column(
            Modifier
                .padding(marges)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ---- Photo de l'étiquette
            if (form.photo != null) {
                PhotoEtiquette(
                    form.photo, 1000,
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(16.dp)),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !lectureEnCours,
                    onClick = {
                        val fichier = Depot.nouvellePhoto()
                        photoEnAttente = fichier
                        val lance = runCatching {
                            prendre.launch(
                                FileProvider.getUriForFile(contexte, contexte.packageName + ".fichiers", fichier)
                            )
                        }.isSuccess
                        if (!lance) {
                            photoEnAttente = null
                            message = "Impossible d'ouvrir l'appareil photo. Choisissez une photo existante."
                        }
                    },
                ) { Text(if (form.photo == null) "Photographier l'étiquette" else "Reprendre la photo") }
                OutlinedButton(
                    enabled = !lectureEnCours,
                    onClick = {
                        runCatching {
                            choisir.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    },
                ) { Text("Galerie") }
            }
            if (lectureEnCours) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("Lecture de l'étiquette…", style = MaterialTheme.typography.bodyMedium)
            }
            message?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
            erreur?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }

            // ---- Identité
            Titre("Le vin")
            Champ("Nom du domaine ou du château", form.nom, { form.nom = it; erreur = null })
            Champ("Cuvée (facultatif)", form.cuvee, { form.cuvee = it })
            Champ("Appellation", form.appellation, {
                form.appellation = it
                Referentiel.exacte(it)?.let { a -> form.choisirAppellation(a, renommer = false) }
                erreur = null
            })
            val propositions = Referentiel.suggestions(form.appellation)
            if (propositions.isNotEmpty()) {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    propositions.forEach { a ->
                        AssistChip(onClick = { form.choisirAppellation(a, renommer = true) }, label = { Text(a.nom) })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Champ(
                    "Millésime", form.millesime,
                    { texte ->
                        form.millesime = texte.filter { it.isDigit() }.take(4)
                        if (form.millesime.length == 4) form.completer(forcer = false)
                    },
                    Modifier.weight(1f), nombre = true,
                )
                Champ("Région", form.region, { form.region = it }, Modifier.weight(2f))
            }
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Couleur.values().forEach { c ->
                    FilterChip(
                        selected = form.couleur == c,
                        onClick = {
                            form.couleur = c
                            form.completer(forcer = false)
                        },
                        label = { Text(c.libelle) },
                        leadingIcon = { PastilleCouleur(c) },
                    )
                }
            }

            // ---- Stock
            Titre("En cave")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { if (form.quantite > 0) form.quantite-- }) { Text("−") }
                Text(
                    if (form.quantite <= 1) "${form.quantite} bouteille" else "${form.quantite} bouteilles",
                    style = MaterialTheme.typography.titleMedium,
                )
                OutlinedButton(onClick = { form.quantite++ }) { Text("+") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Champ(
                    "Prix par bouteille (€)", form.prix,
                    { texte -> form.prix = texte.filter { it.isDigit() || it == ',' || it == '.' } },
                    Modifier.weight(1f), nombre = true, decimal = true,
                )
                Champ("Emplacement", form.emplacement, { form.emplacement = it }, Modifier.weight(1f))
            }

            // ---- Dégustation
            Titre("Dégustation")
            Text(
                "Proposé d'après l'appellation, la couleur et le millésime. Ce sont des moyennes : ajustez selon le producteur et l'année.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChampAnnee("Buvable dès", form.boireDes, { form.boireDes = it; form.degustationManuelle = true }, Modifier.weight(1f))
                ChampAnnee("Apogée de", form.apogeeDebut, { form.apogeeDebut = it; form.degustationManuelle = true }, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChampAnnee("Apogée à", form.apogeeFin, { form.apogeeFin = it; form.degustationManuelle = true }, Modifier.weight(1f))
                ChampAnnee("À boire avant", form.boireAvant, { form.boireAvant = it; form.degustationManuelle = true }, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChampAnnee("Service, de (°C)", form.tempMin, { form.tempMin = it.take(2); form.degustationManuelle = true }, Modifier.weight(1f))
                ChampAnnee("à (°C)", form.tempMax, { form.tempMax = it.take(2); form.degustationManuelle = true }, Modifier.weight(1f))
            }
            OutlinedButton(onClick = { form.completer(forcer = true) }) {
                Text("Recalculer d'après l'appellation")
            }

            // ---- Descriptif
            Titre("Descriptif")
            Champ("Cépages", form.cepages, { form.cepages = it; form.texteModifie() })
            Champ("Accords mets et vin", form.accords, { form.accords = it; form.texteModifie() }, lignes = 2)
            Champ("Descriptif", form.descriptif, { form.descriptif = it; form.texteModifie() }, lignes = 3)
            Champ("Mes notes", form.notes, { form.notes = it }, lignes = 3)
        }
    }
}

@Composable
private fun Titre(texte: String) {
    Text(
        texte,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 14.dp),
    )
}

@Composable
private fun Champ(
    libelle: String,
    valeur: String,
    changer: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    nombre: Boolean = false,
    decimal: Boolean = false,
    lignes: Int = 1,
) {
    OutlinedTextField(
        value = valeur,
        onValueChange = changer,
        label = { Text(libelle) },
        singleLine = lignes == 1,
        minLines = lignes,
        keyboardOptions = when {
            decimal -> KeyboardOptions(keyboardType = KeyboardType.Decimal)
            nombre -> KeyboardOptions(keyboardType = KeyboardType.Number)
            else -> KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        },
        modifier = modifier,
    )
}

@Composable
private fun ChampAnnee(libelle: String, valeur: String, changer: (String) -> Unit, modifier: Modifier) {
    Champ(libelle, valeur, { texte -> changer(texte.filter { it.isDigit() }.take(4)) }, modifier, nombre = true)
}
