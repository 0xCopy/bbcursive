package bbcursive;

import com.databricks.fastbuffer.ByteBufferReader;
import com.databricks.fastbuffer.JavaByteBufferReader;
import com.databricks.fastbuffer.UnsafeDirectByteBufferReader;
import com.databricks.fastbuffer.UnsafeHeapByteBufferReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static bbcursive.Cursive.pre.debug;
import static java.lang.ThreadLocal.withInitial;
import static java.nio.ByteBuffer.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.binarySearch;


/**
 * Created by jim on 8/8/14.
 */
public class std {
    private static Allocator allocator;

    /**
     * when you want to change the behaviors of the main IO parser, insert a new {@link BiFunction} to intercept
     * parameters and returns to fire events and clean up using {@link ThreadLocal#set(Object)}
     */
    private static final ThreadLocal<BiFunction<ByteBuffer, UnaryOperator<ByteBuffer>[], ByteBuffer>> theParser =
            withInitial((Supplier<BiFunction<ByteBuffer, UnaryOperator<ByteBuffer>[], ByteBuffer>>) () -> std::defaultParser);

    /**
     * this is the main bytebuffer io parser most easily coded for.
     * <p>
     * when you want to change the behaviors of the IO parser, insert a new
     *
     * @param b   the bytebuffer
     * @param ops
     * @return
     */
    public static ByteBuffer bb(ByteBuffer b, UnaryOperator<ByteBuffer>... ops) {
        return getTheParser().get().apply(b, ops);
    }

    @Nullable
    public static ByteBuffer defaultParser(ByteBuffer b, UnaryOperator<ByteBuffer>... ops) {
        ByteBuffer r = null;
        UnaryOperator<ByteBuffer> op = null;
        int position = 0;
        if (null != b) {
            position = b.position();
            switch (ops.length) {
                case 0:
                    r = b;
                    break;
                case 1:
                    r = (op=ops[0]).apply(b);
                    break;
                default:
                    op = ops[0];
                    UnaryOperator<ByteBuffer>[] ops1 = Arrays.copyOfRange(ops, 1, ops.length);
                    r = bb(op.apply(b), ops1);
                    break;
            }
        }
        if (r != null&&op!=null) log(op, "===", "@" + position);
        return r;
    }


