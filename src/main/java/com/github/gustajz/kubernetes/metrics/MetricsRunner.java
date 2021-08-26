package com.github.gustajz.kubernetes.metrics;

import com.github.gustajz.kubernetes.commons.ConsoleUtils;
import io.kubernetes.client.Metrics;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
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
        ConsoleUtils.clean();

        ApiClient client = Config.defaultClient();

        Configuration.setDefaultApiClient(client);

        final Metrics metrics = new Metrics(client);

        var nodeMetricsRunner = new NodeMetricsRunner();

        if (showNodes) {
            nodeMetricsRunner.accept(metrics);
        }

        var podMetricsRunner = new PodMetricsRunner(namespace);

        if (showPods) {
            podMetricsRunner.accept(metrics);
        }

        if (!showPods && !showNodes) {
            nodeMetricsRunner.accept(metrics);
            podMetricsRunner.accept(metrics);
        }

        return 0;
    }
}
