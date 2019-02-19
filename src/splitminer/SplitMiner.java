/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitminer;

import Controlador.*;
import Vista.gBuildGraphicModel;
import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author cti
 */
public class SplitMiner {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*//P1.txt NOTATION: a AND{  XOR{  c, h}, b XOR{  e, g}} d f
        
        
        final String filename = "P1.txt";
        double umbral = 0.4; //descarta edges con frecuencia menor a este umbral he manejado hasta 25
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

        System.out.println("\t3. Mostrando TRAZAS IDENTIFICADAS  en el archivo '" + filename + "'.");
        for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
            System.out.println("\t\t" + entry.getKey() + " - " + entry.getValue());
        }

        
        
        ///////
        System.out.println("\n");
        System.out.println("PASO2: INCIANDO LA CONSTRUCCION DEL GRAFO QUE MODELA EL CONJUNTO DE TRAZAS.\n");

        GenerarGrafo generarGrafo = new GenerarGrafo();

        generarGrafo.computeGraph(tracesList, WFG);
 
        
        ///////
        System.out.println("\nPASO 3: PREPROCESAMIENTO DEL GRAFO");

        PreProcesarGrafo preprocesarGrafo = new PreProcesarGrafo(BPMN, WFG, tracesList);

        
        
        Filtering filtering = new Filtering(BPMN, umbral, WFG, generarGrafo.firsts, generarGrafo.lasts);

        
        /////////
        System.out.println("\nPASO 4: CONSTRUCCION DEL MODELO BPMN");

        SplitsFinder crearModelo = new SplitsFinder(BPMN, generarGrafo.firsts, generarGrafo.lasts, WFG, preprocesarGrafo.parallelRelations);
        
        /////////

        System.out.println("\nPASO 5: POST-PROCESAMIENTO");
      
        //g1.postProcesamiento(BPMN);
        
        PostProcesarGrafo postprocesamiento = new PostProcesarGrafo(BPMN, WFG, preprocesarGrafo.autoLoops);
        
        ProcessViewer processViewer = new ProcessViewer();
        
        System.out.println("Notacion al final: " + postprocesamiento.notation);*/
        
        ProcessViewer processViewer = new ProcessViewer();

    }
    
}
