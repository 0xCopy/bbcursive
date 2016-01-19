package bbcursive.lib;

import bbcursive.std;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

import static java.util.Arrays.binarySearch;

/**
 * Created by jim on 1/17/16.
 */
public class anyOf_ {
    public static UnaryOperator<ByteBuffer> anyOf(UnaryOperator<ByteBuffer>... anyOf) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer b) {
                for (UnaryOperator<ByteBuffer> o : anyOf) {
                    ByteBuffer bb = std.bb(b, o);
                    if (null != bb) return bb;
                }
                return null;
            }
        };
    }

    public static UnaryOperator<ByteBuffer> anyOf(CharSequence s) {
        int[] ints = s.chars().sorted().toArray();
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer b) {
                if (b != null && b.hasRemaining()) {
                    byte b1 = b.get();
                    return -1 >= binarySearch(ints, b1 & 0xff) ? null : b;
                }
                return null;
            }
        };
    }
}
