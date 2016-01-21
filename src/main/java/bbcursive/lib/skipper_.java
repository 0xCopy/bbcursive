package bbcursive.lib;

import bbcursive.ann.Skipper;
import bbcursive.std;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

import static bbcursive.std.bb;

@Skipper
public enum  skipper_ {;

    @Skipper public static UnaryOperator<ByteBuffer> skipper( UnaryOperator<ByteBuffer>...allOf) {

        return new ByteBufferUnaryOperator(allOf);

    }

    @Skipper    private static class ByteBufferUnaryOperator implements UnaryOperator<ByteBuffer> {
        private UnaryOperator<ByteBuffer>[] allOf;

        public ByteBufferUnaryOperator(UnaryOperator<ByteBuffer>... allOf) {

            this.allOf = allOf;
        }

        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            std.flags.get().add(std.traits.skipWs);

            return bb(buffer,allOf);
        }
    }
}
