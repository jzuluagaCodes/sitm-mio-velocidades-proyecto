package edu.icesi.sitmmio.app;

import edu.icesi.sitmmio.distributed.WorkerServer;

public final class MainDistributedWorker {
    private MainDistributedWorker() { }

    public static void main(String[] args) throws Exception {
        int port = 9090;
        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            }
        }
        new WorkerServer(port).start();
    }
}
