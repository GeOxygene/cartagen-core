/**
 * 
 */
package fr.ign.cogit.cartagen.agents.core.task;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.ign.cogit.cartagen.agents.cartacom.action.CartacomAction;
import fr.ign.cogit.cartagen.agents.cartacom.action.InternalGeneralisationAction;
import fr.ign.cogit.cartagen.agents.cartacom.agent.ConversationalObject;
import fr.ign.cogit.cartagen.agents.cartacom.agent.interfaces.ICartacomAgent;
import fr.ign.cogit.cartagen.agents.cartacom.conversation.AskToDoAdHocArgument;
import fr.ign.cogit.cartagen.agents.cartacom.conversation.ConversationState;
import fr.ign.cogit.geoxygene.contrib.agents.action.Action;
import fr.ign.cogit.geoxygene.contrib.agents.action.ActionFailureImpl;
import fr.ign.cogit.geoxygene.contrib.agents.action.ActionProposal;
import fr.ign.cogit.geoxygene.contrib.agents.action.ActionResult;
import fr.ign.cogit.geoxygene.contrib.agents.action.FailureValidity;
import fr.ign.cogit.geoxygene.contrib.agents.constraint.Constraint;
import fr.ign.cogit.geoxygene.contrib.agents.constraint.GeographicConstraint;
import fr.ign.cogit.geoxygene.contrib.agents.relation.RelationalConstraint;

/**
 * A task to try an action, that can be generated by a conversation of type
 * "request for action". The action is tried and backtracked is the result is
 * assessed as not satisfactory.
 * 
 * @author CDuchene
 * 
 */
public class TryActionTask extends ProcessingTaskWithinConvImpl {

  // //////////////////////////////////////////
  // Fields //
  // //////////////////////////////////////////

  // All static fields //

  /**
   * Logger for this class
   */
  private static Logger logger = Logger
      .getLogger(TryActionTask.class.getName());

  /*
   * Possible stages for this task
   */
  /**
   * Stage of the execution of this task: beginning
   */
  private final static String BEGINNING = "BEGINNING";
  /**
   * Stage of the execution of this task: execution of the action to try
   */
  private final static String ACTION_EXECUTION = "ACTION_EXECUTION";
  /**
   * Stage of the execution of this task: reevaluation after executing the
   * action
   */
  private final static String ACTION_REEVALUATION = "ACTION_REEVALUATION";
  /**
   * Stage of the execution of this task: commiting (or backtracking) the action
   */
  private final static String ACTION_COMMIT = "ACTION_COMMIT";
  /**
   * Stage of the execution of this task: end, if this task is an aggregated
   * task (has dependent tasks)
   */
  private final static String END_AGGREGATED = "END_AGGREGATED";
  /**
   * Stage of the execution of this task: end, if this task is part of an
   * aggregated task
   */
  private final static String END_PART_OF_AGGREGATED = "END_PART_OF_AGGREGATED";
  /**
   * Stage of the execution of this task: end, if this task is not an aggregated
   * task and is not part of an aggregated task either.
   */
  private final static String END_NOT_PART_OF_AGGREGATED = "END_NOT_PART_OF_AGGREGATED";

  /*
   * Possible results for this task
   */
  /**
   * Result = success
   */
  public final static TaskResult SUCCEEDED = new TaskResult(TryActionTask.class,
      "SUCCEEDED");
  /**
   * Result = failure
   */
  public final static TaskResult FAILED = new TaskResult(TryActionTask.class,
      "FAILED");

  // Public fields //

  // Protected fields //

  // Package visible fields //

  // Private fields with public getter //

  /**
   * The action to try
   */
  private CartacomAction actionToTry;

  /**
   * In the case where this task is part of an aggregated task, indicates if the
   * action it encapsulates was already foreseen (or at least proposed) when the
   * aggregated task was created.
   */
  private boolean triggeringPartOfAggregatedTask = false;

  // Very private fields (no public getter) //

