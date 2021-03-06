package Vista.Web;

/**
 * @author H�ctor Al�n De La Fuente Anaya.
 * @version 1
 * @since Diciembre 2018
 */
import Controlador.*;
import Modelo.BPMNModel;
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
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.swing.JApplet;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.table.TableColumnModel;
import static org.codehaus.groovy.tools.shell.util.Logger.io;

public class ProcessViewer extends JApplet {

    private File fileName;
    private JTextField epsilon_textField = new JTextField("0.3", 5), percentil_textField = new JTextField("0.4", 5);
    private JPanel loadFile_pnl, menu_pnl, viewer_pnl, raw_pnl;
    private JButton information_btn, traces_btn, activities_btn, loadFile_btn, exportAsCSV_btn, deployment_btn, model_btn, exportAsBPMN_btn, mine_btn, convertToXES_btn, filtering_btn;
    private JLabel titulo_txt, epsilon_txt, percentil_txt;
    private DefaultTableModel traces_dtm, activities_dtm, information_dtm, model_dtm;
    private ActionListener loadFile_btnAction, information_btnAction, traces_btnAction, activities_btnAction, exportAsCSV_btnAction, exportAsBPMN_btnAction, model_btnAction, deployment_btnAction, mine_btnAction, convertToXES_btnAction, filtering_btnAction;
    private boolean activitiesSelected, tracesSelected, informationSelected, modelSelected, deploymentSelected, wasMined, wasLoaded;
    private String deployment;
    Dimension screenSize;
    WFG wfg = new WFG(); //Modelo: grafo y modelo BPMN
    LinkedHashMap<Integer, ArrayList<Character>> tracesList = null;

    JPanel Panel1 = new JPanel();
    JPanel Panel2 = new JPanel();
    JPanel Panel3 = new JPanel();

    String tasksDescription = "";
    //Longitud de texto de columna (para ajustar tamaño en las tablas)
    int biggerTrace = 0;
    int biggerActivity1 = 0;
    int biggerActivity0 = 0;

    int biggerModel0 = 0;
    int biggerModel1 = 0;

    BPMNModel BPMN;
    LinkedHashMap<String, Integer> WFG = new LinkedHashMap<>();

