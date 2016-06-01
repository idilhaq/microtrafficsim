package microtrafficsim.examples.mapviewer.tilebased;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.map.layers.TileLayerDefinition;
import microtrafficsim.core.map.tiles.TilingScheme;
import microtrafficsim.core.parser.MapFeatureDefinition;
import microtrafficsim.core.parser.MapFeatureGenerator;
import microtrafficsim.core.parser.OSMParser;
import microtrafficsim.core.vis.UnsupportedFeatureException;
import microtrafficsim.core.vis.VisualizationPanel;
import microtrafficsim.core.vis.VisualizerConfig;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.tiles.TileProvider;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerGenerator;
import microtrafficsim.core.vis.map.tiles.layers.FeatureTileLayerSource;
import microtrafficsim.core.vis.map.tiles.layers.LayeredTileMap;
import microtrafficsim.core.vis.map.tiles.layers.TileLayerProvider;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.utils.Color;
import microtrafficsim.core.vis.tilebased.TileBasedVisualization;
import microtrafficsim.core.vis.tilebased.TileGridOverlay;
import microtrafficsim.osm.parser.features.streets.StreetComponent;
import microtrafficsim.osm.parser.features.streets.StreetComponentFactory;
import microtrafficsim.osm.parser.features.streets.StreetFeatureGenerator;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponent;
import microtrafficsim.osm.parser.processing.osm.sanitizer.SanitizerWayComponentFactory;
import microtrafficsim.osm.parser.relations.restriction.RestrictionRelationFactory;
import microtrafficsim.osm.primitives.Way;
import microtrafficsim.utils.resources.PackagedResource;
import microtrafficsim.utils.resources.Resource;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;


public class Example {

	public static final String DEFAULT_OSM_XML = "map.osm";
	public static final boolean PRINT_FRAME_STATS = false;

	public static final int WINDOW_WIDTH = 1600;
	public static final int WINDOW_HEIGHT = 900;
	public static final int MSAA = 0;
	public static final int NUM_SEGMENT_WORKERS = Math.max(Runtime.getRuntime().availableProcessors() - 2, 2);

	public static final float STREET_SCALE_NORMAL = (float) (1.0 / Math.pow(2, 19));

	public static final int TILE_GRID_LEVEL = 12;
	
	
	public static TileBasedVisualization createVisualization(TileProvider provider) {
		TileBasedVisualization vis = new TileBasedVisualization(
				WINDOW_WIDTH,
				WINDOW_HEIGHT,
				provider,
				NUM_SEGMENT_WORKERS);

		vis.getKeyController().addKeyCommand(
				KeyEvent.EVENT_KEY_PRESSED,
				KeyEvent.VK_F12,
				e -> Utils.asyncScreenshot(vis.getRenderContext()));
		
		vis.getKeyController().addKeyCommand(
				KeyEvent.EVENT_KEY_PRESSED,
				KeyEvent.VK_ESCAPE,
				e -> Runtime.getRuntime().halt(0));
		
		vis.getRenderContext().setUncaughtExceptionHandler(new Utils.DebugExceptionHandler());

		// vis.putOverlay(0, new TileGridOverlay(provider.getTilingScheme()));

		return vis;
	}
	
