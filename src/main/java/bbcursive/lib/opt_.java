package bbcursive.lib;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

import static bbcursive.std.bb;

/**
 * Created by jim on 1/17/16.
 */
public class opt_ {
    public static UnaryOperator<ByteBuffer> opt(UnaryOperator<ByteBuffer>... unaryOperators) {
        return new ByteBufferUnaryOperator(unaryOperators);
    }

    private static class ByteBufferUnaryOperator implements UnaryOperator<ByteBuffer> {
        private UnaryOperator<ByteBuffer>[] allOrPrevious;

        public ByteBufferUnaryOperator(UnaryOperator<ByteBuffer>[] allOrPrevious) {

            this.allOrPrevious = allOrPrevious;
        }

        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            int position = buffer.position();
            ByteBuffer r = bb(buffer, allOrPrevious);
            if(null==r) {
                buffer.position(position);
            }
            return buffer;
        }
    }
}
