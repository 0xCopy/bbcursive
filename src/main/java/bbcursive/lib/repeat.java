package bbcursive.lib;

import bbcursive.std;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Created by jim on 1/17/16.
 */
public class repeat {
    @NotNull
    public static UnaryOperator<ByteBuffer> repeat(UnaryOperator<ByteBuffer> op) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer byteBuffer) {
                ByteBuffer control = null;
                ByteBuffer leadIn = byteBuffer;
                ByteBuffer result;
                do {
                    result = control;
                    leadIn = control = std.bb(leadIn, op);
                } while (null != control);
                return result;
            }
        };
    }
}
