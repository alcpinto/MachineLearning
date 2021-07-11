package alp.plotly.plots

import alp.plotly.exceptions.InvalidScatterOptionsException
import alp.plotly.model.{PlotData, ScatterOptions}
import org.apache.spark.sql.DataFrame
import plotly.Plotly
import plotly.element.ScatterMode
import plotly.layout.Layout

import java.io.File
import scala.util.Try


object ScatterPlot {

    def plot(df: DataFrame, options: ScatterOptions): File = (df, options) match {
        case (null, _)     => throw InvalidScatterOptionsException("DataFrame is null")
        case (_, null)     => throw InvalidScatterOptionsException("Scatter options is null")
        case (_, o)        => (o.data, o.filePath) match {
            case (null, _) => throw InvalidScatterOptionsException("Scatter options' data is null")
            case (Nil, _)  => throw InvalidScatterOptionsException("Scatter options' data is empty")
            case (_, null) => throw InvalidScatterOptionsException("Scatter options' filePath is null")
            case _         => plotInternal(df, options)
        } 
    }

    private def plotInternal(df: DataFrame, options: ScatterOptions): File = {
        // validate filePath
        val file = new File(options.filePath)
        val addSuffix = (file.isDirectory, file.exists, options.overwrite) match {
            case (true, _, _) => throw InvalidScatterOptionsException("Scatter options' filePath is a directory")
            case (false, true, false) => true
            case (false, true, true)  => Try(file.delete()).getOrElse(true)
            case _             => false
        }

        val data = getTraces(df, options.data)

        var layout = Layout()
        layout =  options.title.map(t => layout.withTitle(t)).getOrElse(layout)
        layout =  options.xAxis.map(a => layout.withXaxis(a)).getOrElse(layout)
        layout =  options.yAxis.map(a => layout.withYaxis(a)).getOrElse(layout)
            
        Plotly.plot(options.filePath, data, layout, openInBrowser = false, addSuffixIfExists = addSuffix)
    }


    private def getTraces(df: DataFrame, data: Seq[PlotData]): Seq[plotly.Scatter] = data.map { d =>
        val rows = df.select(d.xCol, d.yCol).collect().toSeq
        val x = rows.map(r => r.get(0).toString)
        val y = rows.map(r => r.get(1).toString)
        plotly.Scatter()
            .withX(x)
            .withY(y)
            .withName(d.name.getOrElse(d.yCol))
            .withMode(d.mode.getOrElse(ScatterMode(ScatterMode.Markers)))
    }

  
}
