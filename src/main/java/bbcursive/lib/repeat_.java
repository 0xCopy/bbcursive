package bbcursive.lib;

import bbcursive.ann.Backtracking;
import bbcursive.ann.Skipper;
import bbcursive.std;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Created by jim on 1/17/16.
 */
public enum repeat_ {;
    @NotNull
    public static UnaryOperator<ByteBuffer> repeat(UnaryOperator<ByteBuffer> op) {

        return new ByteBufferUnaryOperator(op);
    }
@Skipper@Backtracking
    private static class ByteBufferUnaryOperator implements UnaryOperator<ByteBuffer> {
        private final UnaryOperator<ByteBuffer> op;

        ByteBufferUnaryOperator(UnaryOperator<ByteBuffer> op) {
            this.op = op;
        }

        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            ByteBuffer control = null;
            ByteBuffer trailing = byteBuffer;
            ByteBuffer result;
            int mark;
            do {
                result = control;
                mark = trailing.position();
                trailing = control = std.bb(trailing, op);
            } while ((null != control) && byteBuffer.hasRemaining());

            if (null != result)
                result.position(mark);
            return result;
        }
    }
}
