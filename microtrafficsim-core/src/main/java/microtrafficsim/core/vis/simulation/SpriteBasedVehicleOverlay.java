package microtrafficsim.core.vis.simulation;

import com.jogamp.opengl.GL3;
import microtrafficsim.core.entities.street.StreetEntity;
import microtrafficsim.core.entities.vehicle.LogicVehicleEntity;
import microtrafficsim.core.entities.vehicle.VisualizationVehicleEntity;
import microtrafficsim.core.logic.streets.DirectedEdge;
import microtrafficsim.core.map.Coordinate;
import microtrafficsim.core.map.style.VehicleStyleSheet;
import microtrafficsim.core.simulation.core.Simulation;
import microtrafficsim.core.vis.context.RenderContext;
import microtrafficsim.core.vis.map.projections.Projection;
import microtrafficsim.core.vis.opengl.BufferStorage;
import microtrafficsim.core.vis.opengl.DataTypes;
import microtrafficsim.core.vis.opengl.shader.ShaderCompileException;
import microtrafficsim.core.vis.opengl.shader.ShaderLinkException;
import microtrafficsim.core.vis.opengl.shader.ShaderProgram;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributePointer;
import microtrafficsim.core.vis.opengl.shader.attributes.VertexAttributes;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderProgramSource;
import microtrafficsim.core.vis.opengl.shader.resources.ShaderSource;
import microtrafficsim.core.vis.opengl.shader.uniforms.UniformSampler2D;
import microtrafficsim.core.vis.opengl.utils.TextureData2D;
import microtrafficsim.core.vis.view.OrthographicView;
import microtrafficsim.math.Vec2d;
import microtrafficsim.math.Vec2i;
import microtrafficsim.math.Vec3d;
import microtrafficsim.utils.resources.PackagedResource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;


/**
 * Overlay to display simulated vehicles using sprites.
 *
 * @author Maximilian Luz
 */
public class SpriteBasedVehicleOverlay implements VehicleOverlay {

    private static final float VEHICLE_SIZE               = 10.f;
    private static final float VEHICLE_SCALE_NORM         = 1.f / (1 << 18);
    private static final float VEHICLE_LANE_OFFSET        = 6.f;
    private static final int   VIEWPORT_CULLING_EXPANSION = 20;

    private static final int TEX_UNIT_SPRITE    = 0;
    private static final int TEX_UNIT_MAP_DEPTH = 1;

    private static final ShaderProgramSource SHADER_PROG_SRC = new ShaderProgramSource(
            "/shaders/overlay/vehicle/spritebased/vehicle_overlay",
            new ShaderSource(GL3.GL_VERTEX_SHADER, new PackagedResource(SpriteBasedVehicleOverlay.class,
                    "/shaders/overlay/vehicle/spritebased/vehicle_overlay.vs")),
            new ShaderSource(GL3.GL_FRAGMENT_SHADER, new PackagedResource(SpriteBasedVehicleOverlay.class,
                    "/shaders/overlay/vehicle/spritebased/vehicle_overlay.fs"))
    );

    private final Supplier<VisualizationVehicleEntity> vehicleFactory;

    private Simulation simulation;
    private Projection       projection;
    private OrthographicView view;

    private int           sprite;
    private int           vao;
    private BufferStorage vbo;

    private VertexAttributePointer ptrPosition;
    private VertexAttributePointer ptrNormal;
    private VertexAttributePointer ptrColor;

    private ShaderProgram prog;

    private boolean enabled;


    /**
     *
     * @param projection   the projection used in the visualization.
     * @param vehicleStyle the default color used for the vehicles.
     */
    public SpriteBasedVehicleOverlay(Projection projection, VehicleStyleSheet vehicleStyle) {
        this.simulation = null;
        this.projection = projection;

        this.vehicleFactory = () -> new Vehicle(vehicleStyle.getDefaultVehicleColor());

        this.vao = -1;
        this.vbo = null;

        this.ptrPosition = null;
        this.ptrNormal   = null;
        this.ptrColor    = null;

        this.prog = null;

        this.enabled = true;
    }


    @Override
    public void setView(OrthographicView view) {
        this.view = view;
    }


