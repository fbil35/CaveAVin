package online.billard35.caveavin

enum class Budget(val libelle: String, val min: Double, val max: Double) {
    TOUS("Tous les prix", 0.0, Double.MAX_VALUE),
    PETIT("Moins de 15 €", 0.0, 15.0),
    MOYEN("15 à 30 €", 15.0, 30.0),
    BEAU("30 à 60 €", 30.0, 60.0),
    GRAND("Plus de 60 €", 60.0, Double.MAX_VALUE),
}

data class Proposition(val bouteille: Bouteille, val raison: String, val note: Double)

object Suggestion {

    /**
     * Classe les bouteilles en stock : d'abord celles à leur apogée (et parmi elles
     * celles dont l'apogée se termine le plus tôt), puis celles à boire sans tarder,
     * puis celles déjà prêtes. Les vins trop jeunes ou passés viennent en dernier.
     */
    fun proposer(
        bouteilles: List<Bouteille>,
        couleur: Couleur?,
        budget: Budget,
        annee: Int = anneeCourante(),
    ): List<Proposition> = bouteilles
        .asSequence()
        .filter { it.quantite > 0 }
        .filter { couleur == null || it.couleur == couleur }
        .filter { b ->
            if (budget == Budget.TOUS) true
            else b.prix != null && b.prix >= budget.min && b.prix < budget.max
        }
        .map { Proposition(it, raison(it, annee), note(it, annee)) }
        .sortedWith(compareByDescending<Proposition> { it.note }.thenBy { it.bouteille.titre })
        .toList()

    private fun note(b: Bouteille, annee: Int): Double {
        val debut = b.apogeeDebut
        val fin = b.apogeeFin
        val base = when (b.stade(annee)) {
            Stade.APOGEE -> {
                val duree = (fin!! - debut!!).coerceAtLeast(1)
                100.0 + 30.0 * ((annee - debut).toDouble() / duree).coerceIn(0.0, 1.0)
            }
            Stade.DECLIN -> 90.0
            Stade.PRET -> 60.0 - 5.0 * (debut!! - annee)
            Stade.INCONNU -> 30.0
            Stade.PASSE -> 20.0
            Stade.TROP_JEUNE -> 10.0 - ((b.boireDes ?: debut!!) - annee)
        }
        // À maturité égale, on entame plutôt une référence dont il reste plusieurs bouteilles.
        return base + minOf(b.quantite, 6) * 0.5
    }

    private fun raison(b: Bouteille, annee: Int): String = when (b.stade(annee)) {
        Stade.APOGEE ->
            if (b.apogeeFin == annee) "À son apogée, c'est la dernière année idéale."
            else "À son apogée jusqu'en ${b.apogeeFin}."
        Stade.DECLIN -> "Apogée passée depuis ${b.apogeeFin}, à boire sans tarder."
        Stade.PRET -> "Déjà agréable, il sera à son meilleur à partir de ${b.apogeeDebut}."
        Stade.TROP_JEUNE -> "Encore jeune : mieux vaut l'attendre jusqu'en ${b.boireDes ?: b.apogeeDebut}."
        Stade.PASSE -> "Il a sans doute dépassé sa meilleure période, à goûter vite."
        Stade.INCONNU -> "Période de dégustation non renseignée."
    }
}
