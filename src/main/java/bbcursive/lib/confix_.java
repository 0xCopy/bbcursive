package bbcursive.lib;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.UnaryOperator;

import static bbcursive.lib.allOf_.allOf;
import static bbcursive.std.bb;

/**
 * Created by jim on 1/17/16.
 */
public class confix_ {
    public static UnaryOperator<ByteBuffer> confix(UnaryOperator<ByteBuffer> operator, char... chars) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public String toString() {
                return "confix_:"+ Arrays.toString(chars)+" : "+operator;
            }

            @Override
            public ByteBuffer apply(ByteBuffer buffer) {
                UnaryOperator<ByteBuffer> chlit = chlit_.chlit(chars[0]);
                char aChar = chars[2 > chars.length ? 0 : 1];
                UnaryOperator<ByteBuffer> chlit1 = chlit_.chlit(aChar);
                return bb(buffer,confix(chlit, chlit1, operator));
            }
        };
    }
 public    static UnaryOperator<ByteBuffer> confix(UnaryOperator<ByteBuffer> before, UnaryOperator<ByteBuffer> after, UnaryOperator<ByteBuffer> operator) {
        return new trident(before, operator, after);
    }

    public static UnaryOperator<ByteBuffer> confix(char open, UnaryOperator<ByteBuffer> unaryOperator, char close) {
        return confix(unaryOperator, open, close);
    }

    public static UnaryOperator<ByteBuffer> confix(String s, UnaryOperator<ByteBuffer> unaryOperator) {
        return confix(unaryOperator, s.toCharArray());
    }

    private static class trident implements UnaryOperator<ByteBuffer> {
        private final UnaryOperator<ByteBuffer> before;
        private final UnaryOperator<ByteBuffer> operator;
        private final UnaryOperator<ByteBuffer> after;

        public trident(UnaryOperator<ByteBuffer> before, UnaryOperator<ByteBuffer> operator, UnaryOperator<ByteBuffer> after) {
            this.before = before;
            this.operator = operator;
            this.after = after;
        }

        @Override
        public String toString() {
            UnaryOperator[] a = {before, operator, after};
            return "confix"+Arrays.deepToString(a);
        }

        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return bb(buffer,allOf(before, operator, after));
        }
    }
}
;
