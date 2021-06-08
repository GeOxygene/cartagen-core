/*******************************************************************************
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL-C_fr.html see Licence_CeCILL-C_en.html
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info/a>
 * 
 * @copyright IGN
 ******************************************************************************/
package fr.ign.cogit.cartagen.core.genericschema.urban;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import fr.ign.cogit.cartagen.core.genericschema.IGeneObjSurf;
import fr.ign.cogit.cartagen.core.genericschema.IMesoObject;
import fr.ign.cogit.cartagen.core.genericschema.network.INetworkSection;
import fr.ign.cogit.cartagen.spatialanalysis.network.DeadEndGroup;
import fr.ign.cogit.cartagen.spatialanalysis.network.streets.CityAxis;
import fr.ign.cogit.cartagen.spatialanalysis.network.streets.CityPartition;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;

public interface IUrbanBlock extends IGeneObjSurf, IMesoObject<IUrbanElement> {

    /**
     * Get the town the block is part of.
     * 
     * @return
     * @author GTouya
     */
    public ITown getTown();

    public void setTown(ITown town);

    /**
     * Get the network sections (e.g. roads, rivers) surrounding the block.
     * 
     * @return
     */
    public IFeatureCollection<INetworkSection> getSurroundingNetwork();

    public void setSurroundingNetwork(
            IFeatureCollection<INetworkSection> surroundingNetwork);

    /**
     * Gets the urban elements composing the block
     * 
     * @return
     */
    public IFeatureCollection<IUrbanElement> getUrbanElements();

    public void setUrbanElements(
            IFeatureCollection<IUrbanElement> urbanElements);

    void addUrbanElement(IUrbanElement urbanElement);

    /**
     * Gets the inner alignments of the block
     * 
     * @return
     */
    public IFeatureCollection<IUrbanAlignment> getAlignments();

    public void setAlignments(IFeatureCollection<IUrbanAlignment> alignments);

    /**
     * Get the empty spaces of the block, i.e. the parts of the block where
     * there is no urban element.
     * 
     * @return
     */
    public Collection<IEmptySpace> getEmptySpaces();

    /**
     * Determines if the block is fully colored as a meso
     */
    public boolean isColored();

    public void setColored(boolean bool);

    // //////////////////////////
    // Addtional spatial analysis méthods
    // //////////////////////////

    public void setPartition(CityPartition nearest);

    public CityPartition getPartition();

    public Set<IUrbanBlock> getNeighbours();

    public double getDensity();

    /**
     * Determines if a city block is standard, that is to say, if it can be
     * aggregated during the selection process. Road structures like roundabouts
     * are not standard blocks.
     */
    public boolean isStandard();

    public void updateGeom(IPolygon cutGeom);

    /**
     * true if the block is a hole inside another block. It means that the block
     * is a part of a dead end group.
     */
    public boolean isHoleBlock();

    public void setHoleBlock(boolean holeBlock);

    public Set<CityAxis> getAxes();

    public Set<IUrbanBlock> getInitialGeoxBlocks();

    public double getSimulatedDensity();

    public IUrbanBlock aggregateWithBlock(IUrbanBlock neigh);

    public boolean isEdge();

    public void setEdge(boolean b);

    public int getAggregLevel();

    public void setAggregLevel(int i);

    public HashSet<IUrbanBlock> getInsideBlocks();

    public void setStemmingFromN1Transfo(boolean b);

    /**
     * Initialise the components of the block (urban elements, network, etc.).
     */
    public void initComponents();

    /**
     * Feat type name
     */
    public static final String FEAT_TYPE_NAME = "UrbanBlock";

    public HashSet<DeadEndGroup> getInsideDeadEnds();

}
