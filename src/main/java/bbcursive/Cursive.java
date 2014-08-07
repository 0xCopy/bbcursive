package bbcursive;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static bbcursive.Cursive.std.bb;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * some kind of less painful way to do byteBuffer operations and a few new ones thrown in.
 * <p/>
 * evidence that this can be more terse than what jdk pre-8 allows:
 * <pre>
 *
 * res.add(bb(nextChunk, rewind));
 * res.add((ByteBuffer) nextChunk.rewind());
 *
 *
 * </pre>
 */
public interface Cursive {
  <T extends ByteBuffer> T f(T target);

  enum pre implements Cursive {
    duplicate {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.duplicate();
      }
    }, flip {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.flip();
      }
    }, slice {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.slice();
      }
    }, mark {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.mark();
      }
    }, reset {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.reset();
      }
    },
    /**
     * exists in both pre and post Cursive atoms.
     */
    rewind {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.rewind();
      }
    },
    /**
     * rewinds, dumps to console but returns unchanged buffer
     */
    debug {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        System.err.println("%%: " + std.str(target, duplicate, rewind));
        return target;
      }
    }, ro {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.asReadOnlyBuffer();
      }
    },

    /**
     * perfoms get until non-ws returned.  then backtracks.by one.
     * <p/>
     * <p/>
     * resets position and throws BufferUnderFlow if runs out of space before success
     */


    forceSkipWs {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        int position = target.position();

        while (target.hasRemaining() && Character.isWhitespace(target.get())) {
        }
        if (!target.hasRemaining()) {
          target.position(position);
          throw new BufferUnderflowException();
        }
        return bb(target, back1);
      }
    },
    skipWs {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        int position = target.position();

        while (target.hasRemaining() && Character.isWhitespace(target.get())) {
        }

        return bb(target, back1);
      }
    },
    toWs {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        while (target.hasRemaining() && !Character.isWhitespace(target.get())) {
        }
        return target;
      }
    },
    /**
     * @throws java.nio.BufferUnderflowException if EOL was not reached
     */
    forceToEol {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        while (target.hasRemaining() && '\n' != target.get()) {
        }
        if (!target.hasRemaining()) {
          throw new BufferUnderflowException();
        }
        return target;
      }
    },
    /**
     * makes best-attempt at reaching eol or returns end of buffer
     */
    toEol {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        while (target.hasRemaining() && '\n' != target.get()) {
        }
        return target;
      }
    },
    back1 {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        int position = target.position();
        return (T) (0 < position ? target.position(position - 1) : target);
      }
    },
    /**
     * reverses position _up to_ 2.
     */
    back2 {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        int position = target.position();
        return (T) (1 < position ? target.position(position - 2) : bb(target, back1));
      }
    }, /**
     * reduces the position of target until the character is non-white.
     */rtrim {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        int start = target.position(), i = start;
        while (0 <= --i && Character.isWhitespace(target.get(i))) {
        }

        return (T) target.position(++i);
      }
    }, skipDigits {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        while (target.hasRemaining() && Character.isDigit(target.get())) {
        }
        return target;
      }
    }
  }

  enum post implements Cursive {
    compact {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.compact();
      }
    }, reset {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.reset();
      }
    }, rewind {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.rewind();
      }
    }, clear {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.clear();
      }

    }, grow {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) std.grow(target);
      }

    }, ro {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return (T) target.asReadOnlyBuffer();
      }
    },
    /**
     * fills remainder of buffer to 0's
     */

    pad0 {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        while (target.hasRemaining()) {
          target.put((byte) 0);
        }
        return target;
      }
    },
    /**
     * fills prior bytes to current position with 0's
     */

    pad0Until {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        int limit = target.limit();
        target.flip();
        while (target.hasRemaining()) {
          target.put((byte) 0);
        }
        return (T) target.limit(limit);
      }
    }
  }

  class std {
    public static <T extends ByteBuffer> T bb(T b, Cursive... ops) {
      for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
        Cursive op = ops[i];
        b = op.f(b);
      }
      return b;
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
          bytes = operation.f(bytes);
        }
      }
      String s = UTF_8.decode(bytes).toString();
      for (Cursive operation : operations) {
        if (operation instanceof post) {
          bytes = operation.f(bytes);
        }
      }
      return s;
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
        byteBuffer = operation.f(byteBuffer);
      }
      return byteBuffer;
    }

    public static <R extends ByteBuffer, S extends ByteBuffer> R cat(S src, R dest) {
      int need = src
          .remaining(),
          have = dest.remaining();
      if (have > need) {
        return (R) dest.put(src);
      }
      dest.put((S) src.slice().limit(have));
      src.position(src.position() + have);
      return dest;
    }

    public static <T extends ByteBuffer, S extends ByteBuffer> T grow(S src) {
      return (T) ByteBuffer.allocateDirect(src.capacity() << 1).put(src);
    }
  }
}
