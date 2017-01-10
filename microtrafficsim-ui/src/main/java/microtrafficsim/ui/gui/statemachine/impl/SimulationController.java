package microtrafficsim.ui.gui.statemachine.impl;

import microtrafficsim.core.logic.StreetGraph;
import microtrafficsim.core.mapviewer.MapViewer;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.simulation.builder.ScenarioBuilder;
import microtrafficsim.core.simulation.configs.SimulationConfig;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.simulation.scenarios.Scenario;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.input.KeyCommand;
import microtrafficsim.core.vis.simulation.VehicleOverlay;
import microtrafficsim.ui.gui.menues.MTSMenuBar;
import microtrafficsim.ui.gui.statemachine.GUIController;
import microtrafficsim.ui.gui.statemachine.GUIEvent;
import microtrafficsim.ui.gui.statemachine.ScenarioConstructor;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.PrefElement;
import microtrafficsim.ui.preferences.impl.PreferencesFrame;
import microtrafficsim.utils.logging.EasyMarkableLogger;
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Controls user input and represents an interface between visualization/parsing, simulation and GUI.
 *
 * @author Dominic Parga Cacheiro
 */
public class SimulationController implements GUIController {

    private static final Logger logger = new EasyMarkableLogger(SimulationController.class);

    /*
    |=======|
    | ideas |
    |=======|
    */
    // TODO map double buffering
    // TODO interrupt route calculations
    // TODO simulation double buffering

    /* multithreading */
    private final AtomicBoolean isExecuting;
    private final AtomicBoolean isParsing;
    private final ReentrantLock lock_user_input;
    private Thread parsingThread;

    private boolean newSim;
    /* state marker */
    private boolean isCreated;

    /* general */
    private final SimulationConfig config;

    /* visualization and parsing */
    private final MapViewer      mapviewer;
    private final VehicleOverlay overlay;
    private File                 currentDirectory;
    private StreetGraph          streetgraph;

    /* simulation */
    private Simulation          simulation;
    private ScenarioConstructor scenarioConstructor;
    private ScenarioBuilder     scenarioBuilder;

    /* gui */
    private final JFrame     frame;
    private final MTSMenuBar menubar;
    private final PreferencesFrame preferences;

    public SimulationController() {
        this(new BuildSetup());
    }

    public SimulationController(BuildSetup buildSetup) {

        /* multithreading */
        isExecuting     = new AtomicBoolean(false);
        isParsing       = new AtomicBoolean(false);
        lock_user_input = new ReentrantLock();

        /* state marker */
        isCreated = false;
        newSim    = false;

        /* general */
        config = buildSetup.config;

        /* visualization and parsing */
        mapviewer        = buildSetup.mapviewer;
        overlay          = buildSetup.overlay;
        currentDirectory = new File(System.getProperty("user.dir"));

        /* simulation */
        simulation          = buildSetup.simulation;
        scenarioConstructor = buildSetup.scenarioConstructor;
        scenarioBuilder     = buildSetup.scenarioBuilder;
        overlay.setSimulation(simulation);

        /* gui */
        frame       = new JFrame(buildSetup.frameTitle);
        menubar     = new MTSMenuBar();
        preferences = new PreferencesFrame(this);
    }

