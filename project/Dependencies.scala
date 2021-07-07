/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */
import sbt._

object Dependencies {

  object Versions {
    lazy val spark          = "3.1.2"
    lazy val plotly         = "0.8.2"
  }

  object Libraries {
    private def createModuleID(group: String, artifact: String, version: String, crossCompile: Boolean=true): ModuleID =
      if(crossCompile) group %% artifact % version else group % artifact % version
    private def sparkM(artifact: String): ModuleID = createModuleID("org.apache.spark", artifact, Versions.spark)
    private def plotlyM: ModuleID = createModuleID("org.plotly-scala", "plotly-render", Versions.plotly)

    
    lazy val sparkCore                     = sparkM("spark-core")
    lazy val sparkSql                      = sparkM("spark-sql")
    lazy val sparkMllib                    = sparkM("spark-mllib")


    lazy val plotly                        = plotlyM

  }

}
