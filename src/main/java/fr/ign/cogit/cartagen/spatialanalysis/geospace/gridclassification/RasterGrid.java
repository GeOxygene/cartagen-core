/*
 * Créé le 30 août 2008
 * 
 * Pour changer le modèle de ce fichier généré, allez à :
 * Fenêtre>Préférences>Java>Génération de code>Code et commentaires
 */
package fr.ign.cogit.cartagen.spatialanalysis.geospace.gridclassification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.locationtech.jts.geom.Geometry;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.util.algo.JtsAlgorithms;
import fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene;

/**
 * @author moi
 * 
 *         Classe Mère pour les grilles de type raster surimposée sur des
 *         données
 */
public abstract class RasterGrid {
  private final static Logger logger = Logger.getLogger(RasterGrid.class
      .getName());
  private int tailleCellule;// la taille de chaque cellule
  private int radiusCellule;
  private double xBas, xHaut, yBas, yHaut;// coordonnées de l'étendue de la zone
  private int nbLigne, nbColonne;// le nombre de lignes et de colonnes de la
                                 // grille
  private IDirectPosition coordGrille;// coordonnée du point en haut à gauche de
                                      // la grille
  private ArrayList<GridCell> listCellules;// les cellules de la grille classées
                                           // par ligne
  // en commençant par la cellule en haut à gauche.
  private double seuilSimilarite;// en-dessous de cette valeur, deux classes
  // finales de cellules sont considérées comme similaires

  // données géographiques utilisées
  private HashMap<String, IFeatureCollection<? extends IFeature>> data;

  // définition des critères de clustering
  private HashMap<String, Vector<Number>> mapCriteres;// cette map associe à un
                                                      // nom de critère un
                                                      // vecteur

  // contenant tous les paramètres pour ce critère : le poids (Double), le type
  // de données (String), le seuil Haut (Double ou Integer) et le seuil Bas.

  // ************************
  // GETTERS AND SETTERS
  // ************************
  public int getTailleCellule() {
    return this.tailleCellule;
  }

  public void setTailleCellule(int tailleCellule) {
    this.tailleCellule = tailleCellule;
  }

  public int getRadiusCellule() {
    return this.radiusCellule;
  }

  public void setRadiusCellule(int radiusCellule) {
    this.radiusCellule = radiusCellule;
  }

  public double getxBas() {
    return this.xBas;
  }

  public void setxBas(double xBas) {
    this.xBas = xBas;
  }

  public double getxHaut() {
    return this.xHaut;
  }

  public void setxHaut(double xHaut) {
    this.xHaut = xHaut;
  }

  public double getyBas() {
    return this.yBas;
  }

  public void setyBas(double yBas) {
    this.yBas = yBas;
  }

  public double getyHaut() {
    return this.yHaut;
  }

  public void setyHaut(double yHaut) {
    this.yHaut = yHaut;
  }

  public int getNbLigne() {
    return this.nbLigne;
  }

  public void setNbLigne(int nbLigne) {
    this.nbLigne = nbLigne;
  }

  public int getNbColonne() {
    return this.nbColonne;
  }

  public void setNbColonne(int nbColonne) {
    this.nbColonne = nbColonne;
  }

  public IDirectPosition getCoordGrille() {
    return this.coordGrille;
  }

  public void setCoordGrille(IDirectPosition coordGrille) {
    this.coordGrille = coordGrille;
  }

  public ArrayList<GridCell> getListCellules() {
    return this.listCellules;
  }

  public void setListCellules(ArrayList<GridCell> listCellules) {
    this.listCellules = listCellules;
  }

  public HashMap<String, Vector<Number>> getMapCriteres() {
    return this.mapCriteres;
  }

  public void setMapCriteres(HashMap<String, Vector<Number>> mapCriteres) {
    this.mapCriteres = mapCriteres;
  }

  public double getSeuilSimilarite() {
    return this.seuilSimilarite;
  }

  public void setSeuilSimilarite(double seuilSimilarite) {
    this.seuilSimilarite = seuilSimilarite;
  }

  public void setData(
      HashMap<String, IFeatureCollection<? extends IFeature>> data) {
    this.data = data;
  }

