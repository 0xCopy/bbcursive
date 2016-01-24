package bbcursive.lib;

import bbcursive.ann.Backtracking;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

import static bbcursive.std.bb;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.deepToString;
import static java.util.Arrays.stream;

/**
 * Created by jim on 1/17/16.
 */
public class anyOf_ {
    public static UnaryOperator<ByteBuffer> anyOf(UnaryOperator<ByteBuffer>... anyOf) {
        return new anyof1(anyOf);
    }
    @Backtracking
    public static UnaryOperator<ByteBuffer> anyOf(CharSequence s) {
        int[] ints = s.chars().sorted().toArray();
        return new anyOfChars(ints);
    }

    private static class anyOfChars implements UnaryOperator<ByteBuffer> {
        private final int[] ints;

        public anyOfChars(int[] ints) {
            this.ints = ints;
        }

        @Override
        public ByteBuffer apply(ByteBuffer b) {
            if (b != null && b.hasRemaining()) {
                byte b1 = b.get();
                return -1 >= binarySearch(ints, b1 & 0xff) ? null : b;
            }
            return null;
        }
    }

    private static class anyof1 implements UnaryOperator<ByteBuffer> {
        private final UnaryOperator<ByteBuffer>[] anyOf;

        @Override
        public String toString() {
            return "any:"+deepToString(anyOf);
        }

        public anyof1(UnaryOperator<ByteBuffer>... anyOf) {
            this.anyOf = anyOf;
        }

        @Override
        public ByteBuffer apply(ByteBuffer b) {
            stream(anyOf).anyMatch(byteBufferUnaryOperator -> null != bb(b, byteBufferUnaryOperator));
            return b;
        }
    }
}
