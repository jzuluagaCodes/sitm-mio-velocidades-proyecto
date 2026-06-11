package edu.icesi.sitmmio.ice;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

/**
 * Levanta el servidor Worker ICE.
 *
 * Crea un Communicator, registra el objeto CalculadoraImpl en el
 * ObjectAdapter y espera peticiones del Master de forma indefinida.
 *
 * Corresponde al componente "Ice Runtime + ObjectAdapter + Calculadora"
 * dentro de cada nodo Worker del diagrama de deployment.
 *
 * Uso:
 *   java -cp <jar> edu.icesi.sitmmio.app.MainWorker --port 10000 --threads 4
 */
public final class WorkerServerIce {

    private WorkerServerIce() {}

    public static void start(int port, int threads) {
        String[] iceArgs = {
            "--Ice.MessageSizeMax=1048576"  // 128 MB máximo por mensaje ICE
        };

        try (Communicator communicator = Util.initialize(iceArgs)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "CalculadoraAdapter",
                    "tcp -h 0.0.0.0 -p " + port
            );

            CalculadoraImpl impl = new CalculadoraImpl(threads);
            adapter.add(impl, com.zeroc.Ice.Util.stringToIdentity("Calculadora"));
            adapter.activate();

            System.out.println("[Worker] Escuchando en puerto " + port
                    + " con " + threads + " hilo(s) internos.");
            communicator.waitForShutdown();
        }
    }
}
