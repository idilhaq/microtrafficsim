package microtrafficsim.osmcreator.graph;

/**
 * @author Dominic Parga Cacheiro
 */
public enum StreetType {
    MOTORWAY("motorway"),       // 100
    PRIMARY("primary"),         //  50
    SECONDARY("secondary"),     //  50
    RESIDENTIAL("residential"); //  30

    public String osmname;

    StreetType(String osmname) {
        this.osmname = osmname;
    }

    public StreetType arithmeticalShiftLow() {
        switch (this) {
            case MOTORWAY:
                return PRIMARY;
            case PRIMARY:
                return SECONDARY;
            case SECONDARY:
                return RESIDENTIAL;
            case RESIDENTIAL:
                return MOTORWAY;
        }
        return null;
    }

    public StreetType arithmeticalShiftHigh() {
        switch (this) {
            case MOTORWAY:
                return RESIDENTIAL;
            case PRIMARY:
                return MOTORWAY;
            case SECONDARY:
                return PRIMARY;
            case RESIDENTIAL:
                return SECONDARY;
        }
        return null;
    }
}