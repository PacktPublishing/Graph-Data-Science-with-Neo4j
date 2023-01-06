package org.example.pr;

import org.immutables.value.Value;
import org.neo4j.gds.annotation.Configuration;
import org.neo4j.gds.annotation.ValueClass;
import org.neo4j.gds.api.nodeproperties.ValueType;
import org.neo4j.gds.beta.pregel.Messages;
import org.neo4j.gds.beta.pregel.PregelComputation;
import org.neo4j.gds.beta.pregel.PregelProcedureConfig;
import org.neo4j.gds.beta.pregel.PregelSchema;
import org.neo4j.gds.beta.pregel.annotation.GDSMode;
import org.neo4j.gds.beta.pregel.annotation.PregelProcedure;
import org.neo4j.gds.beta.pregel.context.ComputeContext;
import org.neo4j.gds.beta.pregel.context.InitContext;
import org.neo4j.gds.beta.pregel.context.MasterComputeContext;
import org.neo4j.gds.config.ToleranceConfig;
import org.neo4j.gds.core.CypherMapWrapper;

import java.util.function.LongPredicate;


@PregelProcedure(name = "gdsbook.prtol", modes = {GDSMode.STREAM, GDSMode.MUTATE, GDSMode.WRITE})
public class PageRankTol implements PregelComputation<PageRankTol.PrTolConfig> {

    static final String PR_KEY = "pr";
    static final String PR_DIFF = "diff";

    @Override
    public PregelSchema schema(PrTolConfig config) {
        return new PregelSchema.Builder()
                .add(PR_KEY, ValueType.DOUBLE)
                .add(PR_DIFF, ValueType.DOUBLE, PregelSchema.Visibility.PRIVATE)
                .build();
    }

    @Override
    public void init(InitContext<PrTolConfig> context) {
        context.setNodeValue(PR_KEY, 1.0);
        double tol = context.config().tolerance();
        context.setNodeValue(PR_KEY, tol * 10);  // large number compared to tolerance
    }

    @Override
    public void compute(ComputeContext<PrTolConfig> context, Messages messages) {
        // context.logMessage(String.format("starting compute for %s", context.nodeId()));
        double prValue = context.doubleNodeValue(PR_KEY);
        double oldPrValue = prValue;

        // compute new rank based on neighbor ranks
        if (!context.isInitialSuperstep()) {
            double oldPrNeighbors = 0;
            for (var message : messages) {
                // message = oldPr_i / outDegree_i
                oldPrNeighbors += message;
            }
            var dp = context.config().dampingFactor();
            prValue = (1 - dp) + dp * oldPrNeighbors;
            context.setNodeValue(PR_KEY, prValue);
            context.setNodeValue(PR_DIFF, Math.abs(prValue - oldPrValue));
        }

        // send new rank to neighbors, divided by the node degree
        context.sendToNeighbors(prValue / context.degree());
    }

    @Override
    public boolean masterCompute(MasterComputeContext<PrTolConfig> context) {
        PrDiffSum df = new PrDiffSum();
        context.forEachNode(node_id -> {
            df.sumDelta += context.doubleNodeValue(node_id, PR_DIFF);
            return true;
        });
        double tol = context.config().tolerance();
        double sumDeltaNormed = df.sumDelta / context.nodeCount();
        // stop if the sum of all differences normalized to number of nodes
        // is lower than the tolerance
        context.logDebug(String.format("PRRol :: Step=%s, diff=%s%n", context.superstep(), sumDeltaNormed));
        return sumDeltaNormed < tol;
    }

    @ValueClass
    @Configuration("PrTolConfigImpl")
    @SuppressWarnings("immutables:subtype")
    public interface PrTolConfig extends PregelProcedureConfig, ToleranceConfig {
        // set a default value for max iterations from PregelProcedureConfig
        @Value.Default
        default int maxIterations() {
            return 100;
        }

        @Value.Default
        default double tolerance() { return 0.001; }

        // declare new config parameter with a default value
        @Value.Default
        default double dampingFactor() {
            // default value
            return 0.85;
        }

        static PrTolConfig of(CypherMapWrapper userInput) {
            return new PrTolConfigImpl(userInput);
        }
    }

    private static class PrDiffSum {
        public double sumDelta;

        public PrDiffSum() {
            sumDelta = 0;
        }
    }
}
