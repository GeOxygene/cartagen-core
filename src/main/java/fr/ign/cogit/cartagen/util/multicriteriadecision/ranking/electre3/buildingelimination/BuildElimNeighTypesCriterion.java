/*******************************************************************************
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL-C_fr.html see Licence_CeCILL-C_en.html
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info/a>
 * 
 * @copyright IGN
 ******************************************************************************/
package fr.ign.cogit.cartagen.util.multicriteriadecision.ranking.electre3.buildingelimination;

import java.util.Map;

import fr.ign.cogit.cartagen.core.genericschema.urban.IBuilding;
import fr.ign.cogit.cartagen.spatialanalysis.measures.congestion.CongestionComputation;
import fr.ign.cogit.geoxygene.contrib.multicriteriadecision.ranking.ELECTREIIICriterion;

/**
 * A criteria for building elimination in a block based on the types on
 * neighbours in the block triangulation: the value of the criterion decreases
 * when the some of the neighbours are roads.
 * @author GTouya
 *
 */
public class BuildElimNeighTypesCriterion extends ELECTREIIICriterion {

  // //////////////////////////////////////////
  // Fields //
  // //////////////////////////////////////////

  // All static fields //
  public static final String PARAM_BUILDING = "building";
  public static final String PARAM_DIST_MAX = "distanceMax";

  // Public fields //

  // Protected fields //

  // Package visible fields //

  // Private fields //

  // //////////////////////////////////////////
  // Static methods //
  // //////////////////////////////////////////

  // //////////////////////////////////////////
  // Public methods //
  // //////////////////////////////////////////

  // Public constructors //
  public BuildElimNeighTypesCriterion(String name) {
    super(name);
    this.setWeight(1.0);
    this.setIndifference(0.1);
    this.setPreference(0.2);
    this.setVeto(0.7);
  }

  // Getters and setters //

  // Other public methods //
  @Override
  public double value(Map<String, Object> param) {
    IBuilding building = (IBuilding) param
        .get(BuildElimNeighTypesCriterion.PARAM_BUILDING);
    double distMax = (Double) param
        .get(BuildElimNeighTypesCriterion.PARAM_DIST_MAX);
    CongestionComputation congestion = new CongestionComputation();
    congestion.calculEncombrement(building, distMax);

    double neighTypes;
    if (building.getProximitySegments().size() == 0) {
      neighTypes = 0.0;
    } else {
      neighTypes = congestion.getNbSegmentsBatiBati()
          / building.getProximitySegments().size();
    }

    return neighTypes;
  }

}
