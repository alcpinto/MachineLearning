package alp.preprocessing

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import org.apache.spark.ml.feature.Imputer
import org.apache.spark.ml.feature.ImputerModel
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.OneHotEncoder
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.feature.StandardScaler


object DataPreprocessing extends App {

    private val spark: SparkSession = SparkSession.builder()
        .appName("Data Preprocessing")
        .master("local[1]")
        .getOrCreate()


    private val dataSchema = StructType(Array(
        StructField("Country", StringType, nullable = true),
        StructField("Age", IntegerType, nullable = true),
        StructField("Salary", DoubleType, nullable = true),
        StructField("Purchased", StringType, nullable = true)
    ))

    private val dataset: DataFrame = spark.read
        .option("header", "true")
        .schema(dataSchema)
        .csv("/home/abilio/repos/training/MachineLearning/spark-mllib/spark-ml/src/main/resources/data/Data.csv")


    // Split traning and test sets
    private val Array(training, test) = dataset.randomSplit(Array(0.8, 0.2))

    /*
        Data preprocesssing transformers 
    */

    // Taking care of missing data
    private val imputer: Imputer = new Imputer()
        .setInputCols(Array("Age", "Salary"))
        .setOutputCols(Array("ImputedAge", "ImputedSalary"))
        .setStrategy("mean")

    // Encoding categorical data
    // Encoding the independent variables
    private val countryIndexer: StringIndexer = new StringIndexer()
        .setInputCol("Country")
        .setOutputCol("CountryIndexed")
        
    private val encoder = new OneHotEncoder()
        .setInputCol(countryIndexer.getOutputCol)
        .setOutputCol("CountryEncoded")

    // Encoding dependent variable
    private val purchasedIndexer: StringIndexer = new StringIndexer()
        .setInputCol("Purchased")
        .setOutputCol("label")

    // create vector of features
    private val assembler: VectorAssembler = new VectorAssembler()
        .setInputCols(Array(encoder.getOutputCol) ++ imputer.getOutputCols)
        .setOutputCol("assembledFeatures")

    // features scaling
    private val scaler = new StandardScaler()
        .setInputCol(assembler.getOutputCol)
        .setOutputCol("features")
        .setWithMean(false)
        .setWithStd(true)

    private val pipeline: Pipeline = new Pipeline()
        .setStages(Array(imputer, countryIndexer, encoder, purchasedIndexer, assembler, scaler))

    private val preprocessingModel: PipelineModel = pipeline.fit(training)

    private val preprocessedTraining = preprocessingModel.transform(training).select("features", "label")
    preprocessedTraining.show(truncate=false)

    private val preprocessedTest = preprocessingModel.transform(test).select("features", "label")
    preprocessedTest.show(truncate=false)

    spark.close()

  
}
