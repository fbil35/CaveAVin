package online.billard35.caveavin

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EcranFiche(bouteille: Bouteille, retour: () -> Unit, editer: () -> Unit) {
    val b = bouteille
    val contexte = LocalContext.current
    var confirmer by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fiche") },
                navigationIcon = {
                    IconButton(onClick = retour) { Icon(Icons.Default.ArrowBack, contentDescription = "Retour") }
                },
                actions = {
                    IconButton(onClick = editer) { Icon(Icons.Default.Edit, contentDescription = "Modifier") }
                    IconButton(onClick = { confirmer = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                    }
                },
            )
        },
    ) { marges ->
        Column(
            Modifier
                .padding(marges)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            if (b.photo != null) {
                PhotoEtiquette(
                    b.photo, 1200,
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f),
                )
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(b.titre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                if (b.cuvee.isNotBlank() && b.nom.isNotBlank()) {
                    Text(b.cuvee, style = MaterialTheme.typography.titleLarge)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PastilleCouleur(b.couleur)
                    Text(
                        listOf(b.couleur.libelle, b.sousTitre).joinToString(" · "),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                if (b.region.isNotBlank()) {
                    Text(b.region, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Stock
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (b.quantite == 0) "Plus de bouteille" else if (b.quantite == 1) "1 bouteille" else "${b.quantite} bouteilles",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    val ligne = listOfNotNull(
                        b.prix?.let { formatPrix(it) + " la bouteille" },
                        b.emplacement.takeIf { it.isNotBlank() },
                    ).joinToString(" · ")
                    if (ligne.isNotEmpty()) {
                        Text(ligne, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                OutlinedButton(onClick = { Depot.changerQuantite(b.id, 1) }) { Text("+ 1") }
                Button(onClick = { Depot.changerQuantite(b.id, -1) }, enabled = b.quantite > 0) { Text("J'en ouvre une") }
            }

            // Dégustation
            Section("Quand la boire") {
                PastilleStade(b.stade())
                val debut = b.apogeeDebut
                val fin = b.apogeeFin
                if (debut != null && fin != null) {
                    val des = b.boireDes ?: debut
                    val avant = b.boireAvant ?: fin
                    Frise(des, debut, fin, avant, anneeCourante(), teinte(b.stade()))
                    Text(b.texteApogee() ?: "", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Buvable de $des à $avant",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        "Période non renseignée. Touchez le crayon pour la compléter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            b.texteTemperature()?.let { temperature ->
                Section("Service") {
                    Text(temperature, style = MaterialTheme.typography.titleMedium)
                    val conseil = when (b.couleur) {
                        Couleur.ROUGE -> "Sortez-la de la cave une heure avant. Un vin jeune et charpenté gagne à être carafé."
                        Couleur.BLANC -> "Une à deux heures au réfrigérateur suffisent."
                        Couleur.ROSE -> "Deux heures au réfrigérateur."
                        Couleur.EFFERVESCENT -> "Trois heures au réfrigérateur ou vingt minutes dans un seau d'eau et de glace."
                        Couleur.LIQUOREUX -> "Deux heures au réfrigérateur, puis laissez-la remonter un peu dans le verre."
                    }
                    Text(conseil, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (b.descriptif.isNotBlank() || b.cepages.isNotBlank() || b.accords.isNotBlank()) {
                Section("Descriptif") {
                    if (b.descriptif.isNotBlank()) Text(b.descriptif, style = MaterialTheme.typography.bodyLarge)
                    if (b.cepages.isNotBlank()) Detail("Cépages", b.cepages)
                    if (b.accords.isNotBlank()) Detail("Accords", b.accords)
                }
            }
            if (b.notes.isNotBlank()) {
                Section("Mes notes") { Text(b.notes, style = MaterialTheme.typography.bodyLarge) }
            }

            FilledTonalButton(
                onClick = {
                    val question = listOf(b.nom, b.cuvee, b.appellation, b.millesime?.toString() ?: "", "vin avis garde")
                        .filter { it.isNotBlank() }.joinToString(" ")
                    runCatching {
                        contexte.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(question)))
                        )
                    }
                },
                modifier = Modifier.padding(start = 16.dp, top = 20.dp),
            ) { Text("Chercher ce vin sur le web") }
        }
    }

    if (confirmer) {
        AlertDialog(
            onDismissRequest = { confirmer = false },
            title = { Text("Supprimer cette fiche ?") },
            text = { Text("« ${b.titre} » sera retirée de la cave, avec sa photo.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmer = false
                    Depot.supprimer(b.id)
                }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { confirmer = false }) { Text("Annuler") } },
        )
    }
}

@Composable
private fun Section(titre: String, contenu: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(titre, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        contenu()
    }
}

@Composable
private fun Detail(libelle: String, valeur: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(libelle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valeur, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Frise de la vie du vin : période buvable, apogée en couleur, repère sur l'année en cours. */
@Composable
private fun Frise(des: Int, debut: Int, fin: Int, avant: Int, annee: Int, couleurStade: androidx.compose.ui.graphics.Color) {
    val fond = MaterialTheme.colorScheme.surfaceVariant
    val apogee = MaterialTheme.colorScheme.primary
    val repere = MaterialTheme.colorScheme.onSurface
    val premier = minOf(des, annee)
    val dernier = maxOf(avant, annee)
    val etendue = (dernier - premier + 1).toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
        ) {
            val pas = size.width / etendue
            val haut = size.height * 0.3f
            val epaisseur = size.height * 0.4f
            val rayon = CornerRadius(epaisseur / 2, epaisseur / 2)
            // Période buvable
            drawRoundRect(
                color = fond,
                topLeft = Offset((des - premier) * pas, haut),
                size = Size((avant - des + 1) * pas, epaisseur),
                cornerRadius = rayon,
            )
            // Apogée
            drawRoundRect(
                color = apogee,
                topLeft = Offset((debut - premier) * pas, haut),
                size = Size((fin - debut + 1) * pas, epaisseur),
                cornerRadius = rayon,
            )
            // Année en cours
            val x = (annee - premier + 0.5f) * pas
            drawLine(color = repere, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 3.dp.toPx())
            drawCircle(color = couleurStade, radius = 5.dp.toPx(), center = Offset(x, size.height / 2))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(premier.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Aujourd'hui : $annee", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(dernier.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
