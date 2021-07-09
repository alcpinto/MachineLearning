package alp.plotly.model

import plotly.element.ScatterMode

final case class PlotData(xCol: String, 
                          yCol: String,
                          name: Option[String] = None,
                          mode: Option[ScatterMode] = Some(ScatterMode(ScatterMode.Markers)))