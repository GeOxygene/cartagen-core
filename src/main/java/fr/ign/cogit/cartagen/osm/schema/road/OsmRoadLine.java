/*******************************************************************************
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL-C_fr.html see Licence_CeCILL-C_en.html
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info/a>
 * 
 * @copyright IGN
 ******************************************************************************/
package fr.ign.cogit.cartagen.osm.schema.road;

import java.awt.Color;
import java.util.Date;
import java.util.Map;

import javax.persistence.Transient;

import fr.ign.cogit.cartagen.core.GeneralisationLegend;
import fr.ign.cogit.cartagen.core.Legend;
import fr.ign.cogit.cartagen.core.SLDUtilCartagen;
import fr.ign.cogit.cartagen.core.genericschema.network.INetworkNode;
import fr.ign.cogit.cartagen.core.genericschema.road.IRoadLine;
import fr.ign.cogit.cartagen.osm.schema.network.OsmNetworkSection;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.ICurve;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.schemageo.api.routier.TronconDeRoute;
import fr.ign.cogit.geoxygene.schemageo.impl.routier.TronconDeRouteImpl;
import fr.ign.cogit.geoxygene.schemageo.impl.support.reseau.ReseauImpl;

public class OsmRoadLine extends OsmNetworkSection implements IRoadLine {

    public static final Class<?> associatedNodeClass = OsmRoadNode.class;

    /**
     * Associated Geoxygene schema object
     */
    private TronconDeRoute geoxObj;
    private INetworkNode initialNode, finalNode;

    public OsmRoadLine(ILineString line, int importance) {
        super();
        this.geoxObj = new TronconDeRouteImpl(new ReseauImpl(), false, line,
                importance);
        this.setInitialGeom(line);
        this.setGeom(line);
        this.setImportance(importance);
    }

    public OsmRoadLine(TronconDeRoute geoxObj, int importance) {
        super();
        this.geoxObj = geoxObj;
        this.setInitialGeom(geoxObj.getGeom());
        this.setEliminated(false);
        this.setImportance(importance);
        this.setDeadEnd(false);
        this.initialNode = null;
        this.finalNode = null;
    }

    public OsmRoadLine(String contributor, IGeometry geom, int id,
            int changeSet, int version, int uid, Date date) {
        super(contributor, geom, id, changeSet, version, uid, date);
        this.geoxObj = new TronconDeRouteImpl(new ReseauImpl(), false,
                (ICurve) geom, 0);
    }

    public OsmRoadLine() {
        super();
        this.geoxObj = new TronconDeRouteImpl(new ReseauImpl(), false, null, 0);
    }

    @Override
    public double getWidth() {
        return SLDUtilCartagen.getSymbolMaxWidthMapMm(this);
    }

    @Override
    public double getInternWidth() {

        return SLDUtilCartagen.getSymbolInnerWidthMapMm(this);

    }

    @Override
    public INetworkNode getInitialNode() {
        return initialNode;
    }

    @Override
    public void setInitialNode(INetworkNode node) {
        this.initialNode = node;
    }

    @Override
    public INetworkNode getFinalNode() {
        return finalNode;
    }

    @Override
    public void setFinalNode(INetworkNode node) {
        this.finalNode = node;
    }

    @Override
    public Color getSeparatorColor() {
        if (this.getImportance() == 4) {
            return GeneralisationLegend.ROUTIER_COULEUR_SEPARATEUR_4;
        }
        return null;
    }

    @Override
    public Color getFrontColor() {
        if (this.getImportance() == 0) {
            return GeneralisationLegend.ROUTIER_COULEUR_0;
        }
        if (this.getImportance() == 1) {
            return GeneralisationLegend.ROUTIER_COULEUR_1;
        }
        if (this.getImportance() == 2) {
            return GeneralisationLegend.ROUTIER_COULEUR_2;
        }
        if (this.getImportance() == 3) {
            return GeneralisationLegend.ROUTIER_COULEUR_3;
        }
        if (this.getImportance() == 4) {
            return GeneralisationLegend.ROUTIER_COULEUR_4;
        }
        return null;
    }

    @Override
    public int getImportance() {
        if (super.getImportance() != -1)
            return super.getImportance();
        computeImportance();
        return super.getImportance();
    }

    private void computeImportance() {
        // get the highway tag value
        String value = this.getTags().get("highway");
        if (value.equals("motorway") || value.equals("motorway_link")
                || value.equals("trunk") || value.equals("trunk_link"))
            setImportance(4);
        else if (value.equals("primary") || value.equals("primary_link"))
            setImportance(3);
        else if (value.equals("secondary") || value.equals("secondary_link")
                || value.equals("tertiary") || value.equals("tertiary_link"))
            setImportance(2);
        else if (value.equals("road") || value.equals("residential")
                || value.equals("pedestrian"))
            setImportance(1);
        else
            setImportance(0);
    }

    @Override
    @Transient
    public IFeature getGeoxObj() {
        return this.geoxObj;
    }

    @Override
    public IGeometry getSymbolGeom() {
        return super.getGeom()
                .buffer(getWidth() * Legend.getSYMBOLISATI0N_SCALE() / 2000);
    }

    @Override
    public void setTags(Map<String, String> tags) {
        super.setTags(tags);
        // compute the importance from tags
        this.computeImportance();
    }

}
