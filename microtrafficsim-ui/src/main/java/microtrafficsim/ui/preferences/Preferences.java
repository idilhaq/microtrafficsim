package microtrafficsim.ui.preferences;

import microtrafficsim.core.simulation.configs.ScenarioConfig;


/**
 * @author Dominic Parga Cacheiro
 */
public interface Preferences {

    void create();

    /**
     * Sets all relevant settings to the value given by the {@code config}
     *
     * @param config Gives values for the (initial) settings
     */
    void setSettings(ScenarioConfig config);

    ScenarioConfig getCorrectSettings() throws IncorrectSettingsException;

    /**
     * Makes the id-preference editable, if the given parameter {@code enabled} is true. If the id is not enabled
     * ({@link PrefElement#isEnabled()}), the given parameter is ignored and the id-preference is not editable.
     *
     * @param id      This id should be editable.
     * @param enabled Decides whether the given id-preference should be editable or not.
     */
    void setEnabled(PrefElement id, boolean enabled);

    default void setAllEnabled(boolean enabled) {
        for (PrefElement prefElement : PrefElement.values())
            setEnabled(prefElement, enabled);
    }

    default void addSubpreferences(Preferences preferences) {}
}
