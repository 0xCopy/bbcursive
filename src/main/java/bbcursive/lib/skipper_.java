package bbcursive.lib;

import bbcursive.ann.Skipper;
import bbcursive.std;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.UnaryOperator;

import static bbcursive.std.bb;

@Skipper
public enum  skipper_ {;

    @Skipper public static UnaryOperator<ByteBuffer> skipper( UnaryOperator<ByteBuffer>...allOf) {


        return new UnaryOperator<ByteBuffer>() {
            @Override
            public String toString() {
                return "skipper"+ Arrays.deepToString(allOf);
            }



            @Override
            public ByteBuffer apply(ByteBuffer buffer) {
                std.flags.get().add(std.traits.skipWs);

                return bb(buffer,allOf);
            }
        };

    }

}
