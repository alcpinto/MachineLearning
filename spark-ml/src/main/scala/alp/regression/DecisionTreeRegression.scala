package alp.regression

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.DecisionTreeRegressor
import org.apache.spark.sql.{DataFrame, SparkSession}

object DecisionTreeRegression extends App {

  private lazy val spark: SparkSession = SparkSession.builder()
    .appName("Decision Tree Regression")
    .master("local[1]")
    .getOrCreate()


  private lazy val dataset: DataFrame = spark.read
    .option("header", "true")
    .option("inferSchema", "true")
    .csv("spark-ml/src/main/resources/data/Position_Salaries.csv")

  private lazy val featureAssembler = new VectorAssembler()
    .setInputCols(Array("Level"))
    .setOutputCol("features")

  private lazy val dtr = new DecisionTreeRegressor()
    .setLabelCol("Salary")


  private lazy val pipeline = new Pipeline()
    .setStages(Array(featureAssembler, dtr))

  private lazy val model = pipeline.fit(dataset)

  import spark.implicits._
  private lazy val predicted = model.transform(Seq(6.5).toDF("Level"))
  predicted.show()

}
