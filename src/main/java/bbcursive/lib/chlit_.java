package bbcursive.lib;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Created by jim on 1/17/16.
 */
public class chlit_ {
    public static UnaryOperator<ByteBuffer> chlit(char c) {
        return new UnaryOperator<ByteBuffer>() {
            @Nullable
            @Override
            public ByteBuffer apply(ByteBuffer buf) {
                if (null == buf) {
                    return null;
                }
                if (buf.hasRemaining()) {
                    byte b = ((ByteBuffer) buf.mark()).get();
                    if ((c & 0xff) == (b & 0xff))
                        return buf;
                    return null;
                }
                return null;



            }
        };
    }

    public static UnaryOperator<ByteBuffer> chlit(CharSequence s) {
        return chlit(s.charAt(0));
    }
}
