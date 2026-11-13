package online.billard35.caveavin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun EcranAccueil(ouvrir: (String) -> Unit, ajouter: () -> Unit) {
    val bouteilles = Depot.bouteilles
    var couleur by rememberSaveable { mutableStateOf<Couleur?>(null) }
    var budget by rememberSaveable { mutableStateOf(Budget.TOUS) }
    var rang by rememberSaveable { mutableIntStateOf(0) }

    val enStock = bouteilles.filter { it.quantite > 0 }
    if (enStock.isEmpty()) {
        CaveVide(ajouter)
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp),
    ) {
        Text(
            "Que voulez-vous ouvrir ?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        )

        // Couleur
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = couleur == null,
                onClick = { couleur = null; rang = 0 },
                label = { Text("Toutes") },
            )
            Couleur.values().filter { c -> enStock.any { it.couleur == c } }.forEach { c ->
                FilterChip(
                    selected = couleur == c,
                    onClick = { couleur = c; rang = 0 },
                    label = { Text(c.libelle) },
                    leadingIcon = { PastilleCouleur(c) },
                )
            }
        }
        // Prix
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Budget.values().forEach { b ->
                FilterChip(
                    selected = budget == b,
                    onClick = { budget = b; rang = 0 },
                    label = { Text(b.libelle) },
                )
            }
        }

        val propositions = Suggestion.proposer(bouteilles, couleur, budget)

        if (propositions.isEmpty()) {
            Text(
                if (budget != Budget.TOUS)
                    "Aucune bouteille en stock pour ce choix. Les bouteilles sans prix ne sont proposées qu'avec « Tous les prix »."
                else "Aucune bouteille en stock pour cette couleur.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            val choisie = propositions[rang % propositions.size]
            CarteProposition(
                proposition = choisie,
                ouvrir = { ouvrir(choisie.bouteille.id) },
                autre = if (propositions.size > 1) ({ rang = (rang + 1) % propositions.size }) else null,
            )

            val suivantes = propositions.filter { it.bouteille.id != choisie.bouteille.id }.take(3)
            if (suivantes.isNotEmpty()) {
                Text(
                    "Autres idées",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
                )
                suivantes.forEach { p -> LigneBouteille(p.bouteille, onClick = { ouvrir(p.bouteille.id) }) }
            }
        }

        Resume(enStock)
    }
}

@Composable
private fun CaveVide(ajouter: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Votre cave est vide", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            "Ajoutez une première bouteille : photographiez son étiquette, l'appli lit le nom, " +
                "l'appellation et le millésime, puis propose sa période de dégustation et sa température de service.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = ajouter) { Text("Ajouter une bouteille") }
    }
}

@Composable
private fun CarteProposition(proposition: Proposition, ouvrir: () -> Unit, autre: (() -> Unit)?) {
    val b = proposition.bouteille
    Card(
        onClick = ouvrir,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        if (b.photo != null) {
            PhotoEtiquette(
                b.photo, 900,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f),
            )
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Ma proposition",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                b.titre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (b.cuvee.isNotBlank() && b.nom.isNotBlank()) {
                Text(b.cuvee, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PastilleCouleur(b.couleur)
                Text(
                    b.sousTitre,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.height(2.dp))
            PastilleStade(b.stade())
            Text(
                proposition.raison,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            val details = listOfNotNull(
                b.texteTemperature(),
                b.prix?.let { formatPrix(it) },
                if (b.quantite > 1) "${b.quantite} bouteilles" else "Dernière bouteille",
                b.emplacement.takeIf { it.isNotBlank() },
            )
            Text(
                details.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                Button(onClick = ouvrir) { Text("Voir la fiche") }
                if (autre != null) {
                    OutlinedButton(onClick = autre) { Text("Une autre idée") }
                }
            }
        }
    }
}

@Composable
private fun Resume(enStock: List<Bouteille>) {
    val total = enStock.sumOf { it.quantite }
    val valeur = enStock.sumOf { (it.prix ?: 0.0) * it.quantite }
    val urgentes = enStock.count { it.stade() == Stade.DECLIN }
    val apogee = enStock.count { it.stade() == Stade.APOGEE }
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("En cave", style = MaterialTheme.typography.titleMedium)
        Text(
            "$total bouteilles, ${enStock.size} références" + if (valeur > 0) ", environ ${formatPrix(valeur)}" else "",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "$apogee à leur apogée, $urgentes à boire sans tarder",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Ligne de liste commune à l'accueil et à l'onglet « Ma cave ». */
@Composable
fun LigneBouteille(b: Bouteille, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (b.photo != null) {
            PhotoEtiquette(
                b.photo, 160,
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        } else {
            Row(Modifier.width(48.dp), horizontalArrangement = Arrangement.Center) { PastilleCouleur(b.couleur) }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                b.titre,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                b.sousTitre,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            PastilleStade(b.stade())
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("× ${b.quantite}", style = MaterialTheme.typography.titleMedium)
            if (b.prix != null) {
                Text(
                    formatPrix(b.prix),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
