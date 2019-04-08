package Vista;

import Controlador.WFG;
import Modelo.BPMNModel;
import Modelo.Element;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import javax.swing.JTextField;

public class gBuildGraphicModel extends JFrame implements Observer, ActionListener {

    private JRadioButton antesSplitsRadio, splitsRadio, todoRadio, loopsRadio;
    private ButtonGroup bg;

    private LinkedHashMap<String, Integer> WFGLoops;
    private LinkedHashMap<String, Integer> WFGantesSplits;
    private LinkedHashMap<String, Integer> WFGSplits;
    private LinkedHashMap<String, Integer> WFG;

    private LinkedList<String> showTasks;

    private HashMap<String, HashMap<String, Element>> ElementsSaved;

    BPMNModel BPMN;
    String currentMode = "";
    String tasksDescription = "";
    int ScreenWidth;
    int ScreenHeight;
    int PosX;
    int PosY;

    JPanel jpanelGrafica = new JPanel();
    JPanel jpanelComponentes = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JPanel jpanelnotation = new JPanel();

    JPanel jpanelMenu = new JPanel(new BorderLayout());

    JTextArea notationTxt = new JTextArea();

    JTextArea tasksDescriptionTxt = new JTextArea(7, 25);

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
        int widthComponent = (ScreenWidth / 15) - 10;

        antesSplitsRadio = new JRadioButton("Before splits");
        antesSplitsRadio.setBounds(10, 20, widthComponent, 30);
        antesSplitsRadio.addActionListener(this);
        jpanelComponentes.add(antesSplitsRadio);
        bg.add(antesSplitsRadio);
        splitsRadio = new JRadioButton("Splits");
        splitsRadio.setBounds(10, 50, widthComponent, 30);
        splitsRadio.addActionListener(this);
        jpanelComponentes.add(splitsRadio);
        bg.add(splitsRadio);
        todoRadio = new JRadioButton("All");
        todoRadio.setBounds(10, 80, widthComponent, 30);
        todoRadio.addActionListener(this);
        jpanelComponentes.add(todoRadio);
        bg.add(todoRadio);

        loopsRadio = new JRadioButton("All with loops");
        loopsRadio.setBounds(10, 80, widthComponent, 30);
        loopsRadio.setSelected(true);
        loopsRadio.addActionListener(this);
        jpanelComponentes.add(loopsRadio);
        bg.add(loopsRadio);
        int y = 110;
        for (Character t : tasks) {
            JCheckBox ch = new JCheckBox(t.toString());
            ch.setBounds(10, y, widthComponent, 30);
            ch.setSelected(true);
            ch.addActionListener(this);
            jpanelComponentes.add(ch);
            y += 30;
            showTasks.add(t.toString());
        }

        notationTxt.setBounds(5, 5, ScreenWidth, ScreenWidth);
        notationTxt.setText("");
        notationTxt.setFont(notationTxt.getFont().deriveFont(20f));
        notationTxt.setEditable(true);
        /*StyledDocument doc = notationTxt.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);*/
        JTextField notationTitle = new JTextField("Notation: ");
        notationTitle.setBounds(5, 5, ScreenWidth, ScreenWidth);
        notationTitle.setFont(notationTitle.getFont().deriveFont(20f));
        notationTitle.setEditable(false);
        jpanelnotation.add(notationTitle);
        jpanelnotation.add(notationTxt);

        JPanel middlePanel3 = new JPanel();
        middlePanel3.setBorder(new TitledBorder(new EtchedBorder(), "Tasks"));

        tasksDescriptionTxt.setSize(new Dimension(screenSize.width / 5, screenSize.height / 7));
        //display3.setText(tasksDescription);
        JScrollPane scroll3 = new JScrollPane(tasksDescriptionTxt);
        scroll3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        middlePanel3.add(scroll3);

        middlePanel3.setPreferredSize(new Dimension(screenSize.width / 5, screenSize.height / 7));
        jpanelnotation.add(middlePanel3);

