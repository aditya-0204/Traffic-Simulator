package app.urbanflo.urbanflosumoserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class urbanfloSumoServerApplication

fun main(args: Array<String>) {
    System.loadLibrary("libtracijni")
    runApplication<urbanfloSumoServerApplication>(*args)
}
