package Vista.Web;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class FilterOutliersFrame extends JPanel {

    JPanel jpanelComponentes = new JPanel();

    public FilterOutliersFrame(LinkedHashMap<Integer, ArrayList<Character>> originalTraces, LinkedHashMap<Integer, ArrayList<Character>> repairedTraces, String contextOutput, String fileName, String tasksDescription, LinkedHashMap<String, Character> activityList) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLayout(new BorderLayout());
        int ScreenWidth = (int) screenSize.getWidth();
        int ScreenHeight = (int) screenSize.getHeight();

        // Se prepara la tabla de las trazas originales para mostrarse en la interfaz
        String[] columnNamesTraces = new String[]{"Original Traces", "Repaired Traces"};
        String[][] dataTraces = new String[originalTraces.size()][3];
        int i = 0;
        for (Map.Entry<Integer, ArrayList<Character>> entry : originalTraces.entrySet()) {
            dataTraces[i][0] = entry.getValue().toString();
            i++;
        }

        i = 0;
        // Se prepara la tabla de las trazas reparadas para mostrarse en la interfaz
        for (Map.Entry<Integer, ArrayList<Character>> entry : repairedTraces.entrySet()) {
            dataTraces[i][1] = entry.getValue().toString();
            i++;
        }

        // Modelo para la table traces
        DefaultTableModel originalTracesDTM = new DefaultTableModel(dataTraces, columnNamesTraces);

        JLabel title = new JLabel("Filtering outliers");
        title.setFont(new Font("Tahoma", Font.BOLD, 18));

        JPanel superior = new JPanel();

        superior.setLayout(new BorderLayout());
        superior.add(title, BorderLayout.NORTH);

        JPanel j = buildTablePanel(originalTracesDTM, "Traces");
        j.setPreferredSize(new Dimension(screenSize.width - (screenSize.width / 100) * 5, screenSize.height / 2));

        jpanelComponentes.add(j);

        superior.add(jpanelComponentes);
        add(superior, BorderLayout.NORTH);

        //Mostrar los contextos significantes y la descripción de las tareas en un TextArea
        JPanel middlePanel3 = new JPanel();
        middlePanel3.setBorder(new TitledBorder(new EtchedBorder(), "Significant contexts values and tasks description"));

        JTextArea display3 = new JTextArea(12, 30);
        display3.setSize(new Dimension(screenSize.width / 2, screenSize.height / 3));
        display3.setText("*Significant contexts*\n\n" + contextOutput);
        JScrollPane scroll3 = new JScrollPane(display3);
        scroll3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JTextArea display4 = new JTextArea(12, 15);
        display4.setSize(new Dimension(screenSize.width / 2, screenSize.height / 3));
        display4.setText("*Tasks description*\n\n" + tasksDescription);
        JScrollPane scroll4 = new JScrollPane(display4);
        scroll4.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll4.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        middlePanel3.add(scroll3);
        middlePanel3.add(scroll4);

        middlePanel3.setPreferredSize(new Dimension(screenSize.width / 2, screenSize.height / 3));

        JPanel jpanelComponentes2 = new JPanel();
        jpanelComponentes2.add(middlePanel3);

        JPanel main = this;
        //Boton de guardado de log
        JButton save = new JButton("Save repaired log");
        ActionListener save_actionListener = new ActionListener() {
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
                        JOptionPane.showMessageDialog(main, "Error en la ruta.");
                    } else {
                        System.out.println(filePath.getAbsolutePath());
                    }
                }

                if (filePath != null) {
                    System.out.println("Exporting log to: " + filePath.getAbsolutePath() + "/" + fileName + ".repaired");

                    String toSave = "";
                    
                    //Crear nuevo mapa
                    HashMap<Character, String> newActivityList = new HashMap<>();
                    for(Map.Entry<String, Character> entry : activityList.entrySet()){
                            Character value = entry.getValue();
                            if(value != 'I' && value != 'O'){
                                newActivityList.put(value, entry.getKey());
                            }
                        }

                    for (Map.Entry<Integer, ArrayList<Character>> entry : repairedTraces.entrySet()) {
                        for (Character c : entry.getValue()) {
                            if(c != 'I' && c != 'O'){
                                toSave += newActivityList.get(c) + ";";
                            }else{
                                toSave += c + ";";
                            }
                        }
                        toSave = toSave.substring(0, toSave.lastIndexOf(";"));
                        toSave += "\n";
                    }

                    try (PrintStream out = new PrintStream(new FileOutputStream(filePath.getAbsolutePath() + "/" + fileName + ".repaired"))) {
                        out.print(toSave);
                        JOptionPane.showMessageDialog(main, "File: " + fileName + ".repaired" + " exported!");
                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(main, "Error while trying to export a file");
                    }

                }

            }
        };

        save.addActionListener(save_actionListener);

        jpanelComponentes2.add(save);

        add(jpanelComponentes2, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(main, "Filtering done!");
        // setTitle("Filter Outliers output");
        setSize(ScreenWidth, ScreenHeight);
        setVisible(true);
    }

     /**
     * Función que construye un panel y agrega la tabla
     *
     * @param dtb Tabla
     * @param title Titulo del panel
     * @return Retorna el panel
     */
    JPanel buildTablePanel(DefaultTableModel dtb, String title) {
        /* Se inicializa la tablas*/
        JLabel titleTable_txt = new JLabel(title);
        titleTable_txt.setFont(new Font("Tahoma", Font.BOLD, 18));
        JScrollPane tbl_sp = new JScrollPane(new JTable(dtb), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        /* Se prepara el panel*/
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnl.add(titleTable_txt, BorderLayout.NORTH);
        pnl.add(tbl_sp, BorderLayout.CENTER);
        return pnl;
    }

}
