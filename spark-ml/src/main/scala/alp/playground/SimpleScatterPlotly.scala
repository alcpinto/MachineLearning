package alp.playground

import plotly._
import plotly.element._
import plotly.layout._

object SimpleScatterPlotly extends App {

    // See https://alexarchambault.github.io/plotly-scala/

    val trace1 = Scatter()
        .withX(Seq(1, 2, 3, 4))
        .withY(Seq(10, 15, 13, 17))
        .withMode(ScatterMode(ScatterMode.Markers))


    val trace2 = Scatter()
        .withX(Seq(2, 3, 4, 5))
        .withY(Seq(16, 5, 11, 9))
        .withMode(ScatterMode(ScatterMode.Lines))

    val trace3 = Scatter()
        .withX(Seq(1, 2, 3, 4))
        .withY(Seq(12, 9, 15, 12))
        .withMode(ScatterMode(ScatterMode.Lines, ScatterMode.Markers))

    val data = Seq(trace1, trace2, trace3)

    val layout = Layout()
        .withTitle("Line and Scatter Plot")


    Plotly.plot("/home/abilio/repos/training/MachineLearning/spark-mllib/spark-ml/target/scatter.html", data, layout, openInBrowser = false)
  
}
