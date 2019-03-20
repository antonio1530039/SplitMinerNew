package Vista;

import Controlador.BPMNFiles;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class FilterOutliersFrame extends JFrame {

    JPanel jpanelComponentes = new JPanel();

    public FilterOutliersFrame(LinkedHashMap<Integer, ArrayList<Character>> originalTraces, LinkedHashMap<Integer, ArrayList<Character>> repairedTraces, String contextOutput, String fileName) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int ScreenWidth = (int) screenSize.getWidth();
        int ScreenHeight = (int) screenSize.getHeight();
        
        // Se prepara la tabla traces
       // String[] columnNamesTraces = new String[]{"ID", "Original Traces", "Repaired Traces"};
        String[] columnNamesTraces = new String[]{"Original Traces", "Repaired Traces"};
        String[][] dataTraces = new String[originalTraces.size()][3];
        int i = 0;
        for (Map.Entry<Integer, ArrayList<Character>> entry : originalTraces.entrySet()) {
           // dataTraces[i][0] = entry.getKey().toString();
            dataTraces[i][0] = entry.getValue().toString();
            i++;
        }

               
        i = 0;
        for (Map.Entry<Integer, ArrayList<Character>> entry : repairedTraces.entrySet()) {
            dataTraces[i][1] = entry.getValue().toString();
            i++;
        }
        
        // Modelo para la table traces
        DefaultTableModel originalTracesDTM = new DefaultTableModel(dataTraces, columnNamesTraces);
        
        
        JPanel j = buildTablePanel(originalTracesDTM, "Traces");
        j.setPreferredSize(new Dimension(screenSize.width - (screenSize.width/100)*5 , screenSize.height / 2));
        
        jpanelComponentes.add(j);
        
        add(jpanelComponentes, BorderLayout.NORTH);


        //contexts output

        JPanel middlePanel3 = new JPanel ();
        middlePanel3.setBorder ( new TitledBorder ( new EtchedBorder (), "Significant contexts values" ) );
        JTextArea display3 = new JTextArea ( 20, 50);
        display3.setSize(new Dimension(screenSize.width / 2, screenSize.height / 3 ));
        display3.setText(contextOutput);
        JScrollPane scroll3 = new JScrollPane ( display3 );
        scroll3.setVerticalScrollBarPolicy ( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        scroll3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        middlePanel3.add ( scroll3 );
        
        middlePanel3.setPreferredSize(new Dimension(screenSize.width / 2, screenSize.height / 3));
        
        JPanel jpanelComponentes2 = new JPanel();
        jpanelComponentes2.add(middlePanel3);
        
        JFrame main = this;
        
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
                    System.out.println("Exporting log to: " +  filePath.getAbsolutePath() + "/" + fileName + "Repaired.txt");
                    
                    String toSave = "";
                    
                    for (Map.Entry<Integer, ArrayList<Character>> entry : repairedTraces.entrySet()) {
                        for(Character c : entry.getValue()){
                            toSave += c + ";";
                        }
                        toSave = toSave.substring(0, toSave.lastIndexOf(";"));
                        toSave+="\n";
                    }
                    
                    try (PrintStream out = new PrintStream(new FileOutputStream(filePath.getAbsolutePath() + "/" + fileName + "Repaired.txt"))) {
                        out.print(toSave);
                        JOptionPane.showMessageDialog(main, "File: "+ fileName + "Repaired.txt" + " exported!");
                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(main, "Error while trying to export a file");
                    }
                    
                    
                  
                }


            }
        };
        
        save.addActionListener(save_actionListener);
        
        
       jpanelComponentes2.add(save);
        
        
        
        add(jpanelComponentes2, BorderLayout.SOUTH);

        setTitle("Filter Outliers output");
        setSize(ScreenWidth, ScreenHeight);
        setVisible(true);
    }
    
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
