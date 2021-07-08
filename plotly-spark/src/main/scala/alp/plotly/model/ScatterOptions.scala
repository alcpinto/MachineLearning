package alp.plotly.model

import plotly.element.ScatterMode
import plotly.layout.Axis

final case class ScatterOptions(data: Seq[PlotData],
                                filePath: String,
                                title: Option[String] = None,
                                xAxis: Option[Axis] = None,
                                yAxis: Option[Axis] = None,
                                overwrite: Boolean = true)