  /**
   * Holds the action result, just to know if the agent has been modified or not
   * if the stage {@code #END_NOT_PART_OF_AGGREGATED} is executed and the task
   * result is {@code #SUCCEEDED}.
   */
  private ActionResult actionResult = ActionResult.UNCHANGED;

  // //////////////////////////////////////
  // All constructors //
  // //////////////////////////////////////

  /**
   * 
   */
  public TryActionTask() {
    super();
  }

  /**
   * Constructs a try action task from its owner and the action to encapsulate
   * 
   * @param taskOwner the conversational object owning this task
   * @param action the action to encapsulate
   */
  public TryActionTask(ICartacomAgent taskOwner, CartacomAction action) {
    this.setTaskOwner(taskOwner);
    this.setActionToTry(action);
    this.setStage(TryActionTask.BEGINNING);
  }

  // //////////////////////////////////////////
  // Static methods //
  // //////////////////////////////////////////

  // //////////////////////////////////////////////////////////
  // All getters and setters //
  // //////////////////////////////////////////////////////////

  /**
   * {@inheritDoc}
   */
  @Override
  public ICartacomAgent getTaskOwner() {
    return (ICartacomAgent) super.getTaskOwner();
  }

  /**
   * Getter for actionToTry. If no associated AggregableAction, returns null.
   * 
   * @return the actionToTry
   */
  public CartacomAction getActionToTry() {
    return this.actionToTry;
  }

  /**
   * Setter for actionToTry. If actionToTry is an instance of AggregableAction,
   * also updates the reverse reference from actionToTry to {@code this}. To
   * break the reference use {@code this.setActionToTry(null)}
   * 
   * @param actionToTry the actionToTry to set
   */
  public void setActionToTry(CartacomAction actionToTry) {
    CartacomAction oldActionToTry = this.actionToTry;
    this.actionToTry = actionToTry;
    if (oldActionToTry != null) {
      if (oldActionToTry instanceof AggregableAction) {
        ((AggregableAction) oldActionToTry).setEncapsulatingTask(null);
      } else if (oldActionToTry instanceof AggregatedAction) {
        ((AggregatedAction) oldActionToTry).setEncapsulatingTask(null);
      }
    }
    if (actionToTry != null) {
      if (actionToTry instanceof AggregableAction) {
        AggregableAction aggregableActionToTry = (AggregableAction) actionToTry;
        if (aggregableActionToTry.getEncapsulatingTask() != this) {
          aggregableActionToTry.setEncapsulatingTask(this);
        }
      } else if (actionToTry instanceof AggregatedAction) {
        AggregatedAction aggregatedActionToTry = (AggregatedAction) actionToTry;
        if (aggregatedActionToTry.getEncapsulatingTask() != this) {
          aggregatedActionToTry.setEncapsulatingTask(this);
        }
      }
    }
  }

  /**
   * Getter for triggeringPartOfAggregatedTask.
   * 
   * @return the triggeringPartOfAggregatedTask
   */
  public boolean isTriggeringPartOfAggregatedTask() {
    return this.triggeringPartOfAggregatedTask;
  }

  /**
   * Setter for triggeringPartOfAggregatedTask.
   * 
   * @param triggeringPartOfAggregatedTask the triggeringPartOfAggregatedTask to
   *          set
   */
  public void setTriggeringPartOfAggregatedTask(
      boolean triggeringPartOfAggregatedTask) {
    this.triggeringPartOfAggregatedTask = triggeringPartOfAggregatedTask;
  }

  // /////////////////////////////////////////////
  // Other public methods //
  // /////////////////////////////////////////////

