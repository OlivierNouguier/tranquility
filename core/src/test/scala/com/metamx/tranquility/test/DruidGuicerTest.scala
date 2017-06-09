/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.metamx.tranquility.test

import com.metamx.common.scala.Predef._
import com.metamx.tranquility.druid.DruidGuicer
import io.druid.guice.ExtensionsConfig
import io.druid.query.aggregation.AggregatorFactory
import io.druid.query.aggregation.LongSumAggregatorFactory
import java.util.Properties
import org.scalatest.FunSuite
import org.scalatest.Matchers
import scala.collection.JavaConverters._

class DruidGuicerTest extends FunSuite with Matchers
{
  test("Default ExtensionsConfig") {
    val guicer = new DruidGuicer(new Properties)
    val extensionsConfig = guicer.get[ExtensionsConfig]
    extensionsConfig.getLoadList shouldBe(null)
    extensionsConfig.getDirectory shouldBe("extensions")
    extensionsConfig.getHadoopDependenciesDir shouldBe("hadoop-dependencies")
    extensionsConfig.searchCurrentClassloader shouldBe(true)
  }

  test("Overridden ExtensionsConfig") {
    val guicer = new DruidGuicer(
      new Properties withEffect { props =>
        props.setProperty("druid.extensions.loadList", "[]")
        props.setProperty("druid.extensions.directory", "/opt/druid/ext")
        props.setProperty("druid.extensions.hadoopDependenciesDir", "/opt/druid/hadoop")
        props.setProperty("druid.extensions.searchCurrentClassloader", "false")
      }
    )
    val extensionsConfig = guicer.get[ExtensionsConfig]
    extensionsConfig.getLoadList.asScala shouldBe(Nil)
    extensionsConfig.getDirectory shouldBe("/opt/druid/ext")
    extensionsConfig.getHadoopDependenciesDir shouldBe("/opt/druid/hadoop")
    extensionsConfig.searchCurrentClassloader shouldBe(false)
  }

  test("AggregatorFactory serde") {
    val guicer = new DruidGuicer(new Properties)
    val aggregator = guicer.objectMapper.readValue(
      guicer.objectMapper.writeValueAsBytes(new LongSumAggregatorFactory("foo", "bar")),
      classOf[AggregatorFactory]
    )
    aggregator shouldBe(new LongSumAggregatorFactory("foo", "bar"))
  }
}
