package VistaEscritorio;

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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import javax.swing.JTextField;

public class gBuildGraphicModel extends JFrame implements Observer, ActionListener {

    /*
        Ventana que muestra el modelo gráfico del BPMN creado
    */
    
    private JRadioButton antesSplitsRadio, splitsRadio, todoRadio;
    private ButtonGroup bg;
    //Los siguientes tres mapas almacenan las versiones del grafo: antes de los splits, con los splits identificados y el grafo completo (con cierres identificados)
    private LinkedHashMap<String, Integer> WFGantesSplits; 
    private LinkedHashMap<String, Integer> WFGSplits;
    private LinkedHashMap<String, Integer> WFG;
    public List<Character> autoLoops = new LinkedList<>(); //Lista de autoloops
    public LinkedHashSet<String> shortLoops = new LinkedHashSet<>(); //lista de shortloops
    boolean autoloops = false; //indica si se deben mostrar los autoloops
    boolean shortloops = false;//indica si se deben mostrar los shortloops
    private LinkedList<String> showTasks; //Tareas que deben ser mostradas en el modelo gráfico
    private HashMap<String, ArrayList<Element>> quiebres= new HashMap<>();
    public int[] breaks = {1}; //Número de quiebres creados en el panel
    private HashMap<String, HashMap<String, Element>> ElementsSaved; //Mapa de elementos gráficos guardados (para que las posiciones de los elementos no se reinicien)

    BPMNModel BPMN;
    String currentMode = "";
    String tasksDescription = "";
    
    //Las siguientes dos variables almacenan las dimensiones de la pantalla
    int ScreenWidth;
    int ScreenHeight;
    
    int PosX; //Posición en X inicial de los elementos gráficos del BPMN
    int PosY; // ..

    JPanel jpanelGrafica = new JPanel();
    JPanel jpanelComponentes = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JPanel jpanelnotation = new JPanel();

    JPanel jpanelMenu = new JPanel(new BorderLayout());

    JTextArea notationTxt = new JTextArea();

    JTextArea tasksDescriptionTxt = new JTextArea(7, 25);

    JCheckBox autoloopsCheck = new JCheckBox("Autoloops");
    JCheckBox shortloopsCheck = new JCheckBox("Shortloops");
    
    ActionListener helpBtnAction;

