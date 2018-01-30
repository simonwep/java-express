package express.multipart;

public class MultiPartData {

  private final MultiPartStatus MPS;
  private final String HEAD;
  private final byte[] DATA;

  public MultiPartData(MultiPartStatus mps, String head, byte[] data) {
    this.MPS = mps;
    this.HEAD = head;
    this.DATA = data;
  }

  public MultiPartStatus getStatus() {
    return MPS;
  }

  public String getHead() {
    return HEAD;
  }

  public byte[] getBytes() {
    return DATA;
  }
}