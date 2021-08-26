package com.github.gustajz.kubernetes.metrics;

import com.github.gustajz.kubernetes.commons.CommandLineTable;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.custom.NodeMetrics;
import java.util.ArrayList;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/** @author gustavojotz */
@Slf4j
public class NodeMetricsRunner implements Consumer<Metrics> {

    @SneakyThrows
    @Override
    public void accept(Metrics metrics) {
        var nodeMetricsList = metrics.getNodeMetrics();

        var nodes = new ArrayList<NodeMetric>();

        for (NodeMetrics item : nodeMetricsList.getItems()) {
            var node = new NodeMetric();
            node.setName(item.getMetadata().getName());

            var usage = item.getUsage();
            node.setUsageCpu(usage.get("cpu"));
            node.setUsageMemory(usage.get("memory"));

            nodes.add(node);
        }

        CommandLineTable nodeTable = new CommandLineTable();
        nodeTable.setShowVerticalLines(false);
        nodeTable.setHeaders("node", "usageCpu", "usageMemory");

        QuantityFormatter formatter = new QuantityFormatter();

        nodes.forEach(
                o ->
                        nodeTable.addRow(
                                o.getName(),
                                formatter.apply(o.getUsageCpu()),
                                formatter.apply(o.getUsageMemory())));
        nodeTable.print();
    }
}
