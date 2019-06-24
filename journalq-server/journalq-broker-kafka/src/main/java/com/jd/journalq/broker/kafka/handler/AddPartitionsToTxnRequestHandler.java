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
package com.jd.journalq.broker.kafka.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.journalq.broker.kafka.KafkaCommandHandler;
import com.jd.journalq.broker.kafka.KafkaCommandType;
import com.jd.journalq.broker.kafka.KafkaContext;
import com.jd.journalq.broker.kafka.KafkaContextAware;
import com.jd.journalq.broker.kafka.KafkaErrorCode;
import com.jd.journalq.broker.kafka.command.AddPartitionsToTxnRequest;
import com.jd.journalq.broker.kafka.command.AddPartitionsToTxnResponse;
import com.jd.journalq.broker.kafka.coordinator.transaction.TransactionCoordinator;
import com.jd.journalq.broker.kafka.coordinator.transaction.exception.TransactionException;
import com.jd.journalq.broker.kafka.helper.KafkaClientHelper;
import com.jd.journalq.broker.kafka.model.PartitionMetadataAndError;
import com.jd.journalq.network.transport.Transport;
import com.jd.journalq.network.transport.command.Command;
import com.jd.journalq.network.transport.command.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * AddPartitionsToTxnRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/4
 */
public class AddPartitionsToTxnRequestHandler implements KafkaCommandHandler, Type, KafkaContextAware {

    protected static final Logger logger = LoggerFactory.getLogger(AddPartitionsToTxnRequestHandler.class);

    private TransactionCoordinator transactionCoordinator;

    @Override
    public void setKafkaContext(KafkaContext kafkaContext) {
        this.transactionCoordinator = kafkaContext.getTransactionCoordinator();
    }

    @Override
    public Command handle(Transport transport, Command command) {
        AddPartitionsToTxnRequest addPartitionsToTxnRequest = (AddPartitionsToTxnRequest) command.getPayload();
        String clientId = KafkaClientHelper.parseClient(addPartitionsToTxnRequest.getClientId());
        AddPartitionsToTxnResponse response = null;

        try {
            Map<String, List<PartitionMetadataAndError>> errors = transactionCoordinator.handleAddPartitionsToTxn(clientId, addPartitionsToTxnRequest.getTransactionId(),
                    addPartitionsToTxnRequest.getProducerId(), addPartitionsToTxnRequest.getProducerEpoch(), addPartitionsToTxnRequest.getPartitions());
            response = new AddPartitionsToTxnResponse(errors);
        } catch (TransactionException e) {
            logger.warn("add partitions to txn exception, code: {}, message: {}, request: {}", e.getCode(), e.getMessage(), addPartitionsToTxnRequest);
            response = new AddPartitionsToTxnResponse(buildPartitionError(e.getCode(), addPartitionsToTxnRequest.getPartitions()));
        } catch (Exception e) {
            logger.error("add partitions to txn exception, request: {}", addPartitionsToTxnRequest, e);
            response = new AddPartitionsToTxnResponse(buildPartitionError(KafkaErrorCode.exceptionFor(e), addPartitionsToTxnRequest.getPartitions()));
        }

        return new Command(response);
    }

    protected Map<String, List<PartitionMetadataAndError>> buildPartitionError(int code, Map<String, List<Integer>> partitions) {
        Map<String, List<PartitionMetadataAndError>> result = Maps.newHashMapWithExpectedSize(partitions.size());
        for (Map.Entry<String, List<Integer>> entry : partitions.entrySet()) {
            List<PartitionMetadataAndError> partitionMetadataAndErrors = Lists.newArrayListWithCapacity(entry.getValue().size());
            for (Integer partition : entry.getValue()) {
                partitionMetadataAndErrors.add(new PartitionMetadataAndError(partition, (short) code));
            }
            result.put(entry.getKey(), partitionMetadataAndErrors);
        }
        return result;
    }

    @Override
    public int type() {
        return KafkaCommandType.ADD_PARTITIONS_TO_TXN.getCode();
    }
}