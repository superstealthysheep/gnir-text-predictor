// just an array with inverse indexing via hashmap
import java.util.ArrayList;
import java.util.HashMap;

public class YummyArray<T> {
    public ArrayList<T> array; // public for easy iteration
    public HashMap<T,Integer> indices;
    public int size;

    public YummyArray() {
        array = new ArrayList<T>();
        indices = new HashMap<T,Integer>();
        size = 0;
    }

    public boolean add(T new_elt) {
        if (indices.get(new_elt) != null) {
            return false;
        }   
        array.add(new_elt);
        indices.put(new_elt, size++);
        // System.out.println(indices);
        return true;
    }

    public boolean contains(T target) {
        return indices.containsKey(target);
    }

    public int indexOf(T target) {
        // System.out.println(indices);
        // System.out.println(target);
        return indices.get(target);
    }

    public T get(int index) {
        return array.get(index);
    }
}