    /*
    |===================|
    | (i) GUIController |
    |===================|
    */
    @Override
    public void transiate(GUIEvent event, final File file) {
        logger.debug("GUIEvent called = GUIEvent." + event);

        if (!lock_user_input.tryLock())
            return;

        switch (event) {
            case CREATE:
                if (isExecuting.compareAndSet(false, true)) {
                    new Thread(() -> {
                        create();
                        show();

                        if (file != null) {
                            if (isParsing.compareAndSet(false, true)) {
                                parseAndShow(file);
                                isParsing.set(false);
                            }
                        }

                        updateMenuBar();
                        isExecuting.set(false);
                    }).start();
                }
            case LOAD_MAP:
                if (isParsing.get()) {
                    new Thread(() -> {
                        // ask user to cancel parsing
                        Object[] options = { "Yes", "No" };
                        int choice = JOptionPane.showOptionDialog(
                                null,
                                "Are you sure you would like to cancel the parsing?",
                                "Cancel parsing?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null,
                                options,
                                options[1]);
                        // if yes: cancel
                        if (choice == JOptionPane.YES_OPTION)
                            cancelParsing();
                    }).start();
                } else if (isExecuting.compareAndSet(false, true)) {
                    parsingThread = new Thread(() -> {
                        isParsing.set(true);

                        closePreferences();
                        pauseSim();

                        File loadedFile = file == null ? askForMapFile() : file;
                        if (loadedFile != null)
                            parseAndShow(loadedFile);

                        isParsing.set(false);
                        updateMenuBar();
                        isExecuting.set(false);
                    });
                    parsingThread.start();
                }
                break;
            case NEW_SIM:
                if (isExecuting.compareAndSet(false, true)) { // unlock after accept/cancel
                    new Thread(() -> {
                        if (streetgraph != null) {
                            pauseSim();
                            newSim = true;
                            showPreferences();
                        } else
                            isExecuting.set(false);
                    }).start();
                }
                break;
            case ACCEPT:
                new Thread(() -> {
                    if (updateSimulationConfig()) {
                        closePreferences();

                        if (newSim) {
                            cleanupSimulation();
                            startNewSimulation();
                        }

                        updateMenuBar();
                        isExecuting.set(false);
                    }
                }).start();
                break;
            case CANCEL:
                new Thread(() -> {
                    closePreferences();
                    updateMenuBar();
                    isExecuting.set(false);
                }).start();
                break;
            case EDIT_SIM:
                if (isExecuting.compareAndSet(false, true)) { // unlock after accept/cancel
                    new Thread(() -> {
                        if (simulation.getScenario() != null) {
                            pauseSim();
                            newSim = false;
                            showPreferences();

                            updateMenuBar();
                        } else
                            isExecuting.set(false);
                    }).start();
                }
                break;
            case RUN_SIM:
                if (isExecuting.compareAndSet(false, true)) {
                    new Thread(() -> {
                        runSim();

                        updateMenuBar();
                        isExecuting.set(false);
                    }).start();
                }
                break;
            case RUN_SIM_ONE_STEP:
                if (isExecuting.compareAndSet(false, true)) {
                    new Thread(() -> {
                        pauseSim();
                        runSimOneStep();

                        updateMenuBar();
                        isExecuting.set(false);
                    }).start();
                }
                break;
            case PAUSE_SIM:
                if (isExecuting.compareAndSet(false, true)) {
                    new Thread(() -> {
                        pauseSim();

                        updateMenuBar();
                        isExecuting.set(false);
                    }).start();
                }
                break;
            case EXIT:
                exit();
                break;
        }

        lock_user_input.unlock();
    }

    private void updateMenuBar() {
        if (!isCreated) return;

        boolean hasStreetgraph = streetgraph != null;
        boolean hasScenario    = simulation.getScenario() != null;

        menubar.menuMap.setEnabled(true);
        menubar.menuMap.itemLoadMap.setEnabled(true);

        menubar.menuLogic.setEnabled(               hasStreetgraph);
        menubar.menuLogic.itemRunPause.setEnabled(  hasStreetgraph && hasScenario);
        menubar.menuLogic.itemRunOneStep.setEnabled(hasStreetgraph && hasScenario);
        menubar.menuLogic.itemEditSim.setEnabled(   hasStreetgraph && hasScenario);
        menubar.menuLogic.itemNewSim.setEnabled(    hasStreetgraph);
    }

    @Override
    public void addKeyCommand(short event, short vk, KeyCommand command) {
        mapviewer.addKeyCommand(event, vk, command);
    }

