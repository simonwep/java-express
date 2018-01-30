package express.multipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class MultiPartStream {

  private final ByteArrayOutputStream BUFFER = new ByteArrayOutputStream();
  private final InputStream IS;
  private final byte[] BOUNDARY;
  private final long MAX_SIZE;

  public MultiPartStream(InputStream body, byte[] boundary, long maxSize) {
    this.IS = body;
    this.BOUNDARY = boundary;
    this.MAX_SIZE = maxSize;
  }

  public MultiPartData read() throws IOException {
    byte[] buffer = new byte[BOUNDARY.length];
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    String head = null;

    int i;      // Read index
    int il = 0; // Last byte
    int boundaryIndex = 0; // Boundary index to detect boundary

    // Read up to next boundary
    while ((i = IS.read()) != -1) {

      // Check if the byte is a boundary start
      if ((byte) i == BOUNDARY[boundaryIndex]) {
        buffer[boundaryIndex] = (byte) i;
        boundaryIndex++;

        // Check if the boundary is fully matched
        if (boundaryIndex == BOUNDARY.length)
          break;

      } else {

        // Check if the buffer contains data
        if (boundaryIndex > 0)
          b.write(Arrays.copyOf(buffer, boundaryIndex - 1));
        b.write(i);

        // Check if the end of the head is reached
        if (head == null && i == '\n' && il == '\r') {
          head = new String(b.toByteArray());
          b.reset();
        } else if (head != null) {

          // Check if there is an limit
          if (MAX_SIZE > 0 && b.size() > MAX_SIZE) {
            return new MultiPartData(MultiPartStatus.OUT_OF_SIZE, head, new byte[0]);
          }
        }

        // Reset index, save last byte
        boundaryIndex = 0;
        il = i;
      }
    }

    // Check if the end is reached
    if (i == -1)
      return null;

    if (b.size() == 0)
      return read();

    return new MultiPartData(MultiPartStatus.OK, head, b.toByteArray());
  }


}
