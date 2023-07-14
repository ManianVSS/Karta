//file:noinspection GrPackage
package org.mvss.karta.samples.stepdefinitions.groovy

import org.mvss.karta.dependencyinjection.BeanRegistry
import org.mvss.karta.dependencyinjection.KartaDependencyInjector
import org.mvss.karta.dependencyinjection.TestProperties
import org.mvss.karta.dependencyinjection.annotations.KartaAutoWired
import org.mvss.karta.framework.models.result.StepResult
import org.mvss.karta.framework.models.test.PreparedStep
import org.mvss.karta.framework.runtime.KartaRuntime

class SampleStepDefClass1 {

    @KartaAutoWired
    KartaRuntime kartaRuntime

    @KartaAutoWired
    KartaDependencyInjector kartaDependencyInjector

    @KartaAutoWired
    BeanRegistry beanRegistry

    @KartaAutoWired
    TestProperties testProperties

    StepResult run(PreparedStep preparedStep, Object[] params) {
        StepResult stepResult = new StepResult()
        println("Inside SampleStepDefClass1")
        println("preparedStep=" + preparedStep)
        println("params=" + params)
        println("kartaRuntime=" + kartaRuntime)
        println("KartaDependencyInjector=" + kartaDependencyInjector)
        println("beanRegistry=" + beanRegistry)
        println("testProperties=" + testProperties)
        return stepResult
    }
}