  public HashMap<String, IFeatureCollection<? extends IFeature>> getData() {
    return this.data;
  }

  /**
   * Constructeur simple avec les paramètres de base.
   * @throws Exception
   * 
   */
  public RasterGrid(int cellule, int radius, double xB, double xH, double yB,
      double yH, double similarite) {
    this.tailleCellule = cellule;
    this.radiusCellule = radius;
    this.xBas = xB;
    this.xHaut = xH;
    this.yBas = yB;
    this.yHaut = yH;
    this.seuilSimilarite = similarite;
    this.data = new HashMap<String, IFeatureCollection<? extends IFeature>>();
    this.setTailleGrille();
    this.setCoordGrille();
  }

  private void setTailleGrille() {
    this.nbLigne = 1 + (int) Math.round((this.yHaut - this.yBas)
        / this.tailleCellule);
    this.nbColonne = 1 + (int) Math.round((this.xHaut - this.xBas)
        / this.tailleCellule);
  }

  private void setCoordGrille() {
    // on calcule l'écart entre la longueur de la grille et celle de la zone
    double ecartLong = this.nbColonne * this.tailleCellule
        - (this.xHaut - this.xBas);
    double ecartHauteur = this.nbLigne * this.tailleCellule
        - (this.yHaut - this.yBas);
    double xCoord = this.xBas - ecartLong / 2;
    double yCoord = this.yHaut + ecartHauteur / 2;
    this.coordGrille = new DirectPosition(xCoord, yCoord);
  }

  /**
   * Construction des cellules de la grille avec ses critères
   * @throws Exception
   */
  protected void construireCellules() {
    this.listCellules = new ArrayList<GridCell>();
    // on fait une boucle sur toutes les lignes et les colonnes
    for (int i = 1; i <= this.nbLigne * this.nbColonne; i++) {
      int colonne = i % this.nbColonne;
      if (colonne == 0) {
        colonne = this.nbColonne;
      }
      int ligne = (i - colonne) / this.nbColonne + 1;
      GridCell cellule = new GridCell(this, ligne, colonne);
      this.listCellules.add(cellule);
    }
  }

  /**
   * Regroupe les cellules en clusters de cellules de même classe. La méthode
   * renvoie un set de clusters qui sont des sets de cellules. Classifie selon
   * le critère passé en entrée : si c'est "total", la classif se fait selon
   * l'agrégation des critères.
   * 
   */
  public HashMap<HashSet<GridCell>, Double> creerClusters(String critere) {
    HashMap<HashSet<GridCell>, Double> clusters = new HashMap<HashSet<GridCell>, Double>();

    // on met les cellules dans une pile
    Stack<GridCell> pile = new Stack<GridCell>();
    pile.addAll(this.listCellules);
    // on parcourt la pile des cellules
    while (!pile.isEmpty()) {
      // System.out.println(pile.size());
      GridCell cellule = pile.peek();
      // on forme un cluster à partir de cette cellule
      CellClusterResult result = this.clusterAutourCellule(critere, cellule);
      // on l'ajoute aux clusters
      clusters.put(result.cluster, new Double(result.classValue));
      // on enlève ses éléments de la pile
      pile.removeAll(result.cluster);
    }

    return clusters;
  }

