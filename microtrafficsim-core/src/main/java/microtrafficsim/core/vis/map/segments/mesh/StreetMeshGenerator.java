package microtrafficsim.core.vis.map.segments.mesh;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.Feature;
import microtrafficsim.core.map.features.Street;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.map.segments.FeatureSegmentLayerSource;
import microtrafficsim.core.vis.mesh.Mesh;
import microtrafficsim.core.vis.mesh.features.StreetMesh;
import microtrafficsim.core.vis.mesh.style.Style;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec3f;
import microtrafficsim.utils.collections.HashMultiMap;
import microtrafficsim.utils.hashing.FNVHashBuilder;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class StreetMeshGenerator implements FeatureMeshGenerator {

	@Override
	public FeatureMeshKey getKey(RenderContext context, FeatureSegmentLayerSource src, Projection projection) {
		return new StreetMeshKey(
				context,
				src.getFeatureProvider(),
				src.getFeatureName(),
				projection,
				getPropAdjacency(src.getStyle()),
				getPropJoinsWhenPossible(src.getStyle())
        );
	}
	
	@Override
	public Mesh generate(RenderContext context, FeatureSegmentLayerSource src, Projection projection) {
		boolean adjacency = getPropAdjacency(src.getStyle());
		boolean joinsWhenPossible = getPropJoinsWhenPossible(src.getStyle());

        Feature<Street>  feature = src.getFeatureProvider().require(src.getFeatureName());
        if (feature == null) return null;

		// generate mesh
		ArrayList<Vertex> vertices = new ArrayList<>();
		ArrayList<ArrayList<Integer>> indices = new ArrayList<>();
		int mode;

		if (adjacency) {
			generateAdjacencyMesh(context, feature, joinsWhenPossible, vertices, indices);
			mode = GL3.GL_LINE_STRIP_ADJACENCY;
		} else {
			generateStandardMesh(context, feature, vertices, indices);
			mode = GL3.GL_LINE_STRIP;
		}

        src.getFeatureProvider().release(feature);

		// create vertex buffer
		FloatBuffer vb = FloatBuffer.allocate(vertices.size() * 3);
		for (Vertex v : vertices) {
			Vec2d projected = projection.project(v.coordinate);
			vb.put((float) projected.x);
			vb.put((float) projected.y);
			vb.put(v.layer);
		}
		vb.rewind();

		// create index buffer
		int nIndices = 0;
		for (ArrayList<Integer> i : indices) nIndices += i.size();

		IntBuffer ib = IntBuffer.allocate(nIndices);
		for (ArrayList<Integer> bucket : indices)
			for (int i : bucket)
				ib.put(i);
		ib.rewind();

		// create mesh and buckets
		StreetMesh mesh = new StreetMesh(GL3.GL_STATIC_DRAW, mode, vb, ib);
		ArrayList<StreetMesh.Bucket> buckets = new ArrayList<>();

		int offset = 0;
		for (ArrayList<Integer> indexbucket : indices) {
			float layer = vertices.get(indexbucket.get(0)).layer;
			int count = indexbucket.size();

			buckets.add(mesh.new Bucket(new Vec3f(0, 0, layer), offset, count));
			offset += count;
		}

		mesh.setBuckets(buckets);
		return mesh;
	}

	private void generateAdjacencyMesh(
			RenderContext context,
			Feature<? extends Street> feature,
			boolean joinsWhenPossible,
			ArrayList<Vertex> vertices,
			ArrayList<ArrayList<Integer>> indices)
	{
		int restartIndex = context.PrimitiveRestart.getIndex();

		// get all intersections
		HashMultiMap<Coordinate, Street> intersections = null;
		if (joinsWhenPossible) {
			intersections = new HashMultiMap<>();
			for (Street street : feature.getData()) {
				intersections.add(street.coordinates[0], street);
				intersections.add(street.coordinates[street.coordinates.length - 1], street);
			}
		}

		// generate line geometry
		int counter = 0;
		HashMap<Vertex, Integer> indexmap = new HashMap<>();
		HashMap<Float, ArrayList<Integer>> buckets = new HashMap<>();

		for (Street street : feature.getData()) {
			ArrayList<Integer> bucket = buckets.get(street.layer);
			if (bucket == null) {
				bucket = new ArrayList<>();
				buckets.put(street.layer, bucket);
			}

			// duplicate or extrude first vertex
			{
				Vertex v;
				if (joinsWhenPossible && intersections.count(street.coordinates[0]) == 2) {
					Coordinate xpoint = street.coordinates[0];

					Iterator<Street> it = intersections.get(xpoint).iterator();
					Street other = it.next();
					if (other == street) other = it.next();

					Coordinate c;
					if (other.coordinates[0].equals(xpoint))
						c = other.coordinates[1];
					else
						c = other.coordinates[other.coordinates.length - 2];

					v = new Vertex(c, street.layer);
				} else {
					v = new Vertex(street.coordinates[0], street.layer);
				}

				int index;
				Integer indexobj = indexmap.get(v);
				if (indexobj != null) {
					index = indexobj;
				} else {
					index = counter++;
					vertices.add(v);
					indexmap.put(v, index);
				}

				bucket.add(index);
			}

			// generate base line
			for (Coordinate c : street.coordinates) {
				Vertex v = new Vertex(c, street.layer);

				int index;
				Integer indexobj = indexmap.get(v);
				if (indexobj != null) {
					index = indexobj;
				} else {
					index = counter++;
					vertices.add(v);
					indexmap.put(v, index);
				}

				bucket.add(index);
			}

			// duplicate or extrude last vertex
			{
				Vertex v;
				if (joinsWhenPossible && intersections.count(street.coordinates[street.coordinates.length - 1]) == 2) {
					Coordinate xpoint = street.coordinates[street.coordinates.length - 1];

					Iterator<Street> it = intersections.get(xpoint).iterator();
					Street other = it.next();
					if (other == street) other = it.next();

					Coordinate c;
					if (other.coordinates[0].equals(xpoint))
						c = other.coordinates[1];
					else
						c = other.coordinates[other.coordinates.length - 2];

					v = new Vertex(c, street.layer);
				} else {
					v = new Vertex(street.coordinates[street.coordinates.length - 1], street.layer);
				}

				int index;
				Integer indexobj = indexmap.get(v);
				if (indexobj != null) {
					index = indexobj;
				} else {
					index = counter++;
					vertices.add(v);
					indexmap.put(v, index);
				}

				bucket.add(index);
			}

			bucket.add(restartIndex);
		}

		indices.addAll(buckets.values());
	}

	private void generateStandardMesh(
			RenderContext context,
			Feature<? extends Street> feature,
			ArrayList<Vertex> vertices,
			ArrayList<ArrayList<Integer>> indices)
	{
		int restartIndex = context.PrimitiveRestart.getIndex();

		int counter = 0;
		HashMap<Vertex, Integer> indexmap = new HashMap<>();
		HashMap<Float, ArrayList<Integer>> buckets = new HashMap<>();

		for (Street street : feature.getData()) {
			ArrayList<Integer> bucket = buckets.get(street.layer);
			if (bucket == null) {
				bucket = new ArrayList<>();
				buckets.put(street.layer, bucket);
			}

			for (Coordinate c : street.coordinates) {
				Vertex v = new Vertex(c, street.layer);

				int index;
				Integer indexobj = indexmap.get(v);
				if (indexobj != null) {
					index = indexobj;
				} else {
					index = counter++;
					vertices.add(v);
					indexmap.put(v, index);
				}

				bucket.add(index);
			}

			bucket.add(restartIndex);
		}

		indices.addAll(buckets.values());
	}


	private static boolean getPropAdjacency(Style style) {
		return style.getProperty("adjacency_primitives", false);
	}

	private static boolean getPropJoinsWhenPossible(Style style) {
		return style.getProperty("use_joins_when_possible", false);
	}


	private static class Vertex {
		public final Coordinate coordinate;
		public final float layer;

		public Vertex(Coordinate coordinate, float layer) {
			this.coordinate = coordinate;
			this.layer = layer;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Vertex))
				return false;

			Vertex other = (Vertex) obj;

			return this.coordinate.equals(other.coordinate)
					&& this.layer == other.layer;
		}

		@Override
		public int hashCode() {
			return new FNVHashBuilder()
					.add(coordinate)
					.add(layer)
					.getHash();
		}
	}
}
