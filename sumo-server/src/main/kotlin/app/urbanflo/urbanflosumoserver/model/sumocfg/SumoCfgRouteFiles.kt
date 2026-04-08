package app.urbanflo.urbanflosumoserver.model.sumocfg

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SumoCfgRouteFiles(
    @field:JacksonXmlProperty(isAttribute = true)
    val value: String
)
