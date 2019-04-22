package Controlador;

import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class FindLoops {

    public static LinkedHashSet<String> findLoops(BPMNModel BPMN, LinkedHashMap<String, Integer> WFG) {
        LinkedHashSet<String> loops = new LinkedHashSet<>();
        LinkedHashSet<String> splits = getAllSplits(BPMN);
        System.out.println("Splits: " + splits.toString());
        for (String s : splits) {

            HashSet<String> E = Utils.sucesoresOAntecesores(s, 'a', WFG);
            System.out.println("Antecesores de " + s + " : " + E.toString());
            if (E.size() < 2) {
                continue;
            }

            //for e(x,s)
            for (String x : E) {
                //verificar si no es cierre
                //if (!cierres.contains(x)) {
                if (BPMN.T.contains(x.charAt(0))) {
                    continue;
                }
                if (!esMenorQue(x, s, WFG)) {
                    loops.add(s + "," + x);
                }

            }

        }

        return loops;
    }

    public static boolean esMenorQue(String x, String s, LinkedHashMap<String, Integer> WFG) {
        LinkedHashSet<String> antecesores = getAllSucesores(WFG, s, new ArrayList<>(), 'a');

        LinkedHashSet<String> sucesores = getAllSucesores(WFG, s, new ArrayList<>(), 's');

        if (antecesores.contains(x) && sucesores.contains(x)) {
            return false;
        }

        return antecesores.contains(x);
    }

    public static LinkedHashSet<String> getAllSucesores(LinkedHashMap<String, Integer> WFG, String target, ArrayList<String> visited, Character type) {
        LinkedHashSet<String> adelante = new LinkedHashSet<>();

        LinkedHashSet<String> sucesores = getSucesoresOAntecesores(target, WFG, type);
        if (sucesores.contains(target)) // 2
        {
            sucesores.remove(target);
        }
        adelante.addAll(sucesores);

        while (!sucesores.isEmpty()) {
            for (String s : (LinkedHashSet<String>) sucesores.clone()) {
                if (visited.contains(s)) {
                    sucesores.clear();
                    continue;
                }
                visited.add(s);
                sucesores = getAllSucesores(WFG, s, visited, type);
                adelante.addAll(sucesores);
            }
        }

        return adelante;

    }

    //all nodes following 'task', given the current pruened WFG
    public static LinkedHashSet<String> getSucesoresOAntecesores(String task, LinkedHashMap<String, Integer> WFG, Character type) {
        LinkedHashSet<String> x = new LinkedHashSet<>();
        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            String c;
            if (type == 'a') {
                c = vals[1];
            } else {
                c = vals[0];
            }
            if (task.equals(c)) {
                if (type == 'a') {
                    x.add(vals[0]);
                } else {
                    x.add(vals[1]);
                }
            }
        }
        return x;
    }

    public static LinkedHashSet<String> getAllSplits(BPMNModel bpmn) {
        LinkedHashSet<String> splits = new LinkedHashSet<>();
        for (String gateway : bpmn.Gand) {
            splits.add(gateway);
        }
        for (String gateway : bpmn.Gxor) {
            splits.add(gateway);
        }

        for (String gateway : bpmn.Gor) {
            splits.add(gateway);
        }
        return splits;
    }

}
