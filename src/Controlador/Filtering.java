package Controlador;

import Modelo.BPMNModel;
import Modelo.Edge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Filtering {

   LinkedHashMap<String, Integer> WFG;
   public LinkedHashSet<Character> Firsts = new LinkedHashSet<Character>();
   public LinkedHashSet<Character> Lasts = new LinkedHashSet<Character>();

   public Filtering(BPMNModel BPMN, double percentile, LinkedHashMap<String, Integer> wfg, LinkedHashSet<Character> firsts, LinkedHashSet<Character> lasts) {
      WFG = wfg;
      Firsts = firsts;
      Lasts = lasts;
      
      int removed = filtering(BPMN, percentile);
      System.out.println("\n\t\t" + removed + " edges fueron removidos.");
   
      System.out.println("\t Grafo actual (filtering):");
      Utils.mostrarGrafo(2, WFG);
   
   }

   public int filtering(BPMNModel BPMN, double percentile) {
   
      //REGRESA UN GRAFO MODIFICADO
      int freq1 = 0;
      int removed = 0;
      int inf = 100000;
   
      BPMN.i = Firsts.toArray(new Character[0])[0];//findSources(BPMN);
      BPMN.o = Lasts.toArray(new Character[0])[0];//findSinks(BPMN);
   
      System.out.println("\n\t\tTarea inicial: " + BPMN.i + ".\n\t\tTarea final: " + BPMN.o + "\n");
      Map<Character, Integer> Cf = new HashMap<Character, Integer>();
      Map<Character, Integer> Cb = new HashMap<Character, Integer>();
   
      Set<Integer> F = new TreeSet<Integer>();
      
      Cf.put(BPMN.i, inf);
      Cb.put(BPMN.i, 0);
      
      Cf.put(BPMN.o, 0);
      Cb.put(BPMN.o, inf);
   
      int fi,fo;
   
      for (Character t : BPMN.T) {
         fi = -1;
         fo = -1;
         if (!t.equals(BPMN.i)){
            Cf.put(t, 0);
            fi = maxFreqIncomingEdges(t);
            F.add(fi);
         }
      
         if (!t.equals(BPMN.o)){
            Cb.put(t, 0);
            fo = maxFreqOutgoingEdges(t);
            F.add(fo);
         }
      
          
         System.out.println("\t\t" + t + ": maxIn = " + fi + "; maxOut = " + fo);
      }
   
      System.out.println("\n\t\tF = " + F);
   
      int fth = calcularNpercentile(F, percentile);
      System.out.println("\n\t\tft = " + fth);
   
      Map<Character, String> Ei = new HashMap<Character, String>();
      Map<Character, String> Eo = new HashMap<Character, String>();
   
      discoverBestIncomingEdges(BPMN, Cf, Ei);
      discoverBestOutgoingEdges(BPMN, Cb, Eo);
   
      System.out.println("\n\t\tBest incoming edges: " + Ei);
      System.out.println("\t\tBest outgoing edges: " + Eo);
      //se queda con los arcos de Ep que estan o en Ei o en Eo, y que cumplan fe > fth
      List<Map.Entry<String, Integer>> edgesMain = new ArrayList(WFG.entrySet()); //los edges del grafo general
   
      LinkedList<String> endgesInEi = new LinkedList<String>(Ei.values()); //los edges detectados en Ei y en Eo
      LinkedList<String> endgesInEo = new LinkedList<String>(Eo.values());
   
      List<String> filteredEdges = new LinkedList<String>(); //la lista filtrada de edges que permaneceran despues de hacer el filtrado
   
      for (Map.Entry<String, Integer> entry : edgesMain) {
         String key = entry.getKey();
         int fe = entry.getValue();
         //la negación permite eliminar ese key de WFG, ya que la condición se cumpliria para dejar ese arco en el grafo filtrado
         //la condicion es que el arco este en Ei o en Eo, o que tenga freq > valor nPercentile
         if (!(endgesInEi.contains(key) || endgesInEo.contains(key) || fe > fth)) {
            WFG.remove(key);
            //System.out.println("REMOVER************" + key);
            removed++;
         }
      }
   
      return removed;
   
   }

   //revisa todos los arcos *t para determinar cual de los arcos de entrada tiene la freq más grande
   private int maxFreqIncomingEdges(Character t) {
      List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
   
      int maxFreq = 0;
      int freqIn = 0;
   
      for (Map.Entry<String, Integer> entry : edges) {
         String key = entry.getKey();
         String vals[] = key.split(",");
         Character c0 = vals[0].charAt(0);
         Character c1 = vals[1].charAt(0);
         if (t == c1) {
            freqIn = entry.getValue();
            if (freqIn > maxFreq) {
               maxFreq = freqIn;
            }
         }
      }
   
      return maxFreq;
   }

   //revisa todos los arcos t* para determinar cual de los arcos de salida tiene la freq más grande
   private int maxFreqOutgoingEdges(Character t) {
      List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
   
      int maxFreq = 0;
      int freqOut = 0;
   
      for (Map.Entry<String, Integer> entry : edges) {
         String key = entry.getKey();
         String vals[] = key.split(",");
         Character c0 = vals[0].charAt(0);
         Character c1 = vals[1].charAt(0);
         if (t == c0) {
            freqOut = entry.getValue();
            if (freqOut > maxFreq) {
               maxFreq = freqOut;
            }
         }
      }
   
      return maxFreq;
   }

   private int calcularNpercentile(Set F, double percentile) {
   /*
      int n = F.size();
      Object[] array = F.toArray();
   
      double val = n * percentile;
      double techo = Math.ceil(val);
      double prom = 0.0;
   
      if (val < techo) {
          return (Integer) array[(int) techo - 1];
      } else {
   
          if (techo != 0) {
              prom = ((Integer) array[(int) techo - 1] + (Integer) array[(int) techo]) / 2.0;
          }
   
          return (int) prom;
   
      }
      */
      int n = F.size();
      Object[] array = F.toArray();
   
      double val = n * percentile;
      double techo = Math.ceil(val);
      return (Integer) array[(int) techo - 1];
   
   
   }

   private Character findSinks(BPMNModel BPMN){
      Set<Character> sinks = new TreeSet<Character>();
   
      for (Character t : BPMN.T) {
         List<Edge> l = obtenerPout(t);
         System.out.println("... " + t + "-" + l);
      
         if(l.size() == 0)
            sinks.add(t);
      }
      System.out.println("\n\t\tSinks = " + sinks);
      if(sinks.size() > 0){
         Character[] res = sinks.toArray(new Character[0]);
         return res[0];
      }
      else
         return 'g';
   
   }
   
   private Character findSources(BPMNModel BPMN){
      Set<Character> sources = new TreeSet<Character>();
   
      for (Character t : BPMN.T) {
         List<Edge> l = obtenerPin(t);
         
         System.out.println("... " + t + "-" + l);
      
         if(l.size() == 0)
            sources.add(t);
      }
      System.out.println("\n\t\tsources = " + sources);
      if(sources.size() > 0){
         Character[] res = sources.toArray(new Character[0]);
         return res[0];
      }
      else
         return 'a';
   }


   private void discoverBestIncomingEdges(BPMNModel BPMN, Map<Character, Integer> Cf, Map<Character, String> Ei) {
   
      LinkedList<Character> queue = new LinkedList<Character>();
      Set<Character> U = new TreeSet<Character>();
   
      for (Character t : BPMN.T) {
         if (t != BPMN.i) {
            U.add(t);
         }
      }//U = T - {i}
   
      System.out.println("\n\t\tRealizando exploracion hacia adelante iniciando en '" + BPMN.i + "'\n\t\t\tMapa Cf " + Cf);
      queue.add(BPMN.i);
   
      Character p = ' ';
      Character n = ' ';
      int fe = 0;
      int Cmax;
   
      while (queue.size() != 0) {
         p = queue.get(0);
         queue.remove(0);
                     
         System.out.println("\t\t\tNext = '" + p + "'.   Queue = " + queue);
         
         List<Edge> l = obtenerPout(p);
            
         for (Edge e : l) {
            n = e.tarjet;
            fe = e.freq;
            Cmax = Math.min(Cf.get(p),fe);
               
            if (Cmax > Cf.get(n)) {
               
               if (Cf.containsKey(n))
                  System.out.println("\t\t\t\t\tUpdate Cf[" + n + "].");
               Cf.put(n, Cmax);
               
               if (Ei.containsKey(n))
                  System.out.println("\t\t\t\t\tUpdate Ei[" + n + "].");
               Ei.put(n, p + "," + n);
               //se ha coambiado macCapacity de n, si ya no esta en U y ya se exploro previamente, volverlo a gregar.
               if (!(queue.contains(n) && !U.contains(n))) {
                  U.add(n);
               }
            }
         
            if (U.contains(n)) {   //si este sucesor no se ha considerado para explorar, agregarlo a la cola
               U.remove(n);
               queue.add(n);
            }
            
         }
         
         System.out.println("\t\t\t\tCf = " + Cf);
         System.out.println("\t\t\t\tEi = " + Ei);
         System.out.println("\t\t\t\tU = " + U);
      
      }
   }

   private void discoverBestOutgoingEdges(BPMNModel BPMN, Map<Character, Integer> Cb, Map<Character, String> Eo) {
   
      LinkedList<Character> queue = new LinkedList<Character>();
      Set<Character> U = new TreeSet<Character>();
   
      for (Character t : BPMN.T) {
         if (t != BPMN.o) {
            U.add(t);
         }
      }
   
      queue.add(BPMN.o);
      Character p = ' ';
      Character n = ' ';
      int fe = 0;
      System.out.println("\n\t\tRealizando exploracion hacia atras iniciando en '" + BPMN.o + "'\n\t\t\tMapa Cb " + Cb);
      
      int Cmax;
      while (queue.size() != 0) {
         n = queue.get(0);
         queue.remove(0);
      
         System.out.println("\t\t\tNext = '" + n + "'.   Queue = " + queue);
         
         //calcular *n
         List<Edge> l = obtenerPin(n);
      
         for (Edge e : l) {
            p = e.tarjet;
            fe = e.freq;
         
            Cmax = Math.min(Cb.get(n),fe);
            
            if (Cmax > Cb.get(p)) {
            
               if (Cb.containsKey(p))
                  System.out.println("\t\t\t\t\tUpdate Cb[" + p + "].");
               Cb.put(p, Cmax);
               
               if (Eo.containsKey(p))
                  System.out.println("\t\t\t\t\tUpdate Eo[" + p + "].");
               Eo.put(p, p + "," + n);
               
               if (!(queue.contains(p) && !U.contains(p))) {
                  U.add(p);
               }
            }
         
            if (U.contains(p)) {
               U.remove(p);
               queue.add(p);
            }
         }
         
         System.out.println("\t\t\t\tCb = " + Cb);
         System.out.println("\t\t\t\tEo = " + Eo);
         System.out.println("\t\t\t\tU = " + U);
      
      }
   }

   private List<Edge> obtenerPout(Character p) {
      List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
      List<Edge> l = new LinkedList<Edge>();
   
      for (Map.Entry<String, Integer> entry : edges) {
         String key = entry.getKey();
         String vals[] = key.split(",");
         Character c0 = vals[0].charAt(0);
         Character c1 = vals[1].charAt(0);
         if (p == c0) {
            l.add(new Edge(c1, entry.getValue()));
         }
      }
   
      return l;
   }

   private List<Edge> obtenerPin(Character n) {
      List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
      List<Edge> l = new LinkedList<Edge>();
   
      for (Map.Entry<String, Integer> entry : edges) {
         String key = entry.getKey();
         String vals[] = key.split(",");
         Character c0 = vals[0].charAt(0);
         Character c1 = vals[1].charAt(0);
         if (n == c1) {
            l.add(new Edge(c0, entry.getValue()));
         }
      }
   
      return l;
   }
}