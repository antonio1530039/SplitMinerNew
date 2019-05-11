package Controlador;

import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class JoinsFinder {

    LinkedList<String> cloneTask; //Lista de tareas visitadas

    LinkedList<String> visitedGateways;//Lista de compuertas visitadas

    BPMNModel BPMN;

    LinkedHashMap<String, Integer> WFG;

    //Variables para deteccion futura de loops de compuertas
    LinkedHashSet<String> cierres = new LinkedHashSet<>();

    //Variables para creacion de compuertas OR
    ArrayList<HashMap<String, LinkedList<String>>> cierresOr = new ArrayList<>(); //Se almacena la información de las compuertas OR identificadas
    ArrayList<String> cierresOrGateways = new ArrayList<>();
    int numberGatewaysOr = 1;
    ///////////////////

    //Constructor
    public JoinsFinder(BPMNModel bpmn, LinkedHashMap<String, Integer> wfg) {
        this.BPMN = bpmn;
        this.cloneTask = new LinkedList<>();
        this.visitedGateways = new LinkedList<>();
        for (Character c : BPMN.T) {
            this.cloneTask.add(c.toString());
        }

        this.WFG = wfg;//Para utilizar funciones y obtener el grafo
    }

    /**
     * Función que realiza la creación de cierres y genera la notación
     * @return Notación
     */
    public String findNotation() {
        StringBuilder notation = new StringBuilder();
        continueExploring(notation, BPMN.i.toString());
        finishOrs();
        return notation.toString().replace(",}", "}");
    }

    /**
     *  Procedimiento que realiza la creación de compuertas de tipo OR, esto se realiza al final 
        (cuando se identifican todas las compuertas homogeneas)
        De forma paralela a la creación de las compuertas OR, se realiza la simplificación de estas compuertas.
     */
    public void finishOrs() {
        System.out.println("Finishing Or joins...");
        numberGatewaysOr = 1;
        for (int i = 0; i < this.cierresOr.size(); i++) {
            for (Map.Entry<String, LinkedList<String>> entry : cierresOr.get(i).entrySet()) {
                String orSymbol = "O" + cierresOrGateways.get(i).substring(0, cierresOrGateways.get(i).length() - 1) + "C" + numberGatewaysOr;//Definir el simbolo de la compuerta Or
                BPMN.Gor.add(orSymbol);
                String cierre = entry.getKey(); //Recuperar cierre
                //Obtener antecesores del cierre
                HashSet<String> antecesores = Utils.sucesoresOAntecesores(cierre, 'a', WFG);
                boolean orExists = false;
                for (String a : antecesores) {
                    if (BPMN.Gor.contains(a)) {
                        orExists = true;
                        System.out.println("\t\tSe intentó crear Join: " + orSymbol + " pero el cierre contiene OR (simplificación)");
                        BPMN.Gor.remove(orSymbol);
                        break;
                    }

                }
                //Aqui se verifica si al menos un antecesor del cierre es una compuerta OR, entonces no colocar este cierre para evitar la serializacion de estas compuertas
                if (!orExists) {
                    System.out.println("\t\tJoin: " + orSymbol + " creado");
                    for (String a : antecesores) {
                        WFG.remove(a + "," + cierre); //eliminar la antigua conexion
                        WFG.put(a + "," + orSymbol, 1); //nueva conexion a la compuerta
                    }
                    WFG.put(orSymbol + "," + cierre, 1);//Conectar la nueva compuerta al nodo cierre

                    this.cierres.add(orSymbol); //cierres para deteccion futura de loops
                    numberGatewaysOr++;
                }
            }
        }
    }

    

    /**
     * Procedimiento de exploración de nodos
     * @param notation Notación
     * @param actual Nodo a explorar
     */
    public void continueExploring(StringBuilder notation, String actual) {
        if (cloneTask.contains(actual)) { //verificar que actual sea una tarea
            notation.append(" " + actual);
            cloneTask.remove(actual);
            continueExploring(notation, getSucesorOantecesor(actual, 's'));
        } else if ((BPMN.Gand.contains(actual) || BPMN.Gxor.contains(actual)) && !actual.contains("C")) { // es compuerta, resolverla
            LinkedList<String> ramas = new LinkedList<>();
            HashMap<String, LinkedList<String>> cierres = resolveGateway(actual, ramas);
            ArrayList<String> cierresYanteriores = conectarCierres(cierres, notation, actual, ramas);
            for (String s : cierresYanteriores) { //para cada cierre retornado por la compuerta, tomar como siguientes estos, los anteriores no son usados aqui...
                continueExploring(notation, s.split(",")[0]);
            }
        }
    }


    /**
     * Función que reliza la creación de cierres homogeneos y guarda la información de los cierres heterogeneos (estos ultimos son creados al final)
        Esta función también agrega la compuerta explorada a la notación así como el contenido de sus ramas
     * @param cierres Cierres candidatos
     * @param notation Notación
     * @param gateway Compuerta de apertura
     * @param ramas Ramas de la compuerta (Notación)
     * @return Siguientes nodos a explorar
     */
    public ArrayList<String> conectarCierres(HashMap<String, LinkedList<String>> cierres, StringBuilder notation, String gateway, LinkedList<String> ramas) {
        ArrayList<String> paraCierre = new ArrayList<>(); //Esta lista es retornada, obtiene el anterior del o de los cierres con las nuevas compuertas creadas  (es utilizado en exploreBranch)        

        if (visitedGateways.contains(gateway)) {
            return paraCierre;
        }

        visitedGateways.add(gateway);

        notation.append(" " + gateway + "{ ");
        //Agregar sus ramas a la notacion
        for (String rama : ramas) {
            if (!rama.equals("")) {
                notation.append(rama + ",");
            }
        }
        notation.append("}");

        if (cierres.size() == 1) { //Cierre del mismo tipo
            String symbol = gateway.substring(0, gateway.length() - 1) + "C";
            //Talvez no es necesario agregar a la lista correspondiente
            if (BPMN.Gand.contains(gateway)) {
                BPMN.Gand.add(symbol);
            } else if (BPMN.Gxor.contains(gateway)) {
                BPMN.Gxor.add(symbol);
            }
            for (Map.Entry<String, LinkedList<String>> entry : cierres.entrySet()) {
                String cierre = entry.getKey(); //Recuperar cierre
                LinkedList<String> anteriores = entry.getValue();//Recuperar lista de los anteriores del cierre
                for (String a : anteriores) { //Para cada anterior en la lista de anteriores, desconectar del cierre y conectar a la nueva compuerta
                    WFG.remove(a + "," + cierre); //eliminar la antigua conexion
                    WFG.put(a + "," + symbol, 1); //nueva coneccion a la compuerta
                }
                System.out.println("\t\tJoin: " + symbol + " creado");
                WFG.put(symbol + "," + cierre, 1);//Conectar la nueva compuerta al nodo cierre
                this.cierres.add(symbol); //cierres para deteccion futura de loops
                paraCierre.add(cierre + "," + symbol);
            }
        } else if (cierres.size() > 1) { //Cierre de Ors

            this.cierresOr.add(cierres);
            this.cierresOrGateways.add(gateway);

            for (Map.Entry<String, LinkedList<String>> entry : cierres.entrySet()) {
                String orSymbol = "O" + gateway.substring(0, gateway.length() - 1) + "C" + numberGatewaysOr;//Definir el simbolo de la compuerta Or
                String cierre = entry.getKey(); //Recuperar cierre
                paraCierre.add(cierre + "," + orSymbol);
                numberGatewaysOr++;
            }
        }
        return paraCierre;
    }

    /**
     *  Función que dada una compuerta obtiene el contenido de sus ramas para la notación y
    guarda los cierres candidatos de cada rama
     * @param gate Compuerta
     * @param ramas Lista que almacena la notación de las ramas de la compuerta
     * @return Mapa con la información de los cierres
     */
    public HashMap<String, LinkedList<String>> resolveGateway(String gate, LinkedList<String> ramas) {
        HashSet<String> sigs = Utils.successors(gate, WFG);
        HashMap<String, LinkedList<String>> cierres = new HashMap<>(); //Mapa de cierres : la llave representa el nodo de cierre candidato y el valor es la lista de nodos anteriores a este
        for (String s : sigs) {
            StringBuilder notationRama = new StringBuilder();
            ArrayList<String> cierreYanteriores = exploreBranch(s, notationRama, gate);
            if (cierreYanteriores.size() > 0) {
                for (String cierreYanterior : cierreYanteriores) {
                    String vals[] = cierreYanterior.split(",");
                    String cierre = vals[0];
                    String anterior = vals[1];
                    if (cierres.containsKey(cierre)) {
                        LinkedList<String> list = cierres.get(cierre);
                        list.add(anterior);
                        cierres.put(cierre, list);
                    } else {
                        LinkedList<String> list = new LinkedList<>();
                        list.add(anterior);
                        cierres.put(cierre, list);
                    }
                }
                ramas.add(notationRama.toString());
            }
        }
        return cierres;
    }

    /**
     * Función que explora una rama y agrega a la notación el contenido de esta
     * @param nodo Nodo siguiente a explorar
     * @param notation Notación
     * @param fromGateway Compuerta de la rama que que se esta explorando
     * @return Cierre o cierres de la rama
     */
    public ArrayList<String> exploreBranch(String nodo, StringBuilder notation, String fromGateway) {
        ArrayList<String> cierres = new ArrayList<>(); //El cierre esta representado de la forma "cierre, nodo anterior al cierre"
        if (getNumberEdges(nodo, "to") > 1) {
            //si este nodo tiene como antecesor la compuerta de donde viene, entonces retornar como cierre: nodo,fromGateway
            HashSet<String> antecesores = Utils.sucesoresOAntecesores(nodo, 'a', WFG);
            if (antecesores.contains(fromGateway)) {
                //System.out.println("(getNumberEdgesToA(nodo) > 1)...nodo,FromGateway: '" + nodo + "'," + fromGateway + "' ");
                cierres.add(nodo + "," + fromGateway);
            } else {
                //Pendiente revisar esto!
                //System.out.println("(getNumberEdgesToA(nodo) > 1)...nodo: '" + nodo + "'");
                cierres.add(nodo + "," + getSucesorOantecesor(nodo, 'a')); //retornar el mismo nodo y el anterior de este
            }
        } else {
            if ((BPMN.Gand.contains(nodo) || BPMN.Gxor.contains(nodo)) && !nodo.contains("C")) { //si es compuerta... resolver
                LinkedList<String> ramas = new LinkedList<>();
                HashMap<String, LinkedList<String>> cierresGateway = resolveGateway(nodo, ramas);
                cierres.addAll(conectarCierres(cierresGateway, notation, nodo, ramas));
                visitedGateways.add(nodo);
            } else if (cloneTask.contains(nodo)) {//es tarea... agregar a notacion y eliminar de lista
                notation.append(" " + nodo);
                cloneTask.remove(nodo);
                String s = getSucesorOantecesor(nodo, 's');
                if (s != null) {
                    if (getNumberEdges(s, "to") > 1) {
                        cierres.add(s + "," + nodo);
                    } else {
                        cierres = exploreBranch(s, notation, fromGateway);
                    }
                }//Verificar, en caso de ser nulo el sucesor... Que se retorna como cierre? al no retornar nada, no se agrega a la notacion
            }
        }
        return cierres;
    }

    /**
     * Función que encuentra el sucesor o antecesor del nodo dado, la cual es una tarea y se espera que solo tenga un solo sucesor 
     * @param task
     * @param type 's' o 'a' sucesor o antecesor de la tarea
     * @return sucesor o antecesor
     */
    public String getSucesorOantecesor(String task, Character type) {
        List<Map.Entry<String, Integer>> edges = new ArrayList(WFG.entrySet());
        for (Map.Entry<String, Integer> entry : edges) {
            String key = entry.getKey();
            String vals[] = key.split(",");
            String c0 = vals[0];
            String c1 = vals[1];
            if (type == 's') {
                if (task.equals(c0)) {
                    return c1;
                }
            } else {
                if (task.equals(c1)) {
                    return c0;
                }
            }
        }
        return null;
    }

    /**
     * Encontrar el numero de edges entrantes ( *a ) o (a*)
     * @param a 
     * @param type "to" ejes entrantes, "from" ejes salientes
     * @return Número de arcos
     */
    public int getNumberEdges(String a, String type) { //types: To , From
        int i = 0;
        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            if (type.equals("to")) {
                if (a.equals(entry.getKey().split(",")[1])) {
                    i++;
                }
            } else if (type.equals("from")) {
                if (a.equals(entry.getKey().split(",")[0])) {
                    i++;
                }
            }
        }
        return i;
    }

}
