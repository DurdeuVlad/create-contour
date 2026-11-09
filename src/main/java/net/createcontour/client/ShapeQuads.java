package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Occupancy-grid -> face-quad emitter for the 2x2x2 cell model.
 *
 * Units: all texture projection happens in BLOCK units 0..1 (source quad
 * vertices live there); emitted geometry is built from the same corner
 * set scaled to pixels only where MC expects. Corners are emitted in a
 * proper cyclic loop per face (verified outward normals), so no bowtie.
 *
 * UV anchoring (ADR-017 texture continuation, issue 9 audit): every
 * emitted vertex UV is the source material quad's affine position->UV
 * map evaluated at the vertex's ABSOLUTE position on the block's 16x16
 * face plane - never face-local 0..1. A face covering pixel rect
 * [u0,u1]x[v0,v1] samples exactly the sprite region a full block's face
 * shows at that sub-rect, so painted material continues across cells and
 * block boundaries exactly the way vanilla neighbors do (block-local
 * anchoring equals world-anchored mod 16; the atlas region cannot be
 * sampled past the sprite anyway). Diagonal/curved interior quads are
 * anchored by projection onto their dominant axis plane; where the
 * dominant axis changes across a seam the projections differ - those
 * are the inherent lattice-diagonal seams ADR-017 documents, not
 * defects. This contract holds for every emitter below because they all
 * pass absolute pixel corners into emitFace; new emitters must keep it.
 *
 * Cull partition: faces lying ON the block boundary plane (0/16) go into
 * that direction's cullface list; inset exposed faces (interior cut
 * planes) go into the null-direction list. Each exposed face is emitted
 * exactly once.
 */
final class ShapeQuads {

	/** DefaultVertexFormat.BLOCK stride: 8 ints per vertex. */
	private static final int STRIDE = 8;

	private ShapeQuads() {
	}

	record SrcQuad(TextureAtlasSprite sprite, int tint, boolean shade,
		float[] lx, float[] ly, float[] u, float[] v) {
	}

	/** Face-local projection axes for a face direction (0..1 coords). */
	private static float projX(Direction d, Vector3f p) {
		return d.getAxis() == Direction.Axis.X ? p.z() : p.x();
	}

	private static float projY(Direction d, Vector3f p) {
		return d.getAxis() == Direction.Axis.Y ? p.z() : p.y();
	}

	static SrcQuad extract(BakedQuad q) {
		int[] verts = q.getVertices();
		float[] lx = new float[4], ly = new float[4], u = new float[4], v = new float[4];
		for (int i = 0; i < 4; i++) {
			Vector3f pos = new Vector3f(
				Float.intBitsToFloat(verts[i * STRIDE]),
				Float.intBitsToFloat(verts[i * STRIDE + 1]),
				Float.intBitsToFloat(verts[i * STRIDE + 2]));
			lx[i] = projX(q.getDirection(), pos);
			ly[i] = projY(q.getDirection(), pos);
			u[i] = Float.intBitsToFloat(verts[i * STRIDE + 4]);
			v[i] = Float.intBitsToFloat(verts[i * STRIDE + 5]);
		}
		return new SrcQuad(q.getSprite(), q.getTintIndex(), q.isShade(),
			lx, ly, u, v);
	}

	/** Synthetic full-face source (neutral texture, identity UV basis). */
	static SrcQuad neutral(Direction d, TextureAtlasSprite sprite) {
		float[] lx = {0, 0, 1, 1}, ly = {0, 1, 0, 1};
		float[] u = new float[4], v = new float[4];
		for (int i = 0; i < 4; i++) {
			u[i] = sprite.getU(lx[i]);
			v[i] = sprite.getV(ly[i]);
		}
		return new SrcQuad(sprite, -1, true, lx, ly, u, v);
	}