    public static <S extends WantsZeroCopy> ByteBuffer bb(S b, UnaryOperator<ByteBuffer>... ops) {
        ByteBuffer b1 = b.asByteBuffer();
        for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
            UnaryOperator<ByteBuffer> op = ops[i];
            if (null == op) {
                b1 = null;
                break;
            }
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
    public static String str(ByteBuffer bytes, UnaryOperator<ByteBuffer>... operations) {
        for (UnaryOperator<ByteBuffer> operation : operations) {
            if (operation instanceof Cursive.pre) {
                bytes = operation.apply(bytes);
            }
        }
        String s = UTF_8.decode(bytes).toString();
        for (UnaryOperator<ByteBuffer> operation : operations) {
            if (!(operation instanceof Cursive.pre)) bytes = operation.apply(bytes);
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
    public static String str(WantsZeroCopy something, UnaryOperator<ByteBuffer>... atoms) {
        return str(something.asByteBuffer(), atoms);
    }

    /**
     * just saves a few chars
     *
     * @param something toString will run on this
     * @param atoms
     * @return
     */
    public static String str(AtomicReference<? extends WantsZeroCopy> something, UnaryOperator<ByteBuffer>... atoms) {
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
    public static <T extends CharSequence> ByteBuffer bb(T src, UnaryOperator<ByteBuffer>... operations) {

        ByteBuffer byteBuffer = UTF_8.encode(src.toString());
        for (UnaryOperator<ByteBuffer> operation : operations) {
            byteBuffer = operation.apply(byteBuffer);
        }
        return byteBuffer;
    }

    public static Integer parseInt(String r) {
        long x = 0;
        boolean neg = false;

        Integer res = null;


        int length = r.length();
        if (0 < length) {
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
            return dest.put(src);
        }
        dest.put((ByteBuffer) src.slice().limit(have));
        src.position(src.position() + have);
        return dest;
    }

    public static ByteBuffer grow(ByteBuffer src) {
        return allocateDirect(src.capacity() << 1).put(src);
    }

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
        boolean hasSuffix = 1 < prefixSuffix.length;
        if (0 < prefixSuffix.length)
            System.err.print(prefixSuffix[0] + "\t");
        if (ob instanceof ByteBuffer) {
            defaultParser((ByteBuffer) ob, debug);
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
        return null != getAllocator() ? getAllocator().allocate(size) : allocateDirect(size);
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
    public static UnaryOperator<ByteBuffer> pos(int position) {
        return new pos(position);

    }

    /**
     * reposition
     *
     * @param position
     * @return
     */
    public static UnaryOperator<ByteBuffer> lim(int position) {
        return new lim(position);

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

        boolean sign = '-' == b || '+' == b;
        if (!sign) {
            slice.reset();
        }

        boolean dot = false;
        boolean etoken = false;
        boolean esign = false;
        while (slice.hasRemaining()) {
            while (slice.hasRemaining() && Character.isDigit(b = ((ByteBuffer) slice.mark()).get())) ;
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
    public static UnaryOperator<ByteBuffer> genericAdvance(byte... exemplar) {

        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer target) {
                int c = 0;
                while (null != exemplar && null != target && target.hasRemaining() && c < exemplar.length && exemplar[c] == target.get())
                    c++;
                return null != target && c == exemplar.length ? target : null;
            }
        };
    }

    public static UnaryOperator<ByteBuffer> abort(int rollbackPosition) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer b) {
                return null == b ? null : bb(b, pos(rollbackPosition), null);
            }
        };
    }

    public static UnaryOperator<ByteBuffer> anyOf(UnaryOperator<ByteBuffer>... anyOf) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer b) {
                for (UnaryOperator<ByteBuffer> o : anyOf) {
                    ByteBuffer bb = defaultParser(b, o);
                    if (null != bb) return bb;
                }
                return null;
            }
        };
    }

    public static UnaryOperator<ByteBuffer> opt(UnaryOperator<ByteBuffer>... allOrPrevious) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer t) {
                ByteBuffer t1 = t;
                if (null != t1) {
                    int rollback = t1.position();
                    ByteBuffer bb;

                    t1 = null == (bb = bb(t1, allOrPrevious)) ? bb(t1, pos(rollback)) : bb;
                }
                return t1;
            }
        };
    }

    /**
     * allOf of, in sequence, without failures
     *
     * @param allOf
     * @return null if not allOf match in sequence
     */
    public static UnaryOperator<ByteBuffer> allOf(UnaryOperator<ByteBuffer>... allOf) {
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer target) {
                return null == target ? null : bb(target, allOf);
            }
        };
    }

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
                    leadIn = control = bb(leadIn, op);
                } while (null != control);
                return result;
            }
        };
    }

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

    public static UnaryOperator<ByteBuffer> anyOf(CharSequence s) {
        int[] ints = s.chars().sorted().toArray();
        return new UnaryOperator<ByteBuffer>() {
            @Override
            public ByteBuffer apply(ByteBuffer b) {
                if (b != null && b.hasRemaining()) {
                    byte b1 = b.get();
                    return -1 >= binarySearch(ints, b1 & 0xff) ? null : b;
                }
                return null;
            }
        };
    }

    public static UnaryOperator<ByteBuffer> chlit(CharSequence s) {
        return chlit(s.charAt(0));
    }

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

    static UnaryOperator<ByteBuffer> confix(UnaryOperator<ByteBuffer> operator, char... chars) {
        UnaryOperator<ByteBuffer> chlit = chlit(chars[0]);
        char aChar = chars[2 > chars.length ? 0 : 1];
        UnaryOperator<ByteBuffer> chlit1 = chlit(aChar);
        return allOf(chlit, operator, chlit1);
    }

    public static UnaryOperator<ByteBuffer> confix(char open, UnaryOperator<ByteBuffer> unaryOperator, char close) {
        return confix(unaryOperator, open, close);
    }

    public static UnaryOperator<ByteBuffer> confix(String s, UnaryOperator<ByteBuffer> unaryOperator) {
        return confix(unaryOperator, s.toCharArray());
    }

    public static Allocator getAllocator() {
        return allocator;
    }

    public static void setAllocator(Allocator allocator) {
        std.allocator = allocator;
    }

    /**
     * when you want to change the behaviors of the main IO parser, insert a new {@link BiFunction} to intercept
     * parameters and returns to fire events and clean up using {@link ThreadLocal#set(Object)}
     *
     * @return a htradlocal with a lambda
     */

    public static ThreadLocal<BiFunction<ByteBuffer, UnaryOperator<ByteBuffer>[], ByteBuffer>> getTheParser() {
        return theParser;
    }

    private static class lim implements UnaryOperator<ByteBuffer> {
        private final int position;

        public lim(int position) {
            this.position = position;
        }

        @Override
        public ByteBuffer apply(ByteBuffer target) {
            return (ByteBuffer) target.limit(position);
        }
    }

    private static class pos implements UnaryOperator<ByteBuffer> {
        private final int position;

        public pos(int position) {
            this.position = position;
        }

        @Override
        public ByteBuffer apply(ByteBuffer t) {
            return null == t ? t : (ByteBuffer) t.position(position);
        }
    }
}