  // Methods of this class
  /**
   * Looks for other TryToDoTasks, proposed actions with which to aggregate this
   * task. If some are found, an aggregated task is created. This task and the
   * tasks with which it has been aggregated are declared dependent on the new
   * aggregated task. For proposed actions aggregated with this task, a TryToDo
   * task is created and also registered as dependent on the new aggregated
   * task. A call to {@code createAdditionalTasksToAggregate} is also performed
   * on the agent owning this task, so that it can create additionnal TryToDo
   * tasks to aggregate, depending on its specific characteristics. These tasks
   * will also be registered as dependant from the new aggregated task. But
   * contrary to the other dependent tasks, their field
   * {@link #isTriggeringPartOfAggregatedTask()} is usually set to {@code false}
   * .
   * 
   * @return {@code true} if an aggregation has been performed, {@code false} if
   *         not.
   */
  public boolean tryToAggregate() {
    TryActionTask.logger
        .info("Trying to aggregate task " + this.getClass().getName()
            + " encapsulating action " + this.getActionToTry().toString()
            + " on agent " + this.getTaskOwner().toString() + ".");
    // Test if the action associated to this task is an aggregable action
    if (!(this.getActionToTry() instanceof AggregableAction)) {
      return false;
    }
    AggregableAction actionOfThisTask = (AggregableAction) this
        .getActionToTry();
    // Create a set to include all tasks to aggregate with this one
    Set<TryActionTask> tasksToAggregate = new HashSet<TryActionTask>();
    // 1. Loop on the tasks of the agent to search for TryActionTasks that would
    // be aggregable with this one
    for (Task task : this.getTaskOwner().getTasks()) {
      if (task == null) {
        continue;
      }
      // Test if the task is a TryActionTask
      if (!(task instanceof TryActionTask)) {
        continue;
      }
      TryActionTask tryActionTask = (TryActionTask) task;
      // Test if its action is aggregable with the action of this task
      if (actionOfThisTask
          .testAggregableWithAction(tryActionTask.getActionToTry()) == false) {
        continue;
      }
      // Found a task that can be aggregated with this one (as triggering part)
      tryActionTask.setTriggeringPartOfAggregatedTask(true);
      tryActionTask.setStatus(TaskStatus.WAITING);
      tasksToAggregate.add(tryActionTask);
    }

    logger.debug(
        "tasktoaggregate non specific and non action " + tasksToAggregate);
    // 2. Loop on the proposed actions of the agent to search for actions that
    // would be aggregable with the action
    // encapsulated by this task
    Iterator<ActionProposal> iterator = this.getTaskOwner().getActionProposals()
        .iterator();
    while (iterator.hasNext()) {
      ActionProposal actionProposal = iterator.next();
      Action action = actionProposal.getAction();
      // Test if the action is an AggregableAction
      if (!(action instanceof AggregableAction)) {
        continue;
      }
      CartacomAction cartacomAction = (CartacomAction) action;
      // Test if the action is aggregable with the action of this task
      if (actionOfThisTask.testAggregableWithAction(cartacomAction) == false) {
        continue;
      }
      // Found an action that can be aggregated with our task: encapsulate it in
      // a task (triggering) and adds it
      // to the tasks to aggregate
      TryActionTask tryActionTask = new TryActionTask(this.getTaskOwner(),
          cartacomAction);
      tryActionTask.setTriggeringPartOfAggregatedTask(true);
      tryActionTask.setStatus(TaskStatus.WAITING);
      tasksToAggregate.add(tryActionTask);
      // and removes the corresponding action from the proposed actions of the
      // agent
      iterator.remove();
    }

    logger.debug("tasktoaggregate non specific tasks " + tasksToAggregate);

    // 3. Add tasks specific to the agent
    tasksToAggregate.addAll(this.getTaskOwner()
        .getAdditionalTasksToAggregate(this, tasksToAggregate));

    logger.debug("tasktoaggregate specific tasks " + tasksToAggregate);

    // How many tasks to aggregate with this one? If none, return false.
    if (tasksToAggregate.size() == 0) {
      return false;
    }

    // At this stage there are tasks to aggregate
    // Adds this task to the set of tasks to aggregate
    this.setTriggeringPartOfAggregatedTask(true);
    tasksToAggregate.add(this);
    // Constructs the aggregated task
    TryActionTask aggregatedTask = new TryActionTask(this.getTaskOwner(),
        ((AggregableAction) this.getActionToTry()).getAggregatedAction());
    aggregatedTask.setDependentTasks(new HashSet<Task>(tasksToAggregate));
    // Turns the status of this task to waiting
    this.setStatus(TaskStatus.WAITING);

    return true;
  }

