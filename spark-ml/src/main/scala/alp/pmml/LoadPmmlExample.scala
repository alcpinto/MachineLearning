package alp.pmml

import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DoubleType
import org.pmml4s.spark.ScoreModel

object LoadPmmlExample extends App {

  // load model from those help methods, e.g. pathname, file object, a string, an array of bytes, or an input stream.
  private val scoreModel = ScoreModel.fromFile("spark-ml/src/main/scala/alp/pmml/rf_pmml.pmml")

  println("INPUT NAMES:")
  println(scoreModel.model.inputNames.mkString(", "))
  println("TARGET NAMES:")
  println(scoreModel.model.targetNames.mkString(", "))

  private val spark = SparkSession.builder()
    .appName("PMML example")
    .master("local[1]")
    .getOrCreate()


  private val dataset = spark.read
    .option("header", "true")
    .option("inferSchema", "true")
    .csv("spark-ml/src/main/resources/data/iris_test.csv")


  val scoreDf = scoreModel.setPredictionCol("prediction").transform(dataset)
  scoreDf.show(truncate = false)

  val predictionAndLabelsRDD = scoreDf.select(hash(col("prediction")).cast(DoubleType), hash(col("variety")).cast(DoubleType))
    .rdd.map(r => (r.getDouble(0), r.getDouble(1)))

  val metrics = new MulticlassMetrics(predictionAndLabelsRDD)
  println(metrics.confusionMatrix)
  println(metrics.accuracy)


}
