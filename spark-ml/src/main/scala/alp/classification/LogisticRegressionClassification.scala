package alp.classification

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{StandardScaler, VectorAssembler}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{BooleanType, DoubleType}

object LogisticRegressionClassification extends App {

  private val spark = SparkSession.builder()
    .appName("Logistic Regression Classification")
    .master("local[1]")
    .getOrCreate()


  private val dataset = spark.read
    .option("header", "true")
    .option("inferSchema", "true")
    .csv("spark-ml/src/main/resources/data/Social_Network_Ads.csv")

  private val Array(training, test) = dataset.randomSplit(Array(0.8, 0.2), seed = 0L)


  private val featureAssembler = new VectorAssembler()
    .setInputCols(Array("Age", "EstimatedSalary"))
    .setOutputCol("AssembledFeatures")


  private val featureScaler = new StandardScaler()
    .setInputCol(featureAssembler.getOutputCol)
    .setOutputCol("features")

  // default family is auto.
  // Automatically select the family based on the number of classes: If numClasses == 1 || numClasses == 2, set to "binomial". Else, set to "multinomial"
  private val lr = new LogisticRegression()
    .setLabelCol("Purchased")
    .setFamily("binomial")


  private val pipeline = new Pipeline()
    .setStages(Array(featureAssembler, featureScaler, lr))


  private val paramGrid = new ParamGridBuilder()
    .addGrid(lr.regParam, Array(1.0, 0.1, 0.01))
    .build()

  private val evaluator = new BinaryClassificationEvaluator()
      .setLabelCol("Purchased")

  private val crossValidator = new CrossValidator()
    .setEstimator(pipeline)
    .setEvaluator(evaluator)
    .setEstimatorParamMaps(paramGrid)

  private val model = crossValidator.fit(training)



  /**
   * Predicting a single result
   */
  import spark.implicits._
  private lazy val cols = dataset.columns.filter(!_.contains("Purchased"))
  private val singleValue = Seq((30, 87000)).toDF(cols:_*)
  private val singlePredictedDF = model.transform(singleValue)
  private val singlePredicted = singlePredictedDF.select("prediction").collect()(0).getDouble(0).toInt
  println(s"Prediction for entry [Age: 30, EstimatedSalary: 87000]: $singlePredicted")

  /**
   * Predicting the Test set
   */
  private val predictedTestSet = model.transform(test)

  private val predictionAndLabels = predictedTestSet.select(col("prediction"), col("Purchased").cast(DoubleType).as("label"),
    (col("prediction") === col("Purchased").cast(DoubleType)).as("test").cast(BooleanType)).cache()

  println(predictionAndLabels.count())
  predictionAndLabels.show(numRows = 150)
  println(predictionAndLabels.filter(col("test") === false).count())

  predictionAndLabels.unpersist()

  private val predictionAndLabelsRDD = predictionAndLabels.rdd.map(r => (r.getDouble(0), r.getDouble(1)))
  val metrics = new MulticlassMetrics(predictionAndLabelsRDD)
  println(metrics.confusionMatrix)
  println(metrics.accuracy)


  spark.close()

}
