package org.example.pr;

import org.immutables.value.Value;
import org.neo4j.gds.annotation.Configuration;
import org.neo4j.gds.annotation.ValueClass;
import org.neo4j.gds.beta.pregel.annotation.GDSMode;
import org.neo4j.gds.beta.pregel.annotation.PregelProcedure;
import org.neo4j.gds.api.nodeproperties.ValueType;
import org.neo4j.gds.beta.pregel.Messages;
import org.neo4j.gds.beta.pregel.PregelComputation;
import org.neo4j.gds.beta.pregel.PregelProcedureConfig;
import org.neo4j.gds.beta.pregel.PregelSchema;
import org.neo4j.gds.beta.pregel.context.ComputeContext;
import org.neo4j.gds.beta.pregel.context.InitContext;
import org.neo4j.gds.core.CypherMapWrapper;


@PregelProcedure(name = "gdsbook.pr", modes = {GDSMode.STREAM, GDSMode.MUTATE, GDSMode.WRITE})
public class PageRank implements PregelComputation<PageRank.PrConfig> {

    static final String PR_KEY = "pr";

    @Override
    public PregelSchema schema(PrConfig config) {
        return new PregelSchema.Builder()
                .add(PR_KEY, ValueType.DOUBLE)
                .build();
    }

    @Override
    public void init(InitContext<PrConfig> context) {
        var initialValue = 1.0;
        context.setNodeValue(PR_KEY, initialValue);
    }

    @Override
    public void compute(ComputeContext<PrConfig> context, Messages messages) {
        // context.logMessage(String.format("starting compute for %s", context.nodeId()));
        double prValue = context.doubleNodeValue(PR_KEY);

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
        }

        // send new rank to neighbors, divided by the node degree
        context.sendToNeighbors(prValue / context.degree());
    }

    @ValueClass
    @Configuration("PrConfigImpl")
    @SuppressWarnings("immutables:subtype")
    public interface PrConfig extends PregelProcedureConfig {
        // set a default value for max iterations from PregelProcedureConfig
        @Value.Default
        default int maxIterations() {
            return 100;
        }

        // declare new config parameter with a default value
        @Value.Default
        default double dampingFactor() {
            // default value
            return 0.85;
        }

        static PrConfig of(CypherMapWrapper userInput) {
            return new PrConfigImpl(userInput);
        }
    }
}
