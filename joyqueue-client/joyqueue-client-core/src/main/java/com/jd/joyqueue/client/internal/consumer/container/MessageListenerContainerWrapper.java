/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.joyqueue.client.internal.consumer.container;

import com.jd.joyqueue.client.internal.cluster.ClusterClientManager;
import com.jd.joyqueue.client.internal.cluster.ClusterManager;
import com.jd.joyqueue.client.internal.consumer.BatchMessageListener;
import com.jd.joyqueue.client.internal.consumer.MessageListener;
import com.jd.joyqueue.client.internal.consumer.MessageListenerContainer;
import com.jd.joyqueue.client.internal.consumer.config.ConsumerConfig;
import com.jd.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import com.jd.joyqueue.client.internal.nameserver.NameServerConfig;
import com.jd.joyqueue.toolkit.service.Service;

/**
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/27
 */
public class MessageListenerContainerWrapper extends Service implements MessageListenerContainer {

    private ConsumerConfig config;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private ClusterClientManager clusterClientManager;
    private ConsumerClientManager consumerClientManager;
    private MessageListenerContainer delegate;

    public MessageListenerContainerWrapper(ConsumerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager,
                                           ClusterClientManager clusterClientManager, ConsumerClientManager consumerClientManager, MessageListenerContainer delegate) {
        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.clusterManager = clusterManager;
        this.clusterClientManager = clusterClientManager;
        this.consumerClientManager = consumerClientManager;
        this.delegate = delegate;
    }

    @Override
    protected void doStart() throws Exception {
        if (clusterClientManager != null) {
            clusterClientManager.start();
        }
        if (clusterManager != null) {
            clusterManager.start();
        }
        if (consumerClientManager != null) {
            consumerClientManager.start();
        }
        delegate.start();
    }

    @Override
    protected void doStop() {
        delegate.stop();
        if (consumerClientManager != null) {
            consumerClientManager.stop();
        }
        if (clusterManager != null) {
            clusterManager.stop();
        }
        if (clusterClientManager != null) {
            clusterClientManager.stop();
        }
    }

    @Override
    public void addListener(String topic, MessageListener messageListener) {
        delegate.addListener(topic, messageListener);
    }

    @Override
    public void addBatchListener(String topic, BatchMessageListener messageListener) {
        delegate.addBatchListener(topic, messageListener);
    }
}