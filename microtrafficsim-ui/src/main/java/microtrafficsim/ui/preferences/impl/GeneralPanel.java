package microtrafficsim.ui.preferences.impl;

import microtrafficsim.core.simulation.configs.ScenarioConfig;
import microtrafficsim.utils.id.ConcurrentSeedGenerator;
import microtrafficsim.ui.preferences.IncorrectSettingsException;
import microtrafficsim.ui.preferences.PrefElement;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;


/**
 * @author Dominic Parga Cacheiro
 */
public class GeneralPanel extends PreferencesPanel {
    private JSlider    sliderSpeedup;
    private JTextField tfMaxVehicleCount, tfSeed, tfMetersPerCell;

    public GeneralPanel() {
        super("General");
    }

    /*
    |============|
    | components |
    |============|
    */
    private void addSpeedup() {
        incRow();

        /* components */
        sliderSpeedup = new JSlider(0, 100);

        /* JLabel */
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx            = 0;
        constraints.anchor             = GridBagConstraints.WEST;
        JLabel label                   = new JLabel("speedup: ");
        label.setFont(PreferencesFrame.TEXT_FONT);
        incColumn();
        add(label, constraints);

        /* JSlider */
        sliderSpeedup.setMajorTickSpacing(5);
        sliderSpeedup.setMinorTickSpacing(1);
        // set labels for slider
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = sliderSpeedup.getMinimum(); i <= sliderSpeedup.getMaximum(); i++)
            if (i % 10 == 0) labelTable.put(i, new JLabel("" + i));
        sliderSpeedup.setLabelTable(labelTable);
        sliderSpeedup.setPaintTicks(true);
        sliderSpeedup.setSnapToTicks(true);
        sliderSpeedup.setPaintLabels(true);
        sliderSpeedup.setPreferredSize(
                new Dimension(2 * sliderSpeedup.getPreferredSize().width, sliderSpeedup.getPreferredSize().height));

        constraints           = new GridBagConstraints();
        constraints.fill      = GridBagConstraints.HORIZONTAL;
        constraints.weightx   = 1;
        constraints.gridwidth = 2;
        constraints.anchor    = GridBagConstraints.WEST;
        incColumn();
        add(sliderSpeedup, constraints);
    }

    private void addMaxVehicleCount() {
        tfMaxVehicleCount = new JTextField();
        configureAndAddJTextFieldRow("max vehicle count: ", tfMaxVehicleCount);
    }

    private void addSeed() {
        tfSeed = new JTextField();
        configureAndAddJTextFieldRow("seed: ", tfSeed);
    }

    private void addMetersPerCell() {
        tfMetersPerCell = new JTextField();
        configureAndAddJTextFieldRow("meters per cell", tfMetersPerCell);
    }

    /*
    |=================|
    | (i) Preferences |
    |=================|
    */
    @Override
    public void create() {
        addSpeedup();
        addMaxVehicleCount();
        addSeed();
        addMetersPerCell();
        setAllEnabled(false);
    }

    @Override
    public void setSettings(ScenarioConfig config) {
        sliderSpeedup.setValue(config.speedup);
        tfMaxVehicleCount.setText("" + config.maxVehicleCount);
        tfSeed.setText("" + config.seed);
        tfMetersPerCell.setText("" + config.metersPerCell);
    }

    @Override
    public ScenarioConfig getCorrectSettings() throws IncorrectSettingsException {
        ScenarioConfig             config           = new ScenarioConfig();
        boolean                    exceptionOccured = false;
        IncorrectSettingsException exception        = new IncorrectSettingsException();


        config.speedup = sliderSpeedup.getValue();
        try {
            config.maxVehicleCount = Integer.parseInt(tfMaxVehicleCount.getText());
        } catch (NumberFormatException e) {
            exception.appendToMessage("\"Max vehicle count\" should be an integer.\n");
            exceptionOccured = true;
        }
        try {
            config.seed = Long.parseLong(tfSeed.getText());
        } catch (NumberFormatException e) {
            exception.appendToMessage("\"Seed\" should be a long.\n");
            exceptionOccured = true;
        }
        try {
            config.metersPerCell = Float.parseFloat(tfMetersPerCell.getText());
        } catch (NumberFormatException e) {
            exception.appendToMessage("\"Meters per cell\" should be a float.\n");
            exceptionOccured = true;
        }


        if (exceptionOccured) throw exception;


        return config;
    }

    @Override
    public void setEnabled(PrefElement id, boolean enabled) {
        enabled = id.isEnabled() && enabled;

        switch (id) {
        case sliderSpeedup:   sliderSpeedup.setEnabled(enabled);     break;
        case maxVehicleCount: tfMaxVehicleCount.setEnabled(enabled); break;
        case seed:            tfSeed.setEnabled(enabled);            break;
        case metersPerCell:   tfMetersPerCell.setEnabled(enabled);   break;
        }
    }
}
