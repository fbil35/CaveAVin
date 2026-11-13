package online.billard35.caveavin

import java.text.Normalizer

/**
 * Repères de garde et de service, en années après le millésime.
 * Ce sont des moyennes par type de vin : un grand millésime ou un grand
 * producteur tient plus longtemps, une petite année moins.
 */
data class Profil(
    val des: Int,
    val apogeeDebut: Int,
    val apogeeFin: Int,
    val avant: Int,
    val tempMin: Int,
    val tempMax: Int,
    val style: String,
)

data class TypeVin(
    val couleur: Couleur,
    val profil: Profil,
    val cepages: String,
    val accords: String,
)

data class Appellation(
    val nom: String,
    val region: String,
    val types: List<TypeVin>,
    val alias: List<String> = emptyList(),
) {
    fun type(couleur: Couleur): TypeVin? = types.firstOrNull { it.couleur == couleur }
}

object Referentiel {

    // Rouges
    private val R_PRIMEUR = Profil(0, 0, 1, 2, 12, 14, "léger et fruité, à boire jeune")
    private val R_LEGER = Profil(1, 2, 4, 6, 14, 16, "souple et fruité")
    private val R_MOYEN = Profil(2, 3, 7, 10, 15, 17, "équilibré, de moyenne garde")
    private val R_CORSE = Profil(3, 5, 10, 15, 16, 18, "charpenté, de garde")
    private val R_GARDE = Profil(5, 8, 18, 25, 16, 18, "puissant, de longue garde")
    private val R_GRAND = Profil(8, 12, 25, 40, 17, 18, "grand vin de très longue garde")

    // Blancs secs
    private val B_VIF = Profil(0, 1, 3, 4, 8, 10, "sec, vif et léger")
    private val B_MOYEN = Profil(1, 2, 5, 8, 10, 12, "sec et ample")
    private val B_GARDE = Profil(3, 5, 12, 18, 11, 13, "sec, riche, de garde")
    private val B_GRAND = Profil(5, 8, 18, 30, 12, 14, "grand blanc de longue garde")

    // Rosés
    private val ROSE = Profil(0, 0, 2, 3, 8, 10, "frais, à boire dans sa jeunesse")
    private val ROSE_GARDE = Profil(1, 1, 4, 6, 10, 12, "rosé de gastronomie")

    // Effervescents
    private val EFF = Profil(0, 0, 3, 5, 6, 8, "bulles fraîches et légères")
    private val CHAMP = Profil(0, 1, 6, 10, 8, 10, "bulles fines, vineux")

    // Doux
    private val MOELLEUX = Profil(2, 4, 12, 20, 9, 11, "moelleux")
    private val LIQ = Profil(5, 10, 30, 50, 9, 11, "liquoreux de très longue garde")
    private val VDN_R = Profil(1, 3, 15, 30, 13, 15, "vin doux naturel")
    private val VDN_B = Profil(0, 1, 5, 10, 8, 10, "vin doux naturel, sur le fruit")
    private val JAUNE = Profil(6, 10, 40, 60, 14, 16, "vin jaune, d'une garde exceptionnelle")
    private val PORTO = Profil(0, 0, 10, 20, 14, 16, "vin muté, puissant")

    fun generique(couleur: Couleur): Profil = when (couleur) {
        Couleur.ROUGE -> R_MOYEN
        Couleur.BLANC -> B_MOYEN
        Couleur.ROSE -> ROSE
        Couleur.EFFERVESCENT -> EFF
        Couleur.LIQUOREUX -> MOELLEUX
    }

    private fun r(p: Profil, cepages: String, accords: String) = TypeVin(Couleur.ROUGE, p, cepages, accords)
    private fun b(p: Profil, cepages: String, accords: String) = TypeVin(Couleur.BLANC, p, cepages, accords)
    private fun ro(p: Profil, cepages: String, accords: String) = TypeVin(Couleur.ROSE, p, cepages, accords)
    private fun ef(p: Profil, cepages: String, accords: String) = TypeVin(Couleur.EFFERVESCENT, p, cepages, accords)
    private fun li(p: Profil, cepages: String, accords: String) = TypeVin(Couleur.LIQUOREUX, p, cepages, accords)
    private fun ap(nom: String, region: String, vararg types: TypeVin) = Appellation(nom, region, types.toList())
    private fun ap(nom: String, region: String, alias: List<String>, vararg types: TypeVin) =
        Appellation(nom, region, types.toList(), alias)

