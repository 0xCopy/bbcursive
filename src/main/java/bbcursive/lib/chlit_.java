package bbcursive.lib;

import bbcursive.ann.Infix;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Created by jim on 1/17/16.
 */
public class chlit_ {
    public static UnaryOperator<ByteBuffer> chlit(char c) {
        return new ByteBufferUnaryOperator(c);
    }

    public static UnaryOperator<ByteBuffer> chlit(CharSequence s) {
        return chlit(s.charAt(0));
    }

    @Infix
    private static class ByteBufferUnaryOperator implements UnaryOperator<ByteBuffer> {
        private final char c;

        public ByteBufferUnaryOperator(char c) {
            this.c = c;
        }

        @Override
        public String toString() {
            return "c8'" +
                    c+"'";
        }

        @Infix
        @Nullable
        @Override
        public ByteBuffer apply(ByteBuffer buf) {
            if (null == buf) {
                return null;
            }
            if (buf.hasRemaining()) {
                byte b = ((ByteBuffer) buf.mark()).get();
                return (c & 0xff) == (b & 0xff) ? buf : null;
            }
            return null;



        }
    }
}
