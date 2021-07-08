package alp.plotly.model

import plotly.element.ScatterMode

final case class PlotData(xCol: String, 
                        yCol: String, 
                        mode: Option[ScatterMode] = Some(ScatterMode(ScatterMode.Markers)))