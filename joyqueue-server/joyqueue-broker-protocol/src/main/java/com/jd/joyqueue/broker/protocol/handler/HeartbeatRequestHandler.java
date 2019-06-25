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
package com.jd.joyqueue.broker.protocol.handler;

import com.jd.joyqueue.broker.protocol.JoyQueueCommandHandler;
import com.jd.joyqueue.network.command.BooleanAck;
import com.jd.joyqueue.network.command.JoyQueueCommandType;
import com.jd.joyqueue.network.transport.Transport;
import com.jd.joyqueue.network.transport.command.Command;
import com.jd.joyqueue.network.transport.command.Type;

/**
 * HeartbeatRequestHandler
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/28
 */
public class HeartbeatRequestHandler implements JoyQueueCommandHandler, Type {

    @Override
    public Command handle(Transport transport, Command command) {
        return BooleanAck.build();
    }

    @Override
    public int type() {
        return JoyQueueCommandType.HEARTBEAT_REQUEST.getCode();
    }
}