package bbcursive.lib;

import bbcursive.ann.Backtracking;
import bbcursive.std;
import bbcursive.vtables._edge;
import bbcursive.vtables._ptr;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import static bbcursive.std.outbox;
import static bbcursive.std.traits;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.deepToString;

/**
 * Created by jim on 1/17/16.
 */
public class anyOf_ {

    public static final EnumSet<traits> NONE_OF = EnumSet.noneOf(traits.class);

    public static UnaryOperator<ByteBuffer> anyOf(UnaryOperator<ByteBuffer>... anyOf) {


        return new UnaryOperator<ByteBuffer>() {

            @Override
            public String toString() {
                return "any"+deepToString(anyOf);
            }

            private _edge<_edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>, _ptr> successTuple;

            @Override
            public ByteBuffer apply(final ByteBuffer buffer) {
                int mark = buffer.position();
                final int[] offsets = {mark, mark};
                final Set[] flaggs = {NONE_OF};

                Optional<_edge<_edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>, _ptr>> edgePtrEdge = Arrays.stream(anyOf)/*.parallel()*/.map(new Function<UnaryOperator<ByteBuffer>, _edge<_edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>, _ptr>>() {

                    @Override
                    public _edge<_edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>, _ptr> apply(UnaryOperator<ByteBuffer> op) {


                        return new _edge_ptr_edge(buffer, offsets, flaggs, op);

                    }
                }).filter(
                        ed -> {
                            UnaryOperator<ByteBuffer> op = ed.core().location().core();
                            Integer newPosition = ed.location().location();
                            ByteBuffer byteBuffer = (ByteBuffer) ed.location().core().duplicate().position(newPosition);
                            ByteBuffer apply = op.apply(byteBuffer);
                            if (null != apply) {
                                offsets[1] = apply.position();
                                flaggs[0] = EnumSet.copyOf(std.flags.get());
                                return true;
                            }
                            return false;
                        })
                        .findFirst();

                if (edgePtrEdge.isPresent()) {
                    edgePtrEdge.ifPresent(edge_ptr_edge -> outbox.get().accept(edge_ptr_edge));
                    return (ByteBuffer) buffer.position(offsets[1]);
                }
                return null;
            }
        };
    }


    @Backtracking
    public static UnaryOperator<ByteBuffer> anyOf(CharSequence s) {
        final int[] ints = s.chars().sorted().toArray();

        return new UnaryOperator<ByteBuffer>() {


            @Override
            public String toString() {
                StringBuilder any = new StringBuilder("[");
                IntStream.of(ints).forEach(i->any.append((char)(i&0xffff)));

                return String.valueOf(any.append(']'));
            }

            @Override
            public ByteBuffer apply(ByteBuffer b) {
                if (null != b && b.hasRemaining()) {
                    byte b1 = b.get();
                    if (-1 < binarySearch(ints, b1 & 0xff)) return b;
                }
                return null;
            }
        };
    }

    private static class _edge_ptr_edge extends _edge<_edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>, _ptr> {
        private final ByteBuffer buffer;
        private final int[] offsets;
        private final Set[] flaggs;
        private final UnaryOperator<ByteBuffer> op;

        public _edge_ptr_edge(ByteBuffer buffer, int[] offsets, Set[] flaggs, UnaryOperator<ByteBuffer> op) {
            this.buffer = buffer;
            this.offsets = offsets;
            this.flaggs = flaggs;
            this.op = op;
        }

        @Override
        protected _ptr at() {
            return r$();
        }

        @Override
        protected _ptr goTo(_ptr ptr) {
            throw new Error("trifling with an immutable pointer");
        }

        /**
         * this binds a pointer to a pair of ByteBuffer and Integer.  note the bytebuffer is mutated by this
         * operation and will corrupt the source stream if this isn't a slice or a duplicate
         *
         *
         * @return the _ptr
         */
        @Override

        protected _ptr r$() {

            return (_ptr) new _ptr().bind(
                    (ByteBuffer) buffer.duplicate().position(offsets[1]), offsets[0]);
        }

        @Override
        public _edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>> core(_edge<_edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>, _ptr>... e) {
            return new _edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>() {
                @Override
                public Set<traits> core(_edge<Set<traits>, _edge<UnaryOperator<ByteBuffer>, Integer>>... e) {
                    return flaggs[0];
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
                            return op;
                        }

                        @Override
                        protected Integer r$() {
                            return offsets[1];
                        }
                    };
                }
            };
        }
    }
}

