package splitminer;

/**
 * @author H�ctor Al�n De La Fuente Anaya.
 * @version 1
 * @since Diciembre 2018
 */
import Controlador.BPMNFiles;
import Controlador.FilesManagement;
import Controlador.GenerarGrafo;
import Controlador.PostProcesarGrafo;
import Controlador.PreProcesarGrafo;
import Controlador.SplitsFinder;
import Controlador.WFG;
import Modelo.BPMNModel;
import Vista.gBuildGraphicModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.SwingUtilities;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.TextField;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import javax.swing.JSeparator;

// Clases de la libreria
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class VisualizadorDeProcesos {

    public static void main(String args[]) {
        ProcessViewer processViewer = new ProcessViewer();
    }
}

class ProcessViewer {

    private JFrame main_frm;
    private File fileName;
    private TextField epsilon_textField = new TextField("0.3", 5), percentil_textField = new TextField("0.4", 5);
    private JPanel loadFile_pnl, menu_pnl, viewer_pnl, raw_pnl;
    private JButton information_btn, traces_btn, activities_btn, loadFile_btn, exportAsCSV_btn, deployment_btn, model_btn, exportAsXES_btn, execute_btn;
    private JLabel titulo_txt, epsilon_txt, percentil_txt;
    private DefaultTableModel traces_dtm, activities_dtm, information_dtm, model_dtm;
    private ActionListener loadFile_btnAction, information_btnAction, traces_btnAction, activities_btnAction, exportAsCSV_btnAction, exportAsXES_btnAction, model_btnAction, deployment_btnAction, execute_btnAction;
    private boolean activitiesSelected, tracesSelected, informationSelected, modelSelected, deploymentSelected;
    private String deployment;
    Dimension screenSize;

    BPMNModel BPMN;
    LinkedHashMap<String, Integer> WFG = new LinkedHashMap<>();

    public ProcessViewer() {
        Toolkit t = Toolkit.getDefaultToolkit();
        screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Se consigue el tama�o de la ventana
        // Valores de inicio de porgrama
        activitiesSelected = true;
        tracesSelected = true;
        informationSelected = true;
        modelSelected = true;
        deploymentSelected = true;
        deployment = "";
        fileName = null;

        // Se generan las acciones
        initializeActions();

        // Se arma la interfaz
        buildLoadFile();
        buildMenu();
        buildRaw();
        buildViewer();
        buildWindow();

    }

    private void buildLoadFile() {
        // Se iicializa el panel
        loadFile_pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loadFile_pnl.setBackground(Color.black);

        // Se generan los elementos
        titulo_txt = new JLabel();
        titulo_txt.setForeground(Color.white);
        titulo_txt.setFont(new Font("Tahoma", Font.BOLD, 15));

        epsilon_txt = new JLabel("Epsilon:");
        epsilon_txt.setForeground(Color.white);
        epsilon_txt.setFont(new Font("Tahoma", Font.BOLD, 15));

        percentil_txt = new JLabel("Percentil:");
        percentil_txt.setForeground(Color.white);
        percentil_txt.setFont(new Font("Tahoma", Font.BOLD, 15));

        loadFile_pnl.add(epsilon_txt);
        loadFile_pnl.add(epsilon_textField);

        loadFile_pnl.add(percentil_txt);
        loadFile_pnl.add(percentil_textField);

        if (fileName != null) {
            titulo_txt.setText(fileName.getName());
            loadFile_pnl.add(titulo_txt);
            if (fileName.getAbsolutePath().contains(".xes")) {
                exportAsCSV_btn = new JButton("Export as CSV");
                exportAsCSV_btn.addActionListener(exportAsCSV_btnAction);
                loadFile_pnl.add(exportAsCSV_btn);
            }
        } else {
            titulo_txt.setText("No selected file");
            loadFile_pnl.add(titulo_txt);
        }

        if (WFG.size()>0) {
            exportAsXES_btn = new JButton("Export as BPMN 2.0");
            exportAsXES_btn.addActionListener(exportAsXES_btnAction);
            loadFile_pnl.add(exportAsXES_btn);
        }

        loadFile_btn = new JButton("Load File");
        loadFile_btn.addActionListener(loadFile_btnAction);
        loadFile_pnl.add(loadFile_btn);

        execute_btn = new JButton("Execute");
        execute_btn.addActionListener(execute_btnAction);
        loadFile_pnl.add(execute_btn);

    }

