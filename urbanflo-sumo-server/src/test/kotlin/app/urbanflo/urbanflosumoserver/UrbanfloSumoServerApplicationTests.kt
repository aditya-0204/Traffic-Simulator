package app.urbanflo.urbanflosumoserver

import app.urbanflo.urbanflosumoserver.controller.SimulationController
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class urbanfloSumoServerApplicationTests(
    @Autowired private val simulationController: SimulationController,
) {

    @Test
    fun contextLoads() {
        assertThat(simulationController).isNotNull
    }
}
