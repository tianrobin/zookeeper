/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper.server.quorum;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.zookeeper.PortAssignment;
import org.apache.zookeeper.server.quorum.QuorumPeer.LearnerType;
import org.apache.zookeeper.server.quorum.QuorumPeer.QuorumServer;
import org.apache.zookeeper.test.ClientBase;
import org.junit.Test;

public class QuorumPeerTest {

    private int electionAlg = 3;
    private int tickTime = 2000;
    private int initLimit = 3;
    private int syncLimit = 3;

    /**
     * Test case for https://issues.apache.org/jira/browse/ZOOKEEPER-2301
     */
    @Test
    public void testQuorumPeerListendOnSpecifiedClientIP() throws IOException {
        long myId = 1;
        File dataDir = ClientBase.createTmpDir();
        int clientPort = PortAssignment.unique();
        String clientIP = "127.0.0.2";
        Map<Long, QuorumServer> peersView = new HashMap<Long, QuorumServer>();

        peersView.put(Long.valueOf(myId),
                new QuorumServer(myId, new InetSocketAddress("127.0.0.1", PortAssignment.unique()),
                        new InetSocketAddress("127.0.0.1", PortAssignment.unique()),
                        new InetSocketAddress(clientIP, clientPort), LearnerType.PARTICIPANT));

        /**
         * QuorumPeer constructor without QuorumVerifier
         */
        QuorumPeer peer1 = new QuorumPeer(peersView, dataDir, dataDir, clientPort, electionAlg, myId, tickTime,
                initLimit, syncLimit);
        String hostString1 = peer1.cnxnFactory.getLocalAddress().getHostString();
        assertEquals(clientIP, hostString1);

        // cleanup
        peer1.shutdown();

        /**
         * QuorumPeer constructor with QuorumVerifier
         */
        peersView.clear();
        clientPort = PortAssignment.unique();
        clientIP = "127.0.0.3";
        peersView.put(Long.valueOf(myId),
                new QuorumServer(myId, new InetSocketAddress("127.0.0.1", PortAssignment.unique()),
                        new InetSocketAddress("127.0.0.1", PortAssignment.unique()),
                        new InetSocketAddress(clientIP, clientPort), LearnerType.PARTICIPANT));
        QuorumPeer peer2 = new QuorumPeer(peersView, dataDir, dataDir, clientPort, electionAlg, myId, tickTime,
                initLimit, syncLimit);
        String hostString2 = peer2.cnxnFactory.getLocalAddress().getHostString();
        assertEquals(clientIP, hostString2);
        // cleanup
        peer2.shutdown();
    }

}
