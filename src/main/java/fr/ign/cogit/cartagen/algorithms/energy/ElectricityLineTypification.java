/*******************************************************************************
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL-C_fr.html see Licence_CeCILL-C_en.html
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info/a>
 * 
 * @copyright IGN
 ******************************************************************************/
package fr.ign.cogit.cartagen.algorithms.energy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import fr.ign.cogit.cartagen.core.dataset.CartAGenDoc;
import fr.ign.cogit.cartagen.core.genericschema.energy.IElectricityLine;
import fr.ign.cogit.cartagen.core.genericschema.network.INetwork;
import fr.ign.cogit.cartagen.core.genericschema.network.INetworkSection;
import fr.ign.cogit.cartagen.spatialanalysis.network.NetworkEnrichment;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiPoint;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.algo.geometricAlgorithms.CommonAlgorithmsFromCartAGen;
import fr.ign.cogit.geoxygene.util.algo.geomstructure.Vector2D;

/**
 * This class contains instances of an algorithm to typify electricity network
 * by collapsing the clusters of parallel and close lines to a signle line.
 * Intersections with other lines are snapped to the new line.
 * @author GTouya
 * 
 */
public class ElectricityLineTypification {

  private INetwork electricityNet;
  private double bufferSize;

  public ElectricityLineTypification(INetwork electricityNet,
      double bufferSize) {
    super();
    this.setElectricityNet(electricityNet);
    this.setBufferSize(bufferSize);
  }

  public double getBufferSize() {
    return bufferSize;
  }

  public void setBufferSize(double bufferSize) {
    this.bufferSize = bufferSize;
  }

  public INetwork getElectricityNet() {
    return electricityNet;
  }

  public void setElectricityNet(INetwork electricityNet) {
    this.electricityNet = electricityNet;
  }

  public void typify() {
    // first enrich the network if necessary (i.e. build topology)
    enrichNetwork();

    // find the clusters of parallel sections in the network
    Set<ParallelSectionsCluster> clusters = findClusters();
    for (ParallelSectionsCluster cluster : clusters)
      cluster.collapse();

    // update topology
    this.electricityNet.removeAllNodes();
    enrichNetwork();
  }

  public Set<ParallelSectionsCluster> findClusters() {
    Set<ParallelSectionsCluster> clusters = new HashSet<ElectricityLineTypification.ParallelSectionsCluster>();
    Stack<IElectricityLine> lines = new Stack<IElectricityLine>();
    Set<IElectricityLine> treated = new HashSet<IElectricityLine>();
    for (INetworkSection line : this.electricityNet.getSections())
      lines.add((IElectricityLine) line);
    while (!lines.empty()) {
      Set<IElectricityLine> components = new HashSet<IElectricityLine>();
      IElectricityLine line = lines.pop();
      treated.add(line);
      components.add(line);
      IGeometry buffer = line.getGeom().buffer(2 * bufferSize);
      IFeatureCollection<IElectricityLine> fc = new FT_FeatureCollection<IElectricityLine>(
          lines);
      Stack<IElectricityLine> neighbours = new Stack<IElectricityLine>();
      neighbours.add(line);
      while (!neighbours.empty()) {
        IElectricityLine neighbour = neighbours.pop();
        components.add(neighbour);
        Collection<IElectricityLine> querySet = fc
            .select(neighbour.getGeom().buffer(bufferSize));
        querySet.removeAll(components);
        querySet.removeAll(treated);
        // now filter the querySet to keep only the lines partly parallel to
        // neighbour
        for (IElectricityLine buffLine : querySet) {
          IGeometry inter = buffer.intersection(buffLine.getGeom());
          double length = inter.length();
          if (length > 10 * bufferSize) {
            // arrived here, buffLine is parallel to neigh
            neighbours.add(buffLine);
          }
        }
      }
      // build a new cluster if there are several lines in the components set
      if (components.size() > 1)
        clusters.add(new ParallelSectionsCluster(components, line));
    }
    return clusters;
  }

  private void enrichNetwork() {
    NetworkEnrichment
        .enrichNetwork(CartAGenDoc.getInstance().getCurrentDataset(),
            CartAGenDoc.getInstance().getCurrentDataset()
                .getElectricityNetwork(),
            false, CartAGenDoc.getInstance().getCurrentDataset().getCartAGenDB()
                .getGeneObjImpl().getCreationFactory());
  }

  public class ParallelSectionsCluster {
    private Set<IElectricityLine> components;
    private IPolygon geom;
    private IElectricityLine rootLine;
    private Set<IElectricityLine> cutParts;

    public Set<IElectricityLine> getComponents() {
      return components;
    }

    public void setComponents(Set<IElectricityLine> components) {
      this.components = components;
    }

    public IPolygon getGeom() {
      return geom;
    }

    public void setGeom(IPolygon geom) {
      this.geom = geom;
    }

    public ParallelSectionsCluster(Set<IElectricityLine> components,
        IElectricityLine rootLine) {
      super();
      this.setRootLine(rootLine);
      this.components = components;
      this.cutParts = new HashSet<IElectricityLine>();
      buildGeom();
    }

    private void buildGeom() {
      this.geom = (IPolygon) rootLine.getGeom().buffer(2 * bufferSize);
    }

