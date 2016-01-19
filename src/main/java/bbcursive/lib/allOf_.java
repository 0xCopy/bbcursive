package bbcursive.lib;

import bbcursive.std;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Created by jim on 1/17/16.
 */
public class allOf_ {
    /**
     * bbcursive.lib.allOf_ of, in sequence, without failures
     *
     * @param allOf
     * @return null if not bbcursive.lib.allOf_ match in sequence
     */
    public static UnaryOperator<ByteBuffer> allOf(UnaryOperator<ByteBuffer>... allOf) {
        return target -> null == target ? null : std.bb(target, allOf);
    }
}