        jpanelMenu.add(jpanelComponentes, BorderLayout.NORTH);
        jpanelMenu.add(jpanelnotation, BorderLayout.SOUTH);
        add(jpanelMenu, BorderLayout.NORTH);

    }

    public void buildModel(BPMNModel BPMN, LinkedHashMap<String, Integer> WFG, String mode) {
        this.remove(jpanelGrafica);
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
            jpanelGrafica = new gJPanel(ScreenWidth, ScreenHeight, elements, BPMN);
        } else {
            //posicion inicial del primer elemento en el canvas
            PosX = ScreenWidth / 15;
            PosY = ScreenHeight / 10;
            HashMap<String, Element> Elements = new HashMap<>();
            for (Map.Entry<String, Integer> entry : WFG.entrySet()) {
                String vals[] = entry.getKey().split(",");

                String actual = vals[0];
                String sucesor = vals[1];

                String realActual = (actual.charAt(0) == '@') ? actual.charAt(1) + "" : actual;
                String realSucesor = (sucesor.charAt(0) == '@') ? sucesor.charAt(1) + "" : sucesor;
                //Procesar nodo actual
                if (!Elements.containsKey(realActual)) {
                    processElement(new Element(actual), PosX, PosY, BPMN, Elements);
                }
                //procesar sucesor
                if (!Elements.containsKey(realSucesor)) {
                    Element Esucesor = new Element(sucesor);
                    Esucesor.Antecesores.add(actual);
                    processElement(Esucesor, PosX, PosY, BPMN, Elements);

                } else {
                    Elements.get(realSucesor).Antecesores.add(actual);
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
            jpanelGrafica = new gJPanel(ScreenWidth, ScreenHeight, elements, BPMN);
            ElementsSaved.put(mode, Elements);
        }
        add(jpanelGrafica);
        revalidate();
    }

    public void processElement(Element e, int PosX, int PosY, BPMNModel bpmn, HashMap<String, Element> Elements) {
        e.cPosX = PosX;
        e.cPosY = PosY;
        if (bpmn.Gor.contains(e.Name) || bpmn.Gxor.contains(e.Name) || bpmn.Gand.contains(e.Name)) {
            e.type = "Gateway";
        } else {

            if (bpmn.i.toString().equals(e.Name)) {
                e.type = "Start";
            } else if (bpmn.o.toString().equals(e.Name)) {
                e.type = "End";
            } else {

                if (e.Name.charAt(0) == '@') {
                    e.type = "Autoloop";
                } else {
                    e.type = "Task";
                }

            }

        }

        Elements.put((e.Name.charAt(0) == '@') ? e.Name.charAt(1) + "" : e.Name, e);
        this.PosX += this.ScreenWidth / 15;

        if (this.PosX >= this.ScreenWidth - (this.ScreenWidth / 15)) {
            this.PosY += this.ScreenHeight / 10; //salto en caso de exceder el limite del ancho de la pantalla
            this.PosX = this.ScreenWidth / 15; //posicion inicial de X
        }

    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("Actualización de Observable!");
        WFG wfg = (WFG) o;
        WFGantesSplits = wfg.WFGantesSplits;
        WFGSplits = wfg.WFGSplits;
        WFG = wfg.WFGAll;
        WFGLoops = wfg.WFGLoops;
        BPMN = wfg.BPMN;
        notationTxt.setText(wfg.Notation);
        tasksDescriptionTxt.setText(wfg.tasksDescription);
        buildModel(BPMN, WFGLoops, "All with loops");
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

            if (currentMode.equals("All with loops")) {
                buildModel(BPMN, WFGLoops, "All with loops");
            } else if (currentMode.equals("All")) {
                buildModel(BPMN, WFG, "All");
            } else if (currentMode.equals("Splits")) {
                buildModel(BPMN, WFGSplits, "Splits");
            } else {
                buildModel(BPMN, WFGantesSplits, "BeforeSplits");
            }

        } else {
            if (antesSplitsRadio.isSelected()) {
                buildModel(BPMN, WFGantesSplits, "BeforeSplits");
            }
            if (splitsRadio.isSelected()) {
                buildModel(BPMN, WFGSplits, "Splits");
            }
            if (todoRadio.isSelected()) {
                buildModel(BPMN, WFG, "All");
            }
            if (loopsRadio.isSelected()) {
                buildModel(BPMN, WFGLoops, "All with loops");
            }
        }
    }

}
