package com.github.gustajz.kubernetesmetriccl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

/** @author gustavojotz */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsCommandLineRunner implements CommandLineRunner, ExitCodeGenerator {

    private final CommandLine.IFactory factory;

    private final MetricsRunner metricsRunner;

    private int exitCode;

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(metricsRunner, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
