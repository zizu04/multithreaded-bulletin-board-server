public class GetQuery {
    public String color;          // nullable
    public Integer containsX;      // nullable
    public Integer containsY;      // nullable
    public String refersTo;        // nullable

    public boolean hasContains() {
        return containsX != null && containsY != null;
    }
}
