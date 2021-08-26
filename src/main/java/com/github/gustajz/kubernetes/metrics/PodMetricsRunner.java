package com.github.gustajz.kubernetes.metrics;

import com.github.gustajz.kubernetes.commons.CommandLineTable;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.custom.ContainerMetrics;
import io.kubernetes.client.custom.PodMetrics;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/** @author gustavojotz */
@RequiredArgsConstructor
@Slf4j
public class PodMetricsRunner implements Consumer<Metrics> {

    private final AppsV1Api appsV1Api = new AppsV1Api();

    private final String namespace;

    @SneakyThrows
    @Override
    public void accept(Metrics metrics) {

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
                "requestsMemory",
                "percentCpu",
                "percentMemory");

        QuantityFormatter formatter = new QuantityFormatter();

        podMetrics.forEach(
                o ->
                        st.addRow(
                                o.getContainerName(),
                                o.getPodName(),
                                formatter.apply(o.getUsageCpu()),
                                formatter.apply(o.getUsageMemory()),
                                formatter.apply(o.getLimitsCpu()),
                                formatter.apply(o.getLimitsMemory()),
                                formatter.apply(o.getRequestsCpu()),
                                formatter.apply(o.getRequestsMemory()),
                                o.percentOfRequests(),
                                o.getPercentOfMemoryRequests()));
        st.print();
    }
}