	/**
	 * Evaluate the source quad's affine map at a face-plane point (0..1).
	 * `px`/`py` are ABSOLUTE positions on the block's face plane, so the
	 * result is the texel the material's own face shows at that spot -
	 * this is the cell-grid UV anchor; callers must not pass box-local
	 * coordinates.
	 */
	private static float[] evalUV(SrcQuad s, float px, float py) {
		for (int a = 0; a < 4; a++)
			for (int b = 0; b < 4; b++) {
				if (b == a)
					continue;
				for (int c = 0; c < 4; c++) {
					if (c == a || c == b)
						continue;
					float ex = s.lx[b] - s.lx[a], ey = s.ly[b] - s.ly[a];
					float fx = s.lx[c] - s.lx[a], fy = s.ly[c] - s.ly[a];
					float det = ex * fy - fx * ey;
					if (Math.abs(det) < 1e-6f)
						continue;
					float dx = px - s.lx[a], dy = py - s.ly[a];
					float s1 = (dx * fy - fx * dy) / det;
					float s2 = (ex * dy - dx * ey) / det;
					// CTM-shifted sources (e.g. Create connected textures)
					// have UVs outside their declared sprite; clamp final
					// U/V to the OBSERVED quad bounds then, else to the
					// declared sprite bounds (never mix axes).
					boolean shifted = uvShifted(s);
					float loU = shifted ? Math.min(Math.min(s.u[0], s.u[1]), Math.min(s.u[2], s.u[3])) : s.sprite().getU0();
					float hiU = shifted ? Math.max(Math.max(s.u[0], s.u[1]), Math.max(s.u[2], s.u[3])) : s.sprite().getU1();
					float loV = shifted ? Math.min(Math.min(s.v[0], s.v[1]), Math.min(s.v[2], s.v[3])) : s.sprite().getV0();
					float hiV = shifted ? Math.max(Math.max(s.v[0], s.v[1]), Math.max(s.v[2], s.v[3])) : s.sprite().getV1();
					float u = clamp(s.u[a] + s1 * (s.u[b] - s.u[a]) + s2 * (s.u[c] - s.u[a]), loU, hiU);
					float v = clamp(s.v[a] + s1 * (s.v[b] - s.v[a]) + s2 * (s.v[c] - s.v[a]), loV, hiV);
					return new float[]{u, v};
				}
			}
		return new float[]{s.u[0], s.v[0]}; // degenerate source: nearest vert
	}

	/** True when any vertex UV lies outside the declared sprite bounds (CTM shift). */
	private static boolean uvShifted(SrcQuad s) {
		TextureAtlasSprite sp = s.sprite();
		for (int i = 0; i < 4; i++)
			if (s.u[i] < sp.getU0() - 1e-6 || s.u[i] > sp.getU1() + 1e-6
				|| s.v[i] < sp.getV0() - 1e-6 || s.v[i] > sp.getV1() + 1e-6)
				return true;
		return false;
	}

	private static float clamp(float f, float lo, float hi) {
		return Math.max(lo, Math.min(hi, f));
	}

	/** Packed-byte normal for a face direction (FaceBakery convention). */
	private static int packedNormal(Direction d) {
		return (d.getStepX() * 127) & 0xff
			| (((d.getStepY() * 127) & 0xff) << 8)
			| (((d.getStepZ() * 127) & 0xff) << 16);
	}

	static BakedQuad emitFace(Direction d, Vector3f[] corners, SrcQuad src) {
		// Auto-correct winding: the emitted face's geometric normal must
		// agree with its declared direction (the render smoke checks the
		// cross-product sign on both triangle halves of the quad).
		Vector3f e1 = new Vector3f(corners[1]).sub(corners[0]);
		Vector3f e2 = new Vector3f(corners[2]).sub(corners[0]);
		Vector3f n = e1.cross(e2);
		if (n.x() * d.getStepX() + n.y() * d.getStepY() + n.z() * d.getStepZ() < 0)
			corners = new Vector3f[]{corners[3], corners[2], corners[1], corners[0]};
		int[] verts = new int[4 * STRIDE];
		for (int v = 0; v < 4; v++) {
			// corners are ABSOLUTE PIXELS 0..16 on the block face plane;
			// texture projection needs 0..1 - dividing by 16 yields the
			// cell-grid anchor (never renormalize to the quad's own
			// bounds, that would give every sub-face the whole texture)
			float lx = projX(d, corners[v]) / 16f;
			float ly = projY(d, corners[v]) / 16f;
			float[] uv = evalUV(src, lx, ly);
			int b = v * STRIDE;
			verts[b] = Float.floatToRawIntBits(corners[v].x() / 16f);
			verts[b + 1] = Float.floatToRawIntBits(corners[v].y() / 16f);
			verts[b + 2] = Float.floatToRawIntBits(corners[v].z() / 16f);
			verts[b + 3] = -1;
			verts[b + 4] = Float.floatToRawIntBits(uv[0]);
			verts[b + 5] = Float.floatToRawIntBits(uv[1]);
			verts[b + 6] = 0;
			verts[b + 7] = packedNormal(d);
		}
		return new BakedQuad(verts, src.tint(), d, src.sprite(), src.shade());
	}

