package bbcursive;

import bbcursive.Cursive.post;
import bbcursive.Cursive.pre;
import com.databricks.fastbuffer.ByteBufferReader;
import com.databricks.fastbuffer.JavaByteBufferReader;
import com.databricks.fastbuffer.UnsafeDirectByteBufferReader;
import com.databricks.fastbuffer.UnsafeHeapByteBufferReader;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static bbcursive.Cursive.pre.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
* Created by jim on 8/8/14.
*/
public class std {
  public static <T extends ByteBuffer> T bb(T b, Cursive... ops) {
    for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
      Cursive op = ops[i];
      b = op.f(b);
    }
    return b;
  }
  public static <T extends ByteBuffer,S extends WantsZeroCopy> T bb(S b, Cursive... ops) {
    ByteBuffer b1 = b.asByteBuffer();
    for (int i = 0, opsLength = ops.length; i < opsLength; i++) {
      Cursive op = ops[i];
      b1 = op.f(b1);
    }
    return (T) b1;
  }

  public static <T extends ByteBufferReader,S extends WantsZeroCopy> T fast(S zc) {
    return fast(zc.asByteBuffer());
  }

  public static <T extends ByteBufferReader> T fast(ByteBuffer buf) {
    T r;
    try {
      r=(T) (buf.hasArray() ? new UnsafeHeapByteBufferReader(buf) : new UnsafeDirectByteBufferReader(buf));
    } catch (UnsupportedOperationException e) {
      r=(T) new JavaByteBufferReader(buf);
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
   * just saves a few chars
   * @param something toString will run on this
   * @param atoms
   * @return
   */
  public static String str(WantsZeroCopy something, Cursive... atoms) {
    return str(something.asByteBuffer(),atoms);
  }
/**
   * just saves a few chars
   * @param something toString will run on this
   * @param atoms
   * @return
   */
  public static String str(AtomicReference<?extends WantsZeroCopy> something, Cursive... atoms) {
    return str(something.get(),atoms);
  }

  /**
   * just saves a few chars
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
  public static <T extends CharSequence> ByteBuffer bb(T src, Cursive... operations) {

    ByteBuffer byteBuffer = UTF_8.encode(src.toString());
    for (Cursive operation : operations) {
      byteBuffer = operation.f(byteBuffer);
    }
    return byteBuffer;
  }

  /**
   *
   * @param src
   * @param dest
   * @param <R>
   * @param <S>
   * @return
   */

  public static <R extends ByteBuffer, S extends ByteBuffer> R push(S src, R dest) {
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

  ;

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
      bb(wantsZeroCopy.asByteBuffer(),debug);
    }else
    {
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
    if (1 < src.length) {
      for (int i = 0, payloadLength = src.length; i < payloadLength; i++) {
        ByteBuffer byteBuffer = src[i];
        total += byteBuffer.remaining();
      }
      cursor = ByteBuffer.allocateDirect(total);
      for (int i = 0, payloadLength = src.length; i < payloadLength; i++) {
        ByteBuffer byteBuffer = src[i];
        cursor.put(byteBuffer);
      }
      cursor.rewind();
    } else {
      cursor = src[0];
    }
    return cursor;
  }
}
