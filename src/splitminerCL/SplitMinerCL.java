package splitminerCL;

import Controlador.FilesManagement;
import Controlador.GenerarGrafo;
import Controlador.PostProcesarGrafo;
import Controlador.PreProcesarGrafo;
import Controlador.RepairOutliers;
import Controlador.SplitsFinder;
import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class SplitMinerCL {

    public static void main(String[] args) {
        String filename = "P1.txt";
        double epsilon = 0.3;
        double umbral = 0.4;

        System.out.println("\nxMiner 1.0 - ...");

        if (args.length > 0) {
            try {
                epsilon = Double.parseDouble(args[0]);
                umbral = Double.parseDouble(args[1]);
                filename = args[2];
            } catch (Exception e) {
                System.out.println("\tDebe pasar como argumentos el valor de epsilon, percentile y nobre de archivo.");
                return;
            }
        }

        System.out.println("\tEpsilon: '" + epsilon + "'\n\tPercentil: '" + umbral + "'\n\tArchivo log: '" + filename + "'\n");

        System.out.println("PASO 1: LEER TRAZAS DEL ARCHIVO DE ENTRADA '" + filename + "' E IDENTIFICAR TAREAS.");

        BPMNModel BPMN = new BPMNModel();
        FilesManagement f = new FilesManagement(BPMN);
        LinkedHashMap<Integer, ArrayList<Character>> tracesList = null; //lista de trazas

        try {
            if (filename.endsWith(".txt")) {
                tracesList = f.readDataInputTrazas(filename);
            } else if (filename.endsWith(".csv")) {
                tracesList = f.readDataInput(filename);
            } else {
                System.out.println("El tipo de archivo de entrada no es valido...EXIT");
                return;
            }
        } catch (Exception e) {
            System.out.println("El archivo '" + filename + "' no se puede abrir. EXIT");
            return;
        }

        ///////////
        System.out.println("\n\n\tFILTRADO...");

        RepairOutliers r = new RepairOutliers();

        r.Filtering(tracesList, 1, 1, 1, 0.25);

        System.out.println("");
        System.out.println("");
        System.out.println("\t4. Mostrando TRAZAS DESPUES DEL FILTRADO ");
        for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
            System.out.println("\t\t" + entry.getKey() + " - " + entry.getValue());
        }

        if (true) {
            return;
        }

        ///////
        System.out.println("\n");
        System.out.println("PASO2: INCIANDO LA CONSTRUCCION DEL GRAFO QUE MODELA EL CONJUNTO DE TRAZAS.\n");

        LinkedHashMap<String, Integer> WFG = new LinkedHashMap<>();

        GenerarGrafo generarGrafo = new GenerarGrafo();
        generarGrafo.computeGraph(tracesList, WFG);

        System.out.println("\nPASO 3: PREPROCESAMIENTO DEL GRAFO");

        PreProcesarGrafo preprocesarGrafo = new PreProcesarGrafo(BPMN, WFG, tracesList, generarGrafo.firsts, generarGrafo.lasts, umbral, epsilon);

        /////////
        System.out.println("\nPASO 4: CONSTRUCCION DEL MODELO BPMN");

        SplitsFinder crearModelo = new SplitsFinder(BPMN, generarGrafo.firsts, generarGrafo.lasts, WFG, preprocesarGrafo.parallelRelations);

        /////////
        System.out.println("\nPASO 5: POST-PROCESAMIENTO");

        //g1.postProcesamiento(BPMN);
        PostProcesarGrafo postprocesamiento = new PostProcesarGrafo(BPMN, WFG, preprocesarGrafo.autoLoops);

        System.out.println("Notacion al final: " + postprocesamiento.notation);

    }
}