    //Constructor
    public gBuildGraphicModel(LinkedList<Character> tasks) {
        //Tomar dimensiones de la pantalla e inicializar variables
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ScreenWidth = (int) screenSize.getWidth();
        ScreenHeight = (int) screenSize.getHeight();
        ElementsSaved = new HashMap<>();
        showTasks = new LinkedList<>();

        setTitle("Model");
        setSize(ScreenWidth, ScreenHeight);
        setVisible(true);
        breaks[0] = 0;

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
        todoRadio.setSelected(true);
        jpanelComponentes.add(todoRadio);
        bg.add(todoRadio);

        autoloopsCheck.setBounds(10, 10, widthComponent, 30);
        autoloopsCheck.addActionListener(this);
        jpanelComponentes.add(autoloopsCheck);

        shortloopsCheck.setBounds(10, 10, widthComponent, 30);
        shortloopsCheck.addActionListener(this);
        jpanelComponentes.add(shortloopsCheck);
        //Mostrar los checkboxs de las tareas
        for (Character t : tasks) {
            JCheckBox ch = new JCheckBox(t.toString());
            ch.setBounds(10, 10, widthComponent, 30);
            ch.setSelected(true);
            ch.addActionListener(this);
            jpanelComponentes.add(ch);
            showTasks.add(t.toString());
        }

        notationTxt.setBounds(5, 5, ScreenWidth, ScreenWidth);
        notationTxt.setText("");
        notationTxt.setFont(notationTxt.getFont().deriveFont(20f));
        notationTxt.setEditable(true);
        //Mostrar notación
        JTextField notationTitle = new JTextField("Notation: ");
        notationTitle.setBounds(5, 5, ScreenWidth, ScreenWidth);
        notationTitle.setFont(notationTitle.getFont().deriveFont(20f));
        notationTitle.setEditable(false);
        jpanelnotation.add(notationTitle);
        jpanelnotation.add(notationTxt);
        //Mostrar descripción de las tareas
        JPanel tasksPanel = new JPanel();
        tasksPanel.setBorder(new TitledBorder(new EtchedBorder(), "Tasks description"));

        tasksDescriptionTxt.setSize(new Dimension(screenSize.width / 5, screenSize.height / 7));
        //display3.setText(tasksDescription);
        JScrollPane scroll3 = new JScrollPane(tasksDescriptionTxt);
        scroll3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        tasksPanel.add(scroll3);

        tasksPanel.setPreferredSize(new Dimension(screenSize.width / 5, screenSize.height / 7));
        jpanelnotation.add(tasksPanel);
  
        JButton helpBtn = new JButton("How to create breaks?");
        JFrame main = this;
        helpBtnAction = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                
                JOptionPane.showMessageDialog(main, "* Double click on a line: creates a break\n * Double click on a break: hides the break\n * Right click in a break: deletes the break");

            }
        };
        
        helpBtn.addActionListener(helpBtnAction);
        
        jpanelComponentes.add(helpBtn);

        jpanelMenu.add(jpanelComponentes, BorderLayout.NORTH);
        jpanelMenu.add(jpanelnotation, BorderLayout.SOUTH);
        add(jpanelMenu, BorderLayout.NORTH);

    }

    /**
     * Procedimiento que construye el modelo BPMN, crea el mapa de elementos con las posiciones en el canvas
     * @param BPMN Modelo BPMN
     * @param WFG Grafo
     * @param mode Versiones del grafo, las guarda en un mapa para conservar las posiciones en X y Y cada que se realizan modificaciones con el mouse
     */
    public void buildModel(BPMNModel BPMN, LinkedHashMap<String, Integer> WFG, String mode) {
        this.remove(jpanelGrafica);
        currentMode = mode;
        HashMap<String, Element> elementsToPaint = null;
        if (ElementsSaved.containsKey(mode)) {
            elementsToPaint = (HashMap<String, Element>) ElementsSaved.get(mode).clone();
            List<Map.Entry<String, Element>> elems = new ArrayList(elementsToPaint.entrySet());
            for (Map.Entry<String, Element> entry : elems) {
                String key = entry.getKey();
                if (!showTasks.contains(key) && key.length() == 1) {
                    elementsToPaint.remove(entry.getKey());
                }
            }

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
                    Esucesor.Antecesores.put(actual, new ArrayList<Element>());
                    processElement(Esucesor, PosX, PosY, BPMN, Elements);

                } else {
                    Elements.get(realSucesor).Antecesores.put(actual, new ArrayList<Element>());
                }
            }
            elementsToPaint = (HashMap<String, Element>) Elements.clone();
            List<Map.Entry<String, Element>> elems = new ArrayList(elementsToPaint.entrySet());
            for (Map.Entry<String, Element> entry : elems) {
                String key = entry.getKey();
                if (!showTasks.contains(key) && key.length() == 1) {
                    elementsToPaint.remove(entry.getKey());

                }
            }

            ElementsSaved.put(mode, Elements);
        }

        //Verificar si los autoloops estan activados
        if (autoloops) {
            for (Character a : autoLoops) {
                Element e = elementsToPaint.get(a.toString());
                if(e!=null)
                    e.type = "Autoloop";
            }
        } else {
            for (Character a : autoLoops) {
                Element e = elementsToPaint.get(a.toString());
                if(e!=null)
                    e.type = "Task";
            }
        }

        for (String s : shortLoops) {
            String[] edge = s.split(",");

            if (shortloops) {
                
                if(elementsToPaint.containsKey(edge[0])){
                    //Verificar si se tienen quiebres guardados
                    ArrayList<Element> quiebres = this.quiebres.get(edge[1]);
                    if(quiebres!=null){
                        elementsToPaint.get(edge[0]).Antecesores.put(edge[1], this.quiebres.get(edge[1]) );
                    }else{
                        elementsToPaint.get(edge[0]).Antecesores.put(edge[1], new ArrayList<>() );
                    }
                }
                if(elementsToPaint.containsKey(edge[1])){
                    ArrayList<Element> quiebres = this.quiebres.get(edge[0]);
                    if(quiebres!=null){
                        elementsToPaint.get(edge[1]).Antecesores.put(edge[0], this.quiebres.get(edge[0]) );
                    }else{
                        elementsToPaint.get(edge[1]).Antecesores.put(edge[0], new ArrayList<>() );
                    }
                }
                    

            } else {
                if(elementsToPaint.containsKey(edge[0])){
                    //Antes de borrar, guardar quiebres
                    this.quiebres.put( edge[1]  , elementsToPaint.get(edge[0]).Antecesores.get(edge[1]));
                    elementsToPaint.get(edge[0]).Antecesores.remove(edge[1]);
                }
                    
                if(elementsToPaint.containsKey(edge[1])){
                    //Antes de borrar, guardar quiebres
                    this.quiebres.put( edge[0]  , elementsToPaint.get(edge[1]).Antecesores.get(edge[0]));
                    elementsToPaint.get(edge[1]).Antecesores.remove(edge[0]);
                }
                    

            }

        }
        //Crear panel con el modelo gráfico BPMN, envia como parametros las dimensiones de la pantalla y los elementos procesados a mostrar, así como el número de quiebres en la pantalla
        jpanelGrafica = new gJPanel(ScreenWidth, ScreenHeight, elementsToPaint, BPMN, breaks);
        add(jpanelGrafica); //Se agrega el panel a el Frame
        revalidate(); //Se realiza el repintado
    }

    /**
     * Función que dado un Elemento y sus posiciones lo procesa, asigna sus propiedades así como la posición en el canvas que tomará y el tipo (tarea, compuerta, autoloop, evento fin, evento inicio etc)
     * @param e Elemento
     * @param PosX
     * @param PosY
     * @param bpmn
     * @param Elements Mapa de elementos
     */
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

    /**
     * Función de la interfaz Observer, realiza la actualización tomando los valores del objeto WFG
     * @param o Objeto
     * @param arg 
     */
    @Override
    public void update(Observable o, Object arg) {
        System.out.println("Actualización de Observable");
        WFG wfg = (WFG) o;
        WFGantesSplits = wfg.WFGantesSplits;
        WFGSplits = wfg.WFGSplits;
        WFG = wfg.WFG;
        BPMN = wfg.BPMN;
        autoLoops = wfg.autoLoops;
        shortLoops = wfg.shortLoops;
        System.out.println("Shortloops: " + shortLoops.toString());
        notationTxt.setText(wfg.Notation);
        tasksDescriptionTxt.setText(wfg.tasksDescription);
        buildModel(BPMN, WFG, "All");
    }

    /**
     * Listener de acciones
     * @param e 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().getClass().isInstance(new JCheckBox())) { //Se verifica si es un checkbox sobre el cual se realizo la acción
            JCheckBox c = (JCheckBox) e.getSource();
            String label = c.getText();
            if (c.isSelected()) { //Se verifica si se da clic en activar autoloops
                if (label.equals("Autoloops")) { 
                    autoloops = true;
                } else if (label.equals("Shortloops")) {//Se verifica si se da clic en activar shortloops
                    shortloops = true;
                }
            } else {
                if (label.equals("Autoloops")) {
                    autoloops = false;
                } else if (label.equals("Shortloops")) {
                    shortloops = false;
                }
            }
            //Se agregan o remueven de las tareas a mostrar, segùn la selecciòn del usuario
            if (c.isSelected() && !showTasks.contains(label)) {
                showTasks.add(label);
            } else if (!c.isSelected()) {
                showTasks.remove(label);
            }
        }
        //Construir el modelo según la versión que eligió el usuario
        if (antesSplitsRadio.isSelected()) {
            buildModel(BPMN, WFGantesSplits, "BeforeSplits");
        }
        if (splitsRadio.isSelected()) {
            buildModel(BPMN, WFGSplits, "Splits");
        }
        if (todoRadio.isSelected()) {
            buildModel(BPMN, WFG, "All");
        }
    }

}
