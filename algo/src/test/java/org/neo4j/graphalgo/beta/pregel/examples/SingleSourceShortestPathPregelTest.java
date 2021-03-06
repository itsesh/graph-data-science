/*
 * Copyright (c) 2017-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.beta.pregel.examples;

import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.beta.pregel.ImmutablePregelConfig;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.PregelConfig;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.paged.AllocationTracker;
import org.neo4j.graphalgo.core.utils.paged.HugeDoubleArray;
import org.neo4j.graphalgo.extension.GdlExtension;
import org.neo4j.graphalgo.extension.GdlGraph;
import org.neo4j.graphalgo.extension.Inject;
import org.neo4j.graphalgo.extension.TestGraph;

import java.util.Map;

import static org.neo4j.graphalgo.TestSupport.assertLongValues;

@GdlExtension
class SingleSourceShortestPathPregelTest {

    @GdlGraph
    private static final String TEST_GRAPH =
            "CREATE" +
            "  (a:Node)" +
            ", (b:Node)" +
            ", (c:Node)" +
            ", (d:Node)" +
            ", (e:Node)" +
            ", (f:Node)" +
            ", (g:Node)" +
            ", (h:Node)" +
            ", (i:Node)" +
            // {J}
            ", (j:Node)" +
            // {A, B, C, D}
            ", (a)-[:TYPE]->(b)" +
            ", (b)-[:TYPE]->(c)" +
            ", (c)-[:TYPE]->(d)" +
            ", (a)-[:TYPE]->(c)" +
            // {E, F, G}
            ", (e)-[:TYPE]->(f)" +
            ", (f)-[:TYPE]->(g)" +
            // {H, I}
            ", (i)-[:TYPE]->(h)";

    @Inject
    private TestGraph graph;

    @Test
    void runSSSP() {
        int batchSize = 10;
        int maxIterations = 10;

        PregelConfig config = ImmutablePregelConfig.builder()
            .isAsynchronous(true)
            .build();

        Pregel pregelJob = Pregel.withDefaultNodeValues(
            graph,
            config,
            new SingleSourceShortestPathPregel(0),
            batchSize,
            Pools.DEFAULT,
            AllocationTracker.EMPTY
        );

        HugeDoubleArray nodeValues = pregelJob.run(maxIterations);

        assertLongValues(graph, nodeId -> (long) nodeValues.get(nodeId), Map.of(
                "a", 0L,
                "b", 1L,
                "c", 1L,
                "d", 2L,
                "e", Long.MAX_VALUE,
                "f", Long.MAX_VALUE,
                "g", Long.MAX_VALUE,
                "h", Long.MAX_VALUE,
                "i", Long.MAX_VALUE,
                "j", Long.MAX_VALUE
        ));
    }
}
