package bbcursive;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static bbcursive.std.bb;

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
    },

    /**
     * this is just a placeholder for varargs forinstance where {@link bbcursive.std#str(java.lang.Object)} presides over {@link bbcursive.std#str(WantsZeroCopy, Cursive...)}
     */
    noop {
      @Override
      public <T extends ByteBuffer> T f(T target) {
        return target;
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

}
