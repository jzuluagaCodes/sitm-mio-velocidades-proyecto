package edu.icesi.sitmmio.app;

import java.nio.file.Path;

public final class ExecutionArguments {
    private final Path linesPath;
    private final Path datagramsPath;
    private final Path outputPath;
    private final int threads;

    private ExecutionArguments(Path linesPath, Path datagramsPath, Path outputPath, int threads) {
        this.linesPath = linesPath;
        this.datagramsPath = datagramsPath;
        this.outputPath = outputPath;
        this.threads = threads;
    }

    public static ExecutionArguments parse(String[] args) {
        Path lines = Path.of("/opt/sitm-mio/lines-241-ActiveGT.csv");
        Path datagrams = Path.of("/opt/sitm-mio/datagrams-MiniPilot.csv");
        Path output = Path.of("output/velocidades-promedio.csv");
        int threads = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--lines":
                    lines = Path.of(requireValue(args, ++i, "--lines"));
                    break;
                case "--datagrams":
                    datagrams = Path.of(requireValue(args, ++i, "--datagrams"));
                    break;
                case "--output":
                    output = Path.of(requireValue(args, ++i, "--output"));
                    break;
                case "--threads":
                    threads = Integer.parseInt(requireValue(args, ++i, "--threads"));
                    break;
                default:
                    System.err.println("Advertencia: argumento no reconocido ignorado: " + args[i]);
                    break;
            }
        }
        return new ExecutionArguments(lines, datagrams, output, threads);
    }

    private static String requireValue(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Falta el valor para " + option);
        }
        return args[index];
    }

    public Path getLinesPath() {
        return linesPath;
    }

    public Path getDatagramsPath() {
        return datagramsPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public int getThreads() {
        return threads;
    }
}
