package microtrafficsim.core.vis.segmentbased;

import microtrafficsim.core.vis.AbstractVisualization;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.input.OrthoInputController;
import microtrafficsim.core.vis.map.segments.SegmentLayerProvider;
import microtrafficsim.core.vis.view.OrthographicView;


public class SegmentBasedVisualization extends AbstractVisualization {
	
	private static final float ZOOM_LEVEL_MIN = 0.f;
	private static final float ZOOM_LEVEL_MAX = 19.f;
	private static final float ZOOM_MULTIPLIER = 0.1f;
	
	private static final float Z_NEAR = 0.1f;
	private static final float Z_FAR = 1000.f;


	public SegmentBasedVisualization(int width, int height, SegmentLayerProvider provider, int nWorkerThreads) {
		this(
				new OrthographicView(width, height, Z_NEAR, Z_FAR, ZOOM_LEVEL_MIN, ZOOM_LEVEL_MAX),
				provider,
				nWorkerThreads
		);
	}

	private SegmentBasedVisualization(OrthographicView view, SegmentLayerProvider provider, int nWorkerThreads) {
		this(
				new RenderContext(),
				view,
				provider,
				new OrthoInputController(view, ZOOM_MULTIPLIER),
				nWorkerThreads
		);
	}

	private SegmentBasedVisualization(RenderContext context, OrthographicView view, SegmentLayerProvider provider,
								   OrthoInputController controller, int nWorkerThreads) {
		super(context, new SegmentBasedVisualizer(context, view, provider, nWorkerThreads), controller, controller);
	}
}
