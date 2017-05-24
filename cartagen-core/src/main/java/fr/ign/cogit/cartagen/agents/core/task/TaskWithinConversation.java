/**
 * 
 */
package fr.ign.cogit.cartagen.agents.core.task;

import fr.ign.cogit.cartagen.agents.cartacom.conversation.OnGoingConversation;

/**
 * A {@link Task} that can be generated by a conversation, therefore it has a
 * reference to this conversation. Tasks that can be generated by a conversation
 * should implement this interface, or more precisely on of the two interfaces
 * that extend it:
 * <ul>
 * <li>ProcessingTaskWithinConv for tasks linked to a state of the conversation
 * where the conversational object is computing something before answering to
 * its partner</li>
 * <li>EndOfConvTask for tasks associated to a final state of the conversation
 * where the conversational object has to acknowledge a situation.</li>
 * @author CDuchene
 */
public interface TaskWithinConversation extends Task {

  /**
   * Returns the conversation that has generated this task and is therefore
   * dependent on this task, if any.
   * @return the dependent conversation, or null if none.
   */
  public OnGoingConversation getDependentConversation();

  /**
   * Sets the conversation that has generated this task and is therefore
   * dependent on this task.
   * @param dependentConversation the dependent conversaiton to set.
   */
  public void setDependentConversation(
      OnGoingConversation dependentConversation);
}
