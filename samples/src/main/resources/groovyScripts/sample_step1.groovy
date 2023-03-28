//file:noinspection GrPackage
package org.mvss.karta.samples.stepdefinitions.groovy

import org.mvss.karta.dependencyinjection.BeanRegistry
import org.mvss.karta.dependencyinjection.Configurator
import org.mvss.karta.dependencyinjection.annotations.KartaAutoWired
import org.mvss.karta.dependencyinjection.interfaces.DependencyInjector
import org.mvss.karta.framework.models.result.StepResult
import org.mvss.karta.framework.models.test.PreparedStep
import org.mvss.karta.framework.runtime.KartaRuntime

class SampleStepDefClass1 {

    @KartaAutoWired
    KartaRuntime kartaRuntime

    @KartaAutoWired
    DependencyInjector dependencyInjector

    @KartaAutoWired
    BeanRegistry beanRegistry

    @KartaAutoWired
    Configurator configurator

    StepResult run(PreparedStep preparedStep, Object[] params) {
        StepResult stepResult = new StepResult()
        println("Inside SampleStepDefClass1")
        println("preparedStep=" + preparedStep)
        println("params=" + params)
        println("kartaRuntime=" + kartaRuntime)
        println("dependencyInjector=" + dependencyInjector)
        println("beanRegistry=" + beanRegistry)
        println("configurator=" + configurator)
        return stepResult
    }
}
