package bbcursive;

import bbcursive.Cursive.pre;
import com.databricks.fastbuffer.ByteBufferReader;
import com.databricks.fastbuffer.JavaByteBufferReader;
import com.databricks.fastbuffer.UnsafeDirectByteBufferReader;
import com.databricks.fastbuffer.UnsafeHeapByteBufferReader;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static bbcursive.Cursive.pre.debug;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by jim on 8/8/14.
 */
public class std {
    public static Allocator allocator;

    public static ByteBuffer bb(ByteBuffer b, Cursive... ops) {
        if (ops.length == 0) {
            return b;
        }

        Cursive op = ops[0];
        if(op==null)return null;
        if (ops.length == 1) {
            return op.apply(b);
        }
        return op.apply(bb(b, Arrays.copyOfRange(ops, 1, ops.length)));
    }

    public static ByteBuffer br(ByteBuffer b, Cursive... ops) {
        if (null == b) return null;
        for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
            Cursive op = ops[i];
            if (null == op) return null;
            b = op.apply(b);
        }
        return b;
    }

    public static <S extends WantsZeroCopy> ByteBuffer bb(S b, Cursive... ops) {
        ByteBuffer b1 = b.asByteBuffer();
        for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
            Cursive op = ops[i];
            if (null == op) return null;
            b1 = op.apply(b1);
        }
        return b1;
    }

    public static <S extends WantsZeroCopy> ByteBufferReader fast(S zc) {
        return fast(zc.asByteBuffer());
    }

    public static ByteBufferReader fast(ByteBuffer buf) {
        ByteBufferReader r;
        try {
            r = buf.hasArray() ? new UnsafeHeapByteBufferReader(buf) : new UnsafeDirectByteBufferReader(buf);
        } catch (UnsupportedOperationException e) {
            r = new JavaByteBufferReader(buf);
        }
        return r;
    }

    /**
     * convenience method
     *
     * @param bytes
     * @param operations
     * @return
     */
    public static String str(ByteBuffer bytes, Cursive... operations) {
        for (Cursive operation : operations) {
            if (operation instanceof pre) {
                bytes = operation.apply(bytes);
            }
        }
        String s = UTF_8.decode(bytes).toString();
        for (Cursive operation : operations) {
            if (!(operation instanceof pre)) {
                bytes = operation.apply(bytes);
            }
        }
        return s;
    }

    /**
     * just saves a few chars
     *
     * @param something toString will run on this
     * @param atoms
     * @return
     */
    public static String str(WantsZeroCopy something, Cursive... atoms) {
        return str(something.asByteBuffer(), atoms);
    }

    /**
     * just saves a few chars
     *
     * @param something toString will run on this
     * @param atoms
     * @return
     */
    public static String str(AtomicReference<? extends WantsZeroCopy> something, Cursive... atoms) {
        return str(something.get(), atoms);
    }

    /**
     * just saves a few chars
     *
     * @param something toString will run on this
     * @return
     */
    public static String str(Object something) {
        return String.valueOf(something);
    }

    public static Integer parseInt(ByteBuffer r) {
        long x = 0;
        boolean neg = false;

        Integer res = null;
        if (r.hasRemaining()) {
            int i = r.get();
            switch (i) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    x = x * 10 + i - '0';
                    break;
                case '-':
                    neg = true;
                case '+':

            }
            while (r.hasRemaining()) {
                i = r.get();
                switch (i) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        x = x * 10 + i - '0';
                        break;
                    case '-':
                        neg = true;
                    case '+':
                        break;

                }
            }
            res = (int) ((neg ? -x : x) & 0xffffffffL);
        }
        return res;
    }

    /**
     * convenience method
     *
     * @param src
     * @param operations
     * @return
     */
    public static <T extends CharSequence> ByteBuffer bb(T src, Cursive... operations) {

        ByteBuffer byteBuffer = UTF_8.encode(src.toString());
        for (Cursive operation : operations) {
            byteBuffer = operation.apply(byteBuffer);
        }
        return byteBuffer;
    }

    public static Integer parseInt(String r) {
        long x = 0;
        boolean neg = false;

        Integer res = null;


        int length = r.length();
        if (length > 0) {
            int i = r.charAt(0);
            switch (i) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    x = x * 10 + i - '0';
                    break;
                case '-':
                    neg = true;
                case '+':
                    break;

            }

            for (int j = 1; j < length; j++) {
                i = r.charAt(i);
                switch (i) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        x = x * 10 + i - '0';
                        break;
                    case '-':
                        neg = true;
                    case '+':
                        break;
                }
            }


            res = (int) ((neg ? -x : x) & 0xffffffffL);
        }
        return res;
    }

    /**
     * @param src
     * @param dest
     * @return
     */

    public static ByteBuffer push(ByteBuffer src, ByteBuffer dest) {
        int need = src
                .remaining(),
                have = dest.remaining();
        if (have > need) {
            return (ByteBuffer) dest.put(src);
        }
        dest.put((ByteBuffer) src.slice().limit(have));
        src.position(src.position() + have);
        return dest;
    }

    public static ByteBuffer grow(ByteBuffer src) {
        return ByteBuffer.allocateDirect(src.capacity() << 1).put(src);
    }

    ;

    /**
     * conditional debug output assert log(Object,[prefix[,suffix]])
     *
     * @param ob
     * @param prefixSuffix
     * @return
     */
    public static void log(Object ob, String... prefixSuffix) {
        assert log$(ob, prefixSuffix);
    }

    /**
     * conditional debug output assert log(Object,[prefix[,suffix]])
     *
     * @param ob
     * @param prefixSuffix
     * @return
     */
    public static boolean log$(Object ob, String... prefixSuffix) {
        boolean hasSuffix = prefixSuffix.length > 1;
        if (prefixSuffix.length > 0)
            System.err.print(prefixSuffix[0] + "\t");
        if (ob instanceof ByteBuffer) {
            bb((ByteBuffer) ob, debug);
        } else if (ob instanceof WantsZeroCopy) {
            WantsZeroCopy wantsZeroCopy = (WantsZeroCopy) ob;
            bb(wantsZeroCopy.asByteBuffer(), debug);
        } else {
            bb(String.valueOf(ob), debug);
        }
        if (hasSuffix)
            System.err.println(prefixSuffix[1] + "\t");
        return true;
    }

    public static ByteBuffer cat(List<ByteBuffer> byteBuffers) {
        ByteBuffer[] byteBuffers1 = byteBuffers.toArray(new ByteBuffer[byteBuffers.size()]);
        return cat(byteBuffers1);
    }

    public static ByteBuffer cat(ByteBuffer... src) {
        ByteBuffer cursor;
        int total = 0;
        if (1 >= src.length) {
            cursor = src[0];
        } else {
            for (int i = 0, payloadLength = src.length; i < payloadLength; i++) {
                ByteBuffer byteBuffer = src[i];
                total += byteBuffer.remaining();
            }
            cursor = alloc(total);
            for (int i = 0, payloadLength = src.length; i < payloadLength; i++) {
                ByteBuffer byteBuffer = src[i];
                cursor.put(byteBuffer);
            }
            cursor.rewind();
        }
        return cursor;
    }

    public static ByteBuffer alloc(int size) {
        return null != allocator ? allocator.allocate(size) : ByteBuffer.allocateDirect(size);
    }

    public static ByteBufferReader alloca(int size) {
        return fast(alloc(size));
    }

    /**
     * reposition
     *
     * @param position
     * @return
     */
    public static Cursive pos(int position) {
        return t ->t==null?t:(ByteBuffer) t.position(position);

    }

    /**
     * reposition
     *
     * @param position
     * @return
     */
    public static Cursive lim(int position) {
        return target -> (ByteBuffer) target.limit(position);

    }

    public static ByteBuffer consumeString(ByteBuffer buffer) {
        //TODO unicode wat?
        while (buffer.hasRemaining()) {
            byte current = buffer.get();
            switch (current) {
                case '"':
                    return buffer;
                case '\\':
                    byte next = buffer.get();
                    switch (next) {
                        case 'u':
                            buffer.position(buffer.position() + 4);
                        default:
                    }
            }
        }
        return buffer;
    }

    public static ByteBuffer consumeNumber(ByteBuffer slice) {
        byte b = ((ByteBuffer) slice.mark()).get();

        boolean sign = b == '-' || b == '+';
        if (!sign) slice.reset();

        boolean dot = false;
        boolean etoken = false;
        boolean esign = false;
        while (slice.hasRemaining()) {
            while (slice.hasRemaining() && Character.isDigit(b = ((ByteBuffer) slice.mark()).get())) ;
            char x = (char) b;
            switch (b) {
                case '.':
                    assert !dot : "extra dot";
                    dot = true;
                case 'E':
                case 'e':
                    assert !etoken : "missing digits or redundant exponent";
                    etoken = true;
                case '+':
                case '-':
                    assert !esign : "bad exponent sign";
                    esign = true;
                default:
                    if (!Character.isDigit(b)) return (ByteBuffer) slice.reset();
            }
        }
        return null;
    }

    /**
     * consumes a token from the current ByteBuffer position.  null signals fail and should reset.
     *
     * @param exemplar ussually name().getBytes(), but might be other value also.
     * @return null if no match -- rollback not done here use Narsive.$ for whitespace and rollback
     */
    public static Cursive genericAdvance(byte... exemplar) {

        return target -> {
            int c = 0;
            while (null != exemplar && null != target && target.hasRemaining() && c < exemplar.length && exemplar[c] == target.get())
                c++;
            return null != target && c == exemplar.length ? target : null;
        };
    }

    public static Cursive abort(int rollbackPosition) {
        return b -> null==b?null:bb(b, pos(rollbackPosition), null);
    }

    public static Cursive anyOf(Cursive... anyOf) {
        return b -> {
            for (Cursive o : anyOf) {
                ByteBuffer bb = bb(b, o);
                if (null != bb) return bb;
            }
            return null;
        };
    }

    public static Cursive opt(Cursive... allOrPrevious) {
        return t -> {
            if (null != t) {
                int rollback = t.position();
                ByteBuffer bb = bb(t, allOrPrevious);
                return null == bb ? bb(t, pos(rollback)) : bb;
            }
            return t;
        };
    }

    /**
     * allOf of, in sequence, without failures
     *
     * @param allOf
     * @return null if not allOf match in sequence, buffer rolled back
     */
    public static Cursive allOf
    (Cursive... allOf) {
        return target -> bb(target, allOf);
    }
}
