package fr.ign.cogit.cartagen.spatialanalysis.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.feature.AbstractFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.schemageo.api.support.reseau.ArcReseau;
import fr.ign.cogit.geoxygene.schemageo.api.support.reseau.NoeudReseau;

/**
 * @author JTeulade-Denantes
 * 
 *         This class allows to scan easily the strokes So, you can directly use
 *         Stroke object instead of ArcReseau Object
 */
public class StrokeNode extends AbstractFeature {

  /**
   * this constructor build the strokes related to the node thanks to
   * noeudReseau
   * @param noeudReseau gives the arcs which go in and out the node
   * @param strokesNetwork allows to have the strokes list
   */
  public StrokeNode(NoeudReseau noeudReseau, StrokesNetwork strokesNetwork) {
    super();
    this.noeudReseau = noeudReseau;
    // for all the arcs going in the node
    for (ArcReseau arc : noeudReseau.getArcsEntrants()) {
      // we look for the stroke related to the arc
      for (Stroke stroke : strokesNetwork.getStrokes()) {
        // we check the first and the last arc in the stroke features because
        // stroke arcs are ordered
        if (stroke.getFeatures().get(stroke.getFeatures().size() - 1) == arc) {
          this.inStrokes.add(stroke);
          break;
        } else if (stroke.getFeatures().get(0) == arc) {
          this.outStrokes.add(stroke);
          break;
        }
      }
    }

    // for all the arcs going out the node
    for (ArcReseau arc : noeudReseau.getArcsSortants()) {
      for (Stroke stroke : strokesNetwork.getStrokes()) {
        // we switch the if conditions in the case of
        // stroke.getFeatures.size()=1
        if (stroke.getFeatures().get(0) == arc) {
          this.outStrokes.add(stroke);
          break;
        } else if (stroke.getFeatures().get(stroke.getFeatures().size() - 1) == arc) {
          this.inStrokes.add(stroke);
          break;
        }
      }
    }

  }

  private NoeudReseau noeudReseau;

  public NoeudReseau getNoeudReseau() {
    return noeudReseau;
  }

  /**
   * strokes going in the node
   */
  private Collection<Stroke> inStrokes = new FT_FeatureCollection<Stroke>();

  public Collection<Stroke> getInStrokes() {
    return this.inStrokes;
  }

  /**
   * strokes going out the node
   */
  private Collection<Stroke> outStrokes = new FT_FeatureCollection<Stroke>();

  public Collection<Stroke> getOutStrokes() {
    return this.outStrokes;
  }

  @Override
  public IFeature cloneGeom() throws CloneNotSupportedException {
    return null;
  }

  @Override
  public IPoint getGeom() {
    return this.noeudReseau.getGeom();
  }

  /**
   * This function returns ordered strokes related to this nodes
   * @param routeStrokes
   * @return the ordered list of strokes
   */
  public List<Stroke> orderedStrokes() {
    List<Stroke> orderedStrokes = new ArrayList<Stroke>();
    boolean out;
    // from all the selected arcs, we find the related strokes
    for (ArcReseau arc : this.getNoeudReseau().getClockwiseArcs()) {
      out = false;
      for (Stroke stroke : this.getInStrokes()) {
        if (stroke.getFeatures().get(0).equals(arc)
            || stroke.getFeatures().get(stroke.getFeatures().size() - 1)
                .equals(arc)) {
          orderedStrokes.add(stroke);
          out = true;
          break;
        }
      }
      if (out) {
        continue;
      }
      for (Stroke stroke : this.getOutStrokes()) {
        if (stroke.getFeatures().get(0).equals(arc)
            || stroke.getFeatures().get(stroke.getFeatures().size() - 1)
                .equals(arc)) {
          orderedStrokes.add(stroke);
          out = true;
          break;
        }
      }
      if (!out) {
        orderedStrokes.add(null);
      }
    }
    return orderedStrokes;
  }

}
