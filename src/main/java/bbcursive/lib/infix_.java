package bbcursive.lib;

import bbcursive.ann.Infix;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

public enum infix_ {;

    public static UnaryOperator<ByteBuffer> infix(UnaryOperator<ByteBuffer>... allOf) {

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
