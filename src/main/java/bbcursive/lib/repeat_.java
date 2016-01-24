package bbcursive.lib;

import bbcursive.ann.Backtracking;
import bbcursive.ann.Skipper;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Created by jim on 1/17/16.
 */
public enum repeat_ {
    ;

    @NotNull
    public static UnaryOperator<ByteBuffer> repeat(UnaryOperator<ByteBuffer> op) {

        return new repeater(op);
    }

    @Skipper
    @Backtracking
    private static class repeater implements UnaryOperator<ByteBuffer> {
        private final UnaryOperator<ByteBuffer> op;

        repeater(UnaryOperator<ByteBuffer> op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return "rep:[" + String.valueOf(op) + "]";
        }

        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            int mark = byteBuffer.position();
            int matches = 0;
            ByteBuffer handle = byteBuffer;
            ByteBuffer last = null;
            while (handle.hasRemaining()) {
                last = handle;
                if (null != (handle=op.apply(handle))) {
                    matches++;
                    mark = handle.position();
                }else break;
            }

            if (matches > 0 && last.hasRemaining())
                last.position(mark);

            return matches > 0 ? last: null;
        }
    }
}



