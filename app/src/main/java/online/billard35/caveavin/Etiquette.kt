package online.billard35.caveavin

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/** Une ligne de texte lue sur l'étiquette, avec sa hauteur en pixels (les gros caractères portent le nom). */
data class LigneLue(val texte: String, val hauteur: Int)

data class Lecture(
    val nom: String,
    val cuvee: String,
    val appellation: Appellation?,
    val appellationLibre: String,
    val millesime: Int?,
    val couleur: Couleur?,
    val texte: String,
)

object Etiquette {

    /** Lecture du texte sur le téléphone (ML Kit, gratuit, sans connexion). Le résultat arrive sur le fil principal. */
    fun lire(image: Bitmap, resultat: (Lecture?) -> Unit) {
        try {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(InputImage.fromBitmap(image, 0))
                .addOnSuccessListener { texte ->
                    val lignes = texte.textBlocks.flatMap { it.lines }.map {
                        LigneLue(it.text, it.boundingBox?.height() ?: 0)
                    }
                    resultat(if (lignes.isEmpty()) null else analyser(lignes))
                }
                .addOnFailureListener { resultat(null) }
        } catch (e: Exception) {
            resultat(null)
        }
    }

    private val MOTS_DOMAINE = listOf(
        "chateau", "domaine", "clos", "mas", "maison", "cave", "caves", "vignoble", "vignobles",
        "tenuta", "bodega", "bodegas", "quinta", "castello", "abbaye", "prieure", "cellier",
    )

    private val MENTIONS = listOf(
        "mis en bouteille", "mise en bouteille", "produit de france", "product of france", "produce of france",
        "appellation", "controlee", "protegee", "contient", "sulfites", "alc", "vol", "cl", "ml",
        "grand vin", "vin de", "recoltant", "proprietaire", "negociant", "eleve", "www", "depuis",
        "fondee", "fonde", "classe", "cru",
    )

    private val MOTS_SEULS = setOf(
        "rose", "brut", "rouge", "blanc", "vin blanc", "vin rouge", "vin rose", "champagne",
        "extra brut", "demi sec", "sec", "france",
    )

    fun analyser(lignes: List<LigneLue>): Lecture {
        val propres = lignes.map { LigneLue(it.texte.trim(), it.hauteur) }.filter { it.texte.length >= 2 }
        val texte = propres.joinToString("\n") { it.texte }
        val normalise = Referentiel.normaliser(texte)

        val appellation = Referentiel.chercherDans(texte)
        val appellationLibre = if (appellation != null) "" else {
            Regex("appellation (.+?) (?:controlee|protegee)").find(normalise)
                ?.groupValues?.get(1)?.takeIf { it.length in 3..40 }
                ?.let { capitaliser(it) } ?: ""
        }

        val millesime = Regex("\\b(19[5-9]\\d|20\\d\\d)\\b").findAll(normalise)
            .map { it.value.toInt() }
            .filter { it <= anneeCourante() }
            .maxOrNull()

        val mots = " $normalise "
        fun contient(vararg termes: String) = termes.any { mots.contains(" $it ") }
        val couleurLue = when {
            contient("brut", "extra brut", "cremant", "champagne", "blanc de blancs", "blanc de noirs",
                "mousseux", "petillant", "prosecco", "cava") -> Couleur.EFFERVESCENT
            contient("rose") -> Couleur.ROSE
            contient("moelleux", "liquoreux", "vendanges tardives", "grains nobles", "vin doux naturel") -> Couleur.LIQUOREUX
            contient("rouge") -> Couleur.ROUGE
            contient("blanc") -> Couleur.BLANC
            else -> null
        }
        val couleur = when {
            appellation == null -> couleurLue
            couleurLue != null && appellation.type(couleurLue) != null -> couleurLue
            else -> appellation.types.first().couleur
        }

        // Le nom : d'abord une ligne « Château… », « Domaine… » ; sinon la plus grosse ligne qui n'est pas une mention.
        val cleAppellation = appellation?.let { Referentiel.normaliser(it.nom) }
        fun estMention(l: LigneLue): Boolean {
            val n = Referentiel.normaliser(l.texte)
            if (n.isEmpty() || n == cleAppellation) return true
            if (n.all { it.isDigit() || it == ' ' } || n in MOTS_SEULS) return true
            val m = " $n "
            return MENTIONS.any { m.contains(" $it ") }
        }
        val ligneDomaine = propres.firstOrNull { l ->
            val premiers = Referentiel.normaliser(l.texte).split(" ")
            premiers.first() in MOTS_DOMAINE && premiers.size >= 2 && !estMention(l)
        }
        val candidates = propres.filter { !estMention(it) }.sortedByDescending { it.hauteur }
        val ligneNom = ligneDomaine ?: candidates.firstOrNull()
        val nom = ligneNom?.let { capitaliser(it.texte) } ?: ""

        // La cuvée : une ligne « Cuvée… », ou un nom écrit plus gros que celui du domaine.
        val ligneCuvee = propres.firstOrNull { Referentiel.normaliser(it.texte).startsWith("cuvee ") && it != ligneNom }
            ?: candidates.firstOrNull()?.takeIf { ligneDomaine != null && it != ligneDomaine && it.hauteur > ligneDomaine.hauteur }
        val cuvee = ligneCuvee?.let { capitaliser(it.texte) } ?: ""

        return Lecture(nom, cuvee, appellation, appellationLibre, millesime, couleur, texte)
    }

    private val PETITS_MOTS = setOf("de", "du", "des", "la", "le", "les", "et", "en", "sur", "sous", "aux", "au", "di", "del", "della")

    /** « CHÂTEAU DE LA TOUR » → « Château de la Tour ». */
    fun capitaliser(texte: String): String =
        texte.lowercase().split(" ").filter { it.isNotBlank() }.mapIndexed { i, mot ->
            if (i > 0 && mot in PETITS_MOTS) mot
            else mot.split("-").joinToString("-") { partie ->
                val apostrophe = partie.indexOf('\'')
                if (apostrophe in 0..1 && apostrophe < partie.length - 1) {
                    partie.substring(0, apostrophe + 1) + partie.substring(apostrophe + 1).replaceFirstChar { it.uppercase() }
                } else {
                    partie.replaceFirstChar { it.uppercase() }
                }
            }
        }.joinToString(" ")
}
