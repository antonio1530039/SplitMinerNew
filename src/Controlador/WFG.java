package Controlador;

import Modelo.BPMNModel;
import java.util.LinkedHashMap;
import java.util.Observable;


public class WFG extends Observable{
    public LinkedHashMap<String, Integer> WFGantesSplits = new LinkedHashMap<>();
    public LinkedHashMap<String, Integer> WFGSplits = new LinkedHashMap<>();
    public LinkedHashMap<String, Integer> WFG = new LinkedHashMap<>();
    public BPMNModel BPMN = new BPMNModel();
    public String Notation = "";
    
    public void notifyAction(){
        System.out.println("Notifying changes to observer ");
        super.setChanged();
        super.notifyObservers();
    }
    
    
}
