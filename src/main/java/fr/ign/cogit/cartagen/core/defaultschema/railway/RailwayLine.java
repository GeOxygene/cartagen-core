/*******************************************************************************
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL-C_fr.html see Licence_CeCILL-C_en.html
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info/a>
 * 
 * @copyright IGN
 ******************************************************************************/
package fr.ign.cogit.cartagen.core.defaultschema.railway;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import fr.ign.cogit.cartagen.core.GeneralisationLegend;
import fr.ign.cogit.cartagen.core.SLDUtilCartagen;
import fr.ign.cogit.cartagen.core.defaultschema.network.NetworkSection;
import fr.ign.cogit.cartagen.core.genericschema.network.INetworkNode;
import fr.ign.cogit.cartagen.core.genericschema.railway.IRailwayLine;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.schemageo.api.ferre.TronconFerre;
import fr.ign.cogit.geoxygene.schemageo.api.support.reseau.Direction;
import fr.ign.cogit.geoxygene.schemageo.impl.ferre.TronconFerreImpl;
import fr.ign.cogit.geoxygene.schemageo.impl.support.reseau.ReseauImpl;

/*
 * ###### IGN / CartAGen ###### Title: RailwayLine Description: Tronçons du
 * réseau ferré Author: J. Renard Date: 18/09/2009
 */
@Entity
@Access(AccessType.PROPERTY)
public class RailwayLine extends NetworkSection implements IRailwayLine {

    public static final Class<?> associatedNodeClass = RailwayNode.class;

    /**
     * Associated Geoxygene schema object
     */
    private TronconFerre geoxObj;
    private RailwayNode initialNode, finalNode;
    private Direction direction;
    private boolean deadEnd = false;
    private boolean sidetrack = false;

    /**
     * Constructor
     */
    public RailwayLine(TronconFerre geoxObj, int importance) {
        super();
        this.geoxObj = geoxObj;
        this.setInitialGeom(geoxObj.getGeom());
        this.setEliminated(false);
        this.setImportance(importance);
    }

    public RailwayLine(ILineString line, int importance) {
        super();
        this.geoxObj = new TronconFerreImpl(new ReseauImpl(), false, line,
                importance);
        this.setInitialGeom(line);
        this.setEliminated(false);
        this.setImportance(importance);
    }

    /**
     * Default constructor, used by Hibernate.
     */
    public RailwayLine() {
        super();
    }

    @Override
    @Transient
    public IFeature getGeoxObj() {
        return this.geoxObj;
    }

    @Override
    @Transient
    public double getWidth() {
        if (this.getSymbolId() == -2) {// SLD width
            return SLDUtilCartagen.getSymbolMaxWidthMapMm(this);
        }
        return GeneralisationLegend.RES_FER_LARGEUR;
    }

    @Override
    @Transient
    public double getInternWidth() {
        if (this.getSymbolId() == -2) {// SLD width
            return SLDUtilCartagen.getSymbolInnerWidthMapMm(this);
        }
        return GeneralisationLegend.RES_FER_LARGEUR;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    @Transient
    public INetworkNode getFinalNode() {
        return this.finalNode;
    }

    @Override
    @Transient
    public INetworkNode getInitialNode() {
        return this.initialNode;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void setFinalNode(INetworkNode node) {
        this.finalNode = (RailwayNode) node;
    }

    @Override
    public void setInitialNode(INetworkNode node) {
        this.initialNode = (RailwayNode) node;
    }

    @Override
    public boolean isDeadEnd() {
        return this.deadEnd;
    }

    @Override
    public void setDeadEnd(boolean deadEnd) {
        this.deadEnd = deadEnd;
    }

    @Override
    @Type(type = "fr.ign.cogit.cartagen.core.persistence.GeOxygeneGeometryUserType")
    public ILineString getGeom() {
        return super.getGeom();
    }

    /**
     * 
     * {@inheritDoc}
     * <p>
     * 
     */
    @Override
    @Column(name = "CartAGenDB_name")
    public String getDbName() {
        return super.getDbName();
    }

    @Override
    @Id
    public int getId() {
        return super.getId();
    }

    @Override
    public int getSymbolId() {
        return super.getSymbolId();
    }

    @Override
    public boolean isEliminated() {
        return super.isEliminated();
    }

    @Override
    @Transient
    public void setSidetrack(Boolean sidetrack) {
        this.sidetrack = sidetrack;
    }

    @Override
    @Transient
    public boolean isSidetrack() {
        return this.sidetrack;
    }
}
