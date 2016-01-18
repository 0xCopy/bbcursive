package bbcursive.lib;

import bbcursive.std;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Created by jim on 1/17/16.
 */
public class allOf {
    /**
     * bbcursive.lib.allOf of, in sequence, without failures
     *
     * @param allOf
     * @return null if not bbcursive.lib.allOf match in sequence
     */
    public static UnaryOperator<ByteBuffer> allOf(UnaryOperator<ByteBuffer>... allOf) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer target) {
                return null == target ? null : std.bb(target, allOf);
            }
        };
    }
}
