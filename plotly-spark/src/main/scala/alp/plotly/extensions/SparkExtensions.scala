package alp.plotly.extensions

import java.io.File
import org.apache.spark.sql.DataFrame
import plotly.Scatter
import plotly.element.ScatterMode
import plotly.layout.Layout
import plotly.Plotly
import alp.plotly.model.ScatterOptions


object SparkExtensions {

    implicit class DataframeExtensions(df: DataFrame) {

        // TODO plot(options: xxxOptions*)
        def scatter(options: ScatterOptions): File = {
            val rows = df.select(options.xCol, options.yCol).collect().toSeq
            val x = rows.map(r => r.get(0).toString())
            val y = rows.map(r => r.get(1).toString())
            val trace = Scatter()
                .withX(x)
                .withY(y)
                .withMode(ScatterMode(ScatterMode.Lines, ScatterMode.Markers))
            val data = Seq(trace)
            val layout = Layout()
                .withTitle("Line and Scatter Plot")
            Plotly.plot(options.filePath, data, layout, openInBrowser=false)
        }

    }
  
}