    @Override
    public void initialize(RenderContext context) throws IOException, ShaderCompileException, ShaderLinkException {
        GL3 gl = context.getDrawable().getGL().getGL3();

        prog = context.getShaderManager().load(SHADER_PROG_SRC);

        // set samplers
        UniformSampler2D uSpriteSampler = (UniformSampler2D) prog.getUniform("u_sprite_sampler");
        UniformSampler2D uMapDepth      = (UniformSampler2D) prog.getUniform("u_map_depth");

        if (uSpriteSampler != null) uSpriteSampler.set(TEX_UNIT_SPRITE);
        if (uMapDepth != null) uMapDepth.set(TEX_UNIT_MAP_DEPTH);

        // load texture data
        TextureData2D texdata = TextureData2D.loadFromResource(
                this.getClass(), "/shaders/overlay/vehicle/spritebased/vehicle_sprite.png");

        // generate sprite texture
        int[] obj = {-1, -1, -1};

        gl.glGenTextures(1, obj, 0);
        sprite = obj[0];

        gl.glBindTexture(GL3.GL_TEXTURE_2D, sprite);

        // send texture data to gpu
        gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, GL3.GL_RGBA8, texdata.width, texdata.height, 0, GL3.GL_BGRA,
                        GL3.GL_UNSIGNED_BYTE, texdata.data);