    /*
    |=========|
    | general |
    |=========|
    */
    private void create() {
        if (isCreated) throw new RuntimeException("The simulation controller has already been created.");

        try {
            mapviewer.create(config);
        } catch (UnsupportedFeatureException e) { e.printStackTrace(); }

        /* create preferences */
        preferences.create();
        preferences.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        preferences.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                transiate(GUIEvent.CANCEL);
            }
        });
        preferences.pack();
        preferences.setLocationRelativeTo(null);    // center on screen; close to setVisible
        preferences.setVisible(false);

        mapviewer.addOverlay(0, overlay);

        /* setup JFrame */
        menubar.menuMap.addActions(this);
        menubar.menuLogic.addActions(this);
        // todo toolbar for icons for run etc.
        //            JToolBar toolbar = new JToolBar("Menu");
        //            toolbar.add(new MTSMenuBar(this).create());
        //            addToTopBar(toolbar);
        frame.add(menubar, BorderLayout.NORTH);
        frame.setSize(mapviewer.getInitialWindowWidth(), mapviewer.getInitialWindowHeight());
        frame.add(mapviewer.getVisualizationPanel());

        /*
         * Note: JOGL automatically calls glViewport, we need to make sure that this
         * function is not called with a height or width of 0! Otherwise the program
         * crashes.
         */
        frame.setMinimumSize(new Dimension(100, 100));

        /* on close: stop the visualization and exit */
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        /* set state */
        isCreated = true;
    }

    private void show() {
        if (!isCreated) throw new RuntimeException("The simulation controller has to be created before it is shown.");

        frame.setLocationRelativeTo(null);    // center on screen; close to setVisible
        frame.setVisible(true);
        mapviewer.show();
    }

    private void exit() {
        mapviewer.stop();
        System.exit(0);
    }

    /*
    |================|
    | map and parser |
    |================|
    */
    private File askForMapFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(currentDirectory);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return ".osm";
            }

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;

                String extension = null;

                String filename = file.getName();
                int    i        = filename.lastIndexOf('.');

                if (i > 0)
                    if (i < filename.length() - 1)
                        extension = filename.substring(i + 1).toLowerCase();

                if (extension == null) return false;

                switch (extension) {
                    case "osm": return true;
                    default:    return false;
                }
            }
        });

        int action = chooser.showDialog(null, "Open");
        currentDirectory = chooser.getCurrentDirectory();
        if (action == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }

        return null;
    }

    private void parseAndShow(File file) {
        String cachedTitle = frame.getTitle();
        frame.setTitle("Parsing new map, please wait...");

        OSMParser.Result result = null;

        try {
            /* parse file and create tiled provider */
            result = mapviewer.parse(file);
        } catch (InterruptedException e) {
            logger.info("Interrupt parsing");
            result = null; // might be unnecessary here
        } catch (Exception e) {
            e.printStackTrace();
            Runtime.getRuntime().halt(1);
        }


        if (result != null) {
            if (result.streetgraph != null) {
                cleanupSimulation();
                streetgraph = result.streetgraph;

                try {
                    mapviewer.changeMap(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cachedTitle = "MicroTrafficSim - " + file.getName();
            }
        }

        frame.setTitle(cachedTitle);
    }

    private void cancelParsing() {
        while (parsingThread.isAlive()) {
            parsingThread.interrupt();
            Thread.yield();
        }
    }

    /*
    |============|
    | simulation |
    |============|
    */
    private void runSim() {
        menubar.menuLogic.simIsPaused(false);
        simulation.run();
    }

    private void runSimOneStep() {
        menubar.menuLogic.simIsPaused(true);
        simulation.runOneStep();
    }

    private void pauseSim() {
        menubar.menuLogic.simIsPaused(true);
        simulation.cancel();
    }

    private void startNewSimulation() {
        String oldTitle = frame.getTitle();
        frame.setTitle("Calculating vehicle routes 0%");

        /* create the scenario */
        Scenario scenario = scenarioConstructor.instantiate(config, streetgraph);
        scenarioBuilder.prepare(
                scenario,
                currentInPercent -> frame.setTitle("Calculating vehicle routes " + currentInPercent + "%"));
        /* initialize the scenario */
        simulation.setAndInitScenario(scenario);
        simulation.runOneStep();
        frame.setTitle(oldTitle);
    }

    /**
     * You can call this method to cleanup the simulation, which means resetting the streetgraph and removing all
     * scenario data.
     */
    private void cleanupSimulation() {
        if (streetgraph != null) streetgraph.reset();
        simulation.setAndInitScenario(null);
        menubar.menuLogic.simIsPaused(true);
    }

    /*
    |=============|
    | preferences |
    |=============|
    */
    private void showPreferences() {

        /* set enabled */

        /* general */
        preferences.setEnabled(PrefElement.sliderSpeedup,   PrefElement.sliderSpeedup.isEnabled());
        preferences.setEnabled(PrefElement.maxVehicleCount, newSim && PrefElement.maxVehicleCount.isEnabled());
        preferences.setEnabled(PrefElement.seed,            newSim && PrefElement.seed.isEnabled());
        preferences.setEnabled(PrefElement.metersPerCell,   newSim && PrefElement.metersPerCell.isEnabled());

        /* crossing logic */
        preferences.setEnabled(PrefElement.edgePriority,    newSim && PrefElement.edgePriority.isEnabled());
        preferences.setEnabled(PrefElement.priorityToThe,   newSim && PrefElement.priorityToThe.isEnabled());
        preferences.setEnabled(PrefElement.onlyOneVehicle,  newSim && PrefElement.onlyOneVehicle.isEnabled());
        preferences.setEnabled(PrefElement.friendlyStandingInJam,
                newSim && PrefElement.friendlyStandingInJam.isEnabled());

        /* visualization */

        /* concurrency */
        preferences.setEnabled(PrefElement.nThreads,            newSim && PrefElement.nThreads.isEnabled());
        preferences.setEnabled(PrefElement.vehiclesPerRunnable, PrefElement.vehiclesPerRunnable.isEnabled());
        preferences.setEnabled(PrefElement.nodesPerThread,      PrefElement.nodesPerThread.isEnabled());

        /* init values */
        preferences.setSettings(config);

        /* show */
        preferences.setVisible(true);
        preferences.toFront();
    }

    /**
     * @return True if the settings were correct; false otherwise
     */
    private boolean updateSimulationConfig() {
        try {
            config.update(preferences.getCorrectSettings());
            return true;
        } catch (IncorrectSettingsException e) {
            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "Error: wrong preferences values",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void closePreferences() {
        preferences.setVisible(false);
        preferences.setAllEnabled(false);
    }
}
