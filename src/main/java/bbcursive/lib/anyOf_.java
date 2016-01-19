package bbcursive.lib;

import bbcursive.ann.Backtracking;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

import static bbcursive.std.bb;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.stream;

/**
 * Created by jim on 1/17/16.
 */
public class anyOf_ {
    public static UnaryOperator<ByteBuffer> anyOf(UnaryOperator<ByteBuffer>... anyOf) {
        return b -> {
            stream(anyOf).anyMatch(byteBufferUnaryOperator -> null != bb(b, byteBufferUnaryOperator));
            return b;
        };
    }
    @Backtracking
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
