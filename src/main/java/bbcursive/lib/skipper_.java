package bbcursive.lib;

import bbcursive.ann.Skipper;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

@Skipper
public enum  skipper_ {;

    @Skipper public static UnaryOperator<ByteBuffer> skipper( ) {

        return new ByteBufferUnaryOperator();

    }

    @Skipper    private static class ByteBufferUnaryOperator implements UnaryOperator<ByteBuffer> {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return buffer;
        }
    }
}
