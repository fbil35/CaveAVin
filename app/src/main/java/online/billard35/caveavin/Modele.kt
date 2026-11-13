package online.billard35.caveavin

import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.UUID

enum class Couleur(val libelle: String) {
    ROUGE("Rouge"),
    BLANC("Blanc"),
    ROSE("Rosé"),
    EFFERVESCENT("Effervescent"),
    LIQUOREUX("Liquoreux"),
}

/** Où en est la bouteille par rapport à sa fenêtre de dégustation. */
enum class Stade(val libelle: String) {
    TROP_JEUNE("À attendre"),
    PRET("Prêt à boire"),
    APOGEE("À son apogée"),
    DECLIN("À boire sans tarder"),
    PASSE("Sans doute passé"),
    INCONNU("Maturité inconnue"),
}

data class Bouteille(
    val id: String = UUID.randomUUID().toString(),
    val nom: String = "",
    val cuvee: String = "",
    val appellation: String = "",
    val region: String = "",
    val millesime: Int? = null,
    val couleur: Couleur = Couleur.ROUGE,
    val quantite: Int = 1,
    val prix: Double? = null,
    val emplacement: String = "",
    val cepages: String = "",
    val accords: String = "",
    val descriptif: String = "",
    val notes: String = "",
    val boireDes: Int? = null,
    val apogeeDebut: Int? = null,
    val apogeeFin: Int? = null,
    val boireAvant: Int? = null,
    val tempMin: Int? = null,
    val tempMax: Int? = null,
    val photo: String? = null,
    val ajouteLe: Long = System.currentTimeMillis(),
) {
    val titre: String
        get() = when {
            nom.isNotBlank() -> nom
            cuvee.isNotBlank() -> cuvee
            appellation.isNotBlank() -> appellation
            else -> "Bouteille sans nom"
        }

    val sousTitre: String
        get() = listOf(appellation, millesime?.toString() ?: "sans millésime")
            .filter { it.isNotBlank() }
            .joinToString(" · ")

    fun stade(annee: Int = anneeCourante()): Stade {
        val debut = apogeeDebut ?: return Stade.INCONNU
        val fin = apogeeFin ?: return Stade.INCONNU
        val des = boireDes ?: debut
        val avant = boireAvant ?: fin
        return when {
            annee < des -> Stade.TROP_JEUNE
            annee < debut -> Stade.PRET
            annee <= fin -> Stade.APOGEE
            annee <= avant -> Stade.DECLIN
            else -> Stade.PASSE
        }
    }

    fun texteApogee(): String? {
        val debut = apogeeDebut ?: return null
        val fin = apogeeFin ?: return null
        return if (debut == fin) "Apogée en $debut" else "Apogée de $debut à $fin"
    }

    fun texteTemperature(): String? {
        val min = tempMin ?: return null
        val max = tempMax ?: min
        return if (min == max) "Servir à $min °C" else "Servir entre $min et $max °C"
    }

    fun versJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("nom", nom)
        put("cuvee", cuvee)
        put("appellation", appellation)
        put("region", region)
        put("millesime", millesime ?: JSONObject.NULL)
        put("couleur", couleur.name)
        put("quantite", quantite)
        put("prix", prix ?: JSONObject.NULL)
        put("emplacement", emplacement)
        put("cepages", cepages)
        put("accords", accords)
        put("descriptif", descriptif)
        put("notes", notes)
        put("boireDes", boireDes ?: JSONObject.NULL)
        put("apogeeDebut", apogeeDebut ?: JSONObject.NULL)
        put("apogeeFin", apogeeFin ?: JSONObject.NULL)
        put("boireAvant", boireAvant ?: JSONObject.NULL)
        put("tempMin", tempMin ?: JSONObject.NULL)
        put("tempMax", tempMax ?: JSONObject.NULL)
        put("photo", photo ?: JSONObject.NULL)
        put("ajouteLe", ajouteLe)
    }

    companion object {
        fun depuisJson(o: JSONObject): Bouteille = Bouteille(
            id = o.optString("id").ifBlank { UUID.randomUUID().toString() },
            nom = o.optString("nom"),
            cuvee = o.optString("cuvee"),
            appellation = o.optString("appellation"),
            region = o.optString("region"),
            millesime = o.entierOuNull("millesime"),
            couleur = runCatching { Couleur.valueOf(o.optString("couleur")) }.getOrDefault(Couleur.ROUGE),
            quantite = o.optInt("quantite", 1),
            prix = if (o.has("prix") && !o.isNull("prix")) o.optDouble("prix").takeIf { !it.isNaN() } else null,
            emplacement = o.optString("emplacement"),
            cepages = o.optString("cepages"),
            accords = o.optString("accords"),
            descriptif = o.optString("descriptif"),
            notes = o.optString("notes"),
            boireDes = o.entierOuNull("boireDes"),
            apogeeDebut = o.entierOuNull("apogeeDebut"),
            apogeeFin = o.entierOuNull("apogeeFin"),
            boireAvant = o.entierOuNull("boireAvant"),
            tempMin = o.entierOuNull("tempMin"),
            tempMax = o.entierOuNull("tempMax"),
            photo = if (o.has("photo") && !o.isNull("photo")) o.optString("photo").ifBlank { null } else null,
            ajouteLe = o.optLong("ajouteLe", System.currentTimeMillis()),
        )
    }
}

private fun JSONObject.entierOuNull(cle: String): Int? =
    if (has(cle) && !isNull(cle)) optInt(cle) else null

fun anneeCourante(): Int = Calendar.getInstance().get(Calendar.YEAR)

fun formatPrix(prix: Double): String =
    if (prix == Math.floor(prix)) String.format(Locale.FRANCE, "%.0f €", prix)
    else String.format(Locale.FRANCE, "%.2f €", prix)

fun formatNombre(valeur: Double): String =
    if (valeur == Math.floor(valeur)) valeur.toLong().toString()
    else String.format(Locale.FRANCE, "%.2f", valeur)
