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

        BPMN.i = (Character) Firsts.toArray()[0];
        BPMN.o = (Character) Lasts.toArray()[0];

        Map<Character, Integer> Cf = new HashMap<Character, Integer>();
        Map<Character, Integer> Cb = new HashMap<Character, Integer>();

        Set<Integer> F = new TreeSet<Integer>();

        Cf.put(BPMN.i, inf);
        Cb.put(BPMN.o, inf);

        Cf.put(BPMN.o, 0);
        Cb.put(BPMN.i, 0);

        for (Character t : BPMN.T) {
            if (t.equals(BPMN.i) || t.equals(BPMN.o)) //procesa todas las tasks, excepto la tarea inicial y final
            {
                continue;
            }

            Cf.put(t, 0);
            Cb.put(t, 0);

            //para la tarea t, calcular la más alta frecuencia de sus incoming edges y las más alta de sus outgoing edges
            int fi = maxFreqIncomingEdges(t);
            int fo = maxFreqOutgoingEdges(t);
            F.add(fi);
            F.add(fo);
            System.out.println(t + ": maxIn = " + fi + "; maxOut = " + fo);
        }

        System.out.println("F = " + F);

        int fth = calcularNpercentile(F, percentile);
        System.out.println("   ft = " + fth);

        Map<Character, String> Ei = new HashMap<Character, String>();
        Map<Character, String> Eo = new HashMap<Character, String>();

        discoverBestIncomingEdges(BPMN, Cf, Ei);
        discoverBestOutgoingEdges(BPMN, Cb, Eo);

        //se queda con los arcos de Ep que estan o en Ei o en Eo, y que cumplan fe > fth
        List<Map.Entry<String, Integer>> edgesMain = new ArrayList(WFG.entrySet()); //los edges del grafo general

        LinkedList<String> endgesInEi = new LinkedList<String>(Ei.values()); //los edges detectados en Ei y en Eo
        LinkedList<String> endgesInEo = new LinkedList<String>(Eo.values());

        List<String> filteredEdges = new LinkedList<String>(); //la lista filtrada de edges que permaneceran despues de hacer el filtrado

        for (Map.Entry<String, Integer> entry : edgesMain) {
            String key = entry.getKey();
            int fe = entry.getValue();
            //la negación permite eliminar ese key de WFG, ya que la condición se cumpliria para dejar ese arco en el grafo filtrado
            if (!(endgesInEi.contains(key) || endgesInEo.contains(key) || fe > fth)) {
                WFG.remove(key);
                System.out.println("REMOVER************" + key);
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
    //nthpercentile es el valor del conjunto F por debajo del cual cae un %percentile de muestras
   public static int calcularNpercentile(Set F, double percentile) {
   
      int n = F.size();
      Object[] array = F.toArray();
   
      double val = n * percentile;
      double techo = Math.ceil(val);
      return (Integer) array[(int) techo - 1];
      
   }

    private void discoverBestIncomingEdges(BPMNModel BPMN, Map<Character, Integer> Cf, Map<Character, String> Ei) {

        LinkedList<Character> queue = new LinkedList<Character>();
        Set<Character> U = new TreeSet<Character>();

        for (Character t : BPMN.T) {
            if (t != BPMN.i) {
                U.add(t);
            }
        }//U = T - {i}

        System.out.println("Mapa Cf " + Cf);
        queue.add(BPMN.i);

        Character p = ' ';
        Character n = ' ';
        int fe = 0;

        int Cmax;

        while (queue.size() != 0) {
            p = queue.get(0);
            queue.remove(0);

            //calcular p*
            List<Edge> l = obtenerPout(p);

            for (Edge e : l) {
                n = e.tarjet;
                fe = e.freq;

                System.out.println("\t p = " + p + "; n = " + n);
                if (Cf.get(p) <= fe) {
                    Cmax = Cf.get(p);
                } else {
                    Cmax = fe;
                }

                if (Cmax > Cf.get(n)) {
                    Cf.put(n, Cmax);
                    Ei.put(n, p + "," + n);

                    //si 
                    if (!(queue.contains(n) && !U.contains(n))) {
                        U.add(n);
                    }
                }

                if (U.contains(n)) {
                    U.remove(n);
                    queue.add(n);
                }
            }
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

        int Cmax;

        while (queue.size() != 0) {
            n = queue.get(0);
            queue.remove(0);

            //calcular *n
            List<Edge> l = obtenerPin(n);

            for (Edge e : l) {
                p = e.tarjet;
                fe = e.freq;

                if (Cb.get(n) <= fe) {
                    Cmax = Cb.get(n);
                } else {
                    Cmax = fe;
                }

                if (Cmax > Cb.get(p)) {
                    Cb.put(p, Cmax);
                    Eo.put(p, p + "," + n);

                    //si 
                    if (!(queue.contains(p) && !U.contains(p))) {
                        U.add(p);
                    }
                }

                if (U.contains(p)) {
                    U.remove(p);
                    queue.add(p);
                }
            }
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
