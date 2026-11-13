package online.billard35.caveavin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val Clair = lightColorScheme(
    primary = Color(0xFF7A1F2B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6DADC),
    onPrimaryContainer = Color(0xFF3A0A12),
    secondary = Color(0xFF7A6233),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0E3C8),
    onSecondaryContainer = Color(0xFF2B2109),
    background = Color(0xFFFBF8F4),
    onBackground = Color(0xFF211A17),
    surface = Color(0xFFFBF8F4),
    onSurface = Color(0xFF211A17),
    surfaceVariant = Color(0xFFEDE5DD),
    onSurfaceVariant = Color(0xFF54473F),
    outline = Color(0xFF877569),
    outlineVariant = Color(0xFFD9CDC3),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F1EB),
    surfaceContainer = Color(0xFFF1EBE4),
    surfaceContainerHigh = Color(0xFFEBE4DC),
    surfaceContainerHighest = Color(0xFFE5DDD4),
)

private val Sombre = darkColorScheme(
    primary = Color(0xFFF2B3B8),
    onPrimary = Color(0xFF4A0F18),
    primaryContainer = Color(0xFF65212B),
    onPrimaryContainer = Color(0xFFFFDADC),
    secondary = Color(0xFFDDC58F),
    onSecondary = Color(0xFF3D2F08),
    secondaryContainer = Color(0xFF55451E),
    onSecondaryContainer = Color(0xFFFAE1A9),
    background = Color(0xFF181210),
    onBackground = Color(0xFFEDE0D9),
    surface = Color(0xFF181210),
    onSurface = Color(0xFFEDE0D9),
    surfaceVariant = Color(0xFF51443D),
    onSurfaceVariant = Color(0xFFD6C3B8),
    outline = Color(0xFF9E8D83),
    outlineVariant = Color(0xFF51443D),
    surfaceContainerLowest = Color(0xFF120D0B),
    surfaceContainerLow = Color(0xFF211A17),
    surfaceContainer = Color(0xFF251E1B),
    surfaceContainerHigh = Color(0xFF302825),
    surfaceContainerHighest = Color(0xFF3B332F),
)

@Composable
fun ThemeCave(contenu: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Sombre else Clair,
        content = contenu,
    )
}

fun teinte(couleur: Couleur): Color = when (couleur) {
    Couleur.ROUGE -> Color(0xFF8E1B2E)
    Couleur.BLANC -> Color(0xFFE2C864)
    Couleur.ROSE -> Color(0xFFF0A0A8)
    Couleur.EFFERVESCENT -> Color(0xFFBFD3A0)
    Couleur.LIQUOREUX -> Color(0xFFD9962A)
}

fun teinte(stade: Stade): Color = when (stade) {
    Stade.APOGEE -> Color(0xFF2E7D4F)
    Stade.PRET -> Color(0xFF2F6F8F)
    Stade.TROP_JEUNE -> Color(0xFF68727D)
    Stade.DECLIN -> Color(0xFFC0621A)
    Stade.PASSE -> Color(0xFF9A3B2E)
    Stade.INCONNU -> Color(0xFF8A8079)
}

@Composable
fun PastilleCouleur(couleur: Couleur, modifier: Modifier = Modifier) {
    Box(modifier.size(14.dp).clip(CircleShape).background(teinte(couleur)))
}

@Composable
fun PastilleStade(stade: Stade, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(teinte(stade))
            .padding(horizontal = 10.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stade.libelle,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** Photo d'étiquette chargée hors du fil principal ; rien ne s'affiche s'il n'y a pas de photo. */
@Composable
fun PhotoEtiquette(nom: String?, cote: Int, modifier: Modifier = Modifier) {
    val image by produceState<ImageBitmap?>(null, nom, cote) {
        value = if (nom == null) null else withContext(Dispatchers.IO) {
            Depot.decoder(Depot.fichierPhoto(nom), cote)?.asImageBitmap()
        }
    }
    val img = image
    if (img != null) {
        Image(
            bitmap = img,
            contentDescription = "Photo de l'étiquette",
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}
