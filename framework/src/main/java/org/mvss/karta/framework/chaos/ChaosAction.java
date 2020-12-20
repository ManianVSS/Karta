package org.mvss.karta.framework.chaos;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.randomization.ObjectWithChance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Describes a chaos action and a leaf node in the chaos configuration tree </br>
 * 
 * @see org.mvss.karta.framework.chaos.ChaosActionTreeNode
 * @see org.mvss.karta.framework.core.ChaosActionDefinition
 * @author Manian
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder( toBuilder = true )
public class ChaosAction implements Serializable, ObjectWithChance
{
   private static final long serialVersionUID = 1L;

   /**
    * The name of the chaos action.</br>
    * This is used to map to the chaos action definition.
    */
   @Builder.Default
   private String            name             = null;

   /**
    * The name of the node on which to run chaos action.</br>
    * If running chaos action locally to be set to null.</br>
    */
   @Builder.Default
   private String            node             = null;

   /**
    * The probability (>0 and <=1.0f) that this chaos action leaf node is selected for chaos actions generation with respect to parent ChaosActionTreeNode selection type.</br>
    * If parent ChaosActionTreeNode's subNodeSelectionType is ALL, then this is irrelevant.</br>
    * If parent ChaosActionTreeNode's subNodeSelectionType is CASE_TO_CASE_EVALUATION, then this chaos action will be selected based on this probability evaluated independently.</br>
    * If parent ChaosActionTreeNode's subNodeSelectionType is MUTUALLY_EXCLUSIVE, then this chaos action will be selected among others in the list mutually exclusively based on the probability.</br>
    * <b>Note</b> that if ChaosActionTreeNode's subNodeSelectionType is MUTUALLY_EXCLUSIVE, sum of all peer ChaosAction nodes' probability should be 1.0.
    */
   @Builder.Default
   private float             probability      = 1.0f;

   /**
    * The list of subject names on which to apply the chaos action.</br>
    * The subject names are subjective to the implementation of the chaos action and not relevant to the chaos engine. </br>
    * Utility method {@link org.mvss.karta.framework.randomization.RandomizationUtils#selectByChaos(java.util.Random, java.util.List, Chaos)} can be used by chaos action implementations to filter subjects</br>
    */
   @Builder.Default
   private ArrayList<String> subjects         = null;

   /**
    * The chaos amount for this chaos action.</br>
    * Refer to {@link org.mvss.karta.framework.chaos.Chaos}
    */
   @Builder.Default
   private Chaos             chaos            = Chaos.DEFAULT_CHAOS;

   /**
    * Validates the chaos action configuration
    * 
    * @return boolean
    */
   public boolean checkForValidity()
   {
      if ( StringUtils.isBlank( name ) )
      {
         return false;
      }

      if ( ( subjects == null ) || ( subjects.isEmpty() ) )
      {
         return false;
      }
      else
      {
         for ( String subject : subjects )
         {
            if ( StringUtils.isBlank( subject ) )
            {
               return false;
            }
         }
      }

      if ( !chaos.checkForValidity() )
      {
         return false;
      }

      return true;
   }
}
