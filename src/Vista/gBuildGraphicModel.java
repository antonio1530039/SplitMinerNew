package Vista;

import Controlador.WFG;
import Modelo.BPMNModel;
import Modelo.Element;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JFrame;

public class gBuildGraphicModel extends JFrame implements Observer{

    public void buildModel(BPMNModel BPMN, LinkedHashMap<String, Integer> WFG, String Text) {
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int ScreenWidth = (int) screenSize.getWidth();
        int ScreenHeight = (int) screenSize.getHeight();
        
        //posicion inicial del primer elemento en el canvas
        int PosX = ScreenWidth / 15;
        int PosY = ScreenHeight / 3;
        setTitle("Model");
        setSize(ScreenWidth, ScreenHeight);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        HashMap<String, Element> Elements = new HashMap<>();
        LinkedList<String> cloneTasks = new LinkedList<>();
        
        for(Character c : BPMN.T){
            cloneTasks.add(c.toString());
        }
         Elements = new HashMap<>();
       // HashMap<Character, Set<Character>> allSucesores = getAllSucesores();
        for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
            String vals[] = entry.getKey().split(",");
            
            String actual = vals[0];
            String sucesor = vals[1];
            
            //Procesar nodo actual
            if (!Elements.containsKey(actual)) {
                processElement(new Element(actual), PosX, PosY, cloneTasks, Elements, ScreenWidth, ScreenHeight);
                 PosX += ScreenWidth / 15;
        
                if(PosX >= ScreenWidth - (ScreenWidth/15)){
                    PosY += ScreenHeight / 10; //salto en caso de exceder el limite del ancho de la pantalla
                    PosX = ScreenWidth / 15; //posicion inicial de X
                }
            }
            
            //procesar sucesor
            if(!Elements.containsKey(sucesor)){
                Element Esucesor = new Element(sucesor);
                Esucesor.Antecesores.add(actual);
                processElement(Esucesor,  PosX, PosY, cloneTasks, Elements, ScreenWidth, ScreenHeight);
                 PosX += ScreenWidth / 15;
        
                if(PosX >= ScreenWidth - (ScreenWidth/15)){
                    PosY += ScreenHeight / 10; //salto en caso de exceder el limite del ancho de la pantalla
                    PosX = ScreenWidth / 15; //posicion inicial de X
                }
            }else{
                Elements.get(sucesor).Antecesores.add(actual);
            } 
        }
        add(new gJPanel(ScreenWidth, ScreenHeight, Elements, BPMN,Text)); //Agregar el JPanel, mandando en su contructor los elementos necesarios para la graficacion de los elementos (Elements)
    }

    public void processElement(Element e, int PosX, int PosY, LinkedList<String> cloneTasks, HashMap<String, Element> Elements, int ScreenWidth, int ScreenHeight) {
        e.cPosX = PosX;
        e.cPosY = PosY;
        if (cloneTasks.contains(e.Name)) {
            e.type = "Task";
            cloneTasks.remove(e.Name);
        } else {
            e.type = "Gateway";
        }
        Elements.put(e.Name, e);
            
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("Actualización de Observable!");
        WFG wfg = (WFG) o;
        System.out.println("WFG: " + wfg.WFG.toString());
        System.out.println("BPMN: " + wfg.BPMN.T.toString());
        System.out.println("Text: " + wfg.Notation);
        buildModel(wfg.BPMN, wfg.WFG, wfg.Notation);
    }

    
    
    

}
