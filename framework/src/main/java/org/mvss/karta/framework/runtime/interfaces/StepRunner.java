package org.mvss.karta.framework.runtime.interfaces;

import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.runtime.TestFailureException;

public interface StepRunner extends Plugin
{
   StepResult runStep( PreparedStep testStep ) throws TestFailureException;

   String sanitizeStepIdentifier( String stepDefinition );

   StepResult performChaosAction( PreparedChaosAction chaosAction ) throws TestFailureException;
}
