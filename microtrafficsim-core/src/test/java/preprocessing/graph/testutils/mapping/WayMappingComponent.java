package preprocessing.graph.testutils.mapping;

import java.util.ArrayList;

import microtrafficsim.osm.parser.ecs.Component;
import microtrafficsim.osm.parser.ecs.Entity;


/**
 * Component for {@code WayEntities} to map this way to Way-Slices of another
 * {@code DataSet}.
 * 
 * @author Maximilian Luz
 */
public class WayMappingComponent extends Component {
	
	public ArrayList<WaySliceMapping> mapsto;

	public WayMappingComponent(Entity entity, ArrayList<WaySliceMapping> mapsto) {
		super(entity);
		this.mapsto = mapsto;
	}
	
	@Override
	public Class<? extends Component> getType() {
		return WayMappingComponent.class;
	}

	@Override
	public Component clone(Entity e) {
		// not required
		return null;
	}
}
