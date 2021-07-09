package alp.regression

import alp.plotly.extensions.SparkExtensions._
import alp.plotly.model.PlotData
import alp.plotly.model.ScatterOptions
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import plotly.element.ScatterMode
import plotly.layout.Axis

object SimpleLinearRegression extends App {

    private lazy val spark: SparkSession = SparkSession.builder()
        .appName("Simple Linear Regression")
        .master("local[1]")
        .getOrCreate()


    private lazy val dataset = spark.read
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("/home/abilio/repos/training/MachineLearning/spark-mllib/spark-ml/src/main/resources/data/Salary_Data.csv")


    val options: ScatterOptions = ScatterOptions(Seq(PlotData("YearsExperience", 
                                                "Salary")), 
                                                "/home/abilio/repos/training/MachineLearning/spark-mllib/spark-ml/target/dataset.html",
                                                title = Some("Salary Data"),
                                                xAxis = Some(Axis().withTitle("Years Experience")),
                                                yAxis = Some(Axis().withTitle("Salary")))
    dataset.scatter(options)


    private lazy val Array(training, test) = dataset.randomSplit(Array(0.8, 0.2))

    /**
      * Preprocessing data
      */
    private lazy val assembler: VectorAssembler = new VectorAssembler()
        .setInputCols(Array("YearsExperience"))
        .setOutputCol("features")

    private lazy val preprocessingPipeline: Pipeline = new Pipeline()
        .setStages(Array(assembler))
    private lazy val preprocessingModel = preprocessingPipeline.fit(training)
    private lazy val preprocessedTraning = preprocessingModel.transform(training)
    private lazy val preprocessedTest = preprocessingModel.transform(test) 

    preprocessedTraning.show(numRows=100)
    
    /**
      * Liner Regression
      */
    private lazy val lr = new LinearRegression()
        .setLabelCol("Salary")
        .setFeaturesCol("features")
        .setMaxIter(10)
        .setRegParam(1.0)
        

    private lazy val model: LinearRegressionModel = lr.fit(preprocessedTraning)

    private val predicted: DataFrame = model.transform(preprocessedTest)
    predicted.show()


    // Visualising results
    private def predictedScatterOptions(fileName: String, title: String): ScatterOptions = 
        ScatterOptions(Seq(PlotData("YearsExperience", "prediction", Some("Prediction"), Some(ScatterMode(ScatterMode.Lines))),
                        PlotData("YearsExperience", "Salary")),
                        s"/home/abilio/repos/training/MachineLearning/spark-mllib/spark-ml/target/$fileName.html",
                        title = Option(title),
                        xAxis = Some(Axis().withTitle("Years Experience")),
                        yAxis = Some(Axis().withTitle("Salary")))

    // Visualising the Training set results
    model.transform(preprocessedTraning).scatter(predictedScatterOptions("predictedTraining", "Training set results"))
    // Visualising the Test set results
    predicted.scatter(predictedScatterOptions("predicted", "Test set results"))


    // Making a single prediction (employee with 12 years of experience)
    val pred12YE = model.predict(Vectors.dense(Array(12.0)))
    println(s"Predicted 12 Years Experience: $pred12YE")

    // Getting the linear regression formula
    val coef = model.coefficients
    val intercept = model.intercept
    println(s"Formula: $intercept + ${coef(0)} * YearsExperience")

    spark.close
  
}
