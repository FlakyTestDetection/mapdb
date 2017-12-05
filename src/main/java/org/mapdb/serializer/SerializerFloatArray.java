package org.mapdb.serializer;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by jan on 2/28/16.
 */
public class SerializerFloatArray extends GroupSerializerObjectArray<float[]> {
    @Override
    public void serialize(DataOutput2 out, float[] value) throws IOException {
        out.packInt(value.length);
        for (float v : value) {
            out.writeFloat(v);
        }
    }

    @Override
    public float[] deserialize(DataInput2 in, int available) throws IOException {
        float[] ret = new float[in.unpackInt()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = in.readFloat();
        }
        return ret;
    }

    @Override
    public boolean isTrusted() {
        return true;
    }

    @Override
    public boolean equals(float[] a1, float[] a2) {
        return Arrays.equals(a1, a2);
    }

    @Override
    public int hashCode(float[] floats, int seed) {
        for (float element : floats)
            seed = (-1640531527) * seed + Float.floatToIntBits(element);
        return seed;
    }

    @Override
    public int compare(float[] o1, float[] o2) {
        if (o1 == o2) return 0;
        final int len = Math.min(o1.length, o2.length);
        for (int i = 0; i < len; i++) {
            if (o1[i] == o2[i])
                continue;
            if (o1[i] > o2[i])
                return 1;
            return -1;
        }
        return SerializerUtils.compareInt(o1.length, o2.length);
    }
}
