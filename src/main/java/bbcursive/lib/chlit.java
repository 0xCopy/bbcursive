package bbcursive.lib;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Created by jim on 1/17/16.
 */
public class chlit {
    public static UnaryOperator<ByteBuffer> chlit(char c) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer buf) {
                if (null != buf)
                    if (buf.hasRemaining()) {
                        byte b = ((ByteBuffer) buf.mark()).get();
                        if ((c & 0xffff) == (b & 0xff))
                            return buf;
                        else
                            return null;
                    }
                    else
                        return null;
                else
                    return null;
            }
        };
    }

    public static UnaryOperator<ByteBuffer> chlit(CharSequence s) {
        return chlit(s.charAt(0));
    }
}
