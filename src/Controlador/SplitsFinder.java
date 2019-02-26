package Controlador;

import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SplitsFinder {

   int numberGatewaysXor = 1;
   int numberGatewaysAnd = 1;

   LinkedHashMap<String, LinkedList<String>> parallelRelations = new LinkedHashMap<String, LinkedList<String>>();

   LinkedHashMap<String, Integer> WFG;

   public SplitsFinder(BPMNModel BPMN, LinkedHashSet<Character> firsts, LinkedHashSet<Character> lasts, LinkedHashMap<String, Integer> wfg, LinkedHashMap<Character, LinkedList<Character>> parallelRelations) {
      BPMN.i = (Character) firsts.toArray()[0];
      BPMN.o = (Character) lasts.toArray()[0];
   
      WFG = wfg;
      //this.parallelRelations = parallelRelations;
      //Convert parallelRelations character to parallelRelations string
      for(Map.Entry<Character, LinkedList<Character>> entry : parallelRelations.entrySet()){
         LinkedList<String> list = new LinkedList<>();
         for(Character c : entry.getValue()){
            list.add(c.toString());
         }
         this.parallelRelations.put(entry.getKey().toString(), list);
      }
      //SEPTIMO, SE DETECTAN SPLITS 'AND, 'XOR'. ALGORITMOS 2, 3 Y 4 DEL PAPER
      System.out.println("\n\t1.Crear el Modelo BPMN");
      initProcess(BPMN);
   }

   public void initProcess(BPMNModel BPMN) {
   
           
      for (Character task : BPMN.T) {
         Algorithm2(BPMN, task);
      }
   
          
   }

   public void Algorithm2(BPMNModel BPMN, Character a) {
   
      System.out.println("\n\t-Iniciando ALGORITMO 2");
   
      HashSet<String> sucesores = Utils.successors(a.toString(), WFG);
   
      System.out.println("\n\t\t- TAREA = '" + a + "', SUCESORES:" + sucesores);
   
      //Dada la lista de sucesores, para cada uno, encontrar 
      //su 'futuro', esto es cuales de <los otros sucesores> mantienen una relación de concurrencia con el. 
      if(sucesores.size() < 2)
         return;
      
      Object[] S = sucesores.toArray();
      HashSet<String> future = null;
      HashSet<String> cover = null;
   
      LinkedList<String> concurrentTasks = new LinkedList<String>();
            
      LinkedHashMap<String, HashSet<String>> C = new LinkedHashMap<String, HashSet<String>>();
      LinkedHashMap<String, HashSet<String>> F = new LinkedHashMap<String, HashSet<String>>();
   
      //C = { [t, A] }, t in sucesores, A es subconjunto de los nodos   COVERTURA
      //F = { [t, B] }, t in sucesores, B es subconjunto de los nodos   FUTURO
      for (Object s1 : S) {        //procesa cada sucesor
         future = new HashSet<String>();  //vacio
         cover = new HashSet<String>();
         cover.add((String) s1);
         C.put((String) s1, cover);          //la misma tarea
         
         concurrentTasks = parallelRelations.get( s1.toString());
      
         if (concurrentTasks == null) {
            F.put((String) s1, future);   //futuro vacio
            continue;
         }
      
         for (String taskp : concurrentTasks) {// verifica que las tareas concurrentes esten en la lista de sucesores
            if (sucesores.contains(taskp))
               future.add(taskp);
         
         }
      
         F.put((String) s1, future);
      
         System.out.println("\n\t\t\t\tTareas concurrentes de '" + s1 + "': " + concurrentTasks);
         System.out.println("\t\t\t\tF["+ s1 + "]: " + future);
         System.out.println("\t\t\t\tC["+ s1 + "]: " + cover);
      
      }
   
      //Del conjunto de edges Em, en la llamada al Algoritmo 3 y 4, no considerar los edges a*
      //cuando finaliza el algoritmo 3 y 4, se vuelven a considerar para Em los a*
      //System.out.println("\n\n\t\t\tC: " + C); //{(t1, Ct1), (t2, Ct2), ...}
      //System.out.println("\t\t\tF: " + F); //{(t1, Ft1), (t2, Ft2), ...} 
      //BPMN = {
      //    T = set of tasks
      //    i = initial task
      //    o = final task
      //    G+ = and gateways
      //    G* = xor gateways
      //    G0 = or gateways
      //    Em = set of edges (a,b), a != o, b != i, a,b a task or a gateway
      //REMOVER LOS EDGES (a,x): a* 
      System.out.println("\n\n\t\t\tRemoviendo EDGES (" + a + ", *)");
      Utils.removerEdges(a, WFG);
      Utils.mostrarGrafo(3, WFG);
   
      System.out.println("\n\n\t\t\tC: " + C); //{(t1, Ct1), (t2, Ct2), ...}
      System.out.println("\t\t\tF: " + F); //{(t1, Ft1), (t2, Ft2), ...} 
      System.out.println("\t\t\tsucesores: " + sucesores);
      int i = 0;
      while (sucesores.size() > 1) {
         i++;
         System.out.println("\n\t\t\tITERACION " + i);
      
         System.out.println("\n\t\t\t-Iniciando ALGORITMO 3...");
         Algorithm3(BPMN, sucesores, C, F);
         System.out.println("\n\t\t\t-Fin de ALGORITMO 3...");
      
         System.out.println("\t\t\t-Iniciando ALGORITMO 4...");
         // System.out.println("HOLA");
         // System.out.println("\t\t-BPMN"+BPMN+"\t\t-sucesores"+ sucesores+"\t\tC"+C+"\t\tF"+ F);
         Algorithm4(BPMN, sucesores, C, F);
         System.out.println("\n\t\t\t-Fin de ALGORITMO 4...");
      }
   
      System.out.println("\n\t\tFIN de ALGORITMO 2 para TAREA = '" + a + "'. Sucesores MODIFICADOS K = " + sucesores);
      System.out.println("\n\t\t AGREGANDO Nuevos EDGES:");
   
      //agregar los nuevos edges con las task en sucesores
      String key = null;
      key = a + "," + (String) sucesores.toArray()[0];
      WFG.put(key, 1);
      
   
      System.out.println("\n\t\t Grafo resultante:");
   
      Utils.mostrarGrafo(3, WFG);
   
   }

   public void Algorithm3(BPMNModel BPMN, HashSet<String> sucesores, LinkedHashMap<String, HashSet<String>> C, LinkedHashMap<String, HashSet<String>> F) {
   
      HashSet<String> X = null;
      
      do {
         X = new HashSet<String>();
         HashSet<String> Fs = null;
         HashSet<String> Fk1 = null;
         HashSet<String> Fk2 = null;
      
         HashSet<String> Cu = null;
         HashSet<String> Ck2 = null;
      
         for (String k1 : sucesores) {
            Cu = C.get(k1);
            Fs = F.get(k1);
            Fk1 = F.get(k1);
            for (String k2 : sucesores) {
               /*if(k1 == k2) 
            continue;*/
               Fk2 = F.get(k2);
               //System.out.println("\tk1:\t"+k1+"\tk2:\t"+k2+"\tFk1:\t"+Fk1+"\tFk2:\t"+Fk2); //{(t1, Ct1), (t2, Ct2), ...}
            
               if ((Utils.igualesString(Fk1, Fk2)) && (!k1.equals(k2))) {
                  X.add(k2);
                  Ck2 = C.get(k2);
                  union(Cu, Ck2);
               }
            
            }
         
            if (X.size() > 0) {
               X.add(k1);
               break;
            }
         
         }
         if (X.size() > 0) {
            System.out.println("\n\t\t\t Nueva Compuerta XOR. VALOR DE X = " + X);
            
            String xor = "X"+numberGatewaysXor + "A";
            
            BPMN.Gxor.add(xor);
            BPMN.XORs.put(xor, X);
         
            //se agregan los nuevos edges al grafo, para todo x in X.
            String key = null;
            for (String task : X) {
               key = xor + "," + task;
               WFG.put(key, 1);
               //actualiza C y F
               C.put(task, new HashSet<String>());
               F.put(task, new HashSet<String>());
            
            }
         
            C.put(xor, Cu);
            F.put(xor, Fs);
         
            System.out.println("\n\t\t\t C[ " + xor + "]: " + Cu);
            System.out.println("\t\t\t F[ " + xor + "]: " + Fs);
         
            sucesores.add(xor);
            resta(sucesores, X);
            numberGatewaysXor++;
         }
      } while (X.size() > 0);
   
   }

   public void Algorithm4(BPMNModel BPMN, HashSet<String> sucesores, LinkedHashMap<String, HashSet<String>> C, LinkedHashMap<String, HashSet<String>> F) {
   
      HashSet<String> A = null;
      HashSet<String> Cu = null;
      HashSet<String> Fi = null;
   
      HashSet<String> CFk1 = null;
      HashSet<String> CFk2 = null;
   
      HashSet<String> Ck2 = null;
   
      do{
         A = new HashSet<String>();
         for (String k1 : sucesores) {
         
            Cu = C.get(k1);
            Fi = F.get(k1);
            CFk1 = new HashSet<String>();
            union(CFk1, Cu);
            union(CFk1, Fi);
         
            for (String k2 : sucesores) {
               if (k1.equals(k2) ) {
                  continue;
               }
               CFk2 = new HashSet<String>();
               union(CFk2, C.get(k2));
               union(CFk2, F.get(k2));
            
               if (Utils.igualesString(CFk1, CFk2)) {
               //System.out.println("\tk1:\t"+k1+"\tk2:\t"+k2+"\tCFk1:\t"+CFk1+"\tCFk2:\t"+CFk2); //{(t1, Ct1), (t2, Ct2), ...}
               
                  A.add(k2);
                  union(Cu, C.get(k2));
                  interseccion(Fi, F.get(k2));
               
               //System.out.println("\tA:\t"+A+"\tCu:\t"+Cu+"\tFi:\t"+Fi); //{(t1, Ct1), (t2, Ct2), ...}
               }
            
            }
            if (A.size() > 0) {
               A.add(k1);
            //System.out.println("A:"+A);
               break;
            }
         
         }
      
      //System.out.println("SIZE:"+A.size());
         if (A.size() > 0) {
            System.out.println("\n\t\t Nueva compuerta AND. VALOR DE A = " + A);
            String and = "A" + numberGatewaysAnd + "A";
            BPMN.Gand.add(and);
            BPMN.ANDs.put(and, A);
         
            String key = null;
            for (String task : A) {
               key = and + "," + task;
               WFG.put(key, 1);               
            }
         
            C.put(and, Cu);
            F.put(and, Fi);
         
            System.out.println("\n\t\t\t C[ " + and + "]: " + Cu);
            System.out.println("\t\t\t F[ " + and + "]: " + Fi);
         

         
            sucesores.add(and);
            resta(sucesores, A);
            numberGatewaysAnd++;
         }
      
      } while (A.size() > 0);
   
   }

   

   public void removerEdges(String edge) {
   
      List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
   
      for (Map.Entry<String, Integer> entry : edges) {
         String key = entry.getKey();
      
         if (key.equals(edge)) {
            WFG.remove(key);
         }
      }
   
   }

   

   public static void union(HashSet<String> dest, HashSet<String> src) {
      for (String task : src) {
         dest.add(task);
      }
   
   }

   public static void resta(HashSet<String> dest, HashSet<String> src) {
      for (String task : src) {
         dest.remove(task);
      }
   
   }

   public static void interseccion(HashSet<String> dest, HashSet<String> src) {
      HashSet<String> temp = new HashSet<String>();
   
      for (String task : dest) {
         temp.add(task);
      }
   
      dest.clear();
   
      for (String task : src) {
         if (temp.contains(task)) {
            dest.add(task);
         }
      }
   
   }
}