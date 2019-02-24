/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controlador;

import Modelo.BPMNModel;
import java.util.LinkedHashMap;
import java.util.Observable;

/**
 *
 * @author Antonio
 */
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
