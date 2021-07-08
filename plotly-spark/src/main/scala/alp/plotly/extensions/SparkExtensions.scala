package alp.plotly.extensions

import java.io.File
import org.apache.spark.sql.DataFrame
import alp.plotly.model.ScatterOptions
import alp.plotly.plots.Scatter


object SparkExtensions {

    implicit class DataframeExtensions(df: DataFrame) {

        // TODO plot(options: xxxOptions*)
        def scatter(options: ScatterOptions): File = Scatter.plot(df, options)

    }
  
}
