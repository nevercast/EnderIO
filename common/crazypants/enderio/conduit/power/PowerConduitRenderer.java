package crazypants.enderio.conduit.power;

import static crazypants.util.ForgeDirectionOffsets.offsetScaled;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IConduitBundle;
import crazypants.enderio.conduit.geom.ConduitGeometryUtil;
import crazypants.enderio.conduit.render.ConduitBundleRenderer;
import crazypants.enderio.conduit.render.DefaultConduitRenderer;
import crazypants.render.BoundingBox;
import crazypants.render.VertexRotation;
import crazypants.render.VertexTransform;
import crazypants.render.VertexTransformComposite;
import crazypants.render.VertexTranslation;
import crazypants.vecmath.Vector3d;
import crazypants.vecmath.Vertex;

public class PowerConduitRenderer extends DefaultConduitRenderer {

  private static final EnumMap<ForgeDirection, List<Vertex>> VERTS = new EnumMap<ForgeDirection, List<Vertex>>(ForgeDirection.class);

  static {

    float scale = 0.8f;
    BoundingBox refBB = ConduitGeometryUtil.CORE_BOUNDS;
    refBB = refBB.scale(scale, scale, scale);
    refBB = refBB.scale(scale, 1, 1);

    double offset = (ConduitGeometryUtil.HWIDTH * scale * scale) + ConduitBundleRenderer.CONNECTOR_DEPTH;

    ForgeDirection dir;
    Vector3d trans;

    VertexRotation vrot = new VertexRotation(Math.PI / 2, new Vector3d(0, 1, 0), new Vector3d(0.5, 0.5, 0.5));
    VertexTranslation vtrans = new VertexTranslation(0, 0, 0);
    VertexTransformComposite xform = new VertexTransformComposite(vrot, vtrans);

    dir = ForgeDirection.SOUTH;
    trans = offsetScaled(dir, 0.5);
    trans.sub(offsetScaled(dir, offset));
    vtrans.set(trans);
    VERTS.put(dir, createVerticesForDir(refBB, xform));

    dir = ForgeDirection.NORTH;
    vrot.setAngle(Math.PI + Math.PI / 2);
    trans = offsetScaled(dir, 0.5);
    trans.sub(offsetScaled(dir, offset));
    vtrans.set(trans);
    VERTS.put(dir, createVerticesForDir(refBB, xform));

    dir = ForgeDirection.EAST;
    vrot.setAngle(Math.PI);
    trans = offsetScaled(dir, 0.5);
    trans.sub(offsetScaled(dir, offset));
    vtrans.set(trans);
    VERTS.put(dir, createVerticesForDir(refBB, xform));

    dir = ForgeDirection.WEST;
    vrot.setAngle(0);
    trans = offsetScaled(dir, 0.5);
    trans.sub(offsetScaled(dir, offset));
    vtrans.set(trans);
    VERTS.put(dir, createVerticesForDir(refBB, xform));

    vrot.setAxis(new Vector3d(0, 0, 1));

    dir = ForgeDirection.UP;
    vrot.setAngle(-Math.PI / 2);
    trans = offsetScaled(dir, 0.5);
    trans.sub(offsetScaled(dir, offset));
    vtrans.set(trans);
    VERTS.put(dir, createVerticesForDir(refBB, xform));

    dir = ForgeDirection.DOWN;
    vrot.setAngle(Math.PI / 2);
    trans = offsetScaled(dir, 0.5);
    trans.sub(offsetScaled(dir, offset));
    vtrans.set(trans);
    VERTS.put(dir, createVerticesForDir(refBB, xform));

  }

  @Override
  public boolean isRendererForConduit(IConduit conduit) {
    return conduit instanceof IPowerConduit;
  }

  private static List<Vertex> createVerticesForDir(BoundingBox refBB, VertexTransform xform) {
    List<Vertex> result = new ArrayList<Vertex>(24);
    for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
      result.addAll(refBB.getCornersWithUvForFace(face));
    }
    for (Vertex v : result) {
      xform.apply(v.xyz);
      //v.xyz.set(v.x(), v.y() + 1, v.z());
      xform.applyToNormal(v.normal);

    }
    return result;
  }

  @Override
  public void renderEntity(ConduitBundleRenderer conduitBundleRenderer, IConduitBundle te, IConduit conduit, double x, double y, double z, float partialTick,
      float worldLight) {
    super.renderEntity(conduitBundleRenderer, te, conduit, x, y, z, partialTick, worldLight);

    if(!conduit.hasConnectionMode(ConnectionMode.INPUT) && !conduit.hasConnectionMode(ConnectionMode.OUTPUT)) {
      return;
    }
    IPowerConduit pc = (IPowerConduit) conduit;
    for (ForgeDirection dir : conduit.getExternalConnections()) {
      Icon tex = null;
      if(conduit.getConectionMode(dir) == ConnectionMode.INPUT) {
        tex = pc.getTextureForInputMode();
      } else if(conduit.getConectionMode(dir) == ConnectionMode.OUTPUT) {
        tex = pc.getTextureForOutputMode();
      }
      if(tex != null) {
        renderModeConnector(pc, dir, tex);
      }
    }

  }

  private void renderModeConnector(IPowerConduit pc, ForgeDirection dir, Icon tex) {
    List<Vertex> verts = VERTS.get(dir);
    if(verts == null) {
      return;
    }

    float uWidth = tex.getMaxU() - tex.getMinU();
    float uScale = uWidth * 0.64f;
    float minU = tex.getMinU() + (uWidth - uScale);
    float vScale = tex.getMaxV() - tex.getMinV();

    Tessellator tes = Tessellator.instance;
    for (Vertex v : verts) {
      tes.setNormal(v.nx(), v.ny(), v.nz());
      tes.addVertexWithUV(v.x(), v.y(), v.z(), minU + (v.u() * uScale), tex.getMinV() + (v.v() * vScale));
    }

  }

}
