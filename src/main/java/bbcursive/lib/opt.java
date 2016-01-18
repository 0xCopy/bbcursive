package bbcursive.lib;

import bbcursive.std;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Created by jim on 1/17/16.
 */
public class opt {
    public static UnaryOperator<ByteBuffer> opt(UnaryOperator<ByteBuffer>... allOrPrevious) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer t) {
                ByteBuffer t1 = t;
                if (null != t1) {
                    int rollback = t1.position();
                    ByteBuffer bb;

                    t1 = null == (bb = std.bb(t1, allOrPrevious)) ? std.bb(t1, pos.pos(rollback)) : bb;
                }
                return t1;
            }
        };
    }
}
