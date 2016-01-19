package bbcursive.lib;

import bbcursive.ann.Backtracking;
import bbcursive.ann.Infix;
import bbcursive.ann.Skipper;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

public enum infix_ {;

    public static UnaryOperator<ByteBuffer> infix( ) {

        return new ByteBufferUnaryOperator();

    }
@Infix
    private static class ByteBufferUnaryOperator implements UnaryOperator<ByteBuffer> {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return buffer;
        }
    }
}
