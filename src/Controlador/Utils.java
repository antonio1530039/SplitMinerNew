package Controlador;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class Utils {
    public static void mostrarGrafo(int numTabs, LinkedHashMap<String, Integer> WFG) {
      
      Map<String, Integer> treeMap = new TreeMap<String, Integer>(WFG);
        
      System.out.println("\n\t\tGrafo con '" + treeMap.size() + "' ARCOS.\n");
             
      for (Map.Entry<String, Integer> entry : treeMap.entrySet()) {
         for (int i = 0; i < numTabs; i++) {
            System.out.print("\t");
         }
         System.out.println("[" + entry.getKey() + "]" + " - " + entry.getValue());
      }
   }
    
    //all nodes following 'task', given the current pruened WFG
   public static HashSet<Character> successors(Character task, LinkedHashMap<String, Integer> WFG) {
   
      HashSet<Character> sucesores = new LinkedHashSet<Character>();
   
      for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
         String key = entry.getKey();
         String vals[] = key.split(",");
         Character c = vals[0].charAt(0);
      
         if (task == c) {
            sucesores.add(vals[1].charAt(0));
         }
      
      }
   
      return sucesores;
   }
   
   //all nodes following 'task', given the current pruened WFG
   public static HashSet<String> successors(String task, LinkedHashMap<String, Integer> WFG) {
   
      HashSet<String> sucesores = new LinkedHashSet<String>();
   
      for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
         String key = entry.getKey();
         String vals[] = key.split(",");
         String c = vals[0];
      
         if (task.equals(c) ) {
            sucesores.add(vals[1]);
         }
      
      }
   
      return sucesores;
   }
   
   //all nodes following 'task', given the current pruened WFG
    public static HashSet<String> sucesoresOAntecesores(String target, Character type, LinkedHashMap<String, Integer> WFG) { //types: s, a

        HashSet<String> antecesores = new LinkedHashSet<String>();

        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            if (type == 'a') {
                if (target.equals(vals[1])) {
                    antecesores.add(vals[0]);
                }
            } else {
                if (target.equals(vals[0])) {
                    antecesores.add(vals[1]);
                }
            }

        }

        return antecesores;
    }
   
  
   
   
   public static boolean iguales(HashSet<Character> first, HashSet<Character> second) {

        if (first.size() != second.size()) {
            return false;
        }

        for (Character task : second) {
            if (!first.contains(task)) {
                return false;

            }
        }

        return true;
    }
   
   
   public static boolean igualesString(HashSet<String> first, HashSet<String> second) {

        if (first.size() != second.size()) {
            return false;
        }

        for (String task : second) {
            if (!first.contains(task)) {
                return false;

            }
        }

        return true;
    }
   
   
   //remueve edges de la forma a*    
    public static void removerEdges(Character a, LinkedHashMap<String, Integer> WFG) {

        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());

        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            Character c = vals[0].charAt(0);

            if (a == c) {
                WFG.remove(key);
            }
        }

    }
    
    //remueve edges de la forma a*    
    public static void removerEdges(String a, LinkedHashMap<String, Integer> WFG) {

        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());

        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            String c = vals[0];

            if (a.equals(c) ) {
                WFG.remove(key);
            }
        }

    }
    
     //reemplaza 'a' por 'b' en todos los edges *a    
   public static void remplazarEdges(Character a, Character b, LinkedHashMap<String, Integer> WFG) {
   
      List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
   
      for (Map.Entry<String, Integer> entry : edges) {
         String key = entry.getKey();
         String vals[] = key.split(",");
         Character c0 = vals[0].charAt(0);
         Character c1 = vals[1].charAt(0);
         if (a == c1) {
            WFG.remove(key);
            key = c0 + "," + b;
            WFG.put(key, 1);
         }
      }
   
   }
   
     //reemplaza 'a' por 'b' en todos los edges *a    
   public static void remplazarEdges(String a, String b, LinkedHashMap<String, Integer> WFG) {
   
      List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
   
      for (Map.Entry<String, Integer> entry : edges) {
         String key = entry.getKey();
         String vals[] = key.split(",");
         String c0 = vals[0];
         String c1 = vals[1];
         if (a.equals(c1) ) {
            WFG.remove(key);
            key = c0 + "," + b;
            WFG.put(key, 1);
         }
      }
   
   }
}