package Controlador;

import Modelo.BPMNModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;


public class FindLoops {
    
    
    
    public static LinkedHashSet<String> findLoops(BPMNModel BPMN, LinkedHashSet<String> orderGateways, LinkedHashMap<String, Integer> WFG, LinkedHashSet<String> cierres){
        LinkedHashSet<String> loops = new LinkedHashSet<>();
        LinkedHashSet<String> splits = getAllSplits(BPMN);
        
        LinkedList<String> ordenG = new LinkedList<>();
        
        ordenG.addAll(orderGateways);
        
        
        System.out.println("Splits: " + splits.toString());
        System.out.println("Cierres: " + cierres.toString());
        System.out.println("OrdenGateways: " + ordenG.toString());
        
        for(String s : splits){
            
            HashSet<String> E = Utils.sucesoresOAntecesores(s, 'a', WFG);
            System.out.println("Antecesores de " + s + " : " + E.toString());
            if(E.size() < 2)
                continue;
            
            //for e(x,s)
            for(String x : E){
                //verificar si no es cierre
                //if(!cierres.contains(x))
                  //  continue;
                 if( ordenG.indexOf(x) < ordenG.indexOf(s) ){
                    continue;
                }else{
                    LinkedHashSet<String> sucesores = getAllSucesores(WFG, s, new ArrayList<>());
                     System.out.println("Sucesores de " + s + " " + sucesores.toString());
                    if(sucesores.contains(x))
                        loops.add(s + "," + x);
                } 
                
                
            }
            
            
        }
        
        return loops;
    }
    
    
    public static LinkedHashSet<String> getAllSplits(BPMNModel bpmn){
        LinkedHashSet<String> splits = new LinkedHashSet<>();
        for(String gateway : bpmn.Gand){
            //if(gateway.charAt(gateway.length()-1) == 'A'){
                System.out.println("\t\tSplit gateway: " + gateway);
                splits.add(gateway);
           // }
                
        }
        for(String gateway : bpmn.Gxor){
           // if(gateway.charAt(gateway.length()-1) == 'A'){
                System.out.println("\t\tSplit gateway: " + gateway);
                splits.add(gateway);
           // }
                
        }
        
        for(String gateway : bpmn.Gor){
           // if(gateway.charAt(gateway.length()-1) == 'A'){
                System.out.println("\t\tSplit gateway: " + gateway);
                splits.add(gateway);
           // }
                
        }
        return splits;
    }
    
    public static LinkedHashSet<String> getAllSucesores(LinkedHashMap<String, Integer> WFG, String target, ArrayList<String> visited) {
        LinkedHashSet<String> adelante = new LinkedHashSet<>();

        HashSet<String> sucesores = Utils.sucesoresOAntecesores(target, 's', WFG);
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
    
    
}
