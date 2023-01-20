/*
 */
package org.example.pr;

import org.junit.jupiter.api.Test;
import org.neo4j.gds.Orientation;
import org.neo4j.gds.beta.pregel.Pregel;
import org.neo4j.gds.beta.pregel.PregelResult;
import org.neo4j.gds.core.concurrency.Pools;
import org.neo4j.gds.core.utils.paged.HugeDoubleArray;
import org.neo4j.gds.core.utils.progress.tasks.ProgressTracker;
import org.neo4j.gds.extension.GdlExtension;
import org.neo4j.gds.extension.GdlGraph;
import org.neo4j.gds.extension.Inject;
import org.neo4j.gds.extension.TestGraph;

import java.util.HashMap;

import static org.example.pr.PageRank.PR_KEY;
import static org.neo4j.gds.TestSupport.assertDoubleValues;

@GdlExtension
class PageRankTolTest {

    // https://en.wikipedia.org/wiki/PageRank#/media/File:PageRanks-Example.jpg
    @GdlGraph(orientation = Orientation.NATURAL)
    private static final String TEST_GRAPH =
            "CREATE" +
                    "  (A:Node)" +
                    ", (B:Node)" +
                    ", (C:Node)" +
                    ", (D:Node)" +
                    ", (A)-[:REL]->(B)" +
                    ", (A)-[:REL]->(D)" +
                    ", (B)-[:REL]->(A)" +
                    ", (C)-[:REL]->(B)" +
                    ", (D)-[:REL]->(B)";

    @Inject
    private TestGraph graph;

    @Test
    void runPRTol() {

        var config = PrTolConfigImpl.builder()
                .dampingFactor(0.85)
                .isAsynchronous(false)
                .build();

        var pregelJob = Pregel.create(
                graph,
                config,
                new PageRankTol(),
                Pools.DEFAULT,
                ProgressTracker.NULL_TRACKER
        );

        PregelResult res = pregelJob.run();

        int nbIt = res.ranIterations();
        assert nbIt < config.maxIterations();

        HugeDoubleArray nodeValues = res.nodeValues().doubleProperties(PR_KEY);
        // System.out.println(nodeValues);

        var expected = new HashMap<String, Double>();

        expected.put("A", 1.4814);
        expected.put("B", 1.5679);
        expected.put("C", 0.15);
        expected.put("D", 0.7789);

        assertDoubleValues(graph, nodeValues::get, expected, 1E-3);
    }
}
