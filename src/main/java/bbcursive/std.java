package bbcursive;

import bbcursive.Cursive.pre;
import bbcursive.ann.Backtracking;
import bbcursive.ann.ForwardOnly;
import bbcursive.lib.u8tf;
import bbcursive.vtables._edge;
import bbcursive.vtables._ptr;
import com.databricks.fastbuffer.ByteBufferReader;
import com.databricks.fastbuffer.JavaByteBufferReader;
import com.databricks.fastbuffer.UnsafeDirectByteBufferReader;
import com.databricks.fastbuffer.UnsafeHeapByteBufferReader;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static bbcursive.lib.pos.pos;
import static java.lang.Character.isDigit;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Created by jim on 8/8/14.
 */
public class std {
    private static Allocator allocator;

    /**
     * when you want to change the behaviors of the main IO parser, insert a new {@link BiFunction} to intercept
     * parameters and returns to fire events and clean up using {@link ThreadLocal#set(Object)}
     */
    public enum traits {
        debug, backtrackOnNull, skipWs;
    }

    public static final InheritableThreadLocal<Map<traits, Boolean>> flags = new InheritableThreadLocal<Map<traits, Boolean>>(){{
        set(new EnumMap<>(traits.class));
    }};

    /**
     * the outbox -- when a parse term successfully returns and a {@link Consumer}is installed as the outbox the
     * following state is published allowing for a recreation of the event elsewhere within the jvm
     *
     * in reverse order of resolution:
     * <p>
     * flags -- from annotations from lambda class
     * UnaryOperator -- the lambda that fired,
     * Integer -- length, to save time moving and scoring the artifact
     * _ptr -- _edge[ByteBuffer,Integer] state pair
     */
    public static InheritableThreadLocal<Consumer<_edge<_edge< Set<traits>,
            _edge<UnaryOperator<ByteBuffer>, Integer>>, _ptr>>>
            outbox = new InheritableThreadLocal<>();


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
        return defaultParser(b, ops);
    }


    @Nullable
    public static ByteBuffer defaultParser(ByteBuffer b, UnaryOperator<ByteBuffer>... ops) {
        ByteBuffer r = null;

        UnaryOperator<ByteBuffer> op = null;
        if (null != b && ops.length > 0) {
            op = ops[0];
            Class<? extends UnaryOperator> aClass = op.getClass();
            boolean skip = flags.get().get(traits.skipWs);
            if (skip && null != pre.skipWs.apply(b)) ;
            boolean backtrack = flags.get().get(traits.backtrackOnNull) || aClass.isAnnotationPresent(Backtracking.class) && !aClass.isAnnotationPresent(ForwardOnly.class);
            int startPosition = 0;
            if (backtrack) {
                startPosition = b.position();
            }
            switch (ops.length) {
                case 0:
                    r = b;
                    break;
                case 1:
                    r = op.apply(b);
                    break;
                default:
                    r = bb(op.apply(b), Arrays.copyOfRange(ops, 1, ops.length));
                    break;
            }
            if (null == r && backtrack) {
                r = bb(b, pos(startPosition));
            } else if (null != outbox.get()) {

                int startPosition2 = startPosition;

                final ByteBuffer b1 = b;
                final UnaryOperator<ByteBuffer> finalOp = op;
                final Set<traits> immutableTraits = flags.get().entrySet().stream().filter(traitsBooleanEntry -> traitsBooleanEntry.getValue()).map(traitsBooleanEntry -> traitsBooleanEntry.getKey()).collect(Collectors.toSet());

                outbox.get().accept(new _edge<_edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>, _ptr>() {
                    ;

                    @Override
                    protected _ptr at() {
                        return r$();
                    }

                    @Override
                    protected _ptr goTo(_ptr ptr) {
                        throw new Error("trifling with an immutable pointer");
                    }

                    @Override
                    protected _ptr r$() {
                        return (_ptr) at().bind(b1, startPosition2);
                    }

                    @Override
                    public _edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>> core(_edge<_edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>, _ptr>... e) {
                        return new _edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>() {
                            @Override
                            public Set<traits> core(_edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>... e) {
                                return immutableTraits;
                            }

                            @Override
                            protected _edge<UnaryOperator<ByteBuffer>, Integer> at() {
                                return r$();
                            }

                            @Override
                            protected _edge<UnaryOperator<ByteBuffer>, Integer> goTo(_edge<UnaryOperator<ByteBuffer>, Integer> unaryOperatorInteger_edge) {
                                throw new Error("cant move this");
                            }

                            @Override
                            protected _edge<UnaryOperator<ByteBuffer>, Integer> r$() {
                                return new _edge<UnaryOperator<ByteBuffer>, Integer>() {
                                    @Override
                                    protected Integer at() {
                                        return r$();
                                    }

                                    @Override
                                    protected Integer goTo(Integer integer) {
                                        throw new Error("immutable");
                                    }

                                    @Override
                                    public UnaryOperator<ByteBuffer> core(_edge<UnaryOperator<ByteBuffer>, Integer>... e) {
                                        return finalOp;
                                    }

                                    @Override
                                    protected Integer r$() {
                                        return startPosition2;
                                    }
                                };
                            }
                        };
                    }
                });
            }
        }
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
        ByteBuffer bb = bb(bytes, operations);
        return UTF_8.decode(bb).toString();
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

    /**
     * convenience method
     *
     * @param src
     * @param operations
     * @return
     */
    public static <T extends CharSequence> ByteBuffer bb(T src, UnaryOperator<ByteBuffer>... operations) {
        return bb(u8tf.c2b(String.valueOf(src)), operations);
    }

    public static ByteBuffer grow(ByteBuffer src) {
        return allocateDirect(src.capacity() << 1).put(src);
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
            while (slice.hasRemaining() && isDigit(b = ((ByteBuffer) slice.mark()).get())) ;
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
                    if (!isDigit(b)) return (ByteBuffer) slice.reset();
            }
        }
        return null;
    }

    public static Allocator getAllocator() {
        return allocator;
    }

    public static void setAllocator(Allocator allocator) {
        std.allocator = allocator;
    }


}