package alp.linearregression

import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.sql.DataFrame
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.Pipeline

object SimpleLinearRegression extends App {

    private lazy val spark: SparkSession = SparkSession.builder()
        .appName("Simple Linear Regression")
        .master("local[1]")
        .getOrCreate()


    private lazy val dataset = spark.read
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("/home/abilio/repos/training/MachineLearning/spark-mllib/spark-ml/src/main/resources/data/Salary_Data.csv")


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


    spark.close
  
}
