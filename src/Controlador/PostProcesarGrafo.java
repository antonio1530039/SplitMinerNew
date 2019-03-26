package Controlador;

import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class PostProcesarGrafo {

    LinkedHashMap<String, Integer> WFG;
    BPMNModel BPMN;
    List<Character> autoLoops = new LinkedList<Character>();
    public String notation = "";

    public PostProcesarGrafo(BPMNModel bpmn, LinkedHashMap<String, Integer> wfg, List<Character> autoLoops) {
        WFG = wfg;
        BPMN = bpmn;
        this.autoLoops = autoLoops;

        //OCTAVO, SE REMUEVEN COMPUERTAS DUPLICADAS 
        //todas las compuertas XOR y AND se han detectado. Ahora, eliminar compuertas repetidas.
        System.out.println("\n\t1.Remover compuertas duplicadas ANDs");
        removeDuplicateGates();
        System.out.println("\t  Resultado:");
        Utils.mostrarGrafo(2, WFG);

        System.out.println("\n\t1.Remover compuertas duplicadas XORs");
        removeDuplicateGatesXOR();
        System.out.println("\t  Resultado:");
        Utils.mostrarGrafo(2, WFG);

        //Y SE DETECTAN 'JOINS'
        System.out.println("\n\t2.Detectar JOINS (pendiente de realizar)");
        detectarJoins();
        // System.out.println("\t  Resultado:");
        Utils.mostrarGrafo(2, WFG);

        //Y SE REINTEGRAN 'AUTOLOPS'
        System.out.println("\n\t3.Re-integrar autolops al modelo final");
        reintegraALoops();
        System.out.println("\t  Resultado:");
        Utils.mostrarGrafo(2, WFG);
    }

    public void removeDuplicateGates() {

        //para dos símbolos de compuerta, revisar si sus conjuntos son iguales
        Collection<Map.Entry<String, HashSet<String>>> list2 = BPMN.ANDs.entrySet();
        //Collection<HashSet<Character>> list = BPMN.ANDs.values();

        // System.out.println("***LISTA 2:"+BPMN.ANDs.entrySet());
        List<Map.Entry<String, HashSet<String>>> list = new ArrayList<Map.Entry<String, HashSet<String>>>(list2);
        int i = 0;
        while (i < list.size()) {
            Map.Entry<String, HashSet<String>> entry1 = list.get(i);
            //ver si hay alguna repetida
            int j = i + 1;
            while (j < list.size()) {
                Map.Entry<String, HashSet<String>> entry2 = list.get(j);

                if (Utils.igualesString(entry1.getValue(), entry2.getValue())) {
                    //compuertas repetidas, 'c' y 'cr'eliminar una de ellas del grafo
                    //1. remueve todos los arcos 'cr'*
                    Utils.removerEdges(entry2.getKey(), WFG);
                    //2. en todos los arcos *'cr', reemplaza 'cr' por 'c'
                    Utils.remplazarEdges(entry2.getKey(), entry1.getKey(), WFG);

                    list.remove(j);
                } else {
                    j++;
                }
            }
            i++;
        }

    }

    public void removeDuplicateGatesXOR() {

        //para dos símbolos de compuerta, revisar si sus conjuntos son iguales
        Collection<Map.Entry<String, HashSet<String>>> list2 = BPMN.XORs.entrySet();
        //Collection<HashSet<Character>> list = BPMN.ANDs.values();

        // System.out.println("***LISTA 2:"+BPMN.ANDs.entrySet());
        List<Map.Entry<String, HashSet<String>>> list = new ArrayList<Map.Entry<String, HashSet<String>>>(list2);
        int i = 0;
        while (i < list.size()) {
            Map.Entry<String, HashSet<String>> entry1 = list.get(i);
            //ver si hay alguna repetida
            int j = i + 1;
            while (j < list.size()) {
                Map.Entry<String, HashSet<String>> entry2 = list.get(j);

                if (Utils.igualesString(entry1.getValue(), entry2.getValue())) {
                    //compuertas repetidas, 'c' y 'cr'eliminar una de ellas del grafo

                    //1. remueve todos los arcos 'cr'*
                    Utils.removerEdges(entry2.getKey(), WFG);
                    //2. en todos los arcos *'cr', reemplaza 'cr' por 'c'
                    Utils.remplazarEdges(entry2.getKey(), entry1.getKey(), WFG);

                    list.remove(j);
                } else {
                    j++;
                }

            }
            i++;
        }

    }

    public void reintegraALoops() {
        ArrayList<String> keys = new ArrayList<String>();
        Iterator<Map.Entry<String, Integer>> iter = WFG.entrySet().iterator();
        try {
            while (iter.hasNext()) {
                Map.Entry<String, Integer> entry = iter.next();
                String key = entry.getKey();
                
                String keySplit[] = key.split(",");
                
                if (autoLoops.size() > 0) {

                    for (Character car : autoLoops) {

                        String first = "" + keySplit[0];
                        String second = "" + keySplit[1];
                        String clave = "";

                        if (first.indexOf("" + car) > -1) {
                            iter.remove();
                            first = "@" + first;
                            clave = first + "," + second;
                            keys.add(clave);
                            break;
                        } else {
                            if (second.indexOf("" + car) > -1) {
                                iter.remove();
                                second = "@" + second;
                                clave = first + "," + second;
                                keys.add(clave);
                                break;
                            }

                        }

                    }
                }
            }
        } catch (IllegalStateException exeption) {
            System.out.println(" Error de indice en un array");
        }
        for (String cl : keys) {

            WFG.put(cl, 1);

        }

    }

    public void detectarJoins() {

        System.out.println("\t\tDetectando joins y creando noatación...");
        //Lo siguiente es a manera de prueba.....................
 /*
        
      WFG.clear();
      
      
     
      //Modelo 1
      
      WFG.put("X1A,b", 1);
      WFG.put("X1A,f", 1);
      WFG.put("a,X1A", 1);
      WFG.put("A1A,c", 1);
      WFG.put("A1A,d", 1);
      WFG.put("b,A1A", 1);
      WFG.put("c,e", 1);
      WFG.put("d,e", 1);
      WFG.put("f,A1A", 1);
      
      BPMN.Gand.clear();
      BPMN.Gor.clear();
      BPMN.Gxor.clear();
      
      BPMN.Gand.add("A1A");
      BPMN.Gxor.add("X1A");
      
      
      BPMN.T.clear();
      BPMN.T.add('a');
      BPMN.T.add('b');
      BPMN.T.add('f');
      BPMN.T.add('c');
      BPMN.T.add('d');
      BPMN.T.add('e');
      
      BPMN.i = 'a';
      /*
      Notation:  a XOR{  b, f} AND{  c, d} e
   	[A,b] - 1
   	[A,f] - 1
   	[a,A] - 1
   	[1,c] - 1
   	[1,d] - 1
   	[b,B] - 1
   	[f,B] - 1
   	[B,1] - 1
   	[c,2] - 1
   	[d,2] - 1
   	[2,e] - 1
         */
 /*
     
      //Modelo 2
      WFG.put("A1A,b", 1);
      WFG.put("A1A,c", 1);
      WFG.put("X1A,A1A", 1);
      WFG.put("X1A,e", 1);
      WFG.put("a,X1A", 1);
      WFG.put("b,d", 1);
      WFG.put("c,d", 1);
      WFG.put("e,d", 1);
   
      
      BPMN.Gand.clear();
      BPMN.Gor.clear();
      BPMN.Gxor.clear();
      
      BPMN.Gand.add("A1A");
      BPMN.Gxor.add("X1A");
      
   
      
      BPMN.T.clear();
      BPMN.T.add('a');
      BPMN.T.add('b');
      BPMN.T.add('c');
      BPMN.T.add('e');
      BPMN.T.add('d');
      BPMN.i = 'a';
      
      /*
      Notation:  a XOR{  AND{  b, c}, e} d
   	[1,b] - 1
   	[1,c] - 1
   	[A,1] - 1
   	[A,e] - 1
   	[a,A] - 1
   	[b,2] - 1
   	[c,2] - 1
   	[2,d] - 1
   	[b,B] - 1
   	[e,B] - 1
   	[B,d] - 1
      
      
         */
 /*
      //Modelo 3
      WFG.put("a,b", 1);
      WFG.put("b,c", 1);
      WFG.put("X1A,d", 1);
      WFG.put("X1A,h", 1);
      WFG.put("c,X1A", 1);
      WFG.put("d,e", 1);
      WFG.put("X2A,f", 1);
      WFG.put("X2A,g", 1);
      WFG.put("e,X2A", 1);
      WFG.put("g,f", 1);
      WFG.put("h,d", 1);
   
      
      
      BPMN.Gand.clear();
      BPMN.Gor.clear();
      BPMN.Gxor.clear();
      
      
      
      BPMN.Gxor.add("X1A");
      BPMN.Gxor.add("X2A");
      
      
      BPMN.T.clear();
      BPMN.T.add('a');
      BPMN.T.add('b');
      BPMN.T.add('c');
      BPMN.T.add('h');
      BPMN.T.add('d');
      BPMN.T.add('e');
      BPMN.T.add('g');
      BPMN.T.add('f');
      BPMN.i = 'a';
      /*
      Notation:  a b c XOR{ , h} d e XOR{ , g} f
   	[a,b] - 1
   	[b,c] - 1
   	[A,h] - 1
   	[c,A] - 1
   	[d,e] - 1
   	[B,g] - 1
   	[e,B] - 1
   	[A,C] - 1
   	[h,C] - 1
   	[C,d] - 1
   	[B,D] - 1
   	[g,D] - 1
   	[D,f] - 1        
         */
 /*
      //Modelo 4
      WFG.put("a,b", 1);
      WFG.put("X1A,c", 1);
      WFG.put("X1A,h", 1);
      WFG.put("b,X1A", 1);
      WFG.put("c,d", 1);
      WFG.put("d,e", 1);
      WFG.put("X2A,f", 1);
      WFG.put("X2A,g", 1);
      WFG.put("e,X2A", 1);
      WFG.put("g,f", 1);
      WFG.put("h,i", 1);
      WFG.put("i,j", 1);
      WFG.put("j,e", 1);
   
      BPMN.Gand.clear();
      BPMN.Gor.clear();
      BPMN.Gxor.clear();
   
      BPMN.Gxor.add("X1A");
      BPMN.Gxor.add("X2A");
      
      BPMN.T.clear();
      BPMN.T.add('a');
      BPMN.T.add('b');
      BPMN.T.add('c');
      BPMN.T.add('d');
      BPMN.T.add('e');
      BPMN.T.add('h');
      BPMN.T.add('i');
      BPMN.T.add('j');
      BPMN.T.add('g');
      BPMN.T.add('f');
      BPMN.i = 'a';
      
      /* 
      
      Notation:  a b XOR{  c d, h i j} e XOR{ , g} f
   	[a,b] - 1
   	[A,c] - 1
   	[A,h] - 1
   	[b,A] - 1
   	[c,d] - 1
   	[B,g] - 1
   	[e,B] - 1
   	[h,i] - 1
   	[i,j] - 1
   	[d,C] - 1
   	[j,C] - 1
   	[C,e] - 1
   	[B,D] - 1
   	[g,D] - 1
   	[D,f] - 1
      
         */
 /*
      //Modelo 5 - ciclos
      WFG.put("1,b", 1);
      WFG.put("1,c", 1);
      WFG.put("a,1", 1);
      WFG.put("A,d", 1);
      WFG.put("A,e", 1);
      WFG.put("b,A", 1);
      WFG.put("e,f", 1);
      WFG.put("f,1", 1);
      WFG.put("c,A", 1);
   
      
      
      BPMN.Gand.clear();
      BPMN.Gor.clear();
      BPMN.Gxor.clear();
   
      BPMN.Gand.add('1');
      BPMN.Gxor.add('A');
   
   
      BPMN.T.clear();
      BPMN.T.add('a');
      BPMN.T.add('b');
      BPMN.T.add('c');
      BPMN.T.add('d');
      BPMN.T.add('e');
      BPMN.T.add('f');
      BPMN.i = 'a';
         */
        //.....................
        ///
        JoinsFinder jf = new JoinsFinder(BPMN, WFG);

        notation = jf.findNotation();
        System.out.println("Notation: " + notation);

    }
}
