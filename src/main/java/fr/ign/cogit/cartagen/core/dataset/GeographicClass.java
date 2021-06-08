/*******************************************************************************
 * This software is released under the licence CeCILL
 * 
 * see Licence_CeCILL-C_fr.html see Licence_CeCILL-C_en.html
 * 
 * see <a href="http://www.cecill.info/">http://www.cecill.info/a>
 * 
 * @copyright IGN
 ******************************************************************************/
package fr.ign.cogit.cartagen.core.dataset;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;

public interface GeographicClass {

  public String getName();

  public String getFeatureTypeName();

  public Class<? extends IGeometry> getGeometryType();

  /**
   * Adds a CartAGen unique identifier attribute (and value) to the geographic
   * class to allow independant persistent storing of objects related to objects
   * of the geographic class.
   */
  public void addCartAGenId();

  public static final String ID_NAME = "CARTAGENID";
}
