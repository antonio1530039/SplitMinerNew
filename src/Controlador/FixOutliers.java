package Controlador;

import Modelo.SignificantContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class FixOutliers {

    public HashMap<ArrayList<ArrayList<Character>>, SignificantContext> getSignificantContexts(LinkedHashMap<Integer, ArrayList<Character>> tracesList, double tc, int l, int r, int K) {
        //Dado tc (porcentaje para considerar un contexto como significante) calcular el numero de trazas en el que debe existir el contexto
        double minimumTraces = tracesList.size() * tc;

        HashMap<ArrayList<ArrayList<Character>>, SignificantContext> significantContexts = new HashMap<>();
        ArrayList<Integer> secuence = new ArrayList<>();
        
        for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
            ArrayList<Character> trace = entry.getValue();
            for (int i = 0; i < trace.size(); i++) {
                secuence.add(i);
                if (secuence.size() == K) {
                    ArrayList<ArrayList<Character>> con = context(secuence, trace, l, r);

                    ArrayList<Character> realSecuence = new ArrayList<>();
                    for (Integer index : secuence) {
                        realSecuence.add(trace.get(index));
                    }
                    
                    ArrayList<ArrayList<Character>> con2 = new ArrayList<>();
                    con2.add(con.get(0));
                    con2.add((ArrayList) realSecuence.clone());

                    ArrayList<ArrayList> all = new ArrayList<>();
                    all.add(con);
                    all.add(con2);

                    for (int j = 0; j < 2; j++) {
                        LinkedHashSet lhmrealSecuence = new LinkedHashSet<>();
                        if (significantContexts.containsKey(all.get(j))) {
                            SignificantContext sc = significantContexts.get(all.get(j));
                            sc.Frecuency++;
                            if (j == 0) {
                                lhmrealSecuence.addAll(realSecuence);
                            } else {
                                lhmrealSecuence.add('O');
                            }
                            if (sc.ProbableSubsequences.containsKey(lhmrealSecuence)) {
                                Integer freq = sc.ProbableSubsequences.get(lhmrealSecuence);
                                freq++;
                                sc.ProbableSubsequences.put(lhmrealSecuence, freq);
                            } else {
                                sc.ProbableSubsequences.put(lhmrealSecuence, 1);
                            }
                        } else {
                            SignificantContext sc = new SignificantContext();
                            lhmrealSecuence.addAll(realSecuence);
                            sc.ProbableSubsequences.put(lhmrealSecuence, 1);
                            significantContexts.put(all.get(j), sc);
                        }
                    }
                    secuence.clear();
                }
            }
            secuence.clear();
        }

        System.out.println("\tSignificantContexts\n");
        List<Map.Entry<ArrayList<ArrayList<Character>>, SignificantContext>> entries = new ArrayList(significantContexts.entrySet());

        for (Map.Entry<ArrayList<ArrayList<Character>>, SignificantContext> entry : entries) {
            TreeSet treeset = new TreeSet(entry.getValue().ProbableSubsequences.values());
            SignificantContext sc = entry.getValue();
            if ((Double.parseDouble((treeset.last().toString())) / sc.Frecuency) >= tc) {
                System.out.println("\t!!SignificantContext: " + entry.getKey().toString());
                System.out.println("\tFrecuency: " + entry.getValue().Frecuency);
                System.out.println("\tProbableSubsequences: ");
                for (Map.Entry<LinkedHashSet<Character>, Integer> entry2 : entry.getValue().ProbableSubsequences.entrySet()) {
                    System.out.println("\t\tSequence: " + entry2.getKey());
                    System.out.println("\t\t\tfreq: " + entry2.getValue());
                }
                System.out.println("");
            } else {
                significantContexts.remove(entry.getKey());
            }
        }
        return significantContexts;
    }

    public void algorithm(HashMap<ArrayList<ArrayList<Character>>, SignificantContext> significantContexts, LinkedHashMap<Integer, ArrayList<Character>> tracesList, int K) {
        System.out.println("\nExecuting algorithm....");

        List<Map.Entry<Integer, ArrayList<Character>>> traces = new ArrayList(tracesList.entrySet());

        for (Map.Entry<Integer, ArrayList<Character>> traceIterator : traces) {
            ArrayList<Character> trace = traceIterator.getValue();
            System.out.println("Trace: " + trace.toString());
            for (Map.Entry<ArrayList<ArrayList<Character>>, SignificantContext> scIterator : significantContexts.entrySet()) {
                ArrayList<ArrayList<Character>> context = scIterator.getKey();
                //Obtener covertura del contexto 
                ArrayList<Character> covering = covering(trace, context.get(0), context.get(1), K);//modify K value
                System.out.println("\tContext: " + context.toString());
                
                if(covering != null)
                    System.out.println("\tCovering: " + covering.toString());
                else
                    System.out.println("Covering not found");

                System.out.println("");
                System.out.println("");
            }
        }
    }

    public static ArrayList<ArrayList<Character>> context(ArrayList<Integer> secuence, ArrayList<Character> trace, int l, int r) {
        //Dada una secuencia de tareas y una traza; encontrar el contexto de la secuencia...
        //...según el número de vecinos requeridos a la izquierda y a la derecha indicados en los parámetros 'l' y 'r' respectivamente
        ArrayList<Character> leftNeighbour = new ArrayList<>();
        ArrayList<Character> rightNeighbour = new ArrayList<>();
        ArrayList<ArrayList<Character>> context = new ArrayList<>();

        int subLeft = secuence.get(0); //posición de la traza del primer elemento de la secuencia
        int subRight = secuence.get(secuence.size() - 1); //indice de la traza del ultimo elemento en la secuencia

        //verificamos que la posición en la traza del primer elemento en la secuencia menos el número de elementos requeridos hacia la izquierda desde sea mayor a 0
        if ((subLeft - l) >= 0) {
            //ciclo inicia en la posición: posición en la traza del primer elemento de la secuencia menos el número de elementos requeridos, 
            //lo anterior es para agregar los elementos del vecino izquierda de forma ordenada al arraylist, 
            //ciclo se detiene hasta que i es mayor a la posicion en la traza del primer elemento en la secuencia menos 1
            for (int i = (subLeft - l); i <= subLeft - 1; i++) {
                leftNeighbour.add(trace.get(i));
            }
        } else {
            leftNeighbour.add('O');
        }

        //Verificamos que la posición en la traza del ultimo elemento en la secuencia mas el número de elementos requeridos hacia la derecha sea menor al número de elementos en la traza
        if ((subRight + r) < trace.size()) {
            //el ciclo inicia en la posicion: indice del ultimo elemento de la secuencia mas 1
            //ciclo se detiene hasta que i es mayor a la posición en la traza del ultimo elemento en la secuencia más el número de elementos requeridos
            for (int i = (subRight + 1); i <= (subRight + r); i++) {
                rightNeighbour.add(trace.get(i));
            }
        } else {
            rightNeighbour.add('O');
        }
        context.add(leftNeighbour);
        context.add(rightNeighbour);
        return context;
    }

    public static ArrayList<Character> covering(ArrayList<Character> trace, ArrayList<Character> leftNeighbour, ArrayList<Character> rightNeighbour, int K) {
        ArrayList<Character> covering = new ArrayList<>();
        
        leftNeighbour.remove((Character)'O');
        rightNeighbour.remove((Character)'O');
        
        if (leftNeighbour.isEmpty() && rightNeighbour.isEmpty()) {
            return null;
        }
        
        for(Character c : leftNeighbour){
            if(!trace.contains(c)){
                System.out.println("\tcovering: traza no contiene un elemento del contexto izquierdo");
                return null;
            }
        }
        
        for(Character c : rightNeighbour){
            if(!trace.contains(c)){
                System.out.println("\tcovering: traza no contiene un elemento del contexto derecho");
                return null;
            }
        }
         
        
       
        

        int start, end;
        
        if(leftNeighbour.isEmpty()){
            start = 0;
            end = (start) + K;
        }else if (rightNeighbour.isEmpty()) {
            start = trace.indexOf(leftNeighbour.get(leftNeighbour.size() - 1)) + 1;
            end = trace.size();
        } else {
            if(trace.indexOf(leftNeighbour.get(leftNeighbour.size() - 1)) >= trace.indexOf(rightNeighbour.get(0))){
                return null;
            }
            start = trace.indexOf(leftNeighbour.get(leftNeighbour.size() - 1)) + 1;
            end = (start-1) + K;
        }
        //[[], [c]]
        //b c
        if(end > trace.size())
            return covering;
        
        for (int i = start; i < end; i++) {
            covering.add(trace.get(i));
        }

        return covering;
    }
}
