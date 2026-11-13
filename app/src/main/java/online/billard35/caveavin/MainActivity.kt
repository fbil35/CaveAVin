package online.billard35.caveavin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Depot.charger(applicationContext)
        setContent {
            ThemeCave { Appli() }
        }
    }
}

sealed interface Ecran {
    data object Accueil : Ecran
    data object Cave : Ecran
    data class Fiche(val id: String) : Ecran
    data class Edition(val id: String?) : Ecran
}

@Composable
fun Appli() {
    var pile by remember { mutableStateOf(listOf<Ecran>(Ecran.Accueil)) }
    val courant = pile.last()
    val aller: (Ecran) -> Unit = { pile = pile + it }
    val retour: () -> Unit = { if (pile.size > 1) pile = pile.dropLast(1) }

    BackHandler(enabled = pile.size > 1) { retour() }

    when (courant) {
        is Ecran.Accueil, is Ecran.Cave -> EcranPrincipal(
            onglet = courant,
            changerOnglet = { pile = if (it is Ecran.Accueil) listOf(it) else listOf(Ecran.Accueil, it) },
            ouvrir = { aller(Ecran.Fiche(it)) },
            ajouter = { aller(Ecran.Edition(null)) },
        )

        is Ecran.Fiche -> {
            val bouteille = Depot.trouver(courant.id)
            if (bouteille == null) {
                LaunchedEffect(courant.id) { retour() }
            } else {
                EcranFiche(
                    bouteille = bouteille,
                    retour = retour,
                    editer = { aller(Ecran.Edition(bouteille.id)) },
                )
            }
        }

        is Ecran.Edition -> EcranEdition(
            id = courant.id,
            retour = retour,
            enregistree = { id ->
                // Après une création, on arrive sur la fiche ; après une modification, on y revient.
                pile = pile.dropLast(1).let { reste ->
                    if (reste.lastOrNull() == Ecran.Fiche(id)) reste else reste + Ecran.Fiche(id)
                }
            },
        )
    }
}

@Composable
private fun EcranPrincipal(
    onglet: Ecran,
    changerOnglet: (Ecran) -> Unit,
    ouvrir: (String) -> Unit,
    ajouter: () -> Unit,
) {
    val contexte = LocalContext.current
    var menu by remember { mutableStateOf(false) }

    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val ok = runCatching {
                contexte.contentResolver.openOutputStream(uri)!!.use { it.write(Depot.exporter().toByteArray()) }
            }.isSuccess
            Toast.makeText(
                contexte,
                if (ok) "Sauvegarde enregistrée" else "La sauvegarde a échoué",
                Toast.LENGTH_LONG,
            ).show()
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val nombre = runCatching {
                val texte = contexte.contentResolver.openInputStream(uri)!!.use { it.readBytes().decodeToString() }
                Depot.importer(texte)
            }.getOrNull()
            Toast.makeText(
                contexte,
                if (nombre != null) "$nombre références importées" else "Ce fichier n'est pas une sauvegarde de Ma Cave",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (onglet is Ecran.Accueil) "Ma Cave" else "Mes bouteilles") },
                actions = {
                    IconButton(onClick = { menu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Sauvegarde")
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("Sauvegarder la cave dans un fichier") },
                            onClick = {
                                menu = false
                                runCatching { exporter.launch("ma-cave.json") }
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Restaurer depuis un fichier") },
                            onClick = {
                                menu = false
                                runCatching { importer.launch(arrayOf("application/json", "text/plain", "*/*")) }
                            },
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = onglet is Ecran.Accueil,
                    onClick = { changerOnglet(Ecran.Accueil) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("À ouvrir") },
                )
                NavigationBarItem(
                    selected = onglet is Ecran.Cave,
                    onClick = { changerOnglet(Ecran.Cave) },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Ma cave") },
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = ajouter,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Ajouter") },
            )
        },
    ) { marges ->
        Box(Modifier.padding(marges)) {
            if (onglet is Ecran.Accueil) {
                EcranAccueil(ouvrir = ouvrir, ajouter = ajouter)
            } else {
                EcranCave(ouvrir = ouvrir)
            }
        }
    }
}
