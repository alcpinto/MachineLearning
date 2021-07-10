package alp.regression

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.{DecisionTreeRegressor, RandomForestRegressor}
import org.apache.spark.sql.{DataFrame, SparkSession}

object RandomForestRegression extends App {

  private lazy val spark: SparkSession = SparkSession.builder()
    .appName("Random Forest Regression")
    .master("local[1]")
    .getOrCreate()


  private lazy val dataset: DataFrame = spark.read
    .option("header", "true")
    .option("inferSchema", "true")
    .csv("spark-ml/src/main/resources/data/Position_Salaries.csv")

  private lazy val featureAssembler = new VectorAssembler()
    .setInputCols(Array("Level"))
    .setOutputCol("features")

  private lazy val dtr = new RandomForestRegressor()
    .setLabelCol("Salary")
    .setNumTrees(10)


  private lazy val pipeline = new Pipeline()
    .setStages(Array(featureAssembler, dtr))

  private lazy val model = pipeline.fit(dataset)

  import spark.implicits._
  private lazy val predicted = model.transform(Seq(6.5).toDF("Level"))
  predicted.show()

}
