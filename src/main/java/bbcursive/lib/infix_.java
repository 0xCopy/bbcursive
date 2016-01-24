package bbcursive.lib;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

public enum infix_ {;

    public static UnaryOperator<ByteBuffer> infix(UnaryOperator<ByteBuffer>... allOf) {

        return new UnaryOperator<ByteBuffer>() {
            @Override
            public String toString() {
                return "infix";
            }

            @Override
            public ByteBuffer apply(ByteBuffer buffer) {
                return buffer;
            }
        };

    }
}
