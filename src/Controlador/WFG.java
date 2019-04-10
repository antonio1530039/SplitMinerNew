package Controlador;

import Modelo.BPMNModel;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;


public class WFG extends Observable{
    public LinkedHashMap<String, Integer> WFGantesSplits = new LinkedHashMap<>();
    public LinkedHashMap<String, Integer> WFGSplits = new LinkedHashMap<>();
    public LinkedHashMap<String, Integer> WFG = new LinkedHashMap<>();
    
    public List<Character> autoLoops = new LinkedList<Character>();
    public LinkedHashSet<String> shortLoops = new LinkedHashSet<String>();
    
    public BPMNModel BPMN = new BPMNModel();
    public String Notation = "";
    public String tasksDescription = "";
    
    public void notifyAction(){
        System.out.println("Notifying changes to observer ");
        super.setChanged();
        super.notifyObservers();
    }
    
    
}
