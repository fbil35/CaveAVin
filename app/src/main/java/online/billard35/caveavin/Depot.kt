package online.billard35.caveavin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.runtime.mutableStateListOf
import androidx.exifinterface.media.ExifInterface
import org.json.JSONArray
import java.io.File
import java.util.UUID

/** La cave, enregistrée dans un fichier JSON privé de l'appli. */
object Depot {
    val bouteilles = mutableStateListOf<Bouteille>()

    private var racine: File? = null
    private val fichier: File get() = File(racine, "cave.json")
    private val dossierPhotos: File get() = File(racine, "photos").apply { mkdirs() }

    fun charger(contexte: Context) {
        if (racine != null) return
        racine = contexte.filesDir
        val lues = runCatching { lire(fichier.readText()) }.getOrDefault(emptyList())
        bouteilles.clear()
        bouteilles.addAll(lues)
        nettoyerPhotos()
    }

    fun trouver(id: String): Bouteille? = bouteilles.firstOrNull { it.id == id }

    fun enregistrer(b: Bouteille) {
        val i = bouteilles.indexOfFirst { it.id == b.id }
        if (i >= 0) {
            val ancienne = bouteilles[i].photo
            if (ancienne != null && ancienne != b.photo) fichierPhoto(ancienne).delete()
            bouteilles[i] = b
        } else {
            bouteilles.add(b)
        }
        sauver()
    }

    fun supprimer(id: String) {
        val b = trouver(id) ?: return
        b.photo?.let { fichierPhoto(it).delete() }
        bouteilles.removeAll { it.id == id }
        sauver()
    }

    fun changerQuantite(id: String, delta: Int) {
        val b = trouver(id) ?: return
        enregistrer(b.copy(quantite = (b.quantite + delta).coerceAtLeast(0)))
    }

    fun exporter(): String = JSONArray().apply {
        bouteilles.forEach { put(it.versJson()) }
    }.toString(2)

    /** Fusionne une sauvegarde : même identifiant = remplacée, sinon ajoutée. Renvoie le nombre lu. */
    fun importer(texte: String): Int {
        val lues = lire(texte)
        for (b in lues) {
            // Les photos ne voyagent pas avec la sauvegarde.
            val photo = b.photo?.takeIf { fichierPhoto(it).exists() }
            val i = bouteilles.indexOfFirst { it.id == b.id }
            if (i >= 0) bouteilles[i] = b.copy(photo = photo) else bouteilles.add(b.copy(photo = photo))
        }
        sauver()
        return lues.size
    }

    private fun lire(texte: String): List<Bouteille> {
        val tableau = JSONArray(texte)
        return (0 until tableau.length()).map { Bouteille.depuisJson(tableau.getJSONObject(it)) }
    }

    private fun sauver() {
        val cible = fichier
        val temporaire = File(cible.parentFile, "cave.json.tmp")
        temporaire.writeText(exporter())
        if (!temporaire.renameTo(cible)) {
            cible.writeText(temporaire.readText())
            temporaire.delete()
        }
    }

    // ---- Photos d'étiquettes

    fun nouvellePhoto(): File = File(dossierPhotos, UUID.randomUUID().toString() + ".jpg")

    fun fichierPhoto(nom: String): File = File(dossierPhotos, nom)

    private fun nettoyerPhotos() {
        val utilisees = bouteilles.mapNotNull { it.photo }.toSet()
        dossierPhotos.listFiles()?.forEach { if (it.name !in utilisees) it.delete() }
    }

    /**
     * Redresse la photo (les appareils Samsung l'enregistrent couchée avec une
     * indication de rotation), la réduit et la réécrit. Renvoie l'image prête pour la lecture.
     */
    fun preparerPhoto(f: File, cote: Int = 1600): Bitmap? = runCatching {
        val rotation = when (ExifInterface(f.absolutePath)
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val brute = decoder(f, cote) ?: return null
        val echelle = cote.toFloat() / maxOf(brute.width, brute.height)
        val matrice = Matrix()
        if (echelle < 1f) matrice.postScale(echelle, echelle)
        if (rotation != 0f) matrice.postRotate(rotation)
        val image = Bitmap.createBitmap(brute, 0, 0, brute.width, brute.height, matrice, true)
        f.outputStream().use { image.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        image
    }.getOrNull()

    /** Charge une image en la sous-échantillonnant pour ne pas dépasser inutilement [cote] pixels. */
    fun decoder(f: File, cote: Int): Bitmap? {
        val mesure = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(f.absolutePath, mesure)
        if (mesure.outWidth <= 0 || mesure.outHeight <= 0) return null
        var pas = 1
        while (maxOf(mesure.outWidth, mesure.outHeight) / (pas * 2) >= cote) pas *= 2
        return BitmapFactory.decodeFile(f.absolutePath, BitmapFactory.Options().apply { inSampleSize = pas })
    }
}
