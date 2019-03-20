package Vista;

import Controlador.BPMNFiles;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class FilterOutliersFrame extends JFrame {

    JPanel jpanelComponentes = new JPanel();

    public FilterOutliersFrame(LinkedHashMap<Integer, ArrayList<Character>> originalTraces, LinkedHashMap<Integer, ArrayList<Character>> repairedTraces, String contextOutput, String fileName) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int ScreenWidth = (int) screenSize.getWidth();
        int ScreenHeight = (int) screenSize.getHeight();

        String ot = "";
        for (Map.Entry<Integer, ArrayList<Character>> entry : originalTraces.entrySet()) {
            ot += (entry.getKey() + " - " + entry.getValue() + "\n");
        }

        String rt = "";
        for (Map.Entry<Integer, ArrayList<Character>> entry : repairedTraces.entrySet()) {
            rt += (entry.getKey() + " - " + entry.getValue() + "\n");
        }

        //original traces
        
        JPanel middlePanel = new JPanel ();
        middlePanel.setBorder ( new TitledBorder ( new EtchedBorder (), "Original traces" ) );
        JTextArea display = new JTextArea ( 20, 40 );
        display.setText(ot);
        JScrollPane scroll = new JScrollPane ( display );
        scroll.setVerticalScrollBarPolicy ( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        middlePanel.add ( scroll );

        jpanelComponentes.add(middlePanel);

        
        
        //repaired traces

        JPanel middlePanel2 = new JPanel ();
        middlePanel2.setBorder ( new TitledBorder ( new EtchedBorder (), "Repaired traces" ) );
        JTextArea display2 = new JTextArea ( 20, 40 );
        display2.setText(rt);
        JScrollPane scroll2 = new JScrollPane ( display2 );
        scroll2.setVerticalScrollBarPolicy ( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        scroll2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        middlePanel2.add ( scroll2 );

        jpanelComponentes.add(middlePanel2);
        
        
        
        //contexts output

        JPanel middlePanel3 = new JPanel ();
        middlePanel3.setBorder ( new TitledBorder ( new EtchedBorder (), "Significant contexts values" ) );
        JTextArea display3 = new JTextArea ( 20, 40 );
        display3.setText(contextOutput);
        JScrollPane scroll3 = new JScrollPane ( display3 );
        scroll3.setVerticalScrollBarPolicy ( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        scroll3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        middlePanel3.add ( scroll3 );


        jpanelComponentes.add(middlePanel3);
        
        JFrame main = this;
        
        JButton save = new JButton("Save log");
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
        
        
       jpanelComponentes.add(save);
        
        
        
        add(jpanelComponentes);

        setTitle("Filter Outliers output");
        setSize(ScreenWidth, ScreenHeight);
        setVisible(true);
    }

}
