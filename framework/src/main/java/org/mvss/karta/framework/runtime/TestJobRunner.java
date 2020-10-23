package org.mvss.karta.framework.runtime;

import java.util.ArrayList;

import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestJobRunner
{
   private KartaRuntime              kartaRuntime;
   private StepRunner                stepRunner;
   private ArrayList<TestDataSource> testDataSources;
   private TestJob                   job;
}
