package ZZZ_zoomable_microtrafficsim.osmcreator.osm;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ZZZ_zoomable_microtrafficsim.osmcreator.graph.Street;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 * @author Dominic Parga Cacheiro
 */
public class OSMCreator {

  private File currentDirectory;

  public OSMCreator() {
    currentDirectory = new File(System.getProperty("user.dir"));
  }

  public void createOSMFile(Stage stage, Set<Street> streets) {
    File destination = askForDataDirectory(stage);

    if (destination != null) {
      XMLOutputFactory factory = XMLOutputFactory.newInstance();

      try {
        XMLStreamWriter writer = factory.createXMLStreamWriter(
                new FileWriter(destination));

        writer.writeStartDocument();
        writer.writeStartElement("osm");

          writer.writeStartElement("data");
            writer.writeAttribute("name", "value");
          writer.writeEndElement();

        writer.writeEndElement();
        writer.writeEndDocument();

        writer.flush();
        writer.close();
      } catch (XMLStreamException | IOException e) {
        e.printStackTrace();
      }
    }
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
