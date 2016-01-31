package bbcursive.lib;

import bbcursive.ann.Infix;
import bbcursive.std;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.UnaryOperator;

import static bbcursive.std.traits.skipWs;

public enum infix_ {;
@Infix
    public static UnaryOperator<ByteBuffer> infix(UnaryOperator<ByteBuffer>... allOf) {
return new
        UnaryOperator<ByteBuffer>() {
            @Override
            public String toString() {
                return "infix"+ Arrays.deepToString(allOf);
            }

            @Override
            public ByteBuffer apply(ByteBuffer buffer) {
                std.flags.get().remove(skipWs);
                return std.bb(buffer,allOf);
            }
        };

    }
}
