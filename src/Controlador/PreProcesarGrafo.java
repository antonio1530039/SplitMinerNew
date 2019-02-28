package Controlador;

import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class PreProcesarGrafo {
    
   public LinkedHashMap<String, Integer> WFG;
   public LinkedHashMap<Character, LinkedList<Character>> parallelRelations = new LinkedHashMap<Character, LinkedList<Character>>();
   public List<Character> autoLoops = new LinkedList<Character>();

   public PreProcesarGrafo(BPMNModel BPMN, LinkedHashMap<String, Integer> wfg, LinkedHashMap tracesList, LinkedHashSet<Character> firsts, LinkedHashSet<Character> lasts, double percentile, double epsilon) {
        
      WFG = wfg;
      //double epsilon = 0.3;
      
        
        //SEGUNDO se buscan y eliminan los auloLoops: (a,a) en el grafo
      System.out.println("\n\t1.Removiendo AUTOLOOPS y SHORTLOOPS del grafo actual...");
   
      int numLoops = removeAutoLoops(BPMN.T);
   
        //TERCERO se buscan y eliminan los shortLoops: (a,x), (x,a) en el grafo, para una misma traza
      int shortLoops = removeShortLoops(tracesList);
   
        //CUARTO: CALCULAR LAS RELACIONES DE PARALELISMO
      System.out.println("\n\t2.Identificar y remover RELACIONES de PARALELISMO...");
   
      identifyParallelRelations(epsilon);
   
      System.out.println("\t Grafo actual con las modificaciones previas (AUTOLOOP, SHORTLOOP, PARALLEL RELATIONS):");
      Utils.mostrarGrafo(2, WFG);
     
        
        //NUEVOS CAMBIOS: FILTERING      
      System.out.println("\n\t3.FILTERING: percentil = '" + percentile + "'.");
      Filtering f = new Filtering(BPMN, percentile, wfg, firsts, lasts);
        
        //System.exit(0);
   }
    
    
     /*
  Identifica todos los nodos con AUTOLOOP, y remueve los edges del grafo
  
    */
   public int removeAutoLoops(LinkedList<Character> activityList) {
      //para cada actividad 'a', buscar si existe 'a'_'a' en el grafo y quitarlo
      int tam = activityList.size();
      Character current;
      String edge;
   
      for (Character task : activityList) {
         edge = task + "," + task;
         if (WFG.containsKey(edge)) {
            WFG.remove(edge);
            autoLoops.add(task);
            System.out.println("\t\t... Removiendo autolop (" + edge + ")");
         
         }
      }
   
      int autoloops = autoLoops.size();
   
      System.out.println("\n\t\tSe removieron '" + autoloops + "' AUTOLOOPs.\n");
      return autoloops;
   
   }

   /* 
    Identifica y remueve los shortLoops en el grafo. Asume que no hay autoloops
    */
   public int removeShortLoops(LinkedHashMap tracesList) {
   
      //para cada actividad 'a', buscar si existe 'a'_'x' y 'x'_'a' en el Grafo, siempre que a,x,a existe en una traza en la lista
      int shortloops = 0;
   
      String entryKey = null;
      Character first, second, third;
      ArrayList<Character> traza = null;
   
      for (int i = 0; i < tracesList.size(); i++) {
         //procesa cada una de las trazas
         traza = (ArrayList<Character>) tracesList.values().toArray()[i];
      
         for (int j = 0; j < traza.size() - 2; j++) {
            first = traza.get(j);
            second = traza.get(j + 1);
            third = traza.get(j + 2);
         
            if ((first == third) && (first != second)) {
               WFG.remove(first + "," + second);
               WFG.remove(second + "," + third);
               System.out.println("\t\t... Removiendo shorloop (" + first + "," + second + "," + first + ")");
               shortloops++;
            }
         
         }
      
      }
   
      System.out.println("\n\t\tSe removieron '" + shortloops + "' SHORTLOOPs.\n");
      return shortloops;
   }
   
   
   
   public int identifyParallelRelations(double epsilon) {
      //busca edges de la forma a,b y b,a, que cumplan con la relación: |a -> b| - |b -> a|
      //                                                                -------------------  < e
      //                                                                |a -> b| + |b -> a| 
   
      // recorre marca todos los casos (a,b) (b,a) en el grafo
      int numParallelRelations = 0;
   
      List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
      String edge = null;
      int freq1 = 0;
      int freq2 = 0;
   
      System.out.println("\n\t\t E = " + epsilon);
               
               
      for(Map.Entry<String, Integer> entry : edges) {
         edge = entry.getKey();
         freq1 = entry.getValue();
      
         String vals[] = edge.split(",");
         Character activity1 = vals[0].charAt(0);
         Character activity2 = vals[1].charAt(0);
      
         String edgeParalel = activity2 + "," + activity1;
      
         if (!WFG.containsKey(edgeParalel))               
            continue;
         
         freq2 = WFG.get(edgeParalel);
         
         double div = (double) (freq1 - freq2) / (double) (freq1 + freq2);
         div = Math.abs(div);
         System.out.println("\t\t(" + edge + ") y (" + edgeParalel + ") detectados. Div = " + div);
            
         if ( div < epsilon) {
            System.out.println("\t\t\tRemovidos...");
            WFG.remove(edgeParalel);
            WFG.remove(edge);
               
            LinkedList<Character> values = null;
            if (parallelRelations.containsKey(activity1)) {
               values = parallelRelations.get(activity1);
            } 
            else {
               values = new LinkedList<Character>();
            }
            values.add(activity2);
            parallelRelations.put(activity1, values);
            
               //al reves tambien aplica
            if (parallelRelations.containsKey(activity2)) {
               values = parallelRelations.get(activity2);
            } 
            else {
               values = new LinkedList<Character>();
            }
            values.add(activity1);
            parallelRelations.put(activity2, values);
            
            numParallelRelations += 2;
                
         }            
      }
   
      System.out.println("\n\t\t'" + numParallelRelations + "' relaciones paralelas encontradas Y removidas: " + parallelRelations);
   
      return numParallelRelations;
   
   }




}