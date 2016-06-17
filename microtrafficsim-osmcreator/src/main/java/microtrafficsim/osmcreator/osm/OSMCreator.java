package microtrafficsim.osmcreator.osm;

import ZZZ_NEU_microtrafficsim.osmcreator.geometry.GraphPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import microtrafficsim.core.map.Bounds;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.vis.map.projections.MercatorProjection;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.osmcreator.Constants;
import microtrafficsim.osmcreator.graph.Crossroad;
import microtrafficsim.osmcreator.graph.Street;
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
                BiFunction<Double, Double, Coordinate> unproject = transformation(
                        new Bounds(0, 0, Constants.INITIALZE_SCREEN_HEIGHT, Constants.INITIALZE_SCREEN_WIDTH),
                        new Bounds(9.4292700, 48.9392200d, 9.4346300, 9.4292700)
                );

                /* transformation */

                /* write */
                writer.writeStartDocument();
                writer.writeStartElement("osm");

                writer.writeStartElement("bounds");
                writer.writeAttribute("minlat", "48.9392200");
                writer.writeAttribute("minlon=", "9.4292700");
                writer.writeAttribute("maxlat=", "48.9417000");
                writer.writeAttribute("maxlon=", "9.4346300");
                writer.writeEndElement();

                for (Crossroad node : nodes) {
                    writer.writeStartElement("node");
                    writer.writeAttribute("id", "" + longIDGenerator.next());
                    writer.writeAttribute("visible", "true");
                    Coordinate coordinate = unproject.apply(node.getTranslateX(), node.getTranslateY());
                    writer.writeAttribute("lat", "" + coordinate.lat);
                    writer.writeAttribute("lon", "" + coordinate.lon);
                    System.out.println(node.getTranslateX());
                    System.out.println(node.getTranslateY());
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

    private BiFunction<Double, Double, Coordinate> transformation(Bounds from, Bounds to) {
        /* unscale from start bounds */
        double transformX = 1 / (from.maxlon - from.minlon);
        double transformY = -1 / (from.maxlat - from.minlat);

        /* scale to new bounds */
        transformX *= (to.maxlon - to.minlon);
        transformY *= (to.maxlat - to.minlat);

        /* translate */
        transformX += to.minlon;
        transformY += to.minlat;

        /* finish */
        final double factorX = transformX;
        final double factorY = transformY;
        return (x, y) -> new Coordinate(factorY * y, factorX * x);
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
