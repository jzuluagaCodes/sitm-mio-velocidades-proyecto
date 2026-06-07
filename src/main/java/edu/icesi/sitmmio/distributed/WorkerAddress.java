package edu.icesi.sitmmio.distributed;

public final class WorkerAddress {
    private final String host;
    private final int port;

    public WorkerAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static WorkerAddress parse(String value) {
        String[] parts = value.split(":");
        if (parts.length != 2) throw new IllegalArgumentException("Worker inválido. Use host:puerto");
        return new WorkerAddress(parts[0], Integer.parseInt(parts[1]));
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
