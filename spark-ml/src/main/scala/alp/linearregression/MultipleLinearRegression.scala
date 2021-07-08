package alp.linearregression

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.OneHotEncoder
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.tuning.CrossValidator
import org.apache.spark.ml.tuning.CrossValidatorModel
import org.apache.spark.ml.tuning.ParamGridBuilder
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession

object MultipleLinearRegression extends App {

    private lazy val spark: SparkSession = SparkSession.builder()
        .appName("Multiple Linear Regression")
        .master("local[1]")
        .getOrCreate()

    
    private lazy val dataset: DataFrame = spark.read
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("/home/abilio/repos/training/MachineLearning/spark-mllib/spark-ml/src/main/resources/data/50_Startups.csv")

    // Split traning and test sets
    private val Array(training, test) = dataset.randomSplit(Array(0.8, 0.2))

    /**
      * Preprocessing
      */
    // Encoding categorical independent variables
    private lazy val stateIndexer: StringIndexer = new StringIndexer()
        .setInputCol("State")
        .setOutputCol("IndexedState")

    private lazy val stateEncoder: OneHotEncoder = new OneHotEncoder()
        .setInputCol(stateIndexer.getOutputCol)
        .setOutputCol("EncodedState")

    // Assemble features
    private lazy val assembler: VectorAssembler = new VectorAssembler()
        .setInputCols(Array("R&D Spend", "Administration", "Marketing Spend", stateEncoder.getOutputCol))
        .setOutputCol("features")

    private lazy val lr: LinearRegression = new LinearRegression()
        .setLabelCol("Profit")

    private lazy val pipeline: Pipeline = new Pipeline()
        .setStages(Array(stateIndexer, stateEncoder, assembler, lr))
        
    
    private lazy val paramGrid: Array[ParamMap] = new ParamGridBuilder()
        .addGrid(lr.regParam, Array(1.0, 0.1, 0.01))
        .addGrid(lr.maxIter, Array(10, 100))
        .build()

    private lazy val crossVal: CrossValidator = new CrossValidator()
        .setEstimator(pipeline)
        .setEvaluator(new RegressionEvaluator().setLabelCol("Profit"))
        .setEstimatorParamMaps(paramGrid)
        .setNumFolds(3)
        .setParallelism(2)

    private lazy val cvModel: CrossValidatorModel = crossVal.fit(training)

    private lazy val predicted: DataFrame = cvModel.transform(test)
    predicted.show(truncate = false)

    private lazy val predictedAndLabel = predicted.select("prediction", "Profit").rdd.map(r => (r.get(0), r.get(1)))
    private lazy val metrics = new RegressionMetrics(predictedAndLabel)
    println(s"Root Mean Squared Error (RMSE): ${metrics.rootMeanSquaredError}")
    println(s"Mean Squared Error (MSE): ${metrics.meanSquaredError}")
    println(s"Coefficient of Determination (R2): ${metrics.r2}")
    println(s"Mean Absolute Error (MAE): ${metrics.meanAbsoluteError}")
    
    spark.close()
  
}
