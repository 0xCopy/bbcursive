package hacks;

import bbcursive.Cursive;
import bbcursive.std;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static bbcursive.Cursive.pre.*;
import static hacks.Utils.*;
import static hacks.advanceTo.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.EnumSet.of;

public enum Narsive implements Cursive {
    task {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, opt(budget), sentence);
        }
    },
    sentence() {


        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return $(buffer, anyOf(judgement, goal, question, desire));
        }
    },
    judgement {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, statement, dot, opt(tense), opt(truth));
        }
    },
    goal {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, exclamation, opt(truth));
        }
    },
    desire{
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, amp, opt(tense));
        }
    },
    question {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, questionMark, opt(tense));
        }
    },

    listEnd {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return null;//todo: never
        }
    },
    relationship {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, lt, term, copula, term, gt);
        }
    },
    statement {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return terminatingOr(buffer, of(relationship, operation, term));
        }
    }, tense {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return $(buffer, anyOf(Tense.values()));
        }

    },
    truth {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, percent, frequency, percent, frequency, opt(semicolon, confidence), percent);
        }
    },
    budget {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, dollarSign, priority, opt(semicolon, durability), dollarSign);
        }
    },
    copula {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return $(buffer, anyOf(Copula.values()));
        }
    }, term {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {

            return $(buffer, anyOf(word, variable, compoundTerm, statement));
        }
    },

    operation {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return $(buffer, new ListParser("(^", word, term, ")"));
        }
    },

    variable {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return $(buffer, anyOf(NarVar.values()));
        }
    }, compoundTerm {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return $(buffer, new ListParser("(", conjunction, term, ")"));

        }
    }, conjunction {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return $(buffer, anyOf(Conjunction.values()));
        }
    },
    frequency{
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, numeric);
        }
    }, confidence {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, numeric);
        }
    }, priority {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, numeric);
        }
    }, durability {
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            return $(byteBuffer, numeric);
        }
    },
    quotedString {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            $(buffer, quot, b -> {
                consumeString(b);
                return b;
            });
            return buffer;
        }
    },
    numeric{
        @Override
        public ByteBuffer apply(ByteBuffer byteBuffer) {
            byte b = ((ByteBuffer) byteBuffer.mark()).get();

            boolean sign = b == '-' || b == '+';
            if (!sign) byteBuffer.reset();

            boolean dot1 = false;
            boolean etoken = false;
            boolean esign = false;
            while (byteBuffer.hasRemaining()) {
                int c=0;
                while (byteBuffer.hasRemaining() && Character.isDigit(b = ((ByteBuffer) byteBuffer.mark()).get())) c++;

                switch (b) {
                    case '.':
                        assert !dot1 : "extra dot";
                        dot1 = true;
                    case 'E':
                    case 'e':
                        assert !etoken : "missing digits or redundant exponent";
                        etoken = true;
                    case '+':
                    case '-':
                        assert !esign : "bad exponent sign";
                        esign = true;
                    default:
                        if (!Character.isDigit(b))
                            return c > 0 ? $(byteBuffer, byteBuffer.hasRemaining() ? back1 : noop): null;
                }
            }
            return null;
        }
    },
    word {
        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            return $(buffer, anyOf(buf -> {
                int c = 0;
                while (buf.hasRemaining() && isValidAtomChar(buf.get())) c++;
                return c > 0 ? $(buf, back1) : null;
            }, quotedString));
        }
    },
    ;

    private static Cursive anyOf(Cursive... anyOf) {

        return b -> {
            for (Cursive o : anyOf) {
                ByteBuffer bb = $(b, o);
                if (null != bb) {
                    return bb;
                }
            }

            return null;
        };
    }

    private static Cursive zeroOrMore(Cursive sep, Narsive listNode, ArrayList<Integer> integers) {

        return buf -> {
            int rollback = buf.position();
            int c = 0;

            while (null != $(buf, sep)) {
                int position = buf.position();
                if (null == $(buf, listNode)) return $(buf, pos(rollback), null);

                integers.add(position);
                c++;
            }

            return c > 0 ? buf : $(buf, pos(rollback), null);
        };

    }

    public static Cursive confix(String begin, Cursive clause, String end, AtomicInteger contentIndex) {
        return buf -> {
            int position = $(buf, skipWs, mark).position();
            ByteBuffer gotBegin = genericAdvance(buf, begin.getBytes());
            if (null != gotBegin) {
                contentIndex.set(buf.position());
                if (null != $(buf, clause, skipWs)) {
                    ByteBuffer byteBuffer = genericAdvance(buf, end.getBytes());
                    if (null != byteBuffer) return buf;
                }
            }
            return $(buf, pos(position), null);
        };

    }


    public static final int MASK24BITS = 0xffffff;


    void recordFeature(int position) {
        features.put(ordinal() << 24 | position & 0xffffff);
    }


    static IntBuffer features = IntBuffer.allocate(1000); //8/24 bit flags/offsets


    ByteBuffer terminatingOr(ByteBuffer buffer, Iterable<? extends Cursive> judgement) {
        buffer = $(buffer, skipWs, mark);
        int position = buffer.position();
        for (Cursive cursive : judgement) {
            ByteBuffer bb = $(buffer, reset, cursive);
            if (null != bb) {
                recordFeature(position);
                listEnd.recordFeature(bb.position());
                return bb;
            }
        }
        return null;
    }

    private static class Constants {
        private static ListParser compundListParser;
    }


    private class ListParser implements Cursive {

        private final String begin;
        private final Narsive firstFeature;
        private final Narsive listNode;
        private final String end;

        public ListParser(String begin, Narsive firstFeature, Narsive listNode, String end) {
            this.begin = begin;
            this.firstFeature = firstFeature;
            this.listNode = listNode;
            this.end = end;
        }


        @Override
        public ByteBuffer apply(ByteBuffer buffer) {
            AtomicInteger middlePosition = new AtomicInteger();
            ArrayList<Integer> integers = new ArrayList<>();
            int rollback = buffer.position();
            if (null != $(buffer, confix(begin, buf -> $(buf, firstFeature, zeroOrMore(comma, listNode, integers)), end, middlePosition))) {
                recordFeature(rollback);
                firstFeature.recordFeature(middlePosition.get());
                for (Integer integer : integers) listNode.recordFeature(integer);
                listEnd.recordFeature(buffer.position());
                return buffer;
            }
            return std.bb(buffer, pos(rollback), null);
        }
    }

    public static void main(String[] args) {
        String input = "(a --> b)";
        ByteBuffer encode = UTF_8.encode(input);


    }

    public static Cursive opt(Cursive... allOrPrevious) {
        return byteBuffer -> {

            ByteBuffer bb = $(byteBuffer, allOrPrevious);
            return null == bb
                    ? byteBuffer : bb;
        };
    }

    public static ByteBuffer $(ByteBuffer b, Cursive... allOrNull) {
        int position = b.position();

        ByteBuffer bb = std.bb(
                b, allOrNull
        );
        return bb != null ? bb : std.bb(b, pos(position), null);
    }
}
