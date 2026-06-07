package edu.icesi.sitmmio.distributed;

import edu.icesi.sitmmio.concurrent.AggregationTask;
import edu.icesi.sitmmio.domain.AggregationResult;
import edu.icesi.sitmmio.domain.RouteMonthKey;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public final class WorkerServer {
    private final int port;

    public WorkerServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Worker escuchando en puerto " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(() -> handle(socket));
                thread.start();
            }
        }
    }

    private void handle(Socket socket) {
        try (Socket closeableSocket = socket;
             ObjectInputStream input = new ObjectInputStream(closeableSocket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(closeableSocket.getOutputStream())) {
            Object object = input.readObject();
            if (!(object instanceof WorkRequest)) {
                throw new IllegalArgumentException("Solicitud inválida para worker");
            }
            WorkRequest request = (WorkRequest) object;
            AggregationTask task = new AggregationTask(request.getRows(), request.getActiveRoutes());
            Map<RouteMonthKey, AggregationResult> result = task.call();
            output.writeObject(new WorkResponse(result));
            output.flush();
        } catch (Exception exception) {
            System.err.println("Error atendiendo solicitud: " + exception.getMessage());
        }
    }
}