	/**
	 * The 4 corners of the exposed face of cell (cx,cy,cz) toward `d`, in
	 * a proper cyclic loop (cross products verified to point outward).
	 * Positions are in PIXELS 0..16; callers divide for block units.
	 */
	private static Vector3f[] corners(Direction d, int cx, int cy, int cz) {
		float x0 = cx * 8, x1 = x0 + 8, y0 = cy * 8, y1 = y0 + 8, z0 = cz * 8, z1 = z0 + 8;
		return switch (d) {
			case UP -> new Vector3f[]{new Vector3f(x0, y1, z0), new Vector3f(x0, y1, z1),
				new Vector3f(x1, y1, z1), new Vector3f(x1, y1, z0)};
			case DOWN -> new Vector3f[]{new Vector3f(x0, y0, z0), new Vector3f(x1, y0, z0),
				new Vector3f(x1, y0, z1), new Vector3f(x0, y0, z1)};
			case NORTH -> new Vector3f[]{new Vector3f(x0, y0, z0), new Vector3f(x0, y1, z0),
				new Vector3f(x1, y1, z0), new Vector3f(x1, y0, z0)};
			case SOUTH -> new Vector3f[]{new Vector3f(x0, y0, z1), new Vector3f(x1, y0, z1),
				new Vector3f(x1, y1, z1), new Vector3f(x0, y1, z1)};
			case WEST -> new Vector3f[]{new Vector3f(x0, y0, z0), new Vector3f(x0, y0, z1),
				new Vector3f(x0, y1, z1), new Vector3f(x0, y1, z0)};
			case EAST -> new Vector3f[]{new Vector3f(x1, y0, z0), new Vector3f(x1, y1, z0),
				new Vector3f(x1, y1, z1), new Vector3f(x1, y0, z1)};
			default -> new Vector3f[0];
		};
	}

