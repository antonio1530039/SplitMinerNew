package Vista;

import Controlador.WFG;
import Modelo.BPMNModel;
import Modelo.Element;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class gBuildGraphicModel extends JFrame implements Observer, ActionListener {

    private JRadioButton antesSplitsRadio, splitsRadio, todoRadio;
    private ButtonGroup bg;

    private LinkedHashMap<String, Integer> WFGantesSplits;
    private LinkedHashMap<String, Integer> WFGSplits;
    private LinkedHashMap<String, Integer> WFG;

    private LinkedList<String> showTasks;

    private HashMap<String, HashMap<String, Element>> ElementsSaved;

    BPMNModel BPMN;
    String Notation = "";
    String currentMode = "";
    int ScreenWidth;
    int ScreenHeight;
    int PosX;
    int PosY;

    JPanel panel = new JPanel();

    public gBuildGraphicModel(LinkedList<Character> tasks) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ScreenWidth = (int) screenSize.getWidth();
        ScreenHeight = (int) screenSize.getHeight();
        ElementsSaved = new HashMap<>();
        showTasks = new LinkedList<>();

        setTitle("Model");
        setSize(ScreenWidth, ScreenHeight);
        setVisible(true);
        
        bg = new ButtonGroup();
        int widthComponent = (ScreenWidth /15) - 10;
        antesSplitsRadio = new JRadioButton("Antes de splits");
        antesSplitsRadio.setBounds(10, 20, widthComponent, 30);
        antesSplitsRadio.addActionListener(this);
        add(antesSplitsRadio);
        bg.add(antesSplitsRadio);
        splitsRadio = new JRadioButton("Splits");
        splitsRadio.setBounds(10, 50, widthComponent, 30);
        splitsRadio.addActionListener(this);
        add(splitsRadio);
        bg.add(splitsRadio);
        todoRadio = new JRadioButton("Todo");
        todoRadio.setBounds(10, 80, widthComponent, 30);
        todoRadio.setSelected(true);
        todoRadio.addActionListener(this);
        add(todoRadio);
        bg.add(todoRadio);
        int y = 110;
        for (Character t : tasks) {
            JCheckBox ch = new JCheckBox(t.toString());
            ch.setBounds(10, y, widthComponent, 30);
            ch.setSelected(true);
            ch.addActionListener(this);
            add(ch);
            y += 30;
            showTasks.add(t.toString());
        }

    }

    public void buildModel(BPMNModel BPMN, LinkedHashMap<String, Integer> WFG, String Text, String mode) {
        this.remove(panel);
        currentMode = mode;
        if (ElementsSaved.containsKey(mode)) {
            HashMap<String, Element> elements = (HashMap<String, Element>) ElementsSaved.get(mode).clone();
            List<Map.Entry<String, Element>> elems = new ArrayList(elements.entrySet());
            for (Map.Entry<String, Element> entry : elems) {
                String key = entry.getKey();
                if (!showTasks.contains(key) && key.length() == 1) {
                    elements.remove(entry.getKey());
                }
            }
            panel = new gJPanel(ScreenWidth, ScreenHeight, elements, BPMN, Text);
        } else {
            //posicion inicial del primer elemento en el canvas
            PosX = ScreenWidth / 15;
            PosY = ScreenHeight / 3;
            HashMap<String, Element> Elements = new HashMap<>();
            Elements = new HashMap<>();
            for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
                String vals[] = entry.getKey().split(",");

                String actual = vals[0];
                String sucesor = vals[1];

                //Procesar nodo actual
                if (!Elements.containsKey(actual)) {
                    processElement(new Element(actual), PosX, PosY, BPMN.T, Elements, ScreenWidth, ScreenHeight);
                    PosX += ScreenWidth / 15;

                    if (PosX >= ScreenWidth - (ScreenWidth / 15)) {
                        PosY += ScreenHeight / 10; //salto en caso de exceder el limite del ancho de la pantalla
                        PosX = ScreenWidth / 15; //posicion inicial de X
                    }
                }
                //procesar sucesor
                if (!Elements.containsKey(sucesor)) {
                    Element Esucesor = new Element(sucesor);
                    Esucesor.Antecesores.add(actual);
                    processElement(Esucesor, PosX, PosY, BPMN.T, Elements, ScreenWidth, ScreenHeight);
                    PosX += ScreenWidth / 15;

                    if (PosX >= ScreenWidth - (ScreenWidth / 15)) {
                        PosY += ScreenHeight / 10; //salto en caso de exceder el limite del ancho de la pantalla
                        PosX = ScreenWidth / 15; //posicion inicial de X
                    }
                } else {
                    Elements.get(sucesor).Antecesores.add(actual);
                }
            }
            HashMap<String, Element> elements = (HashMap<String, Element>) Elements.clone();
            List<Map.Entry<String, Element>> elems = new ArrayList(elements.entrySet());
            for (Map.Entry<String, Element> entry : elems) {
                String key = entry.getKey();
                if (!showTasks.contains(key) && key.length() == 1) {
                    elements.remove(entry.getKey());
                }
            }
            panel = new gJPanel(ScreenWidth, ScreenHeight, elements, BPMN, Text);
            ElementsSaved.put(mode, Elements);
        }
        add(panel);
        revalidate();
    }

    public void processElement(Element e, int PosX, int PosY, LinkedList<Character> tasks, HashMap<String, Element> Elements, int ScreenWidth, int ScreenHeight) {
        e.cPosX = PosX;
        e.cPosY = PosY;
        if (tasks.contains(e.Name.charAt(0))) {
            e.type = "Task";
        } else {
            e.type = "Gateway";
        }
        Elements.put(e.Name, e);

    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("Actualización de Observable!");
        WFG wfg = (WFG) o;
        WFGantesSplits = wfg.WFGantesSplits;
        WFGSplits = wfg.WFGSplits;
        WFG = wfg.WFG;
        BPMN = wfg.BPMN;
        Notation = wfg.Notation;
        buildModel(BPMN, WFG, Notation, "All");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().getClass().isInstance(new JCheckBox())) {
            JCheckBox c = (JCheckBox) e.getSource();
            if (c.isSelected() && !showTasks.contains(c.getText())) {
                showTasks.add(c.getText());
            } else if (!c.isSelected()) {
                showTasks.remove(c.getText());
            }
            
            if (currentMode.equals("All")) {
                buildModel(BPMN, WFG, Notation, "All");
            } else if (currentMode.equals("Splits")) {
                buildModel(BPMN, WFGSplits, Notation, "Splits");
            } else {
                buildModel(BPMN, WFGSplits, Notation, "BeforeSplits");
            }

        } else {
            if (antesSplitsRadio.isSelected()) {
                buildModel(BPMN, WFGantesSplits, Notation, "BeforeSplits");
            }
            if (splitsRadio.isSelected()) {
                buildModel(BPMN, WFGSplits, Notation, "Splits");
            }
            if (todoRadio.isSelected()) {
                buildModel(BPMN, WFG, Notation, "All");
            }
        }
    }

}
