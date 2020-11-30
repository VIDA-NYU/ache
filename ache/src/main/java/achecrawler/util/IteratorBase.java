package achecrawler.util;

public abstract class IteratorBase<I> {

    protected CloseableIterator<I> it;

    public IteratorBase(CloseableIterator<I> it) {
        this.it = it;
    }

    public void close() throws Exception {
        it.close();
    }

    public boolean hasNext() {
        return it.hasNext();
    }

    public void remove() {
        it.remove();
    }

}