    private const val BDX_MEDOC = "Cabernet sauvignon, merlot, cabernet franc, petit verdot"
    private const val BDX_DROITE = "Merlot, cabernet franc, cabernet sauvignon"
    private const val BDX_BLANC = "Sauvignon, sémillon, muscadelle"
    private const val ACC_ROUGE_GARDE = "Agneau, bœuf rôti, gibier, fromages à pâte pressée"
    private const val ACC_ROUGE_MOYEN = "Viandes grillées, volaille rôtie, plats mijotés, fromages"
    private const val ACC_ROUGE_LEGER = "Charcuterie, volaille, grillades, cuisine de bistrot"
    private const val ACC_BLANC_VIF = "Fruits de mer, poissons grillés, fromages de chèvre, apéritif"
    private const val ACC_BLANC_RICHE = "Poissons en sauce, volaille à la crème, crustacés, fromages affinés"
    private const val ACC_ROSE = "Apéritif, salades, grillades, cuisine méditerranéenne"
    private const val ACC_BULLES = "Apéritif, fruits de mer, poissons, desserts peu sucrés"
    private const val ACC_LIQ = "Foie gras, fromages bleus, desserts aux fruits, tartes"
    private const val ACC_VDN = "Chocolat, desserts aux fruits rouges, fromages bleus"
    private const val PINOT = "Pinot noir"
    private const val CHARDO = "Chardonnay"
    private const val RHONE_SUD = "Grenache, syrah, mourvèdre"
    private const val SYRAH = "Syrah"

