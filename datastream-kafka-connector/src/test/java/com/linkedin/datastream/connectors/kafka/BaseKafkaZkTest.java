package com.linkedin.datastream.connectors.kafka;

import java.util.Properties;

import org.I0Itec.zkclient.ZkConnection;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.codahale.metrics.MetricRegistry;
import kafka.utils.ZkUtils;

import com.linkedin.datastream.common.zk.ZkClient;
import com.linkedin.datastream.kafka.EmbeddedZookeeperKafkaCluster;
import com.linkedin.datastream.metrics.DynamicMetricsManager;


/**
 * Base test class for test cases that rely on EmbeddedZookeeperKafkaCluster so that all tests in a given class could
 * share the same zookeeper-kafka cluster and zk client/utils.
 */
@Test
public abstract class BaseKafkaZkTest {

  protected EmbeddedZookeeperKafkaCluster _kafkaCluster;
  protected ZkClient _zkClient;
  protected ZkUtils _zkUtils;
  protected String _broker;

  @BeforeMethod(alwaysRun = true)
  public void beforeMethodSetup() throws Exception {
    DynamicMetricsManager.createInstance(new MetricRegistry(), getClass().getSimpleName());
    Properties kafkaConfig = new Properties();
    // we will disable auto topic creation for tests
    kafkaConfig.setProperty("auto.create.topics.enable", Boolean.FALSE.toString());
    _kafkaCluster = new EmbeddedZookeeperKafkaCluster(kafkaConfig);
    _kafkaCluster.startup();
    _broker = _kafkaCluster.getBrokers().split("\\s*,\\s*")[0];
    _zkClient = new ZkClient(_kafkaCluster.getZkConnection());
    _zkUtils = new ZkUtils(_zkClient, new ZkConnection(_kafkaCluster.getZkConnection()), false);
  }

  @AfterMethod(alwaysRun = true)
  public void afterMethodTeardown() {
    if (_zkUtils != null) {
      _zkUtils.close();
    }
    if (_zkClient != null) {
      _zkClient.close();
    }
    if (_kafkaCluster != null && _kafkaCluster.isStarted()) {
      _kafkaCluster.shutdown();
    }
    _broker = null;
  }

}