    private void buildMenu() {
        // Se inicializa el panel
        menu_pnl = new JPanel();

        // Se inicializan los botones del menu
        information_btn = new JButton("Description");
        traces_btn = new JButton("Traces");
        activities_btn = new JButton("Activities");
        deployment_btn = new JButton("Output Model");
        model_btn = new JButton("Model");

        // Se asigan acciones a los botones
        information_btn.addActionListener(information_btnAction);
        activities_btn.addActionListener(activities_btnAction);
        traces_btn.addActionListener(traces_btnAction);
        model_btn.addActionListener(model_btnAction);
        deployment_btn.addActionListener(deployment_btnAction);

        // Se le asignan el corresponediente color para identificar que los han seleccionado
        if (informationSelected) {
            information_btn.setBackground(Color.LIGHT_GRAY);
        } else {
            information_btn.setBackground(new JButton().getBackground());
        }
        if (activitiesSelected) {
            activities_btn.setBackground(Color.LIGHT_GRAY);
        } else {
            activities_btn.setBackground(new JButton().getBackground());
        }
        if (tracesSelected) {
            traces_btn.setBackground(Color.LIGHT_GRAY);
        } else {
            traces_btn.setBackground(new JButton().getBackground());
        }
        if (modelSelected) {
            model_btn.setBackground(Color.LIGHT_GRAY);
        } else {
            model_btn.setBackground(new JButton().getBackground());
        }
        if (deploymentSelected) {
            deployment_btn.setBackground(Color.LIGHT_GRAY);
        } else {
            deployment_btn.setBackground(new JButton().getBackground());
        }

        // Se asigna un tama�o
        information_btn.setPreferredSize(new Dimension(150, 30));
        activities_btn.setPreferredSize(new Dimension(150, 30));
        traces_btn.setPreferredSize(new Dimension(150, 30));
        deployment_btn.setPreferredSize(new Dimension(150, 30));
        model_btn.setPreferredSize(new Dimension(150, 30));

        // Se genera el contenedor
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        JPanel buttons_pnl = new JPanel(new GridBagLayout());

        // Se agregan los botones al contenedor
        buttons_pnl.add(new JPanel(), gbc);
        buttons_pnl.add(information_btn, gbc);
        buttons_pnl.add(new JPanel(), gbc);
        buttons_pnl.add(activities_btn, gbc);
        buttons_pnl.add(new JPanel(), gbc);
        buttons_pnl.add(traces_btn, gbc);
        buttons_pnl.add(new JPanel(), gbc);

        //Se agrega un Separador
        buttons_pnl.add(new JSeparator(), gbc);
        buttons_pnl.add(new JPanel(), gbc);

        // Se siguen agregando botones
        buttons_pnl.add(model_btn, gbc);
        buttons_pnl.add(new JPanel(), gbc);
        buttons_pnl.add(deployment_btn, gbc);
        buttons_pnl.add(new JPanel(), gbc);

        // Se agrega el contenedor al panel
        menu_pnl.add(buttons_pnl);
    }

    /*
    * Funcion para generar un panel para visualizar tablas con barra de scroll
    **/
    JPanel buildTablePanel(DefaultTableModel dtb, String title) {
        /* Se inicializa la tablas*/
        JLabel titleTable_txt = new JLabel(title);
        titleTable_txt.setFont(new Font("Tahoma", Font.BOLD, 18));
        JScrollPane tbl_sp = new JScrollPane(new JTable(dtb));

        /* Se prepara el panel*/
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnl.add(titleTable_txt, BorderLayout.NORTH);
        pnl.add(tbl_sp, BorderLayout.CENTER);
        pnl.setSize(100, 110);

        return pnl;
    }