  /**
   * Pour une cellule de la grille, crée un cluster de cellules de même classe.
   * Classifie selon le critère passé en entrée : si c'est "total", la classif
   * se fait selon l'agrégation des critères.
   * 
   * @return Vector[2] : un set de cellules de la grille en 0 et la classif en 1
   */
  private CellClusterResult clusterAutourCellule(String critere, GridCell cell) {
    HashSet<GridCell> cluster = new HashSet<GridCell>();

    // on commence par ajouter cell au cluster
    cluster.add(cell);

    // on détermine la classe de cell selon critere
    double classe = cell.getClasseFinale();
    // System.out.println(classe);
    if (!critere.equals("total")) {
      CellCriterion crit = cell.getCritere(critere);
      classe = crit.getClassif();
    }

    // on met les voisins de cell dans la queue
    Stack<GridCell> pile = new Stack<GridCell>();
    pile.addAll(cell.recupererCellulesVoisines());
    // tant qu'il y a des voisins à tester
    while (!pile.isEmpty()) {
      GridCell voisine = pile.pop();
      // on détermine la classe de voisine
      double classeVoisine = voisine.getClasseFinale();
      if (!critere.equals("total")) {
        CellCriterion crit = voisine.getCritere(critere);
        classeVoisine = crit.getClassif();
      }// if(!critere.equals("total"))

      // si les classes ne sont pas identiques, on passe au suivant
      if (RasterGrid.logger.isTraceEnabled()) {
        RasterGrid.logger.trace(new Double(classe));
        RasterGrid.logger.trace(new Double(classeVoisine));
        RasterGrid.logger.trace("difference : "
            + Math.abs(classe - classeVoisine));
      }
      if (Math.abs(classe - classeVoisine) > this.getSeuilSimilarite()) {
        continue;
      }
      // ici, on a une voisine de même classe
      // on affecte la classe moyenne aux deux cellules
      voisine.setClasseFinale((classe + classeVoisine) / 2.0);
      cell.setClasseFinale((classe + classeVoisine) / 2.0);
      // on l'ajoute au cluster
      cluster.add(voisine);
      // on ajoute les voisines de voisine à la queue
      HashSet<GridCell> voisins = voisine.recupererCellulesVoisines();
      voisins.removeAll(cluster);
      pile.addAll(voisins);
    }// while, tant que la queue n'est pas vide

    return new CellClusterResult(classe, cluster);
  }// clusterAutourCellule(String critere,CelluleGrille cell)

  public void afficheCluster(HashSet<GridCell> cluster, int classe) {
    Iterator<GridCell> iter = cluster.iterator();
    while (iter.hasNext()) {
      GridCell cellule = iter.next();
      cellule.afficheCellule(classe);
    }
  }// afficheCluster(HashSet cluster)

  public IPolygon creerGeomCluster(HashSet<GridCell> cluster) throws Exception {
    // on commence par récupérer une cellule du cluster
    GridCell cell = cluster.iterator().next();
    HashSet<GridCell> voisins = new HashSet<GridCell>();
    voisins.addAll(cluster);
    IPolygon geomCluster = cell.construireGeom();
    voisins.retainAll(cell.recupererCellulesVoisines());
    HashSet<GridCell> traites = new HashSet<GridCell>();
    traites.add(cell);
    // tant qu'il y a des voisins, on les agrège
    while (!voisins.isEmpty()) {
      HashSet<GridCell> copie = new HashSet<GridCell>();
      copie.addAll(voisins);
      Iterator<GridCell> iter = copie.iterator();
      while (iter.hasNext()) {
        GridCell cellule = iter.next();
        IPolygon geomCell = cellule.construireGeom();
        geomCluster = (IPolygon) geomCluster.union(geomCell);

        // on enlève cellule de voisins
        voisins.remove(cellule);
        // on la met dans traites
        traites.add(cellule);
        // on cherche les nouveaux voisins
        HashSet<GridCell> nouveauxVoisins = cellule.recupererCellulesVoisines();
        nouveauxVoisins.retainAll(cluster);
        nouveauxVoisins.removeAll(traites);
        voisins.addAll(nouveauxVoisins);
      }// while, boucle sur la copie de voisins à ce tour de boucle
    }// tant qu'il y a des voisins

    // on fait une fermeture des 2/3 de la taille de la cellule pour
    // supprimer les trous de la taille d'une cellule
    Geometry fermee = JtsAlgorithms.fermeture(JtsGeOxygene
        .makeJtsGeom(geomCluster), 0.66 * this.tailleCellule, 4);
    geomCluster = (IPolygon) JtsGeOxygene.makeGeOxygeneGeom(fermee);

    return geomCluster;
  }

  class CellClusterResult {
    public double classValue;
    public HashSet<GridCell> cluster;

    public CellClusterResult(double classValue, HashSet<GridCell> cluster) {
      super();
      this.classValue = classValue;
      this.cluster = cluster;
    }
  }

}// class GrilleRaster
