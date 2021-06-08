package fr.ign.cogit.cartagen.osm.schema.aero;

import fr.ign.cogit.cartagen.core.genericschema.airport.IAirportArea;
import fr.ign.cogit.cartagen.core.genericschema.airport.IRunwayLine;
import fr.ign.cogit.cartagen.osm.schema.OsmGeneObjLin;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;

public class OsmRunwayLine extends OsmGeneObjLin implements IRunwayLine {

  private IAirportArea airport;
  private int z;

  public OsmRunwayLine(ILineString geom) {
    super(geom);
  }

  @Override
  public IAirportArea getAirport() {
    return airport;
  }

  @Override
  public int getZ() {
    return z;
  }

  public void setZ(int z) {
    this.z = z;
  }

  @Override
  public void setAirport(IAirportArea airport) {
    this.airport = airport;
  }

}
