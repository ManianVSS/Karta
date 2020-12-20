package org.mvss.karta.framework.chaos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import org.mvss.karta.framework.randomization.ObjectWithChance;
import org.mvss.karta.framework.randomization.RandomizationUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class describes the chaos configuration to randomly select chaos actions based on probability and exclusivity.
 * The tree structure's leaf nodes defines only chaosActions and non leaf nodes should define chaosActionSubNodes and optionally define chaosActions as well.
 * This tree structure enables grouping chaos actions subgroups with rules of mutual exclusivity or individual probability of occurrence.
 * The next set of chaos actions selected based on this configuration can be obtained by calling {@link #nextChaosActions}.
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChaosActionTreeNode implements Serializable, ObjectWithChance
{
   /**
    * 
    */
   private static final long              serialVersionUID     = 1L;

   /**
    * The probability (>0 and <=1.0f) that this chaos action sub tree node is selected for chaos actions generation with respect to parent ChaosActionTreeNode selection type.</br>
    * If parent ChaosActionTreeNode's subNodeSelectionType is ALL, then this is irrelevant.</br>
    * If parent ChaosActionTreeNode's subNodeSelectionType is CASE_TO_CASE_EVALUATION, then this chaos action tree sub node will be selected based on this probability evaluated independently.</br>
    * If parent ChaosActionTreeNode's subNodeSelectionType is MUTUALLY_EXCLUSIVE, then this chaos action tree sub node will be selected among others in the list mutually exclusively based on the probability.</br>
    * <b>Note</b> that if ChaosActionTreeNode's subNodeSelectionType is MUTUALLY_EXCLUSIVE, sum of all peer ChaosActionTreeNode nodes' probability should be 1.0.
    */
   @Builder.Default
   private float                          probability          = 1.0f;

   /**
    * The sub node selection criteria to select chaos actions and sub trees for further evaluation when this tree node is evaluated using {@link #nextChaosActions(Random)}.
    */
   @Builder.Default
   private SubNodeSelectionType           subNodeSelectionType = SubNodeSelectionType.MUTUALLY_EXCLUSIVE;

   /**
    * The list of sub nodes to be selected for further evaluation based on {@link #subNodeSelectionType}.
    */
   @Builder.Default
   private ArrayList<ChaosActionTreeNode> chaosActionSubNodes  = null;

   /**
    * The list of chaos actions to be selected for further evaluation based on {@link #subNodeSelectionType}.
    */
   @Builder.Default
   private ArrayList<ChaosAction>         chaosActions         = null;

   /**
    * Validates the chaos configuration tree. Recursively calls validate on sub nodes and chaos actions.
    * 
    * @return boolean
    */
   public boolean checkForValidity()
   {
      boolean isLeafOrHasSubNodes = false;

      if ( ( chaosActionSubNodes != null ) && !chaosActionSubNodes.isEmpty() )
      {
         isLeafOrHasSubNodes = true;

         for ( ChaosActionTreeNode chaosActionSubNode : chaosActionSubNodes )
         {
            if ( !chaosActionSubNode.checkForValidity() )
            {
               return false;
            }
         }

         if ( subNodeSelectionType != SubNodeSelectionType.ALL )
         {
            float probabilityNotCovered = RandomizationUtils.getMissingProbabilityCoverage( chaosActionSubNodes, 1.0f, true );

            if ( ( subNodeSelectionType == SubNodeSelectionType.MUTUALLY_EXCLUSIVE ) && ( probabilityNotCovered != 0 ) )
            {
               return false;
            }
         }
      }
      if ( ( chaosActions != null ) || !chaosActions.isEmpty() )
      {
         isLeafOrHasSubNodes = true;

         for ( ChaosAction chaosAction : chaosActions )
         {
            if ( !chaosAction.checkForValidity() )
            {
               return false;
            }
         }

         float probabilityNotCovered = RandomizationUtils.getMissingProbabilityCoverage( chaosActions, 1.0f, true );
         if ( subNodeSelectionType != SubNodeSelectionType.ALL )
         {
            if ( ( subNodeSelectionType == SubNodeSelectionType.MUTUALLY_EXCLUSIVE ) && ( probabilityNotCovered != 0 ) )
            {
               return false;
            }
         }
      }

      return isLeafOrHasSubNodes;
   }

   /**
    * This method randomly selects the next select of chaos actions to be performed based on {@link #subNodeSelectionType}.
    * Accumulates ChoasActions selected by the selected sub nodes by recursively calling {@link ChaosActionTreeNode#nextChaosActions(Random)}.
    * Chaos actions for this tree node are added to the selection based on {@link #subNodeSelectionType} (even if there are sub nodes).
    * 
    * @param random
    * @return ArrayList<{@link ChaosAction}>
    */
   public ArrayList<ChaosAction> nextChaosActions( Random random )
   {
      ArrayList<ChaosAction> selectedActions = new ArrayList<ChaosAction>();

      if ( ( chaosActions != null ) && !chaosActions.isEmpty() )
      {
         switch ( subNodeSelectionType )
         {
            case ALL:
               selectedActions.addAll( chaosActions );
               break;
            case CASE_TO_CASE_EVALUATION:
               selectedActions.addAll( RandomizationUtils.generateNextComposition( random, chaosActions ) );
               break;
            case MUTUALLY_EXCLUSIVE:
               selectedActions.add( RandomizationUtils.generateNextMutexComposition( random, chaosActions ) );
               break;
            default:
               return null;
         }
      }

      if ( ( chaosActionSubNodes != null ) && !chaosActionSubNodes.isEmpty() )
      {
         ArrayList<ChaosActionTreeNode> selectedSubNodes;

         switch ( subNodeSelectionType )
         {
            case ALL:
               selectedSubNodes = chaosActionSubNodes;
               break;
            case CASE_TO_CASE_EVALUATION:
               selectedSubNodes = RandomizationUtils.generateNextComposition( random, chaosActionSubNodes );
               break;
            case MUTUALLY_EXCLUSIVE:
               selectedSubNodes = new ArrayList<ChaosActionTreeNode>();
               selectedSubNodes.add( RandomizationUtils.generateNextMutexComposition( random, chaosActionSubNodes ) );
               break;
            default:
               return null;
         }

         for ( ChaosActionTreeNode selectedSubNode : selectedSubNodes )
         {
            selectedActions.addAll( selectedSubNode.nextChaosActions( random ) );
         }
      }

      return selectedActions;
   }
}
