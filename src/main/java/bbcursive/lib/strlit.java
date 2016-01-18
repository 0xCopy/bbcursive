package bbcursive.lib;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by jim on 1/17/16.
 */
public class strlit {

    public static UnaryOperator<ByteBuffer> strlit(CharSequence s) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer buffer) {
                ByteBuffer encode = UTF_8.encode(String.valueOf(s));
                while (encode.hasRemaining() && buffer.hasRemaining() && encode.get() == buffer.get()) ;
                return encode.hasRemaining() ? null : buffer;
            }
        };
    }
}
