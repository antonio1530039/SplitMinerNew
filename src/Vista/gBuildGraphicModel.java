/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vista;

import Modelo.BPMNModel;
import Modelo.Element;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JFrame;

/**
 *
 * @author Antonio
 */
public class gBuildGraphicModel extends JFrame {

    int ScreenWidth;
    int ScreenHeight;
    int PosX;
    int PosY;

    HashMap<String, Element> Elements;
    //Graph
    LinkedHashMap<String, Integer> WFG;
    //BPMN model
    BPMNModel BPMN;
    
    LinkedList<String> cloneTasks = new LinkedList<>();

    public gBuildGraphicModel(BPMNModel bpmn, LinkedHashMap<String, Integer> wfg, String text) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ScreenWidth = (int) screenSize.getWidth();
        ScreenHeight = (int) screenSize.getHeight();
        
        for(Character c : bpmn.T){
            cloneTasks.add(c.toString());
        }

        BPMN = bpmn;
        WFG = wfg;
        Elements = new HashMap<>();

        //posicion inicial del primer elemento en el canvas
        PosX = ScreenWidth / 15;
        PosY = ScreenHeight / 3;
        
        buildModel();
        setTitle("Model");
        setSize(ScreenWidth, ScreenHeight);
        add(new gJPanel(ScreenWidth, ScreenHeight, Elements, BPMN, text)); //Agregar el JPanel, mandando en su contructor los elementos necesarios para la graficacion de los elementos (Elements)
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void buildModel() {
       // HashMap<Character, Set<Character>> allSucesores = getAllSucesores();
        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            String vals[] = entry.getKey().split(",");
            
            String actual = vals[0];
            String sucesor = vals[1];
            
            //Procesar nodo actual
            if (!Elements.containsKey(actual)) {
                processElement(new Element(actual));
            }
            
            //procesar sucesor
            if(!Elements.containsKey(sucesor)){
                Element Esucesor = new Element(sucesor);
                Esucesor.Antecesores.add(actual);
                processElement(Esucesor);
            }else{
                Elements.get(sucesor).Antecesores.add(actual);
            } 
        }
    }

    public void processElement(Element e) {
        e.cPosX = PosX;
        e.cPosY = PosY;
        if (cloneTasks.contains(e.Name)) {
            e.type = "Task";
            cloneTasks.remove(e.Name);
        } else {
            e.type = "Gateway";
        }
        Elements.put(e.Name, e);
        PosX += ScreenWidth / 15;
        
        if(PosX >= ScreenWidth - (ScreenWidth/15)){
            PosY += ScreenHeight / 10; //salto en caso de exceder el limite del ancho de la pantalla
            PosX = ScreenWidth / 15; //posicion inicial de X
        }
            
    }

    
    
    

}
