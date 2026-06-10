package edu.icesi.sitmmio.app;

import edu.icesi.sitmmio.ice.WorkerServerIce;

/**
 * Punto de entrada del nodo Worker.
 *
 * Argumentos:
 *   --port  <número>   Puerto TCP donde escucha este Worker  (default: 10000)
 *   --threads <número> Hilos internos del ThreadPool del Worker (default: núcleos disponibles)
 *
 * Ejemplo:
 *   java -cp sitm-mio-distribuido-ice-1.0.0.jar edu.icesi.sitmmio.app.MainWorker --port 10001 --threads 4
 */
public final class MainWorker {

    public static void main(String[] args) {
        int port    = 10000;
        int threads = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port":    port    = Integer.parseInt(args[++i]); break;
                case "--threads": threads = Integer.parseInt(args[++i]); break;
                default:
                    System.err.println("[Worker] Argumento no reconocido: " + args[i]);
            }
        }

        System.out.println("[Worker] Iniciando en puerto " + port + " con " + threads + " hilo(s).");
        WorkerServerIce.start(port, threads);
    }
}
