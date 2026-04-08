package app.urbanflo.urbanflosumoserver.model.sumocfg

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty


data class SumoCfgInput(
    @field:JacksonXmlProperty(localName = "net-file")
    val netFile: SumoCfgNetFile,
    @field:JacksonXmlProperty(localName = "route-files")
    val routeFiles: SumoCfgRouteFiles
)