    @SuppressWarnings("unchecked")
    private void cutComponents() {
      Set<IElectricityLine> loopSet = new HashSet<IElectricityLine>(components);
      for (IElectricityLine line : loopSet) {
        if (line.isEliminated())
          continue;
        if (line.equals(rootLine))
          continue;
        IGeometry newLine = null;
        try {
          newLine = line.getGeom().difference(geom);
        } catch (Exception e) {
          line.eliminate();
          continue;
        }
        if (newLine == null) {
          line.eliminate();
          continue;
        }
        if (newLine.isEmpty()) {
          line.eliminate();
          continue;
        }
        if (!newLine.isValid()) {
          line.eliminate();
          continue;
        }
        if (newLine instanceof ILineString) {
          // the difference JTS function sometimes produces geometries with null
          // vertices
          if (newLine.coord().contains(null)) {
            IDirectPositionList cleanCoords = new DirectPositionList();
            for (IDirectPosition pt : newLine.coord()) {
              if (pt == null)
                continue;
              cleanCoords.add(pt);
            }
            newLine = new GM_LineString(cleanCoords);
          }
          line.setGeom(newLine);
          this.cutParts.add(line);
        } else if (newLine instanceof IMultiCurve<?>) {
          boolean first = true;
          for (int i = 0; i < ((IMultiCurve<IOrientableCurve>) newLine)
              .size(); i++) {
            ILineString simple = (ILineString) ((IMultiCurve<IOrientableCurve>) newLine)
                .get(i);
            if (simple == null)
              continue;
            if (simple.isEmpty())
              continue;
            if (first) {
              first = false;
              line.setGeom(simple);
              continue;
            }
            if (simple.coord().contains(null)) {
              IDirectPositionList cleanCoords = new DirectPositionList();
              for (IDirectPosition pt : simple.coord()) {
                if (pt == null)
                  continue;
                cleanCoords.add(pt);
              }
              simple = new GM_LineString(cleanCoords);
            }
            // create a new feature
            IElectricityLine newFeat = CartAGenDoc.getInstance()
                .getCurrentDataset().getCartAGenDB().getGeneObjImpl()
                .getCreationFactory().createElectricityLine(simple, 0);
            electricityNet.getSections().add(newFeat);
            this.components.add(newFeat);
            this.cutParts.add(newFeat);
          }
        }
      }
    }

    public void collapse() {
      // to begin, cut the components to keep only their part outside the
      // cluster
      cutComponents();
      // then reconnect to the root line
      reconnectComponents();
    }

    private void reconnectComponents() {
      if (rootLine.getGeom() == null)
        return;
      if (rootLine.getGeom().coord().contains(null)) {
        IDirectPositionList cleanCoords = new DirectPositionList();
        for (IDirectPosition pt : rootLine.getGeom().coord()) {
          if (pt == null)
            continue;
          cleanCoords.add(pt);
        }
        rootLine.setGeom(new GM_LineString(cleanCoords));
      }
      for (IElectricityLine line : cutParts) {

        if (!line.getGeom().isValid())
          line.eliminate();

        IGeometry inter = line.getGeom().intersection(geom);
        if (inter instanceof IPoint) {

          // first, find the connection point
          Vector2D vect = new Vector2D(line.getGeom().coord().get(1),
              line.getGeom().coord().get(0));
          if (((IPoint) inter).getPosition().equals2D(line.getGeom().endPoint(),
              0.001)) {
            int length = line.getGeom().coord().size();
            vect = new Vector2D(line.getGeom().coord().get(length - 2),
                line.getGeom().coord().get(length - 1));
          }
          IDirectPosition lastPt = CommonAlgorithmsFromCartAGen.projection(
              ((IPoint) inter).getPosition(), rootLine.getGeom(), vect);
          if (lastPt == null)
            lastPt = CommonAlgorithmsFromCartAGen.projectionOrtho(
                ((IPoint) inter).getPosition(), rootLine.getGeom(), vect);
          if (lastPt == null)
            continue;
          // then, extend line to this connection point
          if (((IPoint) inter).getPosition().equals2D(line.getGeom().endPoint(),
              0.001)) {
            line.getGeom().addControlPoint(lastPt);
          } else {
            line.getGeom().addControlPoint(0, lastPt);
          }
        } else if (inter instanceof IMultiPoint) {
          for (int i = 0; i < ((IMultiPoint) inter).size(); i++) {
            IPoint simple = ((IMultiPoint) inter).get(i);
            if (simple == null)
              continue;
            // first, find the connection point
            Vector2D vect = new Vector2D(line.getGeom().coord().get(1),
                line.getGeom().coord().get(0));

            if (!simple.getPosition().equals2D(line.getGeom().coord().get(0),
                0.001)) {
              int length = line.getGeom().coord().size();
              vect = new Vector2D(line.getGeom().coord().get(length - 2),
                  line.getGeom().coord().get(length - 1));
            }

            IDirectPosition lastPt = CommonAlgorithmsFromCartAGen
                .projection(simple.getPosition(), rootLine.getGeom(), vect);
            if (lastPt == null)
              lastPt = CommonAlgorithmsFromCartAGen.projectionOrtho(
                  simple.getPosition(), rootLine.getGeom(), vect);
            if (lastPt == null)
              continue;
            // then, extend line to this connection point
            if (!simple.getPosition().equals2D(line.getGeom().coord().get(0),
                0.001)) {
              line.getGeom().addControlPoint(lastPt);
            } else {
              line.getGeom().addControlPoint(0, lastPt);
            }
          }
        }
      }
    }

    public IElectricityLine getRootLine() {
      return rootLine;
    }

    public void setRootLine(IElectricityLine rootLine) {
      this.rootLine = rootLine;
    }

    public Set<IElectricityLine> getCutParts() {
      return cutParts;
    }

    public void setCutParts(Set<IElectricityLine> cutParts) {
      this.cutParts = cutParts;
    }
  }
}
