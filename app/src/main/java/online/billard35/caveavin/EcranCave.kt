package online.billard35.caveavin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class Tri(val libelle: String) {
    MATURITE("Maturité"),
    NOM("Nom"),
    MILLESIME("Millésime"),
    PRIX("Prix"),
}

@Composable
fun EcranCave(ouvrir: (String) -> Unit) {
    var recherche by rememberSaveable { mutableStateOf("") }
    var couleur by rememberSaveable { mutableStateOf<Couleur?>(null) }
    var tri by rememberSaveable { mutableStateOf(Tri.MATURITE) }
    var avecVides by rememberSaveable { mutableStateOf(false) }

    val toutes = Depot.bouteilles
    val cle = Referentiel.normaliser(recherche)
    val annee = anneeCourante()
    val ordreStade = listOf(Stade.DECLIN, Stade.APOGEE, Stade.PRET, Stade.TROP_JEUNE, Stade.INCONNU, Stade.PASSE)

    val liste = toutes
        .filter { avecVides || it.quantite > 0 }
        .filter { couleur == null || it.couleur == couleur }
        .filter { b ->
            cle.isEmpty() || Referentiel.normaliser(
                listOf(b.nom, b.cuvee, b.appellation, b.region, b.emplacement, b.millesime?.toString() ?: "").joinToString(" ")
            ).contains(cle)
        }
        .let { l ->
            when (tri) {
                Tri.MATURITE -> l.sortedWith(
                    compareBy<Bouteille> { ordreStade.indexOf(it.stade(annee)) }
                        .thenBy { it.apogeeFin ?: Int.MAX_VALUE }
                        .thenBy { it.titre.lowercase() }
                )
                Tri.NOM -> l.sortedBy { it.titre.lowercase() }
                Tri.MILLESIME -> l.sortedBy { it.millesime ?: Int.MAX_VALUE }
                Tri.PRIX -> l.sortedByDescending { it.prix ?: -1.0 }
            }
        }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = recherche,
            onValueChange = { recherche = it },
            placeholder = { Text("Nom, appellation, millésime, emplacement") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(selected = couleur == null, onClick = { couleur = null }, label = { Text("Toutes") })
            Couleur.values().forEach { c ->
                FilterChip(
                    selected = couleur == c,
                    onClick = { couleur = if (couleur == c) null else c },
                    label = { Text(c.libelle) },
                    leadingIcon = { PastilleCouleur(c) },
                )
            }
        }
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Tri.values().forEach { t ->
                FilterChip(selected = tri == t, onClick = { tri = t }, label = { Text("Tri : ${t.libelle}") })
            }
            FilterChip(
                selected = avecVides,
                onClick = { avecVides = !avecVides },
                label = { Text("Voir les bouteilles finies") },
            )
        }

        if (liste.isEmpty()) {
            Text(
                if (toutes.isEmpty()) "Aucune bouteille pour l'instant. Touchez « Ajouter »."
                else "Aucune bouteille ne correspond.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            Text(
                "${liste.sumOf { it.quantite }} bouteilles, ${liste.size} références",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                items(liste, key = { it.id }) { b ->
                    LigneBouteille(b, onClick = { ouvrir(b.id) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
