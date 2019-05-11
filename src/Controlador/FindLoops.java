package Controlador;

import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class FindLoops {

    /**
     * Función que recibe el modelo BPMN, el grafo dirigido, la notación y los cierres
     * Detecta loops entre compuertas y los indica en la notación
     * @param BPMN
     * @param WFG
     * @param notation
     * @param cierres
     * @return 
     */
    public static String findLoops(BPMNModel BPMN, LinkedHashMap<String, Integer> WFG, String notation, LinkedHashSet<String> cierres) {
        LinkedHashSet<String> loops = new LinkedHashSet<>();

        LinkedHashSet<String> splits = getAllSplits(BPMN); // Se obtienen las compuertas de tipo Split

        for (String s : splits) {
            HashSet<String> E = Utils.sucesoresOAntecesores(s, 'a', WFG); //Se obtienen los antecesores de la compuerta de apertura
            if (E.size() < 2) { //Verificamos si tiene al menos dos arcos incidentes
                continue;
            }
            for (String x : E) { //Analizar cada Antecesor de la compuerta de apertura analizada
                if (!cierres.contains(x)) { //Verificamos que sea un cierre
                    continue;
                }
                if (!esMenorQue(x, s, WFG)) { //Verificamos si el antecesor esta antes que la compuerta de apertura, en caso negativo se considera como un loop
                    loops.add(s + "," + x);
                }
            }

        }
        //Se retorna la notación formateada con los loops identificados
        return formatNotation(loops, notation);
    }

    /**
     * Función que dados los loops identificados y la notación original, se indica en la notación los loops de esta forma: @{ contenido del loop }
     * @param loops
     * @param notation
     * @return 
     */
    public static String formatNotation(LinkedHashSet<String> loops, String notation) {
        if (loops.size() == 0) {
            return notation;
        }
        //Variable que almacena la nueva notación
        StringBuilder newNotation = new StringBuilder();
        for (String l : loops) {
            //Cada elemento de un loop fue guardado de la forma: inicioLoop, finLoop
            //  Donde inicioLoop es una compuerta de apertura
            // finLoop es una compuerta de cierre
            String[] vals = l.split(",");
            String cierre = vals[1];
            //Se remplaza la compuerta de apertura donde inicia el loop por el simbolo + la misma compuerta de apertura en la notación
            notation = notation.replace(vals[0], "@{" + vals[0]);

            //Se busca el índice de la compuerta de apertura del cierre (fin del loop)
            int indexOfcierre = notation.indexOf(cierre.substring(0, cierre.indexOf("C")) + "A");
            //A partir de este índice se explora para obtener el índice donde se debe colocar la llave de cierre del loop
            int openGates = -1;
            //terminoLoop almacena el índice donde se debe colocar la llave del finl del loop }
            int terminoLoop = -1;
            for (int i = indexOfcierre; i < notation.length(); i++) {
                if (notation.charAt(i) == '{') {
                    openGates++;
                } else if (notation.charAt(i) == '}') {
                    if (openGates == 0) {
                        terminoLoop = i;
                        break;
                    } else {
                        openGates--;
                    }
                }
            }

            //Pasar la notación
            for (int i = 0; i < notation.length(); i++) {
                newNotation.append(notation.charAt(i));
                //terminoLoop indica el indice donde se coloca la llave del loop }
                if (i == terminoLoop) {
                    newNotation.append("}");
                }
            }

        }
        //Se retorna la notación formateada
        return newNotation.toString();
    }

    /**
     * Función que dado un nodo x y s se calcula si x va antes que s
     * @param x 
     * @param s
     * @param WFG
     * @return 
     */
    public static boolean esMenorQue(String x, String s, LinkedHashMap<String, Integer> WFG) {
        LinkedHashSet<String> antecesores = getAllSucesores(WFG, s, new ArrayList<>(), 'a');

        LinkedHashSet<String> sucesores = getAllSucesores(WFG, s, new ArrayList<>(), 's');

        //Verificamos si x es alcanzable hacia atras y hacia adelante de s, de ser así se considera un loop.
        if (antecesores.contains(x) && sucesores.contains(x)) {
            return false;
        }

        return antecesores.contains(x);
    }

    /**
     * Función que obtiene todos los nodos hacia adelante de un target dado (nodo)
     * @param WFG Grafo dirigido
     * @param target Nodo a analizar
     * @param visited Nodos visitados
     * @param type 'a' o 's' antecesores o sucesores
     * @return 
     */
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

    
    /**
     * Función que obtiene todas las compuertas de apertura del modelo BPMN
     * @param bpmn
     * @return 
     */
    public static LinkedHashSet<String> getAllSplits(BPMNModel bpmn) {
        LinkedHashSet<String> splits = new LinkedHashSet<>();
        for (String gateway : bpmn.Gand) {
            if (gateway.charAt(gateway.length() - 1) == 'A') {
                splits.add(gateway);
            }
        }
        for (String gateway : bpmn.Gxor) {
            if (gateway.charAt(gateway.length() - 1) == 'A') {
                splits.add(gateway);
            }
        }

        return splits;
    }

}