        // set border wrap-mode and border-color to transparent
        float[] borderColor = {0.0f, 0.0f, 0.0f, 0.0f};
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_BORDER);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_BORDER);
        gl.glTexParameterfv(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_BORDER_COLOR, borderColor, 0);

        // set mipmap and texture min/mag filter (could be changed for performance)
        gl.glGenerateMipmap(GL3.GL_TEXTURE_2D);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR_MIPMAP_LINEAR);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

        // create vbo, vao
        gl.glGenVertexArrays(1, obj, 1);
        gl.glGenBuffers(1, obj, 2);

        vao = obj[1];

        vbo         = new BufferStorage(GL3.GL_ARRAY_BUFFER, obj[2]);
        ptrPosition = VertexAttributePointer.create(VertexAttributes.POSITION3, DataTypes.FLOAT_3, vbo, 24, 0);
        ptrNormal   = VertexAttributePointer.create(VertexAttributes.NORMAL2, DataTypes.FLOAT_2, vbo, 24, 12);
        ptrColor    = VertexAttributePointer.create(VertexAttributes.COLOR, DataTypes.UNSIGNED_BYTE_4, vbo, 24, 20);

        // set up vertex array
        gl.glBindVertexArray(vao);
        gl.glBindBuffer(vbo.target, vbo.handle);
        gl.glEnableVertexAttribArray(ptrPosition.attribute.index);
        ptrPosition.set(gl);
        gl.glEnableVertexAttribArray(ptrNormal.attribute.index);
        ptrNormal.set(gl);
        gl.glEnableVertexAttribArray(ptrColor.attribute.index);
        ptrColor.set(gl);
        gl.glBindVertexArray(0);

        // allocate initial buffer
        gl.glBindBuffer(vbo.target, vbo.handle);
        gl.glBufferData(vbo.target, 1000 * 6 * 4L, null, GL3.GL_DYNAMIC_DRAW);
        gl.glBindBuffer(vbo.target, 0);
    }

    @Override
    public void dispose(RenderContext context) {
        GL3 gl = context.getDrawable().getGL().getGL3();

        // delete buffer
        int[] obj = {sprite, vao, vbo.handle};

        gl.glDeleteTextures(1, obj, 0);
        gl.glDeleteVertexArrays(1, obj, 1);
        gl.glDeleteBuffers(1, obj, 2);

        vao = -1;
        vbo = null;

        // delete shader
        prog.dispose(gl);
    }

    @Override
    public void resize(RenderContext context) {}

    @Override
    public void display(RenderContext context, MapBuffer map) {
        if (!enabled || simulation == null) return;
        if (simulation.getScenario() == null) return;
        GL3 gl = context.getDrawable().getGL().getGL3();

        // get direction of lane offset
        int laneOffsetSign = simulation.getScenario().getConfig().crossingLogic.drivingOnTheRight ? 1 : -1;

        // disable depth test
        context.DepthTest.disable(gl);

        // enable blending
        context.BlendMode.enable(gl);
        context.BlendMode.setEquation(gl, GL3.GL_FUNC_ADD);
        context.BlendMode.setFactors(gl, GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA, GL3.GL_ONE,
                                     GL3.GL_ONE_MINUS_SRC_ALPHA);

        // set point size and point sprite origin
        float ptsize = (float) Math.pow(2, view.getScale() * VEHICLE_SCALE_NORM) * VEHICLE_SIZE;
        if (ptsize < 1.f) ptsize = 1.f;

        context.Points.setPointSpriteCoordOrigin(gl, GL3.GL_LOWER_LEFT);
        context.Points.setPointSize(gl, ptsize);

        // calculate bounding rectangle, assumes top-down (z-axis) orthographic view
        double invscale = 1.f / view.getScale();
        Vec3d  viewpos  = view.getPosition();
        Vec2i  viewport = view.getSize();

        double vx = invscale * (viewport.x + VIEWPORT_CULLING_EXPANSION) / 2;
        double vy = invscale * (viewport.y + VIEWPORT_CULLING_EXPANSION) / 2;

        double left   = viewpos.x - vx;
        double right  = viewpos.x + vx;
        double bottom = viewpos.y - vy;
        double top    = viewpos.y + vy;

        // update vehicle list
        Collection<? extends LogicVehicleEntity>
                vehicles = new ArrayList<>(simulation.getScenario().getVehicleContainer().getSpawnedVehicles());
        int len          = vehicles.size();
        if (len == 0) return;

        // orphan last buffer and load it to a new one
        gl.glBindBuffer(vbo.target, vbo.handle);
        gl.glBufferData(vbo.target, len * 6 * 4L, null, GL3.GL_DYNAMIC_DRAW);

        ByteBuffer buffer = gl.glMapBufferRange(vbo.target, 0, len * 6 * 4L,
                                                GL3.GL_MAP_WRITE_BIT | GL3.GL_MAP_INVALIDATE_BUFFER_BIT);

        // write positions
        int vehicleCount = 0;
        for (LogicVehicleEntity logic : vehicles) {
            Vehicle v = (Vehicle) logic.getEntity().getVisualization();

            Coordinate cpos = v.getPosition();
            Vec2d      pos  = projection.project(cpos);

            Coordinate ctarget = v.getTarget();
            Vec2d      dir     = projection.project(ctarget).sub(pos).normalize();

            DirectedEdge edge = logic.getDirectedEdge();
            if (edge == null) continue;
            StreetEntity street = edge.getEntity();
            if (street.getBackwardEdge() != null && street.getForwardEdge() != null) {
                pos.x += dir.y * laneOffsetSign * VEHICLE_LANE_OFFSET * VEHICLE_SCALE_NORM;
                pos.y -= dir.x * laneOffsetSign * VEHICLE_LANE_OFFSET * VEHICLE_SCALE_NORM;
            }

            // continue if out of bounds
            if (pos.x < left || pos.x > right || pos.y < bottom || pos.y > top) continue;

            buffer.putFloat((float) pos.x);
            buffer.putFloat((float) pos.y);
            buffer.putFloat(v.getLayer());
            buffer.putFloat((float) dir.x);
            buffer.putFloat((float) dir.y);
            buffer.putInt(v.getBaseColor().toIntABGR());
            vehicleCount++;
        }

        gl.glUnmapBuffer(vbo.target);
        gl.glBindBuffer(vbo.target, 0);

        // bind textures
        gl.glActiveTexture(GL3.GL_TEXTURE0 + TEX_UNIT_SPRITE);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, sprite);

        gl.glActiveTexture(GL3.GL_TEXTURE0 + TEX_UNIT_MAP_DEPTH);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, map.depth);

        // draw
        prog.bind(gl);
        gl.glBindVertexArray(vao);
        gl.glDrawArrays(GL3.GL_POINTS, 0, vehicleCount);
        gl.glBindVertexArray(0);
        prog.unbind(gl);

        // unbind texture
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
    }


    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


    /**
     * Returns the simulation displayed in with this overlay.
     *
     * @return the simulation displayed in this overlay.
     */
    public Simulation getSimulation() {
        return simulation;
    }


    @Override
    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public Supplier<VisualizationVehicleEntity> getVehicleFactory() {
        return vehicleFactory;
    }
}
