package microtrafficsim.core.parser.features.polygons;


import microtrafficsim.core.logic.Node;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.features.Polygon;
import microtrafficsim.core.parser.features.MapFeatureGenerator;
import microtrafficsim.core.vis.mesh.utils.Polygons;
import microtrafficsim.osm.parser.base.DataSet;
import microtrafficsim.osm.parser.ecs.entities.NodeEntity;
import microtrafficsim.osm.parser.ecs.entities.WayEntity;
import microtrafficsim.osm.parser.features.FeatureDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PolygonFeatureGenerator implements MapFeatureGenerator<Polygon> {

    private Map<String, Feature<Polygon>> generated;

    public PolygonFeatureGenerator() {
        this.generated = new HashMap<>();
    }

    @Override
    public Map<String, Feature<Polygon>> getGeneratedFeatures() {
        return generated;
    }

    @Override
    public void clear() {
        this.generated.clear();
    }

    @Override
    public void execute(DataSet dataset, FeatureDefinition feature, Properties properties) throws Exception {
        ArrayList<Polygon> polygons = new ArrayList<>();

        for (WayEntity way : dataset.ways.values()) {
            if (!way.features.contains(feature)) continue;

            boolean explicit = way.nodes[0] == way.nodes[way.nodes.length - 1];

            Coordinate[] outline = new Coordinate[explicit ? way.nodes.length : way.nodes.length + 1];
            for (int i = 0; i < way.nodes.length; i++) {
                NodeEntity node = dataset.nodes.get(way.nodes[i]);
                outline[i] = new Coordinate(node.lat, node.lon);
            }

            if (!explicit)
                outline[outline.length - 1] = new Coordinate(outline[0]);

            if (properties.bounds == Properties.BoundaryManagement.CLIP) {
                outline = Polygons.clip(dataset.bounds, outline);
                if (outline == null) continue;
            }

            polygons.add(new Polygon(way.id, outline));
        }

        Polygon[] data = polygons.toArray(new Polygon[polygons.size()]);
        generated.put(feature.getName(), new Feature<>(feature.getName(), Polygon.class, data));
    }
}