    public ProcessViewer() {

        screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        Toolkit t = Toolkit.getDefaultToolkit();
        screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Se consigue el tama�o de la ventana
        // Valores de inicio de porgrama
        activitiesSelected = false;
        tracesSelected = false;
        informationSelected = false;
        modelSelected = false;
        deploymentSelected = false;
        wasMined = false;
        deployment = "";
        fileName = null;
        wasLoaded = false;

        // Se generan las acciones
        initializeActions();

        // Se arma la interfaz
        buildLoadFile();
        buildMenu();
        buildRaw();
        buildViewer();
        buildWindow();

        //setTitle("Model");
        setSize(screenSize.width, screenSize.height);
        setVisible(true);
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
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

        mine_btn = new JButton("Mine");
        mine_btn.setBackground(new Color(0, 153, 0));
        mine_btn.setForeground(Color.black);
        mine_btn.addActionListener(mine_btnAction);

        convertToXES_btn = new JButton("Convert file XES to txt");
        convertToXES_btn.addActionListener(convertToXES_btnAction);

        filtering_btn = new JButton("Filtering outliers");
        filtering_btn.addActionListener(filtering_btnAction);

        if (fileName != null) {
            mine_btn.setVisible(true);
            if (!fileName.getName().endsWith(".repaired")) {
                filtering_btn.setVisible(true);
            } else {
                filtering_btn.setVisible(false);
            }
            titulo_txt.setText(fileName.getName());
            loadFile_pnl.add(titulo_txt);
            if (fileName.getName().toLowerCase().contains(".xes")) {
                convertToXES_btn.setVisible(true);
            } else {
                convertToXES_btn.setVisible(false);
            }
        } else {
            titulo_txt.setText("No selected file");
            loadFile_pnl.add(titulo_txt);
            convertToXES_btn.setVisible(false);
            mine_btn.setVisible(false);
            filtering_btn.setVisible(false);
        }

        loadFile_btn = new JButton("Load File");
        loadFile_btn.addActionListener(loadFile_btnAction);
        loadFile_pnl.add(loadFile_btn);

        if (wasMined) {
            exportAsBPMN_btn = new JButton("Export as BPMN 2.0");
            exportAsBPMN_btn.addActionListener(exportAsBPMN_btnAction);
            loadFile_pnl.add(exportAsBPMN_btn);
        }

        loadFile_pnl.add(filtering_btn);

        loadFile_pnl.add(convertToXES_btn);

        loadFile_pnl.add(mine_btn);

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

        if (!wasLoaded) {
            activities_btn.setEnabled(false);
            traces_btn.setEnabled(false);
            information_btn.setEnabled(false);
        } else {
            activities_btn.setEnabled(true);
            traces_btn.setEnabled(true);
            information_btn.setEnabled(true);
        }

        if (!wasMined) {
            model_btn.setEnabled(false);
            deployment_btn.setEnabled(false);
        } else {
            model_btn.setEnabled(true);
            deployment_btn.setEnabled(true);
        }

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
    JPanel buildTablePanel(DefaultTableModel dtb, String title, int column0Size, int column1Size) {
        /* Se inicializa la tablas*/
        JLabel titleTable_txt = new JLabel(title);
        titleTable_txt.setFont(new Font("Tahoma", Font.BOLD, 18));
        JTable jtable = new JTable(dtb);

        jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = jtable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(column0Size);
        columnModel.getColumn(1).setPreferredWidth(column1Size);

        JScrollPane tbl_sp = new JScrollPane(jtable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

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

        //JPanel north = new JPanel();
        JPanel south = new JPanel();

        int sizeOfColumns = screenSize.width / 10;

        if (informationSelected) {
            int bigger0 = 0;
            int bigger1 = 1;
            for (int i = 0; i < information_dtm.getRowCount(); i++) {
                if (information_dtm.getValueAt(i, 0).toString().length() > bigger0) {
                    bigger0 = information_dtm.getValueAt(i, 0).toString().length();
                }
                if (information_dtm.getValueAt(i, 1).toString().length() > bigger1) {
                    bigger1 = information_dtm.getValueAt(i, 1).toString().length();
                }
            }
            JPanel j = buildTablePanel(information_dtm, "Description", bigger0 * 8, bigger1 * 8);
            j.setPreferredSize(new Dimension(screenSize.width / 10, screenSize.height / 2));
            south.add(j);
        }
        // north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));

        if (activitiesSelected) {
            JPanel j = buildTablePanel(activities_dtm, "Activities", ("Description".length() > biggerActivity0) ? "Description".length() * 8 : biggerActivity0 * 8, ("Item".length() > biggerActivity1) ? "Item".length() * 8 : biggerActivity1 * 8);
            j.setPreferredSize(new Dimension(screenSize.width / 10, screenSize.height / 2));
            south.add(j);
        }
        if (tracesSelected) {
            JPanel j = buildTablePanel(traces_dtm, "Traces", traces_dtm.getValueAt(traces_dtm.getRowCount() - 1, 0).toString().length() * 10, biggerTrace * 8);
            j.setPreferredSize(new Dimension(screenSize.width / 10, screenSize.height / 2));
            south.add(j);
        }
        if (modelSelected) {
            JPanel j = buildTablePanel(model_dtm, "BPMN Model", ("Edge".length() > biggerModel0 ? "Edge".length() * 8 : biggerModel0 * 8), ("Frecuency".length() > biggerModel1 ? "Frecuency".length() * 8 : biggerModel1 * 8));
            j.setPreferredSize(new Dimension(screenSize.width / 10, screenSize.height / 2));
            south.add(j);
        }
        south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
        //raw_pnl.add(north);
        raw_pnl.add(south);

        if (deploymentSelected) {
            JPanel feet = new JPanel();

            JTextPane editor = new JTextPane();
            editor.setEditable(true);
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
        //main_frm = new JFrame("Process Viewer");
        this.setLayout(new BorderLayout());
        /* main_frm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        main_frm.setSize(screenSize.width / 3 * 2, screenSize.height / 3 * 2);
        main_frm.setMaximumSize(new Dimension(screenSize.width, screenSize.height));
        main_frm.setMinimumSize(new Dimension(screenSize.width / 3 * 2, screenSize.height / 3 * 2));
        main_frm.setLocationRelativeTo(null);
        main_frm.setAlwaysOnTop(false);
        
        
         */
        this.Panel2.setLayout(new BorderLayout());

        this.Panel1.removeAll();
        this.Panel1.setLayout(new BorderLayout());
        this.Panel1.add(loadFile_pnl, BorderLayout.NORTH);
        this.Panel1.add(menu_pnl, BorderLayout.WEST);
        this.Panel1.add(viewer_pnl, BorderLayout.CENTER);

        this.add(Panel1, BorderLayout.NORTH);

        this.Panel1.revalidate();
        this.Panel1.repaint();

        // this.pack();
        this.setVisible(true);
    }

    private void initializeActions() {

        mine_btnAction = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (fileName != null) {
                    execute();
                    refreshWindow();
                } else {
                    JOptionPane.showMessageDialog(Panel1, "No file selected");
                }

            }
        };

        filtering_btnAction = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.out.println(ae.getActionCommand());
                // prompt
                String l = JOptionPane.showInputDialog(Panel1, "Filtering outliers\nIngrese el parametro 'l':");
                String r = JOptionPane.showInputDialog(Panel1, "Filtering outliers\nIngrese el parametro 'r':");
                String k = JOptionPane.showInputDialog(Panel1, "Filtering outliers\nIngrese el parametro 'K':");
                String umbral = JOptionPane.showInputDialog(Panel1, "Filtering outliers\nIngrese el 'umbral':");

                if (l != null && r != null && k != null & umbral != null) {
                    int left, right, K;
                    double u;
                    try {
                        left = Integer.parseInt(l);
                        right = Integer.parseInt(r);
                        K = Integer.parseInt(k);
                        u = Double.parseDouble(umbral);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(Panel1, "Alguno de los datos ingresados es invalido! vuelva a ingresarlos");
                        return;
                    }
                    StringBuilder contextOutput = new StringBuilder();
                    BPMNModel bpmn = new BPMNModel();
                    FilesManagement f = new FilesManagement(bpmn, true, false, left, right, K, u, contextOutput); //boolean Filtering, int l, int r, int k, double umbral
                    LinkedHashMap<Integer, ArrayList<Character>> originalTraces; //lista de trazas
                    LinkedHashMap<Integer, ArrayList<Character>> repairedTraces; //lista de trazas
                    Object[] traces = new Object[3];
                    try {
                        if (fileName.getAbsolutePath().endsWith(".txt")) {
                            traces = f.readDataInputTrazas(fileName.getAbsolutePath());
                        } else if (fileName.getAbsolutePath().endsWith(".csv")) {
                            traces = f.readDataInput(fileName.getAbsolutePath());
                        } else {
                            JOptionPane.showMessageDialog(Panel1, "El tipo de archivo de entrada no es valido.");
                            return;
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(Panel1, "El archivo '" + fileName.getAbsolutePath() + "' no se puede abrir.");
                        return;
                    }
                    originalTraces = (LinkedHashMap<Integer, ArrayList<Character>>) traces[0];
                    repairedTraces = (LinkedHashMap<Integer, ArrayList<Character>>) traces[1];
                    LinkedHashMap<String, Character> activityList = (LinkedHashMap<String, Character>) traces[2]; 
                    FilterOutliersFrame fof = new FilterOutliersFrame(originalTraces, repairedTraces, contextOutput.toString(), fileName.getName().substring(0, fileName.getName().indexOf(".")), tasksDescription, activityList);

                    remove(Panel2);
                    Panel3.removeAll();

                    Panel3.add(fof);
                    add(Panel3, BorderLayout.SOUTH);
                    Panel3.revalidate();
                    Panel3.repaint();

                } else {
                    JOptionPane.showMessageDialog(Panel1, "Ingrese todos los parámetros para realizar el filtrado!");
                }
            }
        };

        convertToXES_btnAction = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (fileName != null) {
                    if (fileName.getName().toLowerCase().contains(".xes")) {
                        String name = fileName.getName();
                        try {
                            String path = ReadXES.XESTOTXT(fileName.getAbsolutePath(), name.substring(0, name.indexOf(".")));
                            fileName = new File(path);
                            refreshWindow();
                            JOptionPane.showMessageDialog(Panel1, "File converted!\nPath: " + path + "\nNow you can mine it");

                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(Panel1, "Error while trying to convert xes to txt: " + e.getMessage());
                        }

                    } else {
                        JOptionPane.showMessageDialog(Panel1, "File selected is not XES");
                    }
                } else {
                    JOptionPane.showMessageDialog(Panel1, "No file selected");
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
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TXT, CSV, XES & Repaired outliers files", "txt", "csv", "xes", "repaired"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TXT files", "txt"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("XES files", "xes"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Repaired outliers files", "repaired"));

                if (fileName != null) {
                    fileChooser.setCurrentDirectory(fileName);
                }

                int result = fileChooser.showOpenDialog(fileChooser);

                if (result != JFileChooser.CANCEL_OPTION) {

                    fileName = fileChooser.getSelectedFile();

                    if ((fileName == null) || (fileName.getName().equals(""))) {
                        System.out.println("Error en el archivo.");

                    } else {

                        if (!fileName.getName().toLowerCase().endsWith(".txt") && !fileName.getName().toLowerCase().endsWith(".csv") && !fileName.getName().toLowerCase().endsWith(".xes") && !fileName.getName().toLowerCase().endsWith(".repaired")) {
                            JOptionPane.showMessageDialog(Panel1, "El tipo de archivo de entrada no es valido");
                            refreshWindow();
                            return;
                        }

                        //Reiniciar contenedores
                        tracesList = null;
                        wfg = new WFG();
                        wasMined = false;
                        modelSelected = false;
                        deploymentSelected = false;

                        String filename = fileName.getAbsolutePath();
                        FilesManagement f = new FilesManagement(wfg.BPMN, false, (fileName.getName().endsWith(".repaired")) ? true : false, 0, 0, 0, 0.0, new StringBuilder());
                        ///////
                        System.out.println("PASO 1: LEER TRAZAS DEL ARCHIVO DE ENTRADA '" + filename + "' E IDENTIFICAR TAREAS.");
                        try {
                            if (filename.toLowerCase().endsWith(".txt") || filename.toLowerCase().endsWith(".repaired")) {
                                tracesList = (LinkedHashMap<Integer, ArrayList<Character>>) f.readDataInputTrazas(filename)[0];
                            } else if (filename.toLowerCase().endsWith(".csv")) {
                                tracesList = (LinkedHashMap<Integer, ArrayList<Character>>) f.readDataInput(filename)[0];
                            } else {
                                JOptionPane.showMessageDialog(Panel1, "El tipo de archivo de entrada no es valido. Intente realizar alguna conversión a un formato válido.");
                                refreshWindow();
                                return;
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(Panel1, "El archivo '" + filename + "' no se puede abrir. Intente realizar alguna conversión a un formato válido.");
                            refreshWindow();
                            return;
                        }

                        tasksDescription = "";
                        ///////
                        // Se prepara la tabla Actividades
                        String[] columnNames = new String[]{"Description", "Item"};
                        String[][] data = new String[f.ActivityList.size()][2];
                        int i = 0;
                        biggerActivity1 = 0;
                        biggerActivity0 = 0;
                        for (Map.Entry<String, Character> entry1 : f.ActivityList.entrySet()) {
                            data[i][0] = entry1.getKey();
                            data[i][1] = entry1.getValue().toString();

                            if (data[i][1].length() > biggerActivity1) {
                                biggerActivity1 = data[i][1].length();
                            }
                            if (data[i][0].length() > biggerActivity0) {
                                biggerActivity0 = data[i][0].length();
                            }

                            tasksDescription += " " + data[i][1] + " : " + data[i][0] + "\n";
                            i++;
                        }
                        // Modelo para la table activities
                        activities_dtm = new DefaultTableModel(data, columnNames);
                        ////////
                        // Se prepara la tabla traces
                        String[] columnNamesTraces = new String[]{"ID", "Traces"};
                        String[][] dataTraces = new String[tracesList.size()][2];
                        i = 0;
                        biggerTrace = 0;
                        System.out.println("\t3. Mostrando TRAZAS IDENTIFICADAS  en el archivo '" + filename + "'.");
                        for (Map.Entry<Integer, ArrayList<Character>> entry : tracesList.entrySet()) {
                            System.out.println("\t\t" + entry.getKey() + " - " + entry.getValue());
                            dataTraces[i][0] = entry.getKey().toString();
                            dataTraces[i][1] = entry.getValue().toString();
                            int length = dataTraces[i][1].length();
                            if (length >= biggerTrace) {
                                biggerTrace = length;
                            }
                            i++;
                        }

                        // Modelo para la table traces
                        traces_dtm = new DefaultTableModel(dataTraces, columnNamesTraces);

                        // Se prepara el modelo para la table infrmation
                        String[] columnNamesInfo = new String[]{"", ""};
                        String[][] columnNamesdataInfo = f.showDataInfo(tracesList);
                        information_dtm = new DefaultTableModel(columnNamesdataInfo, columnNamesInfo);
                        wasLoaded = true;
                        activitiesSelected = true;
                        tracesSelected = true;
                        informationSelected = true;

                        remove(Panel3);
                        remove(Panel2);

                    }
                    refreshWindow();
                }
            }

        };

        exportAsBPMN_btnAction
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
                        JOptionPane.showMessageDialog(Panel1, "Error en la ruta.");
                    } else {
                        System.out.println(filePath.getAbsolutePath());
                    }
                }