    private void buildRaw() {

        raw_pnl = new JPanel();

        JPanel north = new JPanel();

        if (informationSelected) {
            JPanel j = buildTablePanel(information_dtm, "Description");
            j.setPreferredSize(new Dimension(100, screenSize.height / 6));
            north.add(j);
        }
        north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));

        JPanel south = new JPanel();
        if (activitiesSelected) {
            JPanel j = buildTablePanel(activities_dtm, "Activities");
            j.setPreferredSize(new Dimension(screenSize.width / 10, 100));
            south.add(j);
        }
        if (tracesSelected) {
            south.add(buildTablePanel(traces_dtm, "Traces"));
        }
        if (modelSelected) {
            south.add(buildTablePanel(model_dtm, "BPMN Model"));
        }
        south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
        raw_pnl.add(north);
        raw_pnl.add(south);

        if (deploymentSelected) {
            JPanel feet = new JPanel();

            JTextPane editor = new JTextPane();
            editor.setEditable(false);
            editor.setSize(400, 20);
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setBold(attrs, true);
            StyleConstants.setFontFamily(attrs, "Courier New");
            try {
                editor.getStyledDocument().insertString(editor.getStyledDocument().getLength(), deployment, attrs);
            } catch (Exception e) {

            }
            JScrollPane dep_sp = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            dep_sp.setMinimumSize(new Dimension(10, 40));
            /* Se prepara el panel Raw*/
            JPanel pnl = new JPanel(new BorderLayout());
            pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JLabel title = new JLabel("Output Model");
            title.setFont(new Font("Tahoma", Font.BOLD, 18));

            pnl.add(title, BorderLayout.NORTH);
            pnl.add(dep_sp, BorderLayout.CENTER);

            feet.add(pnl);
            feet.setLayout(new BoxLayout(feet, BoxLayout.X_AXIS));

            raw_pnl.add(feet);
        }

        raw_pnl.setLayout(new BoxLayout(raw_pnl, BoxLayout.Y_AXIS));
    }

    private void buildViewer() {
        viewer_pnl = new JPanel(new CardLayout());
        viewer_pnl.add(raw_pnl, "raw");
    }

    private void buildWindow() {
        main_frm = new JFrame("Process Viewer");
        main_frm.setLayout(new BorderLayout());
        main_frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        main_frm.setSize(screenSize.width / 3 * 2, screenSize.height / 3 * 2);
        main_frm.setMaximumSize(new Dimension(screenSize.width, screenSize.height));
        main_frm.setMinimumSize(new Dimension(screenSize.width / 3 * 2, screenSize.height / 3 * 2));
        main_frm.setLocationRelativeTo(null);
        main_frm.setAlwaysOnTop(false);

        main_frm.add(loadFile_pnl, BorderLayout.NORTH);
        main_frm.add(menu_pnl, BorderLayout.WEST);
        main_frm.add(viewer_pnl, BorderLayout.CENTER);
        main_frm.pack();

        main_frm.setVisible(true);
    }

    private void initializeActions() {

        execute_btnAction = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.out.println("Clic en execute");

                if (fileName != null) {
                    execute(fileName.getAbsolutePath());
                    refreshWindow();
                } else {
                    System.out.println("No file selected");
                }

            }
        };

        /* Accion del boton LoadFile */
        loadFile_btnAction
                = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                /* Se escoge el archivo */
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setDialogTitle("Select a dataset file");
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.setFileHidingEnabled(true);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TXT, CSV & XES files", "txt", "csv", "xes"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TXT files", "txt"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("XES files", "xes"));

                if (fileName != null) {
                    fileChooser.setCurrentDirectory(fileName);
                }

                int result = fileChooser.showOpenDialog(fileChooser);

                if (result != JFileChooser.CANCEL_OPTION) {

                    fileName = fileChooser.getSelectedFile();

                    if ((fileName == null) || (fileName.getName().equals(""))) {
                        System.out.println("Error en el archivo.");

                    } else {
                        System.out.println(fileName.getAbsolutePath());
                        System.out.println(fileName.getName());
                        if (!fileName.getName().endsWith(".txt") && !fileName.getName().endsWith(".csv") && !fileName.getName().endsWith(".xes")) {
                            System.out.println("El tipo de archivo de entrada no es valido.");
                        }
                    }
                    refreshWindow();
                }
                if (fileName != null) {
                    if (fileName.getName().endsWith(".xes")) {
                        XFactoryBufferedImpl factory = new XFactoryBufferedImpl();
                        XesXmlParser xesXmlParser = new XesXmlParser();

                        try {
                            List<XLog> list = xesXmlParser.parse(new File(fileName.getAbsolutePath()));

                            XAttributeMap xam = list.get(0).get(0).get(0).getAttributes();
                            ArrayList<String> gea = new ArrayList<String>();
                            xam.forEach((k, v) -> gea.add(k));

                            System.out.println(gea.size());
                            for (int i = 0; i < gea.size(); i++) {
                                System.out.println(gea.get(i));
                            }

                            //List<XAttribute> gea = list.get(0).getGlobalEventAttributes();
                            String[] columnNames = new String[gea.size() + 1];
                            ArrayList<String[]> filas = new ArrayList<String[]>();

                            columnNames[0] = "Case ID";
                            for (int j = 1; j < gea.size() + 1; j++) {
                                columnNames[j] = gea.get(j - 1);
                            }

                            for (int i = 0; i < list.size(); i++) {
                                List<XTrace> le = list.get(i);

                                for (int j = 0; j < le.size(); j++) {
                                    XTrace trace = le.get(j);

                                    XAttribute a = trace.getAttributes().get("concept:name");
                                    if (a == null) {
                                        a = trace.getAttributes().get("Case ID");
                                    }

                                    for (int k = 0; k < trace.size(); k++) {
                                        XEvent event = trace.get(k);

                                        String[] fila = new String[gea.size() + 1];
                                        fila[0] = a + "";

                                        for (int l = 1; l <= gea.size(); l++) {
                                            fila[l] = event.getAttributes().get(gea.get(l - 1)) + "";
                                        }
                                        filas.add(fila);
                                    }
                                }
                            }

                            // Se juntan las filas en una matriz
                            String[][] data = new String[filas.size()][columnNames.length];
                            for (int i = 0; i < filas.size(); i++) {
                                for (int j = 0; j < columnNames.length; j++) {
                                    data[i][j] = filas.get(i)[j];
                                }
                            }
                            
                            refreshWindow();

                        } catch (Exception ex) {
                            System.out.println(ex);
                        }

                    }
                }
            }

        };

        exportAsXES_btnAction
                = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                /* Se escoge el directorio destino para el archivo */
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setDialogTitle("Select the destination path");
                int result = fileChooser.showOpenDialog(fileChooser);

                File filePath = null;
                if (result != JFileChooser.CANCEL_OPTION) {
                    filePath = fileChooser.getSelectedFile();
                    if ((filePath == null) || (filePath.getAbsolutePath().equals(""))) {
                        System.out.println("Error la ruta.");
                    } else {
                        System.out.println(filePath.getAbsolutePath());
                    }
                }

                if (filePath != null) {
                    BPMNFiles bpmnFiles = new BPMNFiles(WFG, BPMN, filePath.getAbsolutePath() + "/" + fileName.getName().substring(0, fileName.getName().indexOf(".")));
                }

                // Se reconstruye la ventana
                refreshWindow();
            }
        };

        traces_btnAction
                = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (tracesSelected) {
                    tracesSelected = false;
                } else {
                    tracesSelected = true;
                }

                // Se reconstruye la ventana
                refreshWindow();
            }
        };

        information_btnAction
                = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (informationSelected) {
                    informationSelected = false;
                } else {
                    informationSelected = true;
                }

                // Se reconstruye la ventana
                refreshWindow();
            }
        };

        activities_btnAction
                = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (activitiesSelected) {
                    activitiesSelected = false;
                } else {
                    activitiesSelected = true;
                }

                // Se reconstruye la ventana
                refreshWindow();
            }
        };

        model_btnAction
                = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (modelSelected) {
                    modelSelected = false;
                } else {
                    modelSelected = true;
                }

                // Se reconstruye la ventana
                refreshWindow();
            }
        };

        deployment_btnAction
                = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (deploymentSelected) {
                    deploymentSelected = false;
                } else {
                    deploymentSelected = true;
                }

                // Se reconstruye la ventana
                refreshWindow();
            }
        };

    }

    // Funcion que procesa la informaci�n del Dataset 
    public void readDataInput(String filename) throws Exception {

    }

    public void execute(String filename) {
        double epsilon = 0.0, umbral = 0.0;

        //double umbral = 0.4; //descarta edges con frecuencia menor a este umbral he manejado hasta 25
        //double epsilon = 0.3;
        
        
        if (!epsilon_textField.getText().equals("") && !percentil_textField.getText().equals("")) {
            try{
                epsilon = Double.parseDouble(epsilon_textField.getText());
                umbral = Double.parseDouble(percentil_textField.getText());
            }catch(Exception e){
                System.out.println("Epsilon o percentil inválidos! Ingrese números solamente. " + e.getMessage());
                return;
            }
        }else{
            System.out.println("Ingrese un número epsilon y percentil");
        }

        System.out.println("Epsilon: " + epsilon);
        System.out.println("percentil: " + umbral);

        //P1.txt NOTATION: a AND{  XOR{  c, h}, b XOR{  e, g}} d f
        //final String filename = "P1.txt";
        WFG wfg = new WFG(); //Modelo: grafo y modelo BPMN

        LinkedHashMap<Integer, ArrayList<Character>> tracesList; //lista de trazas

        FilesManagement f = new FilesManagement(wfg.BPMN);
        ///////
        System.out.println("PASO 1: LEER TRAZAS DEL ARCHIVO DE ENTRADA '" + filename + "' E IDENTIFICAR TAREAS.");

        try {
            if (filename.endsWith(".txt")) {
                tracesList = f.readDataInputTrazas(filename);
            } else if (filename.endsWith(".csv")) {
                tracesList = f.readDataInput(filename);
            } else {
                System.out.println("El tipo de archivo de entrada no es valido...EXIT");
                return;
            }
        } catch (Exception e) {
            System.out.println("El archivo '" + filename + "' no se puede abrir. Exit");
            return;
        }

        ///////
        // Se prepara la tabla Actividades
        String[] columnNames = new String[]{"Description", "Item"};
        String[][] data = new String[f.ActivityList.size()][2];
        int i = 0;
        for (Map.Entry<String, Character> entry1 : f.ActivityList.entrySet()) {
            data[i][0] = entry1.getKey();
            data[i][1] = entry1.getValue().toString();
            i++;
        }
        // Modelo para la table activities
        activities_dtm = new DefaultTableModel(data, columnNames);
        ////////
        // Se prepara la tabla traces
        String[] columnNamesTraces = new String[]{"ID", "Traces"};
        String[][] dataTraces = new String[tracesList.size()][2];
        i = 0;
        System.out.println("\t3. Mostrando TRAZAS IDENTIFICADAS  en el archivo '" + filename + "'.");
        for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
            System.out.println("\t\t" + entry.getKey() + " - " + entry.getValue());
            dataTraces[i][0] = entry.getKey().toString();
            dataTraces[i][1] = entry.getValue().toString();
            i++;
        }

        // Modelo para la table traces
        traces_dtm = new DefaultTableModel(dataTraces, columnNamesTraces);

        // Se prepara el modelo para la table infrmation
        String[] columnNamesInfo = new String[]{"", ""};
        String[][] columnNamesdataInfo = f.showDataInfo(tracesList);
        information_dtm = new DefaultTableModel(columnNamesdataInfo, columnNamesInfo);

        ///////
        System.out.println("\n");
        System.out.println("PASO2: INCIANDO LA CONSTRUCCION DEL GRAFO QUE MODELA EL CONJUNTO DE TRAZAS.\n");

        GenerarGrafo generarGrafo = new GenerarGrafo();
        generarGrafo.computeGraph(tracesList, wfg.WFG);

        System.out.println("\nPASO 3: PREPROCESAMIENTO DEL GRAFO");

        PreProcesarGrafo preprocesarGrafo = new PreProcesarGrafo(wfg.BPMN, wfg.WFG, tracesList, generarGrafo.firsts, generarGrafo.lasts, umbral, epsilon);
        
        wfg.WFGantesSplits = (LinkedHashMap)wfg.WFG.clone();
        
        /////////
        System.out.println("\nPASO 4: CONSTRUCCION DEL MODELO BPMN");

        SplitsFinder crearModelo = new SplitsFinder(wfg.BPMN, generarGrafo.firsts, generarGrafo.lasts, wfg.WFG, preprocesarGrafo.parallelRelations);
        
        wfg.WFGSplits = (LinkedHashMap) wfg.WFG.clone();
        
        /////////
        System.out.println("\nPASO 5: POST-PROCESAMIENTO");

        //g1.postProcesamiento(BPMN);
        PostProcesarGrafo postprocesamiento = new PostProcesarGrafo(wfg.BPMN, wfg.WFG, preprocesarGrafo.autoLoops);

        wfg.Notation = postprocesamiento.notation;

        System.out.println("Notacion al final: " + wfg.Notation);

        deployment = wfg.Notation;

        // Se prepara la tabla Modelo
        String[] columnNamesModel = new String[]{"Key", "Value"};
        String[][] dataModel = new String[wfg.WFG.size()][2];
        i = 0;
        for (Map.Entry<String, Integer> entry : wfg.WFG.entrySet()) {
            dataModel[i][0] = "[" + entry.getKey() + "]";
            dataModel[i][1] = String.valueOf(entry.getValue());
            i++;
        }

        // Model para la tabla Modelo
        model_dtm = new DefaultTableModel(dataModel, columnNamesModel);
        ///////
        
        System.out.println("BPMN T: " + wfg.BPMN.T.toString());
        
        gBuildGraphicModel view = new gBuildGraphicModel(wfg.BPMN.T); //Instancia de la vista
        wfg.addObserver(view); //Agregar observador al modelo

        this.WFG = wfg.WFG; //asignar el valor actual del grafo (motivos de exportacion de modelo a archivo XML BPMN 2.0)
        this.BPMN = wfg.BPMN;//asignar el valor actual del modelo BPMN (motivos de exportacion de modelo a archivo XML BPMN 2.0)
        wfg.notifyAction(); //notificar que el modelo tuvo cambios
    }

    void refreshWindow() {
        // Se quitan los paneles del frame
        main_frm.remove(loadFile_pnl);
        main_frm.remove(menu_pnl);
        main_frm.remove(viewer_pnl);

        // Se construyen y revalidan los frames
        buildLoadFile();
        loadFile_pnl.validate();
        buildMenu();
        menu_pnl.validate();
        buildRaw();
        raw_pnl.validate();
        buildViewer();
        viewer_pnl.validate();

        // Se agregan los nuevos paneles al frame
        main_frm.add(loadFile_pnl, BorderLayout.NORTH);
        main_frm.add(menu_pnl, BorderLayout.WEST);
        main_frm.add(viewer_pnl, BorderLayout.CENTER);

        // Se revalida el frame
        SwingUtilities.updateComponentTreeUI(main_frm);

    }

}
