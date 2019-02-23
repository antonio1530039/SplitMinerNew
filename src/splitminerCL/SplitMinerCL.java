
package splitminerCL;

import Controlador.FilesManagement;
import Controlador.GenerarGrafo;
import Controlador.PostProcesarGrafo;
import Controlador.PreProcesarGrafo;
import Controlador.SplitsFinder;
import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.DefaultTableModel;


public class SplitMinerCL {
    public static void main(String[] args) {
        String filename = "";
        double epsilon = 0.0;
        double umbral = 0.0; //descarta edges con frecuencia menor a este umbral he manejado hasta 25
        if(args.length>0){
            epsilon = Double.parseDouble(args[0]);
            umbral = Double.parseDouble(args[1]);
            filename = args[2];
            System.out.println("Parametros ingresados:");
            System.out.println("\tEpsilon: " + epsilon + "\n\tPercentil: " + umbral + "\n\tArchivo log: " + filename);
        }else{
            epsilon = 0.3;
            umbral = 0.4;
            filename = "P1.txt";
            System.out.println("Sin parametros ingresados... parametros por defecto:");
            System.out.println("\tEpsilon: " + epsilon + "\n\tPercentil: " + umbral + "\n\tArchivo log: " + filename);
        }
        
       //P1.txt NOTATION: a AND{  XOR{  c, h}, b XOR{  e, g}} d f

        LinkedHashMap<Integer, ArrayList<Character>> tracesList; //lista de trazas
        LinkedHashMap<String, Integer> WFG = new LinkedHashMap<>(); //Grafo
        BPMNModel BPMN = new BPMNModel(); //Modelo BPMN

        FilesManagement f = new FilesManagement(BPMN);
        ///////
        System.out.println("PASO 1: LEER TRAZAS DEL ARCHIVO DE ENTRADA '" + filename + "' E IDENTIFICAR TAREAS.");

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
            System.out.println("El archivo '" + filename + "' no se puede abrir. Exit");
            return;
        }

        System.out.println("\n");
        System.out.println("PASO2: INCIANDO LA CONSTRUCCION DEL GRAFO QUE MODELA EL CONJUNTO DE TRAZAS.\n");

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
