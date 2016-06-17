package microtrafficsim.osmcreator.osm;

import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.math.Vec2d;
import microtrafficsim.osmcreator.Constants;
import microtrafficsim.osmcreator.graph.Crossroad;
import microtrafficsim.osmcreator.graph.Street;
import microtrafficsim.osmcreator.graph.StreetDirection;
import microtrafficsim.utils.id.BasicLongIDGenerator;
import microtrafficsim.utils.id.LongIDGenerator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author Dominic Parga Cacheiro
 */
public class OSMCreator {

    private File currentDirectory;
    private Projection projection = new MercatorProjection();

    public OSMCreator() {
        currentDirectory = new File(System.getProperty("user.dir"));
    }

    public void createOSMFile(Stage stage, Pane graph, Set<Street> streets) {
        File destination = askForDataDirectory(stage);

        if (destination != null) {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();

            try {
                XMLStreamWriter writer = factory.createXMLStreamWriter(
                        new FileWriter(destination));

                /* prepare data structures*/
                Set<Crossroad> nodes = new HashSet<>();
                for (Street street : streets) {
                    nodes.add(street.origin);
                    nodes.add(street.destination);
                }
                LongIDGenerator longIDGenerator = new BasicLongIDGenerator();
                Bounds screenBounds = new Bounds(Constants.INITIALZE_SCREEN_HEIGHT, 0, 0, Constants.INITIALZE_SCREEN_WIDTH);
                Bounds mapBounds = new Bounds(9.4292700, 48.9392200, 9.4346300, 48.941700);
                BiFunction<Double, Double, Coordinate> transform = transformation(screenBounds, mapBounds);

                /* write */
                writer.writeStartDocument();
                writer.writeStartElement("osm");

                writer.writeStartElement("bounds");
                writer.writeAttribute("minlat", "" + mapBounds.minlat);
                writer.writeAttribute("minlon", "" + mapBounds.minlon);
                writer.writeAttribute("maxlat", "" + mapBounds.maxlat);
                writer.writeAttribute("maxlon", "" + mapBounds.maxlon);
                writer.writeEndElement();

                /* nodes */
                for (Crossroad node : nodes) {
                    node.ID = longIDGenerator.next();

                    writer.writeStartElement("node");
                    writer.writeAttribute("id", "" + node.ID);
                    writer.writeAttribute("visible", "true");
                    Coordinate coordinate = transform.apply(node.getTranslateY(), node.getTranslateX());
//                    Coordinate coordinate = projection.unproject(pos);
                    writer.writeAttribute("lat", "" + coordinate.lat);
                    writer.writeAttribute("lon", "" + coordinate.lon);
                    writer.writeEndElement();
                }

                /* streets */
                for (Street street : streets) {
                    street.ID = longIDGenerator.next();

                    writer.writeStartElement("way");
                    writer.writeAttribute("id", "" + street.ID);
                    writer.writeAttribute("visible", "true");

                    writer.writeStartElement("nd");
                    writer.writeAttribute("ref", "" + street.origin.ID);
                    writer.writeEndElement();

                    writer.writeStartElement("nd");
                    writer.writeAttribute("ref", "" + street.destination.ID);
                    writer.writeEndElement();

                    writer.writeStartElement("tag");
                    writer.writeAttribute("k", "oneway");
                    writer.writeAttribute("v", (
                            street.getStreetDirectionFrom(street.origin) == StreetDirection.BIDIRECTIONAL)
                            ? "no"
                            : "yes");
                    writer.writeEndElement();

                    writer.writeStartElement("tag");
                    writer.writeAttribute("k", "highway");
                    writer.writeAttribute("v", street.getStreetType().osmname);
                    writer.writeEndElement();

                    writer.writeEndElement();
                }

                writer.writeEndElement();
                writer.writeEndDocument();

                writer.flush();
                writer.close();
            } catch (XMLStreamException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * todo HARDCODED! and not unprojected at all
     */
    private BiFunction<Double, Double, Coordinate> transformation(Bounds from, Bounds to) {
        return (lat, lon) -> {
            double fromWidth = Math.abs(from.maxlon - from.minlon);
            double fromHeight = Math.abs(from.maxlat - from.minlat);
            double toWidth = Math.abs(to.maxlon - to.minlon);
            double toHeight = Math.abs(to.maxlat - to.minlat);

            /* unscale */
            lat = 1 - lat / fromHeight;
            lon = lon / fromWidth;

            /* scale */
            lat = lat * toHeight + to.minlat;
            lon = lon * toWidth + to.minlon;

            /* finish */
            return new Coordinate(lat, lon);
        };
    }

    private File askForDataDirectory(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save map as osm-file");
        chooser.setInitialDirectory(currentDirectory);
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("OSM Files", "*.osm"));
        return chooser.showSaveDialog(stage);
    }

    private File o_O() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(currentDirectory);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return ".osm";
            }

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) return true;

                String extension = null;

                String s = f.getName();
                int i = s.lastIndexOf('.');

                if (i > 0 &&  i < s.length() - 1)
                    extension = s.substring(i+1).toLowerCase();

                if (extension == null) return false;

                switch (extension) {
                    case "osm":		return true;
                    default:		return false;
                }
            }
        });

        int action = chooser.showOpenDialog(null);

        currentDirectory = chooser.getCurrentDirectory();
        if (action == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile();

        return null;
    }
}
