package alp.regression

import alp.plotly.model.{PlotData, ScatterOptions}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.{PolynomialExpansion, VectorAssembler}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.tuning.{CrossValidator, CrossValidatorModel, ParamGridBuilder}
import org.apache.spark.sql.{DataFrame, SparkSession}
import plotly.element.ScatterMode
import plotly.layout.Axis
import alp.plotly.extensions.SparkExtensions._

object PolynomialLinearRegression extends App {

  private lazy val spark: SparkSession = SparkSession.builder()
    .appName("Polynomial Linear Regression")
    .master("local[1]")
    .getOrCreate()

  private lazy val dataset = spark.read
    .option("header", "true")
    .option("inferSchema", "true")
    .csv("spark-ml/src/main/resources/data/Position_Salaries.csv")

  private lazy val assembler: VectorAssembler = new VectorAssembler()
    .setInputCols(Array("Level"))
    .setOutputCol("linearFeatures")

  private lazy val polyExpansion = new PolynomialExpansion()
    .setInputCol(assembler.getOutputCol)
    .setOutputCol("features")
    .setDegree(4)

  private lazy val lr: LinearRegression = new LinearRegression()
    .setLabelCol("Salary")

  private lazy val pipeline: Pipeline = new Pipeline()
    .setStages(Array(assembler, polyExpansion, lr))

  private lazy val paramGrid: Array[ParamMap] = new ParamGridBuilder()
    .addGrid(lr.regParam, Array(1.0, 0.1, 0.01))
    .addGrid(polyExpansion.degree, Array(2, 3, 4))
    .build()

  private lazy val crossValidation = new CrossValidator()
    .setEstimator(pipeline)
    .setEvaluator(new RegressionEvaluator().setLabelCol("Salary"))
    .setEstimatorParamMaps(paramGrid)
    .setNumFolds(3)
    .setParallelism(2)

  private lazy val crossModel: CrossValidatorModel = crossValidation.fit(dataset)

  private lazy val predicted = crossModel.transform(dataset).cache()
  predicted.show()

  // Visualising results
  private lazy val predictedScatterOptions: ScatterOptions =
    ScatterOptions(Seq(PlotData("Level", "prediction", Some("Predicted Salary"), Some(ScatterMode(ScatterMode.Lines))),
      PlotData("Level", "Salary", Some("Real Salary"))),
      s"/home/abilio/repos/training/MachineLearning/spark-mllib/spark-ml/target/polynomial.html",
      title = Option("Polynomial Regression"),
      xAxis = Some(Axis().withTitle("Position Level")),
      yAxis = Some(Axis().withTitle("Salary")))

  predicted.scatter(predictedScatterOptions)
  predicted.unpersist()

  import spark.implicits._
  private lazy val level: Double = 6.5
  private lazy val singlePrediction: DataFrame = Seq(level).toDF("Level")

  private lazy val pred = crossModel.transform(singlePrediction).select("prediction").collect()(0).getDouble(0)
  println(s"Predicted salary for [$level] level: $pred")

  spark.close()

}