	/**
	 * Emit quads for `mask`. Partition rule: faces on the block boundary
	 * plane go into the direction's cullface list (emitted only when
	 * `side == dir`); inset exposed faces go into the null-direction list
	 * (emitted only when `side == null`). One face -> exactly one list.
	 */
	static List<BakedQuad> emit(int mask, @Nullable Direction side,
		java.util.function.Function<Direction, List<SrcQuad>> srcFor) {
		List<BakedQuad> out = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			if ((mask & (1 << i)) == 0)
				continue;
			int cx = i & 1, cy = (i >> 1) & 1, cz = (i >> 2) & 1;
			for (Direction d : Direction.values()) {
				int nx = cx + d.getStepX(), ny = cy + d.getStepY(), nz = cz + d.getStepZ();
				boolean inside = nx >= 0 && nx < 2 && ny >= 0 && ny < 2 && nz >= 0 && nz < 2;
				if (inside && (mask & (1 << (nx | (ny << 1) | (nz << 2)))) != 0)
					continue; // internal face
				boolean boundary = !inside;
				if (boundary ? side != d : side != null)
					continue;
				for (SrcQuad src : srcFor.apply(d))
					out.add(emitFace(d, corners(d, cx, cy, cz), src));
			}
		}
		return out;
	}

	/**
	 * Sphere-surface grid patch, emitted double-sided (interior surface).
	 * P(t,p) = c + r*(sin t * cos p * eU + cos t * eP + sin t * sin p * eV)
	 * where eU/eP/eV are signed axis unit vectors. Polar rows whose far
	 * edge degenerates to a point are emitted as triangles via tri().
	 */
	static void emitSphere(List<BakedQuad> out,
		java.util.function.Function<Direction, List<SrcQuad>> srcFor,
		float[] c, float r, float[] eU, float[] eP, float[] eV,
		int segT, int segP, double t0, double t1, double p0, double p1) {
		java.util.function.BiFunction<Double, Double, Vector3f> pt = (t, p) -> {
			double st = Math.sin(t), ct = Math.cos(t);
			double cp = Math.cos(p), sp = Math.sin(p);
			return new Vector3f(
				c[0] + r * (float) (st * cp * eU[0] + ct * eP[0] + st * sp * eV[0]),
				c[1] + r * (float) (st * cp * eU[1] + ct * eP[1] + st * sp * eV[1]),
				c[2] + r * (float) (st * cp * eU[2] + ct * eP[2] + st * sp * eV[2]));
		};
		for (SrcQuad src : srcFor.apply(Direction.UP)) {
			for (int i = 0; i < segT; i++) {
				double ta = t0 + (t1 - t0) * i / segT;
				double tb = t0 + (t1 - t0) * (i + 1) / segT;
				boolean poleA = Math.abs(ta) < 1e-6 || Math.abs(ta - Math.PI) < 1e-6;
				boolean poleB = Math.abs(tb) < 1e-6 || Math.abs(tb - Math.PI) < 1e-6;
				for (int j = 0; j < segP; j++) {
					double pa = p0 + (p1 - p0) * j / segP;
					double pb = p0 + (p1 - p0) * (j + 1) / segP;
					Vector3f v00 = pt.apply(ta, pa), v01 = pt.apply(ta, pb);
					Vector3f v10 = pt.apply(tb, pa), v11 = pt.apply(tb, pb);
					if (poleA || poleB)
						continue; // pole rows are fanned below
					emitDouble(out, src, new Vector3f[]{v00, v01, v11, v10});
				}
				// pole row: fan the ring around the pole into real quads
				// (pairs of adjacent triangles - no slivers)
				if (poleA || poleB) {
					for (int j = 0; j + 2 <= segP; j += 2) {
						double pa = p0 + (p1 - p0) * j / segP;
						double pb = p0 + (p1 - p0) * (j + 1) / segP;
						double pc = p0 + (p1 - p0) * (j + 2) / segP;
						double tr = poleA ? tb : ta;
						Vector3f r1 = pt.apply(tr, pa), r2 = pt.apply(tr, pb),
							r3 = pt.apply(tr, pc);
						Vector3f pole = pt.apply(poleA ? ta : tb, 0.0);
						emitDouble(out, src, poleA
							? new Vector3f[]{pole, r1, r2, r3}
							: new Vector3f[]{r1, r2, r3, pole});
					}
				}
			}
		}
	}

	/**
	 * Annular arc band extruded along an axis: P(r,t,d) = c +
	 * r*(cos t * eU + sin t * eV) + d*eD. Emits `seg` outer and inner
	 * surface segments plus the two radial end caps, all double-sided
	 * (interior surface - radial normals vary along the arc).
	 */
	static void emitArcBand(List<BakedQuad> out,
		java.util.function.Function<Direction, List<SrcQuad>> srcFor,
		float[] c, float[] eU, float[] eV, float[] eD,
		float rOut, float rIn, double t0, double t1, int seg,
		float d0, float d1) {
		for (SrcQuad src : srcFor.apply(Direction.UP)) {
			java.util.function.Function<double[], Vector3f> pv = a -> {
				double t = a[0], r = a[1], d = a[2];
				return new Vector3f(
					c[0] + (float) (r * Math.cos(t) * eU[0]
						+ r * Math.sin(t) * eV[0] + d * eD[0]),
					c[1] + (float) (r * Math.cos(t) * eU[1]
						+ r * Math.sin(t) * eV[1] + d * eD[1]),
					c[2] + (float) (r * Math.cos(t) * eU[2]
						+ r * Math.sin(t) * eV[2] + d * eD[2]));
			};
			for (int i = 0; i < seg; i++) {
				double ta = t0 + (t1 - t0) * i / seg;
				double tb = t0 + (t1 - t0) * (i + 1) / seg;
				emitDouble(out, src, new Vector3f[]{
					pv.apply(new double[]{ta, rOut, d0}),
					pv.apply(new double[]{tb, rOut, d0}),
					pv.apply(new double[]{tb, rOut, d1}),
					pv.apply(new double[]{ta, rOut, d1})});
				emitDouble(out, src, new Vector3f[]{
					pv.apply(new double[]{ta, rIn, d0}),
					pv.apply(new double[]{tb, rIn, d0}),
					pv.apply(new double[]{tb, rIn, d1}),
					pv.apply(new double[]{ta, rIn, d1})});
			}
			// radial end caps at t0 and t1
			for (double tc : new double[]{t0, t1})
				emitDouble(out, src, new Vector3f[]{
					pv.apply(new double[]{tc, rIn, d0}),
					pv.apply(new double[]{tc, rIn, d1}),
					pv.apply(new double[]{tc, rOut, d1}),
					pv.apply(new double[]{tc, rOut, d0})});
		}
	}

	/**
	 * Flat annular-sector face on a boundary plane: ring between rIn
	 * and rOut swept t0..t1 in `seg` quads, lying in `face`'s plane.
	 * `c` is the arc center in pixel coords (plane depth included).
	 * Declared direction is the face (joins its cull list).
	 */
	static void emitAnnularSector(List<BakedQuad> out,
		java.util.function.Function<Direction, List<SrcQuad>> srcFor,
		Direction face, float[] c, float[] eU, float[] eV,
		float rIn, float rOut, double t0, double t1, int seg) {
		java.util.function.BiFunction<Double, Float, Vector3f> p = (t, r) ->
			new Vector3f(
				c[0] + (float) (r * Math.cos(t) * eU[0]
					+ r * Math.sin(t) * eV[0]),
				c[1] + (float) (r * Math.cos(t) * eU[1]
					+ r * Math.sin(t) * eV[1]),
				c[2] + (float) (r * Math.cos(t) * eU[2]
					+ r * Math.sin(t) * eV[2]));
		for (SrcQuad src : srcFor.apply(face))
			for (int i = 0; i < seg; i++) {
				double ta = t0 + (t1 - t0) * i / seg;
				double tb = t0 + (t1 - t0) * (i + 1) / seg;
				out.add(emitFace(face, new Vector3f[]{
					p.apply(ta, rIn), p.apply(tb, rIn),
					p.apply(tb, rOut), p.apply(ta, rOut)}, src));
			}
	}

	/**
	 * Single-surface arc: P(r,t,d) = c + r*(cos t * eU + sin t * eV) +
	 * d*eD, swept t0..t1 in `seg` quads, extruded d0..d1. One sided
	 * surface emitted double-sided (interior). No caps - callers
	 * supply end faces (disk fan, sector, or none).
	 */
	static void emitArc(List<BakedQuad> out,
		java.util.function.Function<Direction, List<SrcQuad>> srcFor,
		float[] c, float[] eU, float[] eV, float[] eD,
		float r, double t0, double t1, int seg, float d0, float d1) {
		java.util.function.BiFunction<Double, Float, Vector3f> p = (t, d) ->
			new Vector3f(
				c[0] + (float) (r * Math.cos(t) * eU[0]
					+ r * Math.sin(t) * eV[0] + d * eD[0]),
				c[1] + (float) (r * Math.cos(t) * eU[1]
					+ r * Math.sin(t) * eV[1] + d * eD[1]),
				c[2] + (float) (r * Math.cos(t) * eU[2]
					+ r * Math.sin(t) * eV[2] + d * eD[2]));
		for (SrcQuad src : srcFor.apply(Direction.UP))
			for (int i = 0; i < seg; i++) {
				double ta = t0 + (t1 - t0) * i / seg;
				double tb = t0 + (t1 - t0) * (i + 1) / seg;
				emitDouble(out, src, new Vector3f[]{
					p.apply(ta, d0), p.apply(tb, d0),
					p.apply(tb, d1), p.apply(ta, d1)});
			}
	}

	/**
	 * Interior double-sided emission for non-axis-aligned quads: the
	 * declared direction is the geometric normal's dominant signed axis
	 * (the smoke checks normal-vs-declared sign; a sideways quad
	 * declared UP would sit at the ~0 float boundary).
	 */
	static void emitDouble(List<BakedQuad> out, SrcQuad src,
		Vector3f[] q) {
		Vector3f n = new Vector3f(q[1]).sub(q[0])
			.cross(new Vector3f(q[2]).sub(q[0]))
			.add(new Vector3f(q[2]).sub(q[0])
				.cross(new Vector3f(q[3]).sub(q[0])));
		Direction d = Math.abs(n.x()) >= Math.abs(n.y())
			&& Math.abs(n.x()) >= Math.abs(n.z())
			? (n.x() >= 0 ? Direction.EAST : Direction.WEST)
			: Math.abs(n.y()) >= Math.abs(n.z())
				? (n.y() >= 0 ? Direction.UP : Direction.DOWN)
				: (n.z() >= 0 ? Direction.SOUTH : Direction.NORTH);
		out.add(emitFace(d, q, src));
		out.add(emitFace(d.getOpposite(),
			new Vector3f[]{q[3], q[2], q[1], q[0]}, src));
	}

	/**
	 * Flat disk/sector cap on a boundary face: a triangle fan from
	 * `center` to `segs` arc points spanning [a0,a1] radians in the
	 * face's two in-plane axes. Declared direction is the face (culls).
	 */
	static void emitDiskFan(List<BakedQuad> out,
		java.util.function.Function<Direction, List<SrcQuad>> srcFor,
		Direction face, float[] center, float r, double a0, double a1,
		int segs) {
		int au = (face.getAxis().ordinal() + 1) % 3;
		int av = (face.getAxis().ordinal() + 2) % 3;
		for (SrcQuad src : srcFor.apply(face)) {
			// pair adjacent fan triangles into real quads
			// (C, P_i, P_i+1, P_i+2) - no sliver 4th vertices
			for (int i = 0; i + 2 <= segs; i += 2) {
				float[] p1 = center.clone(), p2 = center.clone(),
					p3 = center.clone();
				double aa = a0 + (a1 - a0) * i / segs;
				double ab = a0 + (a1 - a0) * (i + 1) / segs;
				double ac = a0 + (a1 - a0) * (i + 2) / segs;
				p1[au] += r * (float) Math.cos(aa);
				p1[av] += r * (float) Math.sin(aa);
				p2[au] += r * (float) Math.cos(ab);
				p2[av] += r * (float) Math.sin(ab);
				p3[au] += r * (float) Math.cos(ac);
				p3[av] += r * (float) Math.sin(ac);
				out.add(emitFace(face, new Vector3f[]{
					new Vector3f(center), new Vector3f(p1),
					new Vector3f(p2), new Vector3f(p3)}, src));
			}
		}
	}

	/**
	 * Cone side surface, emitted double-sided (interior surface).
	 * P(s,p) = c + (1-s)*r*(cos p * eU + sin p * eV) + s*h*eD,
	 * s in [0,1] from base ring to apex. The apex row fans the final
	 * ring around the tip in paired quads (no slivers). segP even.
	 */
	static void emitCone(List<BakedQuad> out,
		java.util.function.Function<Direction, List<SrcQuad>> srcFor,
		float[] c, float r, float h, float[] eU, float[] eV, float[] eD,
		int segS, int segP) {
		java.util.function.BiFunction<Double, Double, Vector3f> pt = (s, p) -> {
			double w = (1 - s) * r, cp = Math.cos(p), sp = Math.sin(p);
			return new Vector3f(
				c[0] + (float) (w * cp * eU[0] + w * sp * eV[0]
					+ s * h * eD[0]),
				c[1] + (float) (w * cp * eU[1] + w * sp * eV[1]
					+ s * h * eD[1]),
				c[2] + (float) (w * cp * eU[2] + w * sp * eV[2]
					+ s * h * eD[2]));
		};
		for (SrcQuad src : srcFor.apply(Direction.UP)) {
			for (int i = 0; i < segS - 1; i++)
				for (int j = 0; j < segP; j++) {
					double sa = (double) i / segS, sb = (double) (i + 1) / segS;
					double pa = Math.PI * 2 * j / segP,
						pb = Math.PI * 2 * (j + 1) / segP;
					emitDouble(out, src, new Vector3f[]{pt.apply(sa, pa),
						pt.apply(sa, pb), pt.apply(sb, pb),
						pt.apply(sb, pa)});
				}
			// apex row: one winding-stable sliver-quad per segment -
			// a 4-vert fan quad leaves a flat ring triangle in the
			// reversed copy's triangulation and fails winding checks
			double sr = (double) (segS - 1) / segS;
			Vector3f apex = pt.apply(1.0, 0.0);
			for (int j = 0; j < segP; j++) {
				double pa = Math.PI * 2 * j / segP,
					pb = Math.PI * 2 * (j + 1) / segP;
				emitDouble(out, src,
					tri(apex, pt.apply(sr, pa), pt.apply(sr, pb)));
			}
		}
	}

	/**
	 * Surface of revolution (lathe): profile stations (ys[i], rs[i])
	 * measured along eD from base center c; each consecutive station
	 * pair emits one frustum row per azimuth segment - a flat ring
	 * when the two stations share a y (stepped shoulder), a frustum
	 * otherwise. A station with r=0 becomes a winding-stable apex
	 * tri() row (same rule as emitCone). Double-sided interior
	 * surface; segP even.
	 */
	static void emitLathe(List<BakedQuad> out,
		java.util.function.Function<Direction, List<SrcQuad>> srcFor,
		float[] c, float[] eU, float[] eV, float[] eD,
		float[] ys, float[] rs, int segP) {
		java.util.function.BiFunction<Integer, Double, Vector3f> pt = (i, p) -> {
			double w = rs[i], cp = Math.cos(p), sp = Math.sin(p);
			return new Vector3f(
				c[0] + (float) (w * cp * eU[0] + w * sp * eV[0]
					+ ys[i] * eD[0]),
				c[1] + (float) (w * cp * eU[1] + w * sp * eV[1]
					+ ys[i] * eD[1]),
				c[2] + (float) (w * cp * eU[2] + w * sp * eV[2]
					+ ys[i] * eD[2]));
		};
		for (SrcQuad src : srcFor.apply(Direction.UP)) {
			for (int i = 0; i < ys.length - 1; i++)
				for (int j = 0; j < segP; j++) {
					double pa = Math.PI * 2 * j / segP,
						pb = Math.PI * 2 * (j + 1) / segP;
					if (rs[i + 1] == 0f)
						emitDouble(out, src, tri(pt.apply(i + 1, 0.0),
							pt.apply(i, pa), pt.apply(i, pb)));
					else if (rs[i] == 0f)
						emitDouble(out, src, tri(pt.apply(i, 0.0),
							pt.apply(i + 1, pb), pt.apply(i + 1, pa)));
					else
						emitDouble(out, src, new Vector3f[]{
							pt.apply(i, pa), pt.apply(i, pb),
							pt.apply(i + 1, pb), pt.apply(i + 1, pa)});
				}
		}
	}

	/** Occupancy mask -> one 8px box per occupied 2x2x2 cell. */
	static List<float[]> boxesForMask(int mask) {
		List<float[]> boxes = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			if ((mask & (1 << i)) == 0)
				continue;
			float x0 = (i & 1) * 8f, y0 = ((i >> 1) & 1) * 8f, z0 = ((i >> 2) & 1) * 8f;
			boxes.add(new float[]{x0, y0, z0, x0 + 8, y0 + 8, z0 + 8});
		}
		return boxes;
	}

	/** (u,v) face-local bounds of a face rect for axis `axis` of `box`. */
	private static float uOf(Direction.Axis axis, float[] b, boolean lo) {
		return switch (axis) {
			case Y -> lo ? b[0] : b[3];
			case Z -> lo ? b[0] : b[3];
			default -> lo ? b[2] : b[5];
		};
	}

	private static float vOf(Direction.Axis axis, float[] b, boolean lo) {
		return switch (axis) {
			case Y -> lo ? b[2] : b[5];
			case Z -> lo ? b[1] : b[4];
			default -> lo ? b[1] : b[4];
		};
	}

	private static float planeOf(Direction d, float[] b) {
		return switch (d.getAxis()) {
			case X -> d.getStepX() > 0 ? b[3] : b[0];
			case Y -> d.getStepY() > 0 ? b[4] : b[1];
			case Z -> d.getStepZ() > 0 ? b[5] : b[2];
		};
	}

	private static float nearPlaneOf(Direction d, float[] b) {
		return switch (d.getAxis()) {
			case X -> d.getStepX() > 0 ? b[0] : b[3];
			case Y -> d.getStepY() > 0 ? b[1] : b[4];
			case Z -> d.getStepZ() > 0 ? b[2] : b[5];
		};
	}

	/**
	 * Triangle-as-quad: returns 4 verts whose two triangulated halves
	 * wind the same way. The degenerate half is a sliver on one edge:
	 * for order (o0,o1,o2), d = o2 - (o1-o0)*e keeps n2 parallel to n1.
	 * Boundary-locked triangles may have no in-bounds d for one order,
	 * so three cyclic orders (all preserve the triangle's winding) and
	 * four sign-preserving offsets are tried; last resort clamps.
	 */
	static Vector3f[] tri(Vector3f a, Vector3f b, Vector3f c) {
		float e = 0.01f;
		Vector3f[][] orders = {{a, b, c}, {b, c, a}, {c, a, b}};
		for (Vector3f[] o : orders) {
			Vector3f sf = new Vector3f(o[1]).sub(o[0]);
			Vector3f ts = new Vector3f(o[2]).sub(o[1]);
			Vector3f n1 = new Vector3f(sf).cross(ts);
			Vector3f[] cands = {
				new Vector3f(o[2]).sub(new Vector3f(sf).mul(e)),
				new Vector3f(o[0]).sub(new Vector3f(sf).mul(e)),
				new Vector3f(o[2]).add(new Vector3f(ts).mul(e)),
				new Vector3f(o[0]).add(new Vector3f(ts).mul(e))};
			for (Vector3f d : cands) {
				if (!inBox(d))
					continue;
				// the sliver half (o0,o2,d) must wind the same way as the
				// real triangle: d has to sit on the far side of the
				// o0->o2 diagonal from o1
				Vector3f n2 = new Vector3f(o[2]).sub(o[0])
					.cross(new Vector3f(d).sub(o[0]));
				if (n1.dot(n2) > 0)
					return new Vector3f[]{o[0], o[1], o[2], d};
			}
		}
		Vector3f d = new Vector3f(c).sub(new Vector3f(b).sub(a).mul(e));
		d.set(Math.min(16f, Math.max(0f, d.x())),
			Math.min(16f, Math.max(0f, d.y())),
			Math.min(16f, Math.max(0f, d.z())));
		return new Vector3f[]{a, b, c, d};
	}

	private static boolean inBox(Vector3f v) {
		return v.x() >= 0 && v.x() <= 16 && v.y() >= 0 && v.y() <= 16
			&& v.z() >= 0 && v.z() <= 16;
	}

	/** Cyclic 4 corners of face rect (u0..u1)x(v0..v1) at plane w, pixels. */
	static Vector3f[] faceCorners(Direction d, float u0, float u1,
		float v0, float v1, float w) {
		float[][] uv = switch (d) {
			case UP, NORTH, EAST -> new float[][]{{u0, v0}, {u0, v1}, {u1, v1}, {u1, v0}};
			default -> new float[][]{{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
		};
		Vector3f[] out = new Vector3f[4];
		for (int i = 0; i < 4; i++) {
			float u = uv[i][0], v = uv[i][1];
			out[i] = switch (d.getAxis()) {
				case Y -> new Vector3f(u, w, v);
				case Z -> new Vector3f(u, v, w);
				default -> new Vector3f(w, v, u);
			};
		}
		return out;
	}

	/**
	 * Emit quads for an arbitrary list of pixel-space AABBs
	 * (x0,y0,z0,x1,y1,z1 in 0..16). A box face toward `d` is hidden where
	 * another box touches it on that side; residual rects are emitted.
	 * Same partition rule as emit(): boundary planes -> the direction's
	 * cullface list; interior planes -> the null-direction list.
	 */
	static List<BakedQuad> emitBoxes(List<float[]> boxes, @Nullable Direction side,
		java.util.function.Function<Direction, List<SrcQuad>> srcFor) {
		List<BakedQuad> out = new ArrayList<>();
		for (float[] b : boxes) {
			for (Direction d : Direction.values()) {
				float plane = planeOf(d, b);
				boolean boundary = d.getAxisDirection() == Direction.AxisDirection.POSITIVE
					? plane >= 16f - 1e-4 : plane <= 1e-4;
				if (boundary ? side != d : side != null)
					continue;
				// residual face rects after subtracting touching boxes
				float u0 = uOf(d.getAxis(), b, true), u1 = uOf(d.getAxis(), b, false);
				float v0 = vOf(d.getAxis(), b, true), v1 = vOf(d.getAxis(), b, false);
				List<float[]> rects = new ArrayList<>();
				rects.add(new float[]{u0, u1, v0, v1});
				for (float[] o : boxes) {
					if (o == b)
						continue;
					float near = nearPlaneOf(d, o);
					if (Math.abs(near - plane) > 1e-4)
						continue; // not touching on this side
					float ou0 = uOf(d.getAxis(), o, true), ou1 = uOf(d.getAxis(), o, false);
					float ov0 = vOf(d.getAxis(), o, true), ov1 = vOf(d.getAxis(), o, false);
					List<float[]> next = new ArrayList<>();
					for (float[] r : rects) {
						float ix0 = Math.max(r[0], ou0), ix1 = Math.min(r[1], ou1);
						float iy0 = Math.max(r[2], ov0), iy1 = Math.min(r[3], ov1);
						if (ix0 >= ix1 - 1e-4 || iy0 >= iy1 - 1e-4) {
							next.add(r);
							continue;
						}
						if (r[2] < iy0 - 1e-4) next.add(new float[]{r[0], r[1], r[2], iy0});
						if (iy1 < r[3] - 1e-4) next.add(new float[]{r[0], r[1], iy1, r[3]});
						if (r[0] < ix0 - 1e-4) next.add(new float[]{r[0], ix0, iy0, iy1});
						if (ix1 < r[1] - 1e-4) next.add(new float[]{ix1, r[1], iy0, iy1});
					}
					rects = next;
					if (rects.isEmpty())
						break;
				}
				for (float[] r : rects)
					for (SrcQuad src : srcFor.apply(d))
						out.add(emitFace(d, faceCorners(d, r[0], r[1], r[2], r[3], plane), src));
			}
		}
		return out;
	}
}
