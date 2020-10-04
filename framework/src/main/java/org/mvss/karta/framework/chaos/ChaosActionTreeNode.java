package org.mvss.karta.framework.chaos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import org.mvss.karta.framework.randomization.ObjectWithChance;
import org.mvss.karta.framework.randomization.RandomizationUtils;
import org.mvss.karta.framework.utils.DataUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChaosActionTreeNode implements Serializable, ObjectWithChance
{
   /**
    * 
    */
   private static final long              serialVersionUID        = 1L;

   @Builder.Default
   private float                          probabilityOfOccurrence = 100;

   @Builder.Default
   private SubNodeSelectionType           subNodeSelectionType    = SubNodeSelectionType.MutuallyExclusive;

   private ArrayList<ChaosActionTreeNode> chaosActionSubNodes;

   private ArrayList<ChaosAction>         chaosActions;

   public boolean checkForValidity()
   {
      boolean isLeafOrHasSubNodes = false;

      if ( ( chaosActionSubNodes != null ) && !chaosActionSubNodes.isEmpty() )
      {
         isLeafOrHasSubNodes = true;

         if ( subNodeSelectionType != SubNodeSelectionType.All )
         {
            float probabilityNotCovered = RandomizationUtils.getMissingProbabilityCoverage( chaosActionSubNodes, 100, true );

            if ( ( subNodeSelectionType == SubNodeSelectionType.MutuallyExclusive ) && ( probabilityNotCovered != 0 ) )
            {
               return false;
            }
         }
      }
      if ( ( chaosActions != null ) || !chaosActions.isEmpty() )
      {
         isLeafOrHasSubNodes = true;

         float probabilityNotCovered = 100;
         for ( ChaosAction chaosAction : chaosActions )
         {
            if ( !chaosAction.checkForValidity() )
            {
               return false;
            }

            float probabilityOfOccurrence = chaosAction.getProbabilityOfOccurrence();

            if ( ( probabilityOfOccurrence == 0 ) || !DataUtils.inRange( probabilityOfOccurrence, 0, 100 ) )
            {
               return false;
            }

            probabilityNotCovered -= chaosAction.getProbabilityOfOccurrence();
         }

         if ( subNodeSelectionType != SubNodeSelectionType.All )
         {
            if ( ( subNodeSelectionType == SubNodeSelectionType.MutuallyExclusive ) && ( probabilityNotCovered != 0 ) )
            {
               return false;
            }
         }
      }

      return isLeafOrHasSubNodes;
   }

   public ArrayList<ChaosAction> nextChaosActions( Random random )
   {
      ArrayList<ChaosAction> selectedActions = new ArrayList<ChaosAction>();

      if ( ( chaosActions != null ) && !chaosActions.isEmpty() )
      {
         switch ( subNodeSelectionType )
         {
            case All:
               selectedActions.addAll( chaosActions );
               break;
            case CaseToCaseEvaluation:
               selectedActions.addAll( RandomizationUtils.generateNextComposition( random, chaosActions ) );
               break;
            case MutuallyExclusive:
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
            case All:
               selectedSubNodes = chaosActionSubNodes;
               break;
            case CaseToCaseEvaluation:
               selectedSubNodes = RandomizationUtils.generateNextComposition( random, chaosActionSubNodes );
               break;
            case MutuallyExclusive:
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
