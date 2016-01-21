package bbcursive.lib;

import bbcursive.ann.ForwardOnly;
import bbcursive.ann.Infix;

import java.nio.ByteBuffer;
import java.util.function.UnaryOperator;

import static bbcursive.lib.anyOf_.anyOf;
import static bbcursive.lib.chlit_.chlit;
import static bbcursive.lib.infix_.infix;
import static bbcursive.lib.opt.opt;
import static bbcursive.lib.repeat_.repeat;

/**
 * Created by jim on 1/21/16.
 */
@Infix
@ForwardOnly
public class value_ implements UnaryOperator<ByteBuffer> {

    public static final value_ VALUE_ = new value_();

    private value_() {
    }

    public static value_ value() {
        return VALUE_;
    }

    @Override
    public ByteBuffer apply(ByteBuffer buffer) {
        return (ByteBuffer) infix(opt(chlit("0")), anyOf("1.0"), opt(repeat(anyOf("1029384756"))));
    }
}
