package UI;

import Input.InputReader;
import Input.SimulationInput;
import Output.SimulationOutput;
import Simulation.Model.Vector3;
import Simulation.Simulation;
import Utility.Application;
import Utility.PathUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FloatUtilUI {
    private static JFrame frame;
    static JPanel optionsPanel;
    static JPanel statusPanel;
    static JPanel outputPreviewPanel;
    static StatusIndicator[] statusIndicators;
    static ImageIcon notStartedIcon;
    static ImageIcon inProgressIcon;
    static ImageIcon failIcon;
    static ImageIcon succeedIcon;
    static ColorButton simulateButton;
    static FilePickerOption inputFileOption;
    static FilePickerOption outputFileOption;
    static ModelView sideView;
    static ModelStats modelStats;

    public static void init(){

        SwingUtilities.invokeLater(()-> initUi());
    }

    private static void initUi(){
        notStartedIcon = new ImageIcon("resources/icons/notstarted16x.png","Task Not Started");
        inProgressIcon = new ImageIcon("resources/icons/started.gif","Task Started");
        failIcon = new ImageIcon("resources/icons/failure16x.png","Task Failed");
        succeedIcon = new ImageIcon("resources/icons/success16x.png","Task Succeeded");

        frame = new JFrame("Float-Util");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        optionsPanel = new JPanel();
        statusPanel = new JPanel();
        outputPreviewPanel = new JPanel();
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        outputPreviewPanel.setBorder(BorderFactory.createTitledBorder("Output Preview"));
        statusPanel.setPreferredSize(new Dimension(250,1));
        outputPreviewPanel.setPreferredSize(new Dimension(1,200));

        // Initialize status indicators
        statusIndicators = new StatusIndicator[6];
        statusIndicators[0] = new StatusIndicator("Input File",notStartedIcon);
        statusIndicators[1] = new StatusIndicator("Output File",notStartedIcon);
        statusIndicators[2] = new StatusIndicator("Scenario Definition",notStartedIcon);
        statusIndicators[3] = new StatusIndicator("Model",notStartedIcon);
        statusIndicators[4] = new StatusIndicator("Simulation",notStartedIcon);
        statusIndicators[5] = new StatusIndicator("Output",notStartedIcon);

        statusPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,0,5,0);
        int i = 0;
        for (StatusIndicator indicator : statusIndicators){
            c.anchor = GridBagConstraints.PAGE_START;
            c.fill = GridBagConstraints.BOTH;
            c.weightx=1;
            c.weighty=0;
            c.gridx = 0;
            c.gridy = i++;
            statusPanel.add(indicator,c);
        }

        //add panel which pushes indicators to top
        c.gridy = i++;
        c.weighty=1;
        statusPanel.add(new JPanel(),c);

        //Add button to bottom
        simulateButton = new ColorButton("Start Simulation");
        simulateButton.setBackground(new Color(0,128,0));
        simulateButton.setHoverBackgroundColor(new Color(0,150,0));
        simulateButton.setPressedBackgroundColor(new Color(0,108,0));
        simulateButton.setFont(simulateButton.getFont().deriveFont(Font.BOLD));
        simulateButton.setForeground(Color.WHITE);
        simulateButton.setOpaque(true);
        simulateButton.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        c.gridy = i++;
        c.weighty = 0;
        statusPanel.add(simulateButton,c);
        simulateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(()-> {
                    checkState();
                    if (isReady) {
                        if (isRunning) {
                            cancelSimulation();
                        } else {
                            startSimulation();
                        }
                    } else {
                        //JOptionPane.showMessageDialog(null, "Cannot start simulation - Steps not ready! All steps prior to Simulation must be completed.");
                    }
                    updateSimulationButton();
                });
            }
        });

        inputFileOption = new FilePickerOption("Input File",()->statusIndicators[0].setIcon(inProgressIcon), ()->checkState());
        outputFileOption = new FilePickerOption("Output File", ()->statusIndicators[1].setIcon(inProgressIcon),()->checkState());

        optionsPanel.setLayout(new GridBagLayout());
        c.gridy = 0;
        c.weighty=0;
        optionsPanel.add(inputFileOption,c);
        c.gridy = 1;
        optionsPanel.add(outputFileOption,c);

        // fill bottom of options panel
        c.gridy = 2;
        c.weighty=1;
        optionsPanel.add(new JPanel(),c);

        // Set up output model views
        sideView = new ModelView();
        modelStats = new ModelStats();

        outputPreviewPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        outputPreviewPanel.add(modelStats,c);
        c.gridx = 1;
        c.weightx = 1;
        outputPreviewPanel.add(sideView,c);

        frame.getContentPane().add(optionsPanel, BorderLayout.CENTER);
        frame.getContentPane().add(statusPanel, BorderLayout.LINE_END);
        frame.getContentPane().add(outputPreviewPanel, BorderLayout.SOUTH);

        frame.setPreferredSize(new Dimension(800,600));
        frame.setResizable(false);
        frame.pack();

        // Center window
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);

        // set visible
        frame.setVisible(true);

    }

    public static void clearIndicatorState(){
        for (StatusIndicator indicator : statusIndicators){
            indicator.setIcon(notStartedIcon);
        }
    }

    public static void updateSimulationButton(){
        if (isReady) {
            if (isRunning) {
                simulateButton.setBackground(new Color(255, 208, 120));
                simulateButton.setText("Stop Simulation");
                simulateButton.setForeground(Color.BLACK);
                statusIndicators[4].setIcon(inProgressIcon);
            } else {
                simulateButton.setBackground(new Color(0, 128, 0));
                simulateButton.setText("Start Simulation");
                simulateButton.setForeground(Color.WHITE);
                statusIndicators[4].setIcon(notStartedIcon);
            }
        } else {
            //JOptionPane.showMessageDialog(null, "Cannot start simulation - Steps not ready! All steps prior to Simulation must be completed.");
        }
    }

    static boolean isReady;
    static boolean isRunning;

    // Checks the state of the system and sees i
    public static void checkState(){
        isReady = true;

        clearIndicatorState();

        // Check if the input file is a valid file
        if (new File(inputFileOption.file).exists()){
            statusIndicators[0].setIcon(succeedIcon);
        } else {
            statusIndicators[0].setIcon(failIcon);
            isReady = false;
        }

        // Check if the output file is a valid file
        // skip for now
        if (true || new File(outputFileOption.file).exists()){
            statusIndicators[1].setIcon(succeedIcon);
        } else {
            statusIndicators[1].setIcon(failIcon);
            isReady = false;
        }

        String inputPath = "/"+PathUtilities.getPath(inputFileOption.file);
        System.out.println("Input path: "+inputPath);

        // If so far we are ready, perform actual file validation checks

        // Get simulation input object
        SimulationInput simulationInput = new InputReader().read(inputFileOption.file);

        // Check sim file
        if (isReady){
            statusIndicators[2].setIcon(inProgressIcon);
            if (simulationInput != null){
                statusIndicators[2].setIcon(succeedIcon);
                isReady = true;
            } else {
                statusIndicators[2].setIcon(failIcon);
                isReady = false;
            }
        }


        // Check model file
        if (isReady){
            statusIndicators[3].setIcon(inProgressIcon);

            boolean isValid = Simulation.checkModelDimensions(simulationInput,inputPath);

            if (isValid){
                statusIndicators[3].setIcon(succeedIcon);
                isReady = true;
            } else {
                statusIndicators[3].setIcon(failIcon);
                isReady = false;
                System.out.println(Application.detailedError);
                JOptionPane.showMessageDialog(null, Application.detailedError);
            }
        }

        // If we're not ready, change the button style
        if (!isRunning){
            if (isReady){
                simulateButton.setBackground(new Color(0, 128, 0));
                simulateButton.setText("Start Simulation");
                simulateButton.setForeground(Color.WHITE);
            } else {
                simulateButton.setBackground(new Color(128, 128, 128));
                simulateButton.setText("Refresh");
                simulateButton.setForeground(Color.BLACK);
            }
        }
    }

    static Thread simulationThread;
    static SimulationOutput simulationOutput;

    // Start the simulation in a background thread
    public static void startSimulation(){
        isRunning = true;
        disableUI();
        updateSimulationButton();
        simulationThread = new Thread(() -> {
            SimulationInput simulationInput = new InputReader().read(inputFileOption.file);
            String inputPath = "/"+PathUtilities.getPath(inputFileOption.file);
            final SimulationOutput simulationOutput = Simulation.run(simulationInput,inputPath);
            // Reset output model position
            simulationOutput.getModel().setPosition(new Vector3());
            simulationOutput.getModel().setRotation(new Vector3());
            simulationOutput.getModel().calculateCSG();

            if (simulationOutput == null){
                SwingUtilities.invokeLater(() -> cancelSimulation());
            } else {
                SwingUtilities.invokeLater(() -> finishSimulation(simulationOutput));
            }

        });
        simulationThread.start();
    }

    // Cancel a running simulation
    public static void cancelSimulation(){
        isRunning = false;
        enableUI();
        updateSimulationButton();
        simulationThread.stop();
        statusIndicators[4].setIcon(failIcon);
    }

    // Marks that the main simulation is finished
    public static void finishSimulation(SimulationOutput out){
        isRunning = false;
        enableUI();
        updateSimulationButton();
        statusIndicators[4].setIcon(succeedIcon);
        statusIndicators[5].setIcon(succeedIcon);
        modelStats.setSimOutput(out);
        sideView.setSimOutput(out);
    }


    // Enables the UI elements
    public static void disableUI(){
        inputFileOption.setButtonEnabled(false);
        outputFileOption.setButtonEnabled(false);
    }

    // Disables the UI elements
    public static void enableUI(){
        inputFileOption.setButtonEnabled(true);
        outputFileOption.setButtonEnabled(true);
    }

    public static void terminate(){

    }

}
