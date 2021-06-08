package fr.ign.cogit.cartagen.agents.diogen.interaction.point;

import java.lang.reflect.Method;
import java.util.Set;

import fr.ign.cogit.cartagen.agents.diogen.agent.model.IDiogenAgent;
import fr.ign.cogit.cartagen.agents.diogen.interaction.aggregation.AggregableInteraction;
import fr.ign.cogit.cartagen.agents.diogen.interactionmodel.constrained.ConstrainedMultipleTargetsInteraction;
import fr.ign.cogit.cartagen.agents.diogen.interactionmodel.constrained.ConstrainedSingleTargetInteraction;
import fr.ign.cogit.cartagen.agents.diogen.interactionmodel.constrained.ConstraintType;
import fr.ign.cogit.cartagen.agents.diogen.padawan.Environment;
import fr.ign.cogit.geoxygene.contrib.agents.constraint.GeographicConstraint;

public class PointDisplacementAggregableInteraction extends
    ConstrainedSingleTargetInteraction implements AggregableInteraction {

  /**
   * The singleton object for the unique instance of the class.
   */
  private static PointDisplacementAggregableInteraction singletonObject;

  /** A private Constructor prevents any other class from instantiating. */
  private PointDisplacementAggregableInteraction() {
    this.addConstraintTypeName(getConstraintType(
        "fr.ign.cogit.cartagen.agentGeneralisation.gael.gaelDeformation.constraint.simple.angle.Value"));
    this.addConstraintTypeName(getConstraintType(
        "fr.ign.cogit.cartagen.agentGeneralisation.gael.gaelDeformation.constraint.simple.segment.Length"));
    this.addConstraintTypeName(getConstraintType(
        "fr.ign.cogit.cartagen.agentGeneralisation.gael.gaelDeformation.constraint.simple.segment.Orientation"));
    this.addConstraintTypeName(getConstraintType(
        "fr.ign.cogit.cartagen.agentGeneralisation.gael.gaelDeformation.constraint.simple.singletonPoint.Position"));
    this.addConstraintTypeName(getConstraintType(
        "fr.ign.cogit.cartagen.agentGeneralisation.padawan.constraint.points.PointNotUnderRoad"));
    this.setName("Point Displacement with target");
    this.setWeight(2);

  }

  private ConstraintType getConstraintType(String s) {
    ConstraintType constraintTypeObject = new ConstraintType(s, 1, 0);
    Method method = null;
    try {
      method = this.getClass().getMethod("isUnsatisfied", signature2);
    } catch (SecurityException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    System.out.println("method " + method);
    // this.addConstraintTypeName(constraintTypeObject);
    constraintTypeObject.addInfluence(this.getValueFromInfluence("favorable"),
        method, "");

    Method method2 = null;
    try {
      method2 = this.getClass().getMethod("isNotSameTarget", signature2);
    } catch (SecurityException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    System.out.println("method2 " + method2);
    constraintTypeObject.addInfluence(this.getValueFromInfluence("indifferent"),
        method2, "");

    return constraintTypeObject;
  }

  /**
   * Get the unique instance.
   * @return
   */
  public static synchronized PointDisplacementAggregableInteraction getInstance() {
    if (singletonObject == null) {
      singletonObject = new PointDisplacementAggregableInteraction();
    }
    return singletonObject;
  }

  @Override
  public boolean testAggregableWithInteraction(
      AggregableInteraction aggregableInteraction) {
    return (aggregableInteraction == this
        || aggregableInteraction == PointAutoDisplacementInteraction
            .getInstance());
  }

  @Override
  public ConstrainedMultipleTargetsInteraction getAggregatedInteraction() {
    return PointDisplacementInteraction.getInstance();
  }

  @Override
  public void perform(Environment environment, IDiogenAgent source,
      IDiogenAgent target, Set<GeographicConstraint> constraints)
      throws InterruptedException, ClassNotFoundException {
    // TODO Auto-generated method stub

  }

}
