package bbcursive.lib;

import bbcursive.std;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

/**
 * Created by jim on 1/17/16.
 */
public class confix {
    public static UnaryOperator<ByteBuffer> confix(UnaryOperator<ByteBuffer> operator, char... chars) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer buffer) {
                UnaryOperator<ByteBuffer> chlit = bbcursive.lib.chlit.chlit(chars[0]);
                char aChar = chars[2 > chars.length ? 0 : 1];
                UnaryOperator<ByteBuffer> chlit1 = bbcursive.lib.chlit.chlit(aChar);
                return std.bb(buffer,chlit, operator, chlit1);

            }
        };

    }

    public static UnaryOperator<ByteBuffer> confix(char open, UnaryOperator<ByteBuffer> unaryOperator, char close) {
        return confix(unaryOperator, open, close);
    }

    public static UnaryOperator<ByteBuffer> confix(String s, UnaryOperator<ByteBuffer> unaryOperator) {
        return confix(unaryOperator, s.toCharArray());
    }
}