	public static VisualizationPanel createVisualizationPanel(TileBasedVisualization vis) throws UnsupportedFeatureException {
		VisualizerConfig config = vis.getDefaultConfig();
		
		if (MSAA > 1) {
			config.glcapabilities.setSampleBuffers(true);
			config.glcapabilities.setNumSamples(MSAA);
		}
		
		return new VisualizationPanel(vis, config);
	}
	
	
	public static OSMParser getParser() {
		// predicates to match/select features
		Predicate<Way> predMotorway = w -> w.visible
				&& ("motorway".equals(w.tags.get("highway")) || "motorway_link".equals(w.tags.get("highway")))
				&& ((w.tags.get("area") == null) || w.tags.get("area").equals("no"));

		Predicate<Way> predTrunk = w -> w.visible
				&& ("trunk".equals(w.tags.get("highway")) || "trunk_link".equals(w.tags.get("highway")))
				&& ((w.tags.get("area") == null) || w.tags.get("area").equals("no"));

		Predicate<Way> predPrimary = w -> w.visible
				&& ("primary".equals(w.tags.get("highway")) || "primary_link".equals(w.tags.get("highway")))
				&& ((w.tags.get("area") == null) || w.tags.get("area").equals("no"));

		Predicate<Way> predOther = w -> {
			if (!w.visible) return false;
			if (w.tags.get("highway") == null) return false;
			if (w.tags.get("area") != null && !w.tags.get("area").equals("no")) return false;

			switch (w.tags.get("highway")) {
			case "secondary":		return true;
			case "tertiary":		return true;
			case "unclassified":	return true;
			case "residential":		return true;
			//case "service":			return true;
			
			case "tertiary_link":	return true;
			
			case "living_street":	return true;
			case "track":			return true;
			case "road":			return true;
			}
			
			return false;
		};

		// set the generator-indices
		int genindexBefore = 256;
		int genindexStreetGraph = 512;
		
		// create the feature generators
		MapFeatureGenerator<Street> streetsGenerator = new StreetFeatureGenerator();
		
		// define the features
		MapFeatureDefinition<Street> motorway = new MapFeatureDefinition<>(
				"streets:motorway",
				genindexStreetGraph + 1,		// generate after StreetGraph
				streetsGenerator,
				n -> false,
				predMotorway);

		MapFeatureDefinition<Street> trunk = new MapFeatureDefinition<>(
				"streets:trunk",
				genindexStreetGraph + 1,		// generate after StreetGraph
				streetsGenerator,
				n -> false,
				predTrunk);

		MapFeatureDefinition<Street> primary = new MapFeatureDefinition<>(
				"streets:primary",
				genindexStreetGraph + 1,		// generate after StreetGraph
				streetsGenerator,
				n -> false,
				predPrimary);

		MapFeatureDefinition<Street> other = new MapFeatureDefinition<>(
				"streets:other",
				genindexStreetGraph + 1,		// generate after StreetGraph
				streetsGenerator,
				n -> false,
				predOther);

		// create and return the parser
		return new OSMParser.Config()
				.setGeneratorIndexBefore(genindexBefore)
				.setGeneratorIndexStreetGraph(genindexStreetGraph)
				.putMapFeatureDefinition(motorway)
				.putMapFeatureDefinition(trunk)
				.putMapFeatureDefinition(primary)
				.putMapFeatureDefinition(other)
				.putWayInitializer(StreetComponent.class, new StreetComponentFactory())
				.putWayInitializer(SanitizerWayComponent.class, new SanitizerWayComponentFactory())
				.putRelationInitializer("restriction", new RestrictionRelationFactory())
				.createParser();
	}

	
	public static Set<TileLayerDefinition> getLayerDefinitions() {
		boolean useAdjacencyPrimitives = true;

		HashSet<TileLayerDefinition> layers = new HashSet<>();

		// shader resources
		Resource vertShader;
		Resource fragShader;
		Resource geomShader;

		if (useAdjacencyPrimitives) {
			vertShader = new PackagedResource(Example.class, "/shaders/features/streets/streets.vs");
			fragShader = new PackagedResource(Example.class, "/shaders/features/streets/streets.fs");
			geomShader = new PackagedResource(Example.class, "/shaders/features/streets/streets_round.gs");
		} else {
			vertShader = new PackagedResource(Example.class, "/shaders/basic.vs");
			fragShader = new PackagedResource(Example.class, "/shaders/basic.fs");
			geomShader = null;
		}

		// create shader
		ShaderProgramSource progStreets = new ShaderProgramSource("streets");
		progStreets.addSource(GL3.GL_VERTEX_SHADER, vertShader);
		progStreets.addSource(GL3.GL_FRAGMENT_SHADER, fragShader);

		if (geomShader != null)
			progStreets.addSource(GL3.GL_GEOMETRY_SHADER, geomShader);

		// create styles
		Style motorwayOutline = new Style(progStreets);
		motorwayOutline.setUniformSupplier("u_color", () -> Color.fromRGB(0xFF7336).toVec4f());
		motorwayOutline.setUniformSupplier("u_linewidth", () -> 46.0f);
		motorwayOutline.setUniformSupplier("u_viewscale_norm", () -> STREET_SCALE_NORMAL);
		//motorwayOutline.setUniformSupplier("u_cap_type", () -> 2);
		//motorwayOutline.setUniformSupplier("u_join_type", () -> 3);
		motorwayOutline.setProperty("adjacency_primitives", useAdjacencyPrimitives);
		motorwayOutline.setProperty("use_joins_when_possible", true);

		Style motorwayInner = new Style(progStreets);
		motorwayInner.setUniformSupplier("u_color", () -> Color.fromRGB(0xFFFFFF).toVec4f());
		motorwayInner.setUniformSupplier("u_linewidth", () -> 40.0f);
		motorwayInner.setUniformSupplier("u_viewscale_norm", () -> STREET_SCALE_NORMAL);
		//motorwayInner.setUniformSupplier("u_cap_type", () -> 2);
		//motorwayInner.setUniformSupplier("u_join_type", () -> 3);
		motorwayInner.setProperty("adjacency_primitives", useAdjacencyPrimitives);
		motorwayInner.setProperty("use_joins_when_possible", true);


		Style trunkOutline = new Style(progStreets);
		trunkOutline.setUniformSupplier("u_color", () -> Color.fromRGB(0x8FC270).toVec4f());
		trunkOutline.setUniformSupplier("u_linewidth", () -> 46.0f);
		trunkOutline.setUniformSupplier("u_viewscale_norm", () -> STREET_SCALE_NORMAL);
		//trunkOutline.setUniformSupplier("u_cap_type", () -> 2);
		//trunkOutline.setUniformSupplier("u_join_type", () -> 3);
		trunkOutline.setProperty("adjacency_primitives", useAdjacencyPrimitives);
		trunkOutline.setProperty("use_joins_when_possible", true);

		Style trunkInner = new Style(progStreets);
		trunkInner.setUniformSupplier("u_color", () -> Color.fromRGB(0xFFFFFF).toVec4f());
		trunkInner.setUniformSupplier("u_linewidth", () -> 40.0f);
		trunkInner.setUniformSupplier("u_viewscale_norm", () -> STREET_SCALE_NORMAL);
		//trunkInner.setUniformSupplier("u_cap_type", () -> 2);
		//trunkInner.setUniformSupplier("u_join_type", () -> 3);
		trunkInner.setProperty("adjacency_primitives", useAdjacencyPrimitives);
		trunkInner.setProperty("use_joins_when_possible", true);


		Style primaryOutline = new Style(progStreets);
		primaryOutline.setUniformSupplier("u_color", () -> Color.fromRGB(0x0595D1).toVec4f());
		primaryOutline.setUniformSupplier("u_linewidth", () -> 46.0f);
		primaryOutline.setUniformSupplier("u_viewscale_norm", () -> STREET_SCALE_NORMAL);
		//primaryOutline.setUniformSupplier("u_cap_type", () -> 2);
		//primaryOutline.setUniformSupplier("u_join_type", () -> 3);
		primaryOutline.setProperty("adjacency_primitives", useAdjacencyPrimitives);
		primaryOutline.setProperty("use_joins_when_possible", true);

		Style primaryInner = new Style(progStreets);
		primaryInner.setUniformSupplier("u_color", () -> Color.fromRGB(0xFFFFFF).toVec4f());
		primaryInner.setUniformSupplier("u_linewidth", () -> 40.0f);
		primaryInner.setUniformSupplier("u_viewscale_norm", () -> STREET_SCALE_NORMAL);
		//primaryInner.setUniformSupplier("u_cap_type", () -> 2);
		//primaryInner.setUniformSupplier("u_join_type", () -> 3);
		primaryInner.setProperty("adjacency_primitives", useAdjacencyPrimitives);
		primaryInner.setProperty("use_joins_when_possible", true);


		Style otherOutline = new Style(progStreets);
		otherOutline.setUniformSupplier("u_color", () -> Color.fromRGB(0x686868).toVec4f());
		otherOutline.setUniformSupplier("u_linewidth", () -> 28.0f);
		otherOutline.setUniformSupplier("u_viewscale_norm", () -> STREET_SCALE_NORMAL);
		//otherOutline.setUniformSupplier("u_cap_type", () -> 2);
		//otherOutline.setUniformSupplier("u_join_type", () -> 3);
		otherOutline.setProperty("adjacency_primitives", useAdjacencyPrimitives);
		otherOutline.setProperty("use_joins_when_possible", true);

		Style otherInner = new Style(progStreets);
		otherInner.setUniformSupplier("u_color", () -> Color.fromRGB(0xFFFFFF).toVec4f());
		otherInner.setUniformSupplier("u_linewidth", () -> 24.0f);
		otherInner.setUniformSupplier("u_viewscale_norm", () -> STREET_SCALE_NORMAL);
		//otherInner.setUniformSupplier("u_cap_type", () -> 2);
		//otherInner.setUniformSupplier("u_join_type", () -> 3);
		otherInner.setProperty("adjacency_primitives", useAdjacencyPrimitives);
		otherInner.setProperty("use_joins_when_possible", true);

		
		// create layers
		int index = 0;
		layers.add(new TileLayerDefinition("streets:other:outline", index++,
				new FeatureTileLayerSource("streets:other", otherOutline)));

		layers.add(new TileLayerDefinition("streets:primary:outline", index++,
				new FeatureTileLayerSource("streets:primary", primaryOutline)));

		layers.add(new TileLayerDefinition("streets:trunk:outline", index++,
				new FeatureTileLayerSource("streets:trunk", trunkOutline)));

		layers.add(new TileLayerDefinition("streets:motorway:outline", index++,
				new FeatureTileLayerSource("streets:motorway", motorwayOutline)));


		layers.add(new TileLayerDefinition("streets:other:base", index++,
				new FeatureTileLayerSource("streets:other", otherInner)));

		layers.add(new TileLayerDefinition("streets:primary:base", index++,
				new FeatureTileLayerSource("streets:primary", primaryInner)));

		layers.add(new TileLayerDefinition("streets:trunk:base", index++,
				new FeatureTileLayerSource("streets:trunk", trunkInner)));

		layers.add(new TileLayerDefinition("streets:motorway:base", index++,
				new FeatureTileLayerSource("streets:motorway", motorwayInner)));

		return layers;
	}

	public static TileLayerProvider getLayerProvider(Projection projection, TilingScheme scheme, Set<TileLayerDefinition> layers) {
        LayeredTileMap provider = new LayeredTileMap(projection, scheme);
        FeatureTileLayerGenerator generator = new FeatureTileLayerGenerator();
        provider.putGenerator(FeatureTileLayerSource.class, generator);

        layers.forEach(provider::addLayer);

		return provider;
	}
}
