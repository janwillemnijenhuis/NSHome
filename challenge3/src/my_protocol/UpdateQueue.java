package my_protocol;

import java.util.ArrayList;

public class UpdateQueue<E> extends ArrayList<E> {
    private int size;

    public UpdateQueue(int size) {
        this.size = size;
    }

    public boolean add(E number) {
        boolean r = super.add(number);
        if (this.size() > this.size) {
            removeRange(0, this.size() - this.size);
        }
        return r;
    }

    public int getAmt(E number) {
        int count = 0;
        for (E i: this) {
            if (i == number) {
                count++;
            }
        }
        return count;
    }

}
