package alp.plotly.extensions

import java.io.File
import org.apache.spark.sql.DataFrame
import alp.plotly.model.ScatterOptions
import alp.plotly.plots.ScatterPlot


object SparkExtensions {

    implicit class DataframeExtensions(df: DataFrame) {

        def scatter(options: ScatterOptions): File = ScatterPlot.plot(df, options)

    }
  
}
