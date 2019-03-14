package Controlador;

import Modelo.SignificantContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class FixOutliers {

    public void Algorithm(LinkedHashMap<Integer, ArrayList<Character>> tracesList, int l, int r, int K, double umbral) {
        List<Map.Entry<Integer, ArrayList<Character>>> traces = new ArrayList(tracesList.entrySet());
        
        HashMap<ArrayList<ArrayList<Character>>, SignificantContext> significantContexts = new HashMap<>();
        
        for (Map.Entry<Integer, ArrayList<Character>> t : traces) {
            ArrayList<Character> trace = t.getValue();
            trace.add(0, 'O');
            trace.add('O');
            getSignificantContext(trace, l, r, K, significantContexts);
        }

        System.out.println("\n\tSignificantContexts\n");
        List<Map.Entry<ArrayList<ArrayList<Character>>, SignificantContext>> entries = new ArrayList(significantContexts.entrySet());

        for (Map.Entry<ArrayList<ArrayList<Character>>, SignificantContext> entry : entries) {
            SignificantContext sc = entry.getValue();
            System.out.println("\t!!SignificantContext: " + entry.getKey().toString() + "\n" + "\tFrecuency: " + entry.getValue().Frecuency + "\n\tProbableSubsequences");
            for (Map.Entry<LinkedHashSet<Character>, Integer> entry2 : entry.getValue().ProbableSubsequences.entrySet()) {
                System.out.println("\t\tSequence: " + entry2.getKey() + "\n" + "\t\t\tfreq: " + entry2.getValue());
            }
            System.out.println("");
        }

    }

    public void getSignificantContext(ArrayList<Character> trace, int l, int r, int K, HashMap<ArrayList<ArrayList<Character>>, SignificantContext> significantContexts) {
        ArrayList<Character> left = new ArrayList<>();
        for (int i = 0; i < trace.size(); i++) {
            //En este ciclo se agregan los vecinos de la izquierda
            left.add(trace.get(i));
            if (left.size() == l) {
                ArrayList<Character> subsequence = new ArrayList<>();
                for (int j = i + 1; j < trace.size(); j++) {
                    subsequence.add(trace.get(j));
                    if (subsequence.size() == K) {
                        System.out.println("finishing context with sequence: " + subsequence);
                        finishContext(left, j + 1, (j + 1 + r), significantContexts, subsequence, trace, l, r);
                        //makeChanges(significantContexts, trace, umbral);
                        break;
                    }
                }
                left.clear();
            }
        }
    }

    public void finishContext(ArrayList<Character> left, int start, int end, HashMap<ArrayList<ArrayList<Character>>, SignificantContext> significantContexts, ArrayList<Character> subsequence,
            ArrayList<Character> actualTrace, int l, int r) {

        ArrayList<Character> right = new ArrayList<>();
        if (start >= actualTrace.size()) {
            right.add('O');
        } else {
            for (int z = start; z < end; z++) {
                right.add(actualTrace.get(z));
            }
        }
        
        ArrayList<ArrayList<Character>> con = new ArrayList<>();
        con.add((ArrayList<Character>) left.clone());
        con.add((ArrayList<Character>)right.clone());
        
        //Desde K hasta K=1 disminuyendo en 1, agregar las subsequencias
        for (int i = 0; i <= subsequence.size() - 1; i++) { //verificar fin de i
            pushContext(con, subsequence, significantContexts);

            //verificar si la subsequencia partida tiene el tamaño r, tomar como contexto esta
            if (subsequence.size() == r && subsequence.get(0) != 'O') {
                
                //Crear nuevo contexto
                ArrayList<ArrayList<Character>> subcontext = new ArrayList<>();
                subcontext.add((ArrayList<Character>)left.clone());
                subcontext.add((ArrayList<Character>)subsequence.clone());

                ArrayList<Character> subs = new ArrayList<>();
                subs.add('O');
                pushContext(subcontext, subs, significantContexts);
                
                System.out.println("subsequenceCopy == r - context: " + subcontext.toString() );
            }
            
            subsequence.remove(subsequence.size() - 1);
        }
        
        //es necesario agregar el vacio como subsequencia ultima? 
    }

    public void pushContext(ArrayList<ArrayList<Character>> con, ArrayList<Character> subsequence, HashMap<ArrayList<ArrayList<Character>>, SignificantContext> significantContexts) {
        LinkedHashSet<Character> realSecuence = new LinkedHashSet();
        realSecuence.addAll((ArrayList<Character>) subsequence.clone());
        SignificantContext sc = new SignificantContext();
        if (significantContexts.containsKey(con)) {
            sc = significantContexts.get(con);
            sc.Frecuency++;
            Integer freq = 0;
            if (sc.ProbableSubsequences.containsKey(realSecuence)) {
                freq = sc.ProbableSubsequences.get(realSecuence) + 1;
            } else {
                freq = 1;
            }
            sc.ProbableSubsequences.put(realSecuence, freq);
        } else {
            sc.ProbableSubsequences.put(realSecuence, 1);
            significantContexts.put(con, sc);
        }
    }
}
