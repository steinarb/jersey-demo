package no.priv.bang.demos.whiteboardwebapi.webapi;

public class Count {

    private int count; // NOSONAR This is just a demo

    public Count() {
        this(0);
    }

    public Count(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

}