    val appellations: List<Appellation> = listOf(
        // ---- Bordeaux
        ap("Pauillac", "Bordeaux", r(R_GRAND, BDX_MEDOC, ACC_ROUGE_GARDE)),
        ap("Margaux", "Bordeaux", r(R_GRAND, BDX_MEDOC, ACC_ROUGE_GARDE)),
        ap("Saint-Julien", "Bordeaux", r(R_GRAND, BDX_MEDOC, ACC_ROUGE_GARDE)),
        ap("Saint-Estèphe", "Bordeaux", r(R_GRAND, BDX_MEDOC, ACC_ROUGE_GARDE)),
        ap("Pessac-Léognan", "Bordeaux",
            r(R_GARDE, BDX_MEDOC, ACC_ROUGE_GARDE), b(B_GARDE, BDX_BLANC, ACC_BLANC_RICHE)),
        ap("Haut-Médoc", "Bordeaux", r(R_CORSE, BDX_MEDOC, ACC_ROUGE_GARDE)),
        ap("Médoc", "Bordeaux", r(R_CORSE, BDX_MEDOC, ACC_ROUGE_MOYEN)),
        ap("Moulis", "Bordeaux", listOf("Moulis-en-Médoc"), r(R_CORSE, BDX_MEDOC, ACC_ROUGE_GARDE)),
        ap("Listrac-Médoc", "Bordeaux", listOf("Listrac"), r(R_CORSE, BDX_MEDOC, ACC_ROUGE_GARDE)),
        ap("Graves", "Bordeaux",
            r(R_MOYEN, BDX_MEDOC, ACC_ROUGE_MOYEN), b(B_MOYEN, BDX_BLANC, ACC_BLANC_VIF)),
        ap("Pomerol", "Bordeaux", r(R_GARDE, BDX_DROITE, ACC_ROUGE_GARDE)),
        ap("Lalande-de-Pomerol", "Bordeaux", r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Saint-Émilion Grand Cru", "Bordeaux", r(R_GARDE, BDX_DROITE, ACC_ROUGE_GARDE)),
        ap("Saint-Émilion", "Bordeaux", r(R_CORSE, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Montagne-Saint-Émilion", "Bordeaux", r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Lussac-Saint-Émilion", "Bordeaux", r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Puisseguin-Saint-Émilion", "Bordeaux", r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Fronsac", "Bordeaux", r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Canon-Fronsac", "Bordeaux", r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Castillon Côtes de Bordeaux", "Bordeaux", listOf("Côtes de Castillon"),
            r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Blaye Côtes de Bordeaux", "Bordeaux", listOf("Côtes de Blaye", "Premières Côtes de Blaye"),
            r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Côtes de Bourg", "Bordeaux", r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Côtes de Bordeaux", "Bordeaux", r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Entre-Deux-Mers", "Bordeaux", b(B_VIF, BDX_BLANC, ACC_BLANC_VIF)),
        ap("Bordeaux Supérieur", "Bordeaux", r(R_MOYEN, BDX_DROITE, ACC_ROUGE_MOYEN)),
        ap("Bordeaux", "Bordeaux",
            r(R_LEGER, BDX_DROITE, ACC_ROUGE_LEGER), b(B_VIF, BDX_BLANC, ACC_BLANC_VIF),
            ro(ROSE, "Merlot, cabernet", ACC_ROSE)),
        ap("Sauternes", "Bordeaux", li(LIQ, "Sémillon, sauvignon, muscadelle", ACC_LIQ)),
        ap("Barsac", "Bordeaux", li(LIQ, "Sémillon, sauvignon, muscadelle", ACC_LIQ)),
        ap("Loupiac", "Bordeaux", li(MOELLEUX, "Sémillon, sauvignon", ACC_LIQ)),
        ap("Sainte-Croix-du-Mont", "Bordeaux", li(MOELLEUX, "Sémillon, sauvignon", ACC_LIQ)),
        ap("Cadillac", "Bordeaux", li(MOELLEUX, "Sémillon, sauvignon", ACC_LIQ)),

        // ---- Bourgogne
        ap("Gevrey-Chambertin", "Bourgogne", r(R_GARDE, PINOT, ACC_ROUGE_GARDE)),
        ap("Chambertin", "Bourgogne", r(R_GRAND, PINOT, ACC_ROUGE_GARDE)),
        ap("Morey-Saint-Denis", "Bourgogne", r(R_GARDE, PINOT, ACC_ROUGE_GARDE)),
        ap("Chambolle-Musigny", "Bourgogne", r(R_GARDE, PINOT, ACC_ROUGE_GARDE)),
        ap("Clos de Vougeot", "Bourgogne", listOf("Clos Vougeot"), r(R_GRAND, PINOT, ACC_ROUGE_GARDE)),
        ap("Vosne-Romanée", "Bourgogne", r(R_GARDE, PINOT, ACC_ROUGE_GARDE)),
        ap("Nuits-Saint-Georges", "Bourgogne", r(R_GARDE, PINOT, ACC_ROUGE_GARDE)),
        ap("Fixin", "Bourgogne", r(R_CORSE, PINOT, ACC_ROUGE_MOYEN)),
        ap("Marsannay", "Bourgogne",
            r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN), ro(ROSE, PINOT, ACC_ROSE), b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Aloxe-Corton", "Bourgogne", r(R_GARDE, PINOT, ACC_ROUGE_GARDE)),
        ap("Corton-Charlemagne", "Bourgogne", b(B_GRAND, CHARDO, ACC_BLANC_RICHE)),
        ap("Corton", "Bourgogne", r(R_GRAND, PINOT, ACC_ROUGE_GARDE)),
        ap("Pernand-Vergelesses", "Bourgogne",
            r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN), b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Savigny-lès-Beaune", "Bourgogne", listOf("Savigny les Beaune", "Savigny"),
            r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN), b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Beaune", "Bourgogne", r(R_CORSE, PINOT, ACC_ROUGE_MOYEN), b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Pommard", "Bourgogne", r(R_GARDE, PINOT, ACC_ROUGE_GARDE)),
        ap("Volnay", "Bourgogne", r(R_CORSE, PINOT, ACC_ROUGE_MOYEN)),
        ap("Monthélie", "Bourgogne", r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN)),
        ap("Auxey-Duresses", "Bourgogne",
            r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN), b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Saint-Romain", "Bourgogne", b(B_MOYEN, CHARDO, ACC_BLANC_RICHE), r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN)),
        ap("Saint-Aubin", "Bourgogne", b(B_MOYEN, CHARDO, ACC_BLANC_RICHE), r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN)),
        ap("Meursault", "Bourgogne", b(B_GARDE, CHARDO, ACC_BLANC_RICHE)),
        ap("Puligny-Montrachet", "Bourgogne", b(B_GARDE, CHARDO, ACC_BLANC_RICHE)),
        ap("Chassagne-Montrachet", "Bourgogne",
            b(B_GARDE, CHARDO, ACC_BLANC_RICHE), r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN)),
        ap("Montrachet", "Bourgogne", b(B_GRAND, CHARDO, ACC_BLANC_RICHE)),
        ap("Santenay", "Bourgogne", r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN), b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Maranges", "Bourgogne", r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN)),
        ap("Bourgogne Hautes-Côtes de Nuits", "Bourgogne", listOf("Hautes Côtes de Nuits"),
            r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN), b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Bourgogne Hautes-Côtes de Beaune", "Bourgogne", listOf("Hautes Côtes de Beaune"),
            r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN), b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Côte de Nuits-Villages", "Bourgogne", r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN)),
        ap("Côte de Beaune-Villages", "Bourgogne", r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN)),
        ap("Mercurey", "Bourgogne", r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN), b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Givry", "Bourgogne", r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN), b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Rully", "Bourgogne", b(B_MOYEN, CHARDO, ACC_BLANC_RICHE), r(R_MOYEN, PINOT, ACC_ROUGE_MOYEN)),
        ap("Montagny", "Bourgogne", b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Pouilly-Fuissé", "Bourgogne", b(B_MOYEN, CHARDO, ACC_BLANC_RICHE)),
        ap("Saint-Véran", "Bourgogne", b(B_MOYEN, CHARDO, ACC_BLANC_VIF)),
        ap("Viré-Clessé", "Bourgogne", b(B_MOYEN, CHARDO, ACC_BLANC_VIF)),
        ap("Mâcon-Villages", "Bourgogne", listOf("Mâcon"), b(B_VIF, CHARDO, ACC_BLANC_VIF)),
        ap("Chablis Grand Cru", "Bourgogne", b(B_GRAND, CHARDO, ACC_BLANC_RICHE)),
        ap("Chablis Premier Cru", "Bourgogne", listOf("Chablis 1er Cru"), b(B_GARDE, CHARDO, ACC_BLANC_RICHE)),
        ap("Petit Chablis", "Bourgogne", b(B_VIF, CHARDO, ACC_BLANC_VIF)),
        ap("Chablis", "Bourgogne", b(B_MOYEN, CHARDO, ACC_BLANC_VIF)),
        ap("Bourgogne Aligoté", "Bourgogne", b(B_VIF, "Aligoté", ACC_BLANC_VIF)),
        ap("Crémant de Bourgogne", "Bourgogne", ef(EFF, "Chardonnay, pinot noir", ACC_BULLES)),
        ap("Bourgogne", "Bourgogne", r(R_LEGER, PINOT, ACC_ROUGE_LEGER), b(B_MOYEN, CHARDO, ACC_BLANC_VIF)),

        // ---- Beaujolais
        ap("Beaujolais Nouveau", "Beaujolais", r(R_PRIMEUR, "Gamay", ACC_ROUGE_LEGER)),
        ap("Beaujolais-Villages", "Beaujolais", r(R_LEGER, "Gamay", ACC_ROUGE_LEGER)),
        ap("Beaujolais", "Beaujolais", r(R_LEGER, "Gamay", ACC_ROUGE_LEGER)),
        ap("Morgon", "Beaujolais", r(R_MOYEN, "Gamay", ACC_ROUGE_LEGER)),
        ap("Moulin-à-Vent", "Beaujolais", r(R_MOYEN, "Gamay", ACC_ROUGE_MOYEN)),
        ap("Fleurie", "Beaujolais", r(R_LEGER, "Gamay", ACC_ROUGE_LEGER)),
        ap("Brouilly", "Beaujolais", r(R_LEGER, "Gamay", ACC_ROUGE_LEGER)),
        ap("Côte de Brouilly", "Beaujolais", r(R_LEGER, "Gamay", ACC_ROUGE_LEGER)),
        ap("Chiroubles", "Beaujolais", r(R_LEGER, "Gamay", ACC_ROUGE_LEGER)),
        ap("Juliénas", "Beaujolais", r(R_LEGER, "Gamay", ACC_ROUGE_LEGER)),
        ap("Chénas", "Beaujolais", r(R_MOYEN, "Gamay", ACC_ROUGE_LEGER)),
        ap("Saint-Amour", "Beaujolais", r(R_LEGER, "Gamay", ACC_ROUGE_LEGER)),
        ap("Régnié", "Beaujolais", r(R_LEGER, "Gamay", ACC_ROUGE_LEGER)),

        // ---- Vallée du Rhône
        ap("Côte-Rôtie", "Vallée du Rhône", r(R_GARDE, "Syrah, viognier", ACC_ROUGE_GARDE)),
        ap("Condrieu", "Vallée du Rhône", b(B_MOYEN, "Viognier", ACC_BLANC_RICHE)),
        ap("Saint-Joseph", "Vallée du Rhône",
            r(R_MOYEN, SYRAH, ACC_ROUGE_MOYEN), b(B_MOYEN, "Marsanne, roussanne", ACC_BLANC_RICHE)),
        ap("Crozes-Hermitage", "Vallée du Rhône",
            r(R_MOYEN, SYRAH, ACC_ROUGE_MOYEN), b(B_MOYEN, "Marsanne, roussanne", ACC_BLANC_RICHE)),
        ap("Hermitage", "Vallée du Rhône",
            r(R_GRAND, SYRAH, ACC_ROUGE_GARDE), b(B_GRAND, "Marsanne, roussanne", ACC_BLANC_RICHE)),
        ap("Cornas", "Vallée du Rhône", r(R_GARDE, SYRAH, ACC_ROUGE_GARDE)),
        ap("Saint-Péray", "Vallée du Rhône", b(B_MOYEN, "Marsanne, roussanne", ACC_BLANC_RICHE)),
        ap("Châteauneuf-du-Pape", "Vallée du Rhône",
            r(R_GARDE, RHONE_SUD, ACC_ROUGE_GARDE), b(B_MOYEN, "Grenache blanc, clairette, roussanne", ACC_BLANC_RICHE)),
        ap("Gigondas", "Vallée du Rhône", r(R_CORSE, RHONE_SUD, ACC_ROUGE_GARDE)),
        ap("Vacqueyras", "Vallée du Rhône", r(R_CORSE, RHONE_SUD, ACC_ROUGE_MOYEN)),
        ap("Rasteau", "Vallée du Rhône", r(R_CORSE, RHONE_SUD, ACC_ROUGE_MOYEN)),
        ap("Cairanne", "Vallée du Rhône", r(R_MOYEN, RHONE_SUD, ACC_ROUGE_MOYEN)),
        ap("Vinsobres", "Vallée du Rhône", r(R_MOYEN, RHONE_SUD, ACC_ROUGE_MOYEN)),
        ap("Lirac", "Vallée du Rhône", r(R_MOYEN, RHONE_SUD, ACC_ROUGE_MOYEN), ro(ROSE, RHONE_SUD, ACC_ROSE)),
        ap("Tavel", "Vallée du Rhône", ro(ROSE_GARDE, "Grenache, cinsault", ACC_ROSE)),
        ap("Beaumes-de-Venise", "Vallée du Rhône", listOf("Muscat de Beaumes-de-Venise"),
            li(VDN_B, "Muscat à petits grains", ACC_LIQ), r(R_MOYEN, RHONE_SUD, ACC_ROUGE_MOYEN)),
        ap("Côtes du Rhône Villages", "Vallée du Rhône", r(R_MOYEN, RHONE_SUD, ACC_ROUGE_MOYEN)),
        ap("Côtes du Rhône", "Vallée du Rhône",
            r(R_LEGER, RHONE_SUD, ACC_ROUGE_LEGER), b(B_VIF, "Grenache blanc, viognier", ACC_BLANC_VIF),
            ro(ROSE, RHONE_SUD, ACC_ROSE)),
        ap("Ventoux", "Vallée du Rhône", r(R_LEGER, RHONE_SUD, ACC_ROUGE_LEGER), ro(ROSE, RHONE_SUD, ACC_ROSE)),
        ap("Luberon", "Vallée du Rhône", r(R_LEGER, RHONE_SUD, ACC_ROUGE_LEGER), ro(ROSE, RHONE_SUD, ACC_ROSE)),
        ap("Costières de Nîmes", "Vallée du Rhône", r(R_LEGER, RHONE_SUD, ACC_ROUGE_LEGER), ro(ROSE, RHONE_SUD, ACC_ROSE)),

        // ---- Loire
        ap("Sancerre", "Loire",
            b(B_MOYEN, "Sauvignon", ACC_BLANC_VIF), r(R_LEGER, PINOT, ACC_ROUGE_LEGER), ro(ROSE, PINOT, ACC_ROSE)),
        ap("Pouilly-Fumé", "Loire", b(B_MOYEN, "Sauvignon", ACC_BLANC_VIF)),
        ap("Menetou-Salon", "Loire", b(B_VIF, "Sauvignon", ACC_BLANC_VIF), r(R_LEGER, PINOT, ACC_ROUGE_LEGER)),
        ap("Quincy", "Loire", b(B_VIF, "Sauvignon", ACC_BLANC_VIF)),
        ap("Reuilly", "Loire", b(B_VIF, "Sauvignon", ACC_BLANC_VIF)),
        ap("Touraine", "Loire", b(B_VIF, "Sauvignon", ACC_BLANC_VIF), r(R_LEGER, "Gamay, cabernet franc, côt", ACC_ROUGE_LEGER)),
        ap("Vouvray", "Loire",
            b(B_GARDE, "Chenin", ACC_BLANC_RICHE), ef(EFF, "Chenin", ACC_BULLES), li(MOELLEUX, "Chenin", ACC_LIQ)),
        ap("Montlouis-sur-Loire", "Loire", listOf("Montlouis"),
            b(B_GARDE, "Chenin", ACC_BLANC_RICHE), ef(EFF, "Chenin", ACC_BULLES)),
        ap("Chinon", "Loire", r(R_MOYEN, "Cabernet franc", ACC_ROUGE_MOYEN)),
        ap("Saint-Nicolas-de-Bourgueil", "Loire", r(R_LEGER, "Cabernet franc", ACC_ROUGE_LEGER)),
        ap("Bourgueil", "Loire", r(R_MOYEN, "Cabernet franc", ACC_ROUGE_MOYEN)),
        ap("Saumur-Champigny", "Loire", r(R_MOYEN, "Cabernet franc", ACC_ROUGE_MOYEN)),
        ap("Saumur", "Loire",
            r(R_LEGER, "Cabernet franc", ACC_ROUGE_LEGER), b(B_MOYEN, "Chenin", ACC_BLANC_VIF), ef(EFF, "Chenin", ACC_BULLES)),
        ap("Savennières", "Loire", b(B_GARDE, "Chenin", ACC_BLANC_RICHE)),
        ap("Coteaux du Layon", "Loire", li(MOELLEUX, "Chenin", ACC_LIQ)),
        ap("Quarts de Chaume", "Loire", li(LIQ, "Chenin", ACC_LIQ)),
        ap("Bonnezeaux", "Loire", li(LIQ, "Chenin", ACC_LIQ)),
        ap("Anjou", "Loire",
            r(R_LEGER, "Cabernet franc", ACC_ROUGE_LEGER), b(B_MOYEN, "Chenin", ACC_BLANC_VIF), ro(ROSE, "Grolleau, cabernet", ACC_ROSE)),
        ap("Muscadet Sèvre et Maine", "Loire", listOf("Muscadet"), b(B_VIF, "Melon de Bourgogne", ACC_BLANC_VIF)),
        ap("Crémant de Loire", "Loire", ef(EFF, "Chenin, chardonnay", ACC_BULLES)),

        // ---- Alsace
        ap("Alsace Riesling", "Alsace", listOf("Riesling"), b(B_GARDE, "Riesling", ACC_BLANC_VIF)),
        ap("Alsace Gewurztraminer", "Alsace", listOf("Gewurztraminer"),
            b(B_MOYEN, "Gewurztraminer", "Cuisine épicée, munster, desserts aux fruits")),
        ap("Alsace Pinot Gris", "Alsace", listOf("Pinot Gris"), b(B_MOYEN, "Pinot gris", ACC_BLANC_RICHE)),
        ap("Alsace Pinot Blanc", "Alsace", listOf("Pinot Blanc"), b(B_VIF, "Pinot blanc", ACC_BLANC_VIF)),
        ap("Alsace Sylvaner", "Alsace", listOf("Sylvaner"), b(B_VIF, "Sylvaner", ACC_BLANC_VIF)),
        ap("Alsace Muscat", "Alsace", b(B_VIF, "Muscat", "Apéritif, asperges")),
        ap("Alsace Pinot Noir", "Alsace", r(R_LEGER, PINOT, ACC_ROUGE_LEGER)),
        ap("Alsace Grand Cru", "Alsace", b(B_GRAND, "Riesling, gewurztraminer, pinot gris, muscat", ACC_BLANC_RICHE)),
        ap("Crémant d'Alsace", "Alsace", ef(EFF, "Pinot blanc, auxerrois, chardonnay", ACC_BULLES)),
        ap("Alsace", "Alsace", b(B_MOYEN, "Riesling, pinot gris, gewurztraminer", ACC_BLANC_VIF)),

        // ---- Champagne
        ap("Champagne", "Champagne", ef(CHAMP, "Chardonnay, pinot noir, meunier", ACC_BULLES)),

        // ---- Jura et Savoie
        ap("Château-Chalon", "Jura", b(JAUNE, "Savagnin", "Comté, volaille aux morilles")),
        ap("Arbois", "Jura",
            r(R_LEGER, "Poulsard, trousseau, pinot noir", ACC_ROUGE_LEGER), b(B_GARDE, "Savagnin, chardonnay", "Comté, volaille à la crème")),
        ap("Côtes du Jura", "Jura",
            b(B_GARDE, "Chardonnay, savagnin", "Comté, volaille à la crème"), r(R_LEGER, "Poulsard, trousseau", ACC_ROUGE_LEGER)),
        ap("L'Étoile", "Jura", b(B_GARDE, "Chardonnay, savagnin", "Comté, poissons en sauce")),
        ap("Crémant du Jura", "Jura", ef(EFF, "Chardonnay, pinot noir", ACC_BULLES)),
        ap("Roussette de Savoie", "Savoie", b(B_MOYEN, "Altesse", "Poissons de lac, fromages de Savoie")),
        ap("Vin de Savoie", "Savoie", listOf("Savoie"),
            b(B_VIF, "Jacquère, altesse", "Fondue, raclette, poissons de lac"), r(R_LEGER, "Mondeuse, gamay", ACC_ROUGE_LEGER)),

        // ---- Provence et Corse
        ap("Bandol", "Provence",
            r(R_GARDE, "Mourvèdre, grenache, cinsault", ACC_ROUGE_GARDE), ro(ROSE_GARDE, "Mourvèdre, grenache, cinsault", ACC_ROSE),
            b(B_MOYEN, "Clairette, ugni blanc", ACC_BLANC_VIF)),
        ap("Cassis", "Provence", b(B_MOYEN, "Marsanne, clairette", "Bouillabaisse, poissons grillés")),
        ap("Palette", "Provence", r(R_CORSE, "Grenache, mourvèdre", ACC_ROUGE_GARDE), b(B_GARDE, "Clairette", ACC_BLANC_RICHE)),
        ap("Les Baux-de-Provence", "Provence", r(R_MOYEN, "Grenache, syrah, cabernet sauvignon", ACC_ROUGE_MOYEN)),
        ap("Coteaux d'Aix-en-Provence", "Provence", ro(ROSE, "Grenache, cinsault, syrah", ACC_ROSE), r(R_LEGER, "Grenache, syrah", ACC_ROUGE_LEGER)),
        ap("Coteaux Varois en Provence", "Provence", listOf("Coteaux Varois"), ro(ROSE, "Grenache, cinsault, syrah", ACC_ROSE)),
        ap("Côtes de Provence", "Provence",
            ro(ROSE, "Grenache, cinsault, syrah", ACC_ROSE), r(R_MOYEN, "Grenache, syrah, mourvèdre", ACC_ROUGE_MOYEN),
            b(B_VIF, "Rolle, ugni blanc", ACC_BLANC_VIF)),
        ap("Patrimonio", "Corse", r(R_MOYEN, "Niellucciu", ACC_ROUGE_MOYEN), b(B_VIF, "Vermentinu", ACC_BLANC_VIF)),
        ap("Ajaccio", "Corse", r(R_MOYEN, "Sciaccarellu", ACC_ROUGE_MOYEN)),
        ap("Vin de Corse", "Corse", listOf("Corse"), r(R_LEGER, "Niellucciu, sciaccarellu", ACC_ROUGE_LEGER), ro(ROSE, "Sciaccarellu", ACC_ROSE)),

        // ---- Languedoc-Roussillon
        ap("Pic Saint-Loup", "Languedoc", r(R_MOYEN, "Syrah, grenache, mourvèdre", ACC_ROUGE_MOYEN)),
        ap("Terrasses du Larzac", "Languedoc", r(R_MOYEN, "Syrah, grenache, carignan, mourvèdre", ACC_ROUGE_MOYEN)),
        ap("Faugères", "Languedoc", r(R_MOYEN, "Syrah, grenache, carignan", ACC_ROUGE_MOYEN)),
        ap("Saint-Chinian", "Languedoc", r(R_MOYEN, "Syrah, grenache, carignan", ACC_ROUGE_MOYEN)),
        ap("Minervois", "Languedoc", r(R_MOYEN, "Syrah, grenache, carignan", ACC_ROUGE_MOYEN)),
        ap("Corbières", "Languedoc", r(R_MOYEN, "Carignan, grenache, syrah", ACC_ROUGE_MOYEN)),
        ap("Fitou", "Languedoc", r(R_MOYEN, "Carignan, grenache, syrah", ACC_ROUGE_MOYEN)),
        ap("La Clape", "Languedoc", r(R_MOYEN, "Grenache, syrah, mourvèdre", ACC_ROUGE_MOYEN)),
        ap("Picpoul de Pinet", "Languedoc", b(B_VIF, "Piquepoul", "Huîtres, coquillages")),
        ap("Blanquette de Limoux", "Languedoc", ef(EFF, "Mauzac", ACC_BULLES)),
        ap("Crémant de Limoux", "Languedoc", ef(EFF, "Chardonnay, chenin", ACC_BULLES)),
        ap("Limoux", "Languedoc", b(B_MOYEN, "Chardonnay, chenin", ACC_BLANC_RICHE)),
        ap("Languedoc", "Languedoc", listOf("Coteaux du Languedoc"),
            r(R_LEGER, "Grenache, syrah, carignan", ACC_ROUGE_LEGER), ro(ROSE, "Grenache, cinsault", ACC_ROSE)),
        ap("Collioure", "Roussillon", r(R_MOYEN, "Grenache, mourvèdre, syrah", ACC_ROUGE_MOYEN)),
        ap("Côtes du Roussillon Villages", "Roussillon", r(R_MOYEN, "Grenache, syrah, carignan", ACC_ROUGE_MOYEN)),
        ap("Côtes du Roussillon", "Roussillon", r(R_LEGER, "Grenache, syrah, carignan", ACC_ROUGE_LEGER)),
        ap("Banyuls", "Roussillon", li(VDN_R, "Grenache noir", ACC_VDN)),
        ap("Maury", "Roussillon", li(VDN_R, "Grenache noir", ACC_VDN), r(R_MOYEN, "Grenache, syrah, carignan", ACC_ROUGE_MOYEN)),
        ap("Rivesaltes", "Roussillon", li(VDN_R, "Grenache, macabeu", ACC_VDN)),
        ap("Muscat de Rivesaltes", "Roussillon", li(VDN_B, "Muscat", ACC_LIQ)),

        // ---- Sud-Ouest
        ap("Cahors", "Sud-Ouest", r(R_CORSE, "Malbec (côt), merlot", ACC_ROUGE_GARDE)),
        ap("Madiran", "Sud-Ouest", r(R_GARDE, "Tannat, cabernet franc", "Confit, magret, cassoulet")),
        ap("Pacherenc du Vic-Bilh", "Sud-Ouest", li(MOELLEUX, "Petit manseng, gros manseng", ACC_LIQ)),
        ap("Jurançon", "Sud-Ouest",
            li(MOELLEUX, "Petit manseng, gros manseng", ACC_LIQ), b(B_MOYEN, "Gros manseng", ACC_BLANC_VIF)),
        ap("Irouléguy", "Sud-Ouest", r(R_MOYEN, "Tannat, cabernet franc", ACC_ROUGE_MOYEN)),
        ap("Bergerac", "Sud-Ouest", r(R_LEGER, "Merlot, cabernet", ACC_ROUGE_LEGER), b(B_VIF, "Sauvignon, sémillon", ACC_BLANC_VIF)),
        ap("Pécharmant", "Sud-Ouest", r(R_MOYEN, "Merlot, cabernet", ACC_ROUGE_MOYEN)),
        ap("Monbazillac", "Sud-Ouest", li(MOELLEUX, "Sémillon, sauvignon, muscadelle", ACC_LIQ)),
        ap("Gaillac", "Sud-Ouest", r(R_LEGER, "Braucol, duras, syrah", ACC_ROUGE_LEGER), b(B_VIF, "Mauzac, loin de l'œil", ACC_BLANC_VIF)),
        ap("Fronton", "Sud-Ouest", r(R_LEGER, "Négrette", ACC_ROUGE_LEGER)),
        ap("Buzet", "Sud-Ouest", r(R_LEGER, "Merlot, cabernet", ACC_ROUGE_LEGER)),
        ap("Côtes de Gascogne", "Sud-Ouest", b(B_VIF, "Colombard, ugni blanc, gros manseng", ACC_BLANC_VIF)),

        // ---- Hors de France (les plus courants)
        ap("Rioja", "Espagne", r(R_CORSE, "Tempranillo, garnacha", ACC_ROUGE_MOYEN)),
        ap("Ribera del Duero", "Espagne", r(R_GARDE, "Tempranillo", ACC_ROUGE_GARDE)),
        ap("Priorat", "Espagne", r(R_GARDE, "Garnacha, cariñena", ACC_ROUGE_GARDE)),
        ap("Cava", "Espagne", ef(EFF, "Macabeu, xarel·lo, parellada", ACC_BULLES)),
        ap("Barolo", "Italie", r(R_GRAND, "Nebbiolo", ACC_ROUGE_GARDE)),
        ap("Barbaresco", "Italie", r(R_GARDE, "Nebbiolo", ACC_ROUGE_GARDE)),
        ap("Brunello di Montalcino", "Italie", r(R_GRAND, "Sangiovese", ACC_ROUGE_GARDE)),
        ap("Chianti Classico", "Italie", r(R_CORSE, "Sangiovese", ACC_ROUGE_MOYEN)),
        ap("Chianti", "Italie", r(R_MOYEN, "Sangiovese", "Pâtes à la tomate, viandes grillées")),
        ap("Amarone della Valpolicella", "Italie", listOf("Amarone"), r(R_GARDE, "Corvina, rondinella", ACC_ROUGE_GARDE)),
        ap("Valpolicella", "Italie", r(R_LEGER, "Corvina, rondinella", ACC_ROUGE_LEGER)),
        ap("Montepulciano d'Abruzzo", "Italie", r(R_MOYEN, "Montepulciano", ACC_ROUGE_MOYEN)),
        ap("Prosecco", "Italie", ef(EFF, "Glera", ACC_BULLES)),
        ap("Douro", "Portugal", r(R_CORSE, "Touriga nacional, touriga franca", ACC_ROUGE_GARDE)),
        ap("Porto", "Portugal", listOf("Port"), li(PORTO, "Touriga nacional, touriga franca, tinta roriz", ACC_VDN)),
    )

    private data class Entree(val cle: String, val appellation: Appellation)

    /** Toutes les clés normalisées, les plus longues d'abord pour préférer « Saint-Émilion Grand Cru » à « Saint-Émilion ». */
    private val index: List<Entree> = appellations
        .flatMap { a -> (listOf(a.nom) + a.alias).map { Entree(normaliser(it), a) } }
        .sortedByDescending { it.cle.length }

    /** Retrouve une appellation citée quelque part dans un texte (celui de l'étiquette). */
    fun chercherDans(texte: String): Appellation? {
        val t = " " + normaliser(texte) + " "
        return index.firstOrNull { t.contains(" " + it.cle + " ") }?.appellation
    }

    /** Appellation dont le nom (ou un alias) est exactement celui saisi. */
    fun exacte(nom: String): Appellation? {
        val n = normaliser(nom)
        if (n.isEmpty()) return null
        return index.firstOrNull { it.cle == n }?.appellation
    }

    /** Propositions pendant la saisie. */
    fun suggestions(saisie: String, max: Int = 6): List<Appellation> {
        val n = normaliser(saisie)
        if (n.length < 2) return emptyList()
        val debut = appellations.filter { normaliser(it.nom).startsWith(n) }
        val milieu = appellations.filter { a ->
            a !in debut && (listOf(a.nom) + a.alias).any { normaliser(it).contains(n) }
        }
        return (debut + milieu).filter { normaliser(it.nom) != n }.take(max)
    }

    fun normaliser(texte: String): String {
        val sansAccent = Normalizer.normalize(texte.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace("œ", "oe")
        return sansAccent
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\bst\\b"), "saint")
            .replace(Regex("\\bste\\b"), "sainte")
            .trim()
    }
}
