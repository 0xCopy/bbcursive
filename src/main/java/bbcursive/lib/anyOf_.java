package bbcursive.lib;

import bbcursive.ann.Backtracking;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import static bbcursive.std.bb;
import static java.util.Arrays.*;

/**
 * Created by jim on 1/17/16.
 */
public class anyOf_ {
    public static UnaryOperator<ByteBuffer> anyOf(UnaryOperator<ByteBuffer>... anyOf) {
         return new UnaryOperator<ByteBuffer>() {


            @Override
            public String toString() {
                return "any"+deepToString(anyOf);
            }

            @Override
            public ByteBuffer apply(ByteBuffer b) {
                stream(anyOf).anyMatch(byteBufferUnaryOperator -> null != bb(b, byteBufferUnaryOperator));
                return b;
            }
        };
    }

    @Backtracking
    public static UnaryOperator<ByteBuffer> anyOf(CharSequence s) {
        final int[] ints = s.chars().sorted().toArray();

        return new UnaryOperator<ByteBuffer>() {


            @Override
            public String toString() {
                StringBuilder any = new StringBuilder("[");
                IntStream.of(ints).forEach(i->any.append((char)(i&0xffff)));

                return String.valueOf(any.append(']'));
            }

            @Override
            public ByteBuffer apply(ByteBuffer b) {
                if (null != b && b.hasRemaining()) {
                    byte b1 = b.get();
                    return -1 >= binarySearch(ints, b1 & 0xff) ? null : b;
                }
                return null;
            }
        };
    }

}