  // Inherited from Task

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute() {
    // Message
    if (this.getActionToTry() == null) {
      TryActionTask.logger.info("Entering Task "
          + TryActionTask.class.getSimpleName() + " on agent "
          + this.getTaskOwner().toString() + " (status : " + this.getStatus()
          + "). Action to try = null" + ", stage = " + this.getStage() + ".");
    } else {
      TryActionTask.logger.info("Entering Task "
          + TryActionTask.class.getSimpleName() + " on agent "
          + this.getTaskOwner().toString() + " (status : " + this.getStatus()
          + "). Action to try = " + this.getActionToTry().toString()
          + ", stage = " + this.getStage() + ".");
    }
    // This task is now processing...
    this.setStatus(TaskStatus.PROCESSING);
    // Loop on stages: execute current stage until the status of this task
    // becomes
    // waiting or finished
    TryActionTask.logger.info("Entering stages loop...");
    while (this.getStatus() == TaskStatus.PROCESSING) {
      TryActionTask.logger.info("In loop: stage = " + this.getStage());
      // Executes the current stage of this task
      if (this.getStage().equals(TryActionTask.BEGINNING)) {
        this.executeBeginning();
      } else if (this.getStage().equals(TryActionTask.ACTION_EXECUTION)) {
        this.executeActionExecution();
      } else if (this.getStage().equals(TryActionTask.ACTION_REEVALUATION)) {
        this.executeActionReevaluation();
      } else if (this.getStage().equals(TryActionTask.ACTION_COMMIT)) {
        this.executeActionCommit();
      } else if (this.getStage().equals(TryActionTask.END_AGGREGATED)) {
        this.executeEndAggregated();
      } else if (this.getStage().equals(TryActionTask.END_PART_OF_AGGREGATED)) {
        this.executeEndPartOfAggregated();
      } else if (this.getStage()
          .equals(TryActionTask.END_NOT_PART_OF_AGGREGATED)) {
        this.executeEndNotPartOfAggregated();
      } else {
        TryActionTask.logger.error("Unknown stage in TryAction task.");
      }
    } // (this.getStatus() == TaskStatus.PROCESSING)
    // Tidy up
    if (this.getStatus() == TaskStatus.FINISHED) {
      // Breaks the two ways reference to the encapsulated action
      this.setActionToTry(null);
      // Breaks the two ways reference to the dependent tasks
      this.setDependentTasks(new HashSet<Task>());
      // DO NOT break the two ways reference to the dependent
      // conversation:
      // This will be done later by the owner of this task, after
      // executing
      // the appropriate next transition of this conversation
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialiseBasedOnReceivedMessage(ConversationalObject convObj,
      ConversationState convState, Object receivedArgument) {
    // initialise task owner and stage
    this.setTaskOwner(convObj);
    this.setStage(TryActionTask.BEGINNING);
    // Check wether the conversation state generating the task is known
    // if (convState.getName() != "TryingRequestedAction") {
    // logger
    // .error("Unknown conversation state during TryActionTask initialisation");
    // return;
    // }
    // Here the received argument should be of type AskToDoAdHocArgument...
    AskToDoAdHocArgument askToDoArgument = (AskToDoAdHocArgument) receivedArgument;
    // Retrieves and initialise action to try
    this.setActionToTry((CartacomAction) askToDoArgument.getAction());
  }

  // Inherited from ProcessingTaskWithinConv

  // //////////////////////////////////////////
  // Protected methods //
  // //////////////////////////////////////////

  // //////////////////////////////////////////
  // Package visible methods //
  // //////////////////////////////////////////

  // ////////////////////////////////////////
  // Private methods //
  // ////////////////////////////////////////

  /**
   * Executes the beginning stage of this task
   */
  private void executeBeginning() {
    TryActionTask.logger.info("beginning...");
    // If aggregated task, directly go to execution stage
    if (!this.getDependentTasks().isEmpty()) {
      this.setStage(TryActionTask.ACTION_EXECUTION);
      return;
    }
    // Same thing if the encapsulated action is not trying to solve a
    // relational constraint (but an internal constraint)
    if (!(this.getActionToTry()
        .getConstraint() instanceof RelationalConstraint)) {
      this.setStage(TryActionTask.ACTION_EXECUTION);
      return;
    }
    // Now the constraint to satisfy is a relational constraint
    RelationalConstraint constraint = (RelationalConstraint) this
        .getActionToTry().getConstraint();
    // We have to check if it is still wirth triggering the action: the
    // situation could
    // have changed since the agent has generated this task
    // If this constraint is completely satisfied, consider the result is a
    // succes and
    // go to ending stage
    // TODO Rendre generique la gestion des max de statisfaction, avec une
    // methode
    // getMaxSatisfaction sur l'interface constraint.
    if (constraint.getSatisfaction() == 5.0) {
      TryActionTask.logger.info("satisfaction of constraint to solve ("
          + this.getActionToTry().getConstraint().toString()
          + ") already 5 => task considered successfull");
      this.setResult(TryActionTask.SUCCEEDED);
      this.setStage(TryActionTask.END_NOT_PART_OF_AGGREGATED);
      return;
    }
    // If the action to try can be found in the failures list of the agent,
    // consider the result is a failure and go to ending stage
    if (this.getTaskOwner().isActionInFailuresList(this.getActionToTry())) {
      TryActionTask.logger.info(
          "action to try in failures list => task considered failed (contraint to solve was "
              + this.getActionToTry().getConstraint().toString() + ").");
      this.setResult(TryActionTask.FAILED);
      this.setStage(TryActionTask.END_NOT_PART_OF_AGGREGATED);
      return;
    }
    // Try to aggregate this task with others. If it succeeds, sets its
    // stage
    // (the one that will be executed when the task resumes) to the ending
    // stage
    // The status of this stage will have been turned to WAITING
    if (this.tryToAggregate()) {
      TryActionTask.logger.info("Try to Aggregate True.");
      this.setStage(TryActionTask.END_PART_OF_AGGREGATED);
      this.setStatus(TaskStatus.WAITING);
      return;
    }
    // This task has not been aggregated with other ones, and its action
    // has to be tried: go to the execution stage
    this.setStage(TryActionTask.ACTION_EXECUTION);
    return;
  }

  /**
   * Executes the action execution stage of this task
   */
  private void executeActionExecution() {
    TryActionTask.logger.info("action execution...");
    // Execute the action
    try {
      this.actionResult = this.getActionToTry().compute();
    } catch (InterruptedException e) {
      // The thread has been interrupted during the action. We do not manage
      // this (it should not happen). In such a case consider the result is
      // unknown, as we do not know when the interruption happened
      this.actionResult = ActionResult.UNKNOWN;
      TryActionTask.logger.warn("Action " + this.getActionToTry().toString()
          + " interrupted (thread interruption??) in "
          + this.getClass().getSimpleName()
          + ". Action result considered unknown");
      e.printStackTrace();
    }
    logger.debug("this.actionResult = " + this.actionResult);
    // Goes to the right next stage, depending on the result of the action
    // Agent modified: build new state for the agent and go to reevaluation
    // stage
    if ((this.actionResult == ActionResult.MODIFIED)
        || (this.actionResult == ActionResult.UNKNOWN)) {
      // Update the agent's internal representation of its spatial environment
      this.getTaskOwner().updateEnvironmentRepresentation();
      // Update the agent's satisfaction - this updates the current value and
      // satisfaction of its constraints, and stores the old satisfactions
      double oldSatisfaction = 0.0;
      if (this.getActionToTry().getConstraint() != null)
        oldSatisfaction = this.getActionToTry().getConstraint()
            .getSatisfaction();
      Map<Constraint, Double> otherOldSatisfactions = new HashMap<>();
      for (Constraint constraint : this.getTaskOwner().getConstraints())
        otherOldSatisfactions.put(constraint,
            ((GeographicConstraint) constraint).getSatisfaction());
      this.getTaskOwner().computeSatisfaction();
      // Updates the agent's possible actions - this updates the priorities
      // of its constraints
      this.getTaskOwner().updateActionProposals();
      if (this.actionToTry instanceof InternalGeneralisationAction) {
        this.setResult(TryActionTask.SUCCEEDED);
        logger.info("Internal generalisation always succeed.");
      } else {
        // here, this is a CartACom action that has been triggered. The success
        // ha
        // sto be evaluated regarding the constraint that proposed the action.
        boolean improvement = false;
        boolean noDamage = true;
        if (this.getActionToTry().getConstraint()
            .getSatisfaction() > oldSatisfaction
            || this.getActionToTry().getConstraint().getSatisfaction() == 5.0)
          improvement = true;
        for (Constraint constraint : this.getTaskOwner().getConstraints()) {
          double old = otherOldSatisfactions.get(constraint);
          if (((GeographicConstraint) constraint).getSatisfaction() < old - 1) {
            noDamage = false;
            break;
          }
        }
        if (improvement && noDamage) {
          this.setResult(TryActionTask.SUCCEEDED);
          logger.info("Satisfaction == 5 : SUCCEEDED");
        } else {
          this.setResult(TryActionTask.FAILED);
          logger.info("Satisfaction < 5 : FAILED");
        }
      }
      // Build a new state for the agent
      this.getTaskOwner().buildCurrentState(
          this.getTaskOwner().getCurrentState(), this.getActionToTry());
      this.setStage(TryActionTask.ACTION_REEVALUATION);
      return;
    }
    // Agent unchanged: the action is considered as having failed
    // Go to not aggregated or aggregated ending stage depending if this
    // task has dependent tasks or not
    else if (this.actionResult == ActionResult.UNCHANGED) {
      this.setResult(TryActionTask.FAILED);
      if (this.getDependentTasks().isEmpty()) {
        this.setStage(TryActionTask.END_NOT_PART_OF_AGGREGATED);
      } else {
        this.setStage(TryActionTask.END_AGGREGATED);
      }
      return;
    }
    // Agent eliminated: the task is finished
    else if (this.actionResult == ActionResult.ELIMINATED) {
      this.getTaskOwner().cleanActionsToTry();
      // TODO Faire un clean et créer un nouvel état - voir lifecycle Julien
      // pour savoir comment faire.
      this.setResult(TryActionTask.SUCCEEDED);
      this.setStatus(TaskStatus.FINISHED);
      return;
    }
    // If null has been returned: error.
    else {
      TryActionTask.logger.error("Returned action result is probably null in "
          + this.getClass().getSimpleName() + "for action "
          + this.getActionToTry().toString());
      return;
    } // if (this.actionResult == ...) else...
  }

  /**
   * Executes the action reevaluation stage of this task. When this method is
   * executed it means that the action to try has been executed, and its result
   * is a modification of the agent. At the end of this stage, the definitve
   * result of this task is known. If it is a failure, the agent has backtracked
   * to its previous state
   */
  private void executeActionReevaluation() {
    TryActionTask.logger.info("action reevaluation...");
    // TODO At this moment, consider always successfull (assuming no
    // modification
    // would have been performed if the constraints were not better
    // satisfied).
    // This is normally true for the displacement actions. The real
    // reevaluation
    // should be written later based on the lull code.
    // this.setResult(TryActionTask.SUCCEEDED);
    this.setStage(TryActionTask.ACTION_COMMIT);
  }

  /**
   * Commits or backtrack the action, depending on the result of the evaluation
   * stage.
   */
  private void executeActionCommit() {
    TryActionTask.logger.info("action commit...");
    // TODO Complete with the management of diffusion when the evaluation
    // stage will include it (backtack diffused agents or inform them and
    // their neighbours that they have been diffused, depending if the
    // evaluation result is negative or positive).
    // Evaluation not passed: backtrack the agent
    // if (this.getResult() == TryActionTask.FAILED) {
    // this.getTaskOwner().goBackToState(
    // this.getTaskOwner().getCurrentState().getPreviousState());
    // return;
    // }
    // If evaluation passed, nothing special to do.
    // If result not set, error message
    // else
    if (this.getResult() == null) {
      TryActionTask.logger
          .error("Reached validation stage and task result is null");
    }
    // Go to the right ending stage
    if (this.getDependentTasks().isEmpty()) {
      this.setStage(TryActionTask.END_NOT_PART_OF_AGGREGATED);
    } else {
      this.setStage(TryActionTask.END_AGGREGATED);
    }
  }

  /**
   * Executes the ending stage of this task, if it an aggregated task (it has
   * dependent tasks). If the task result is SUCCEEDED, the agent has been
   * modified. This is acknowledged on the agent by triggering it method
   * {@link ICartacomAgent#manageHavingJustBeenModifiedByATask()
   * manageHavingJustBeenModifiedByATask()}. Whatever the result, this result is
   * also reproduced on the tasks that are dependent on this task to enable them
   * to know how to finish when they resume.
   */
  private void executeEndAggregated() {
    TryActionTask.logger.info("action endAggregated...");
    // If agent modified, acknowledge it.
    if (this.getResult() == TryActionTask.SUCCEEDED) {
      this.getTaskOwner().manageHavingJustBeenModifiedByATask(
          actionToTry.modifieEnvironmentRepresentation());
    }
    // Copy the result of this task on the dependent tasks. For those having
    // a dependent conversation, instantiate their argumentToSend
    // TODO LOOKS STRANGE, TO CHECK ... The actual result of the dependent
    // tasks is
    // computed by them when they resume. This is how it is coded in lull.
    // But
    // their result could also be computed here, basing, for each task, on
    // if the handled
    // constraint has improved or not (at least for the tasks that are
    // "asking parts"
    // of this task).
    Set<Task> dependentTryActionTasks = new HashSet<Task>(
        this.getDependentTasks());
    for (Task task : dependentTryActionTasks) {
      // all dependent tasks should be of type TryActionTask...
      TryActionTask tryTask = (TryActionTask) task;
      // Compute the result of the dependent task
      // Basically: if the
      // Set the computed result on the dependent task
      tryTask.setResult(this.getResult());
      // if the dependent task is dependent of a conversation, set the
      // next argument to send in this conversation
      if (tryTask.getDependentConversation() != null) {
        tryTask.setArgumentToSend(
            this.getActionToTry().computeDescribingArgument());
      }
      // Set the Waiting status to resumable
      task.setStatus(TaskStatus.RESUMABLE);
    }
    // This task is now finished
    this.setStatus(TaskStatus.FINISHED);
    return;
  }

  /**
   * Executes the ending stage of this task, if it is part of an aggregated
   * task. If this method is executed it means that the aggregated task on which
   * this task was dependent is finished. The result of the aggregated task has
   * been reproduced on this task, but the actual result of this task can be
   * different (depending if the constraint handled by this task could be
   * satisfied by the aggregated task or not). NB: The reference to the
   * aggregated task has been broken at the end of its execution, so it is
   * normal that {@code #getAggregatedTask()} now returns {@code null}.
   */
  private void executeEndPartOfAggregated() {
    TryActionTask.logger.info("action EndPartOfAggregated...");
    TaskResult aggregatedResult = this.getResult();
    // Case where the aggregated task consideres having failed.
    if (aggregatedResult == TryActionTask.FAILED) {
      // If this task was a triggering part of the aggregated task, its
      // result
      // is also a failure
      if (this.isTriggeringPartOfAggregatedTask()) {
        // does not modify the result - it is already set to FAILED
        TryActionTask.logger.info(
            "Aggregated result = FAILED and triggering task => Result = FAILED");
      }
      // If this task is not a triggering part of the action to try, its
      // result
      // can still be a success if the aggregated task has handled its
      // constraint
      // so that its satisfaction has reached (or stayed to) the maximum
      // value
      // TODO manage the maximum constraint satisfaction on a generic way
      else if (this.getActionToTry().getConstraint().getSatisfaction() < 5.0) {
        // does not modify the result - it is already set to FAILED
        TryActionTask.logger.info(
            "Aggregated result = FAILED and constraint satisfaction not maximum => Result = FAILED");
      }
      // Now the satisfaction is maximum => success
      else {
        this.setResult(TryActionTask.SUCCEEDED);
        TryActionTask.logger.info(
            "Aggregated result = FAILED but constraint satisfaction maximum => Result = SUCCEEDED");
      }
    } // if (aggregatedResult == FAILED)

    // Case where the aggregated task has succeeded i.e. the global
    // situation has been
    // improved. But we still have to check if the satisfaction of the
    // constraint
    // handled by the action encapsulated in this task has reached the
    // maximum,
    // in order to determine if this task has encountered a failure or a
    // success.
    else {
      if (this.getActionToTry().getConstraint().getSatisfaction() < 5.0) {
        this.setResult(TryActionTask.FAILED);
        // does not modify the result - it is already set to FAILED
        TryActionTask.logger.info(
            "Aggregated result = SUCCEEDED but constraint satisfaction not maximum => Result = FAILED");
      }
      // Case where the satisfaction is maximum => success
      else {
        this.setResult(TryActionTask.SUCCEEDED);
        TryActionTask.logger.info(
            "Aggregated result = SUCCEEDED and constraint satisfaction maximum => Result = SUCCEEDED");
      }
    } // if (aggregatedResult == FAILED)... else

    // If the result is a failure, this failure must be registered
    // on the task owner, unless the action has no constraint to handle
    if ((this.getResult() == TryActionTask.FAILED)
        && (this.getActionToTry().getConstraint() != null)
        && !this.getTaskOwner().isActionInFailuresList(this.getActionToTry())) {
      FailureValidity failureValidity = this.getActionToTry()
          .computeFailureValidity();
      this.getTaskOwner().getFailures()
          .add(new ActionFailureImpl(this.getActionToTry(), failureValidity,
              (this.getDependentConversation() != null)));
    }

    // Updates the status of this task
    this.setStatus(TaskStatus.FINISHED);
  }

  /**
   * Executes the ending stage of this task, if it has no dependent neither
   * aggregated tasks. If the result of this task is {@code #SUCCEEDED}, checks
   * if the agent has been modified during this task (it could also have reached
   * this tage just because the constraint to handle was already satisfied, see
   * {@code #BEGINNING} stage), and if yes ackowledges it. If the result of this
   * task is {@code #FAILED}, registers the failure (unless the encapsulated
   * action has no handled constraint). In any case, if this task has a
   * dependent conversation the argument to send during the next message of this
   * conversation is instantiated.
   */
  private void executeEndNotPartOfAggregated() {
    TryActionTask.logger.info("action EndNotPartOfAggregated");
    // Successfull result and agent actually modified

    logger.debug("this.getResult() = " + this.getResult());
    logger.debug("this.actionResult = " + this.actionResult);
    if ((this.getResult() == TryActionTask.SUCCEEDED)
        && (this.actionResult == ActionResult.MODIFIED)) {
      logger.debug("manageHavingJustBeenModifiedByATask");
      this.getTaskOwner().manageHavingJustBeenModifiedByATask(
          actionToTry.modifieEnvironmentRepresentation());
    }
    // Failing result and the action had a constraint to handle
    else if ((this.getResult() == TryActionTask.FAILED)
        && (this.getActionToTry().getConstraint() != null)
        && !this.getTaskOwner().isActionInFailuresList(this.getActionToTry())) {
      FailureValidity failureValidity = this.getActionToTry()
          .computeFailureValidity();
      this.getTaskOwner().getFailures()
          .add(new ActionFailureImpl(this.getActionToTry(), failureValidity,
              (this.getDependentConversation() != null)));
    }
    // Now if there is a dependent conversation, retrieves the next argument
    // to send.
    if (this.getDependentConversation() != null) {
      this.setArgumentToSend(this.getActionToTry().computeDescribingArgument());
    }

    this.setStatus(TaskStatus.FINISHED);
  }

}