                if (filePath != null) {
                    BPMNFiles bpmnFiles = new BPMNFiles(WFG, BPMN, filePath.getAbsolutePath() + "/" + fileName.getName().substring(0, fileName.getName().indexOf(".")));
                    JOptionPane.showMessageDialog(Panel1, "File exported!");
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

    /**
     * Función ejecutar, realiza el minado del modelo seleccionado
     *
     * @param filename Nombre del archivo del modelo
     */
    public void execute() {
        double epsilon = 0.0, umbral = 0.0;

        //double umbral = 0.4; //descarta edges con frecuencia menor a este umbral he manejado hasta 25
        //double epsilon = 0.3;
        wfg.WFG.clear();
        wfg.WFGSplits.clear();
        wfg.WFGantesSplits.clear();

        if (!epsilon_textField.getText().equals("") || !percentil_textField.getText().equals("")) {
            try {
                epsilon = Double.parseDouble(epsilon_textField.getText());
                umbral = Double.parseDouble(percentil_textField.getText());
            } catch (Exception e) {
                System.out.println("Epsilon o percentil inválidos! Ingrese números solamente. " + e.getMessage());
                return;
            }
        } else {
            System.out.println("Ingrese un número epsilon y percentil");
            return;
        }

        System.out.println("Epsilon: " + epsilon);
        System.out.println("percentil: " + umbral);

        //P1.txt NOTATION: a AND{  XOR{  c, h}, b XOR{  e, g}} d f
        //final String filename = "P1.txt";
        ///////
        System.out.println("\n");
        System.out.println("PASO2: INCIANDO LA CONSTRUCCION DEL GRAFO QUE MODELA EL CONJUNTO DE TRAZAS.\n");

        GenerarGrafo generarGrafo = new GenerarGrafo();
        generarGrafo.computeGraph(tracesList, wfg.WFG);

        System.out.println("\nPASO 3: PREPROCESAMIENTO DEL GRAFO");

        System.out.println("BPMN: " + wfg.BPMN.toString());
        System.out.println("tracesList: " + tracesList.toString());
        System.out.println("generarGrafo.firsts: " + generarGrafo.firsts.toString());
        System.out.println("generarGrafo.lasts: " + generarGrafo.lasts.toString());
        System.out.println("umbral: " + umbral);
        System.out.println("epsilon: " + epsilon);
        PreProcesarGrafo preprocesarGrafo = new PreProcesarGrafo(wfg.BPMN, wfg.WFG, tracesList, generarGrafo.firsts, generarGrafo.lasts, umbral, epsilon);

        wfg.WFGantesSplits = (LinkedHashMap) wfg.WFG.clone();

        /////////
        System.out.println("\nPASO 4: CONSTRUCCION DEL MODELO BPMN");

        SplitsFinder crearModelo = new SplitsFinder(wfg.BPMN, generarGrafo.firsts, generarGrafo.lasts, wfg.WFG, preprocesarGrafo.parallelRelations);

        wfg.WFGSplits = (LinkedHashMap) wfg.WFG.clone();

        /////////
        System.out.println("\nPASO 5: POST-PROCESAMIENTO");

        //g1.postProcesamiento(BPMN);
        PostProcesarGrafo postprocesamiento = new PostProcesarGrafo(wfg.BPMN, wfg.WFG);

        wfg.Notation = postprocesamiento.notation;

        System.out.println("====================Loop detection===============");

        String finalNotation = FindLoops.findLoops(wfg.BPMN, wfg.WFG, wfg.Notation, postprocesamiento.cierres);

        wfg.Notation = finalNotation;

        System.out.println("Notacion al final: " + wfg.Notation);

        deployment = wfg.Notation;

        // Se prepara la tabla Modelo
        String[] columnNamesModel = new String[]{"Key", "Value"};
        String[][] dataModel = new String[wfg.WFG.size()][2];
        int i = 0;
        biggerModel0 = 0;
        biggerModel1 = 0;
        for (Map.Entry<String, Integer> entry : wfg.WFG.entrySet()) {
            dataModel[i][0] = "   [ " + entry.getKey() + " ]";
            dataModel[i][1] = String.valueOf(entry.getValue());

            if (dataModel[i][0].length() > biggerModel0) {
                biggerModel0 = dataModel[i][0].length();
            }
            if (dataModel[i][1].length() > biggerModel1) {
                biggerModel1 = dataModel[i][1].length();
            }

            i++;
        }

        // Model para la tabla Modelo
        model_dtm = new DefaultTableModel(dataModel, columnNamesModel);
        ///////

        gBuildGraphicModel view = new gBuildGraphicModel(wfg.BPMN.T); //Instancia de la vista
        wfg.addObserver(view); //Agregar observador al modelo

        this.WFG = wfg.WFG; //asignar el valor actual del grafo (motivos de exportacion de modelo a archivo XML BPMN 2.0)
        this.BPMN = wfg.BPMN;//asignar el valor actual del modelo BPMN (motivos de exportacion de modelo a archivo XML BPMN 2.0)
        wfg.tasksDescription = this.tasksDescription;

        wfg.autoLoops = preprocesarGrafo.autoLoops;
        wfg.shortLoops = preprocesarGrafo.shortLoops;

        wfg.notifyAction(); //notificar que el modelo tuvo cambios
        wasMined = true;
        modelSelected = true;
        deploymentSelected = true;

        //refreshWindow();
        /*getContentPane().removeAll();
        revalidate();
        repaint();
        
         */
        this.remove(Panel3);

        this.Panel2.removeAll();
        this.Panel2.add(view);
        this.add(Panel2, BorderLayout.SOUTH);
        this.Panel2.revalidate();
        this.Panel2.repaint();
        refreshWindow();

        // Se revalida el frame
        //SwingUtilities.updateComponentTreeUI(this);
    }

    void refreshWindow() {

        this.Panel1.remove(loadFile_pnl);
        this.Panel1.remove(menu_pnl);
        this.Panel1.remove(viewer_pnl);

        // Se construyen y revalidan los frames
        buildLoadFile();
        loadFile_pnl.validate();
        buildMenu();
        menu_pnl.validate();
        buildRaw();
        raw_pnl.validate();
        buildViewer();
        viewer_pnl.validate();

        this.Panel1.add(loadFile_pnl, BorderLayout.NORTH);
        this.Panel1.add(menu_pnl, BorderLayout.WEST);
        this.Panel1.add(viewer_pnl, BorderLayout.CENTER);

        this.add(Panel1, BorderLayout.NORTH);

        this.Panel1.revalidate();
        this.Panel1.repaint();

        // this.pack();

        /*JPanel north = new JPanel();
        north.setPreferredSize( new Dimension(screenSize.width/2, screenSize.height/2));
        
        north.add(loadFile_pnl, BorderLayout.NORTH);
        north.add(menu_pnl, BorderLayout.WEST);
        north.add(viewer_pnl, BorderLayout.CENTER);
        
        this.add(north, BorderLayout.NORTH);*/
        // Se revalida el frame
        //SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();

    }

}
