package Controlador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class GatewayLoops {

    public static ArrayList<String> findLoops(LinkedHashMap<String, Integer> WFG, String target) {
        ArrayList<String> loops = new ArrayList<>();
        
        
        //Obtener ejes incidentes al objetivo
        LinkedHashSet<String> antecesores = getSucesoresOAntecesores(target, WFG, 'a');

        // Verificar si desde el objetivo, es posible alcanzar a uno de sus ejes incidentes
        // 1 Si alcanza a al menos uno de sus ejes incidentes, quiere decir que existe un loop entre el objetivo y el eje incidente
        // 2 Al realizar esta verificación es necesario ignorar el caso en el que la exploración se tope con el mismo objetivo
        // 3 Realizar una lista en la que se tenga el control de los nodos visitados, esto para evitar los loops en la exploración
        //Obtener todos los sucesores del objetivo
        LinkedHashSet<String> sucesores = getAllSucesores(WFG, target, new ArrayList<>()); // 3

        System.out.println("\t\t\tEjes incidentes a: " + target + " " + antecesores.toString());
        for (String a : antecesores) {
            if (sucesores.contains(a)) { // 1
                System.out.println("\t\t\t *** Loop between: " + target + " " + a);
                loops.add(target + "," + a);
            }
        }
        return loops;
    }

    public static LinkedHashSet<String> getAllSucesores(LinkedHashMap<String, Integer> WFG, String target, ArrayList<String> visited) {
        LinkedHashSet<String> adelante = new LinkedHashSet<>();

        LinkedHashSet<String> sucesores = getSucesoresOAntecesores(target, WFG, 's');
        if (sucesores.contains(target)) // 2
        {
            sucesores.remove(target);
        }
        adelante.addAll(sucesores);

        while (!sucesores.isEmpty()) {
            for (String s : (LinkedHashSet<String>) sucesores.clone()) {
                if(visited.contains(s)){
                    sucesores.clear();
                    continue;
                }
                visited.add(s);
                sucesores = getAllSucesores(WFG, s, visited);
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
    

}
