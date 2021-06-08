/*******************************************************************************
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL-C_fr.html see Licence_CeCILL-C_en.html
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info/a>
 * 
 * @copyright IGN
 ******************************************************************************/
package fr.ign.cogit.cartagen.core.genericschema.hydro;

import fr.ign.cogit.cartagen.core.genericschema.IGeneObjSurf;

/*
 * ###### IGN / CartAGen ###### Title: WaterArea Description: Surfaces d'eau
 * Author: J. Renard Date: 16/09/2009
 */

public interface IWaterArea extends IGeneObjSurf {
  public static final String FEAT_TYPE_NAME = "WaterArea"; //$NON-NLS-1$

  public enum WaterAreaNature {
    LAKE, RIVER, UNKNOWN
  }

  public WaterAreaNature getNature();

  /**
   * Get the type as a String value for symbolisation purposes
   * @return
   */
  public String getTypeSymbol();

  /**
   * The name of the lake or river.
   * @return
   */
  public String getName();
}
