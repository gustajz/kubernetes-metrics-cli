package com.github.gustajz.kubernetesmetriccl;

import io.kubernetes.client.Metrics;
import io.kubernetes.client.custom.ContainerMetrics;
import io.kubernetes.client.custom.NodeMetrics;
import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.util.Config;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

/** @author gustavojotz */
@Slf4j
@Component
@RequiredArgsConstructor
@CommandLine.Command(
        description = "Prints information about current usage, limits and request of resources.")
public class MetricsRunner implements Callable<Integer> {

    @CommandLine.Option(
            names = {"-n", "--namespace"},
            description = "Namespace name.",
            required = true)
    private String namespace;

    @CommandLine.Option(
            names = {"-no", "--nodes"},
            description = "Nodes metrics.")
    private boolean showNodes;

    @CommandLine.Option(
            names = {"-po", "--pods"},
            description = "Pods metrics and configuration.")
    private boolean showPods;

    @CommandLine.Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Show this help message and exit.")
    private boolean helpRequested = false;

    @Override
    public Integer call() throws Exception {

        ApiClient client = Config.defaultClient();

        Configuration.setDefaultApiClient(client);

        final Metrics metrics = new Metrics(client);

        if (showNodes) {
            nodeMetrics(metrics);
        }

        if (showPods) {
            podMetrics(metrics, namespace);
        }

        if (!showPods && !showNodes) {
            nodeMetrics(metrics);
            podMetrics(metrics, namespace);
        }

        return 0;
    }

    /**
     * @param metrics
     * @param namespace
     * @throws ApiException
     */
    private void podMetrics(Metrics metrics, String namespace) throws ApiException {
        final AppsV1Api appsV1Api = new AppsV1Api();

        var podMetrics = new ArrayList<PodMetric>();

        for (PodMetrics item : metrics.getPodMetrics(namespace).getItems()) {

            if (item.getContainers() == null) {
                continue;
            }

            for (ContainerMetrics container : item.getContainers()) {
                var podMetric = new PodMetric();

                String containerName = container.getName();
                String podName = item.getMetadata().getName();

                podMetric.setPodName(podName);
                podMetric.setContainerName(containerName);

                try {
                    V1Deployment deployment =
                            appsV1Api.readNamespacedDeployment(
                                    containerName,
                                    item.getMetadata().getNamespace(),
                                    null,
                                    null,
                                    null);

                    if (deployment != null) {
                        deployment.getSpec().getTemplate().getSpec().getContainers().stream()
                                .filter(v1Container -> v1Container.getName().equals(containerName))
                                .map(V1Container::getResources)
                                .forEach(
                                        resource -> {
                                            podMetric.setLimitsCpu(resource.getLimits().get("cpu"));
                                            podMetric.setLimitsMemory(
                                                    resource.getLimits().get("memory"));
                                            podMetric.setRequestsCpu(
                                                    resource.getRequests().get("cpu"));
                                            podMetric.setRequestsMemory(
                                                    resource.getRequests().get("memory"));
                                        });
                    }
                } catch (Exception e) {
                    // noop
                }

                container
                        .getUsage()
                        .forEach(
                                (s, quantity) -> {
                                    if ("cpu".equals(s)) podMetric.setUsageCpu(quantity);
                                    if ("memory".equals(s)) podMetric.setUsageMemory(quantity);
                                });

                podMetrics.add(podMetric);
                log.trace("{}", podMetric);
            }
        }

        podMetrics.sort(Comparator.comparing(PodMetric::getPodName));

        CommandLineTable st = new CommandLineTable();
        st.setShowVerticalLines(false);
        st.setHeaders(
                "containerName",
                "podName",
                "usageCpu",
                "usageMemory",
                "limitsCpu",
                "limitsMemory",
                "requestsCpu",
                "requestsMemory");

        podMetrics.forEach(
                o ->
                        st.addRow(
                                o.getContainerName(),
                                o.getPodName(),
                                toString(o.getUsageCpu()),
                                toString(o.getUsageMemory()),
                                toString(o.getLimitsCpu()),
                                toString(o.getLimitsMemory()),
                                toString(o.getRequestsCpu()),
                                toString(o.getRequestsMemory())));
        st.print();
    }

    /**
     * @param metrics
     * @throws ApiException
     */
    private void nodeMetrics(Metrics metrics) throws ApiException {
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

        nodes.forEach(
                o ->
                        nodeTable.addRow(
                                o.getName(),
                                toString(o.getUsageCpu()),
                                toString(o.getUsageMemory())));
        nodeTable.print();
    }

    /**
     * @param quantity
     * @return
     */
    private String toString(Quantity quantity) {
        if (quantity != null) {
            if (quantity.getFormat() == Quantity.Format.BINARY_SI) {
                var ki = quantity.toSuffixedString();
                if (ki.endsWith("Ki")) {
                    var mi =
                            quantity.getNumber()
                                    .divideToIntegralValue(BigDecimal.valueOf(1024 * 1024));
                    return String.format("%sMi", mi);
                }
                return ki;
            } else if (quantity.getFormat() == Quantity.Format.DECIMAL_SI) {
                var c = quantity.getNumber().multiply(BigDecimal.valueOf(1000));
                return new DecimalFormat("#0'm'").format(c);
            }
            return new DecimalFormat("#0.000").format(quantity.getNumber());
        }
        return null;
    }
}
