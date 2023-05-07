// like TextPredictor.java but with adjustable context length
// maybe later I'll make the tokens be larger than characters too
import java.lang.Math;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;
import java.lang.StringBuilder;
import java.util.Stack;

public class FancyPredictor2 {
    public static void main(String[] args) throws Exception {
        YummyArray<Character> alphabet = new YummyArray<Character>();
        // char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ ".toCharArray();
        char[] letters = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ 1234567890[]().,?!;:'-\n\"").toCharArray(); // gatsby alphabet
        // char[] letters = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ []().,?!;:'-\n\"").toCharArray(); // gatsby alphabet
        // char[] letters = "cG".toCharArray();
        for (char l : letters) alphabet.add(l);

        // char[] input = "JOHN THE CAT WENT TO THE STORE AND BOUGHT EIGHTEEN DOLLARS OF BREAD TO FEED TO HIS DOG NAMED JIM AND JIM ATE THE BREAD AND THEN JOHN THE CAT DECIDED TO REFLECT ON HIS LIFE UP UNTIL THAT POINT AND SO HE WENT ON A LONG JOURNEY TO THE MOUNTAINS AT THE EDGE OF THE WORLD WHERE A GREAT SAGE TOLD JOHN THE CAT THAT HE PROBABLY SHOULD NOT HAVE ABANDONED JIM THE DOG AT HIS HOME AND COME TO THIS LONELY MOUNTAIN THEN JOHN THE CAT WEPT AND SOBBED AND CRIED THE END".toCharArray();
        BufferedReader br =  new BufferedReader(new FileReader("01 - The Fellowship Of The Ring.txt"));
        // final int BUF_SIZE = 10_000;
        StringBuilder sb = new StringBuilder();
        while(br.ready()) {
            char c = (char) br.read();
            if (c != '\\' && c != '\r') {
                sb.append(c);
            }
        }
        br.close();
        char[] input = sb.toString().toUpperCase().toCharArray();
        
        // char[] input = """
// """.toUpperCase().toCharArray();
        // char[] input = "ccGGGGGccGGGcGcccGcGGGGGGcGGGcG".toCharArray();

        int contextSize = 8;
        Tensor weights = populateTensor(input, alphabet, contextSize);

        

        // print tensor
        // System.out.println(weights.toString());
        
        // System.out.println(score(input, weights, alphabet));

        // char[] seed = new char[contextSize-1];
        char[] seed = "FAIR IS".toCharArray();
        System.out.print(seed);
        for (int i = 0; i < 10000; i++) {
            char next = nextChar(seed, weights, alphabet);
            System.out.print(next);

            for (int j = 0; j < contextSize-2; j++) {
                seed[j] = seed[j+1];
            }
            seed[contextSize-2] = next;
        }
    }

    private static class Tensor { // now it's a sparse array/tree!
        int dimension;
        int sideLength;
        Tensor[] array;
        float value; // only populated for 0-dimensional tensors
        private class TensorException extends Exception {
            public TensorException(String msg) {
                super(msg);
            }
        }

        private class TensorSubtreeException extends Exception {
            public TensorSubtreeException(String msg) {
                super(msg);
            }
        }

        public Tensor(int dimension, int sideLength) throws TensorException {
            this.dimension = dimension;
            this.sideLength = sideLength;
            if (dimension < 0) {
                throw new TensorException("Cannot create tensor with dimension less than 0");
            }
            if (dimension == 0) {
                array = null;
                value = 0; // this is unnecessary but I'll write it out explicitly
            } else {
                array = new Tensor[sideLength];
                // for (int i = 0; i < sideLength; i++) {
                //     array[i] = new Tensor(dimension-1, sideLength);
                // }
            }
        }

        public boolean hasSubarray(int[] indices) throws TensorException {
            // if (indices.length > dimension) {
            //     throw new TensorException("Index depth " + indices.length + " is deeper than tensor dimension " + dimension);
            // }
            // Tensor ptr = this;
            // for (int idx : indices) {
            //     Tensor child = ptr.array[idx];
            //     if (child == null) {
            //         return false;
            //     }
            // }
            // return true;
            return (getSubarray(indices) != null);
        }

        // given `indices`, ensure that a subtree with this path exists.
        // general method: start from the root of the tree, then walk down along the path, adding new subtrees as needed.
        // returns the touched subtree.
        private Tensor touchSubarray(int[] indices) throws TensorException {
            if (indices.length > dimension) {
                throw new TensorException("Index depth " + indices.length + " is deeper than tensor dimension " + dimension);
            }
            Tensor leaf = this;
            for (int idx : indices) { // walk down the path, creating new subtrees as needed to reach the end
                Tensor trailer = leaf;
                leaf = leaf.array[idx];
                if (leaf == null) { // create new subtree if it's absent
                    leaf = new Tensor(trailer.dimension-1, sideLength);
                    trailer.array[idx] = leaf;
                }
            }
            return leaf;
        }

        // given a path from the root to a certain subarray, return the subarray!
        // returns null if the subarray does not exist
        public Tensor getSubarray(int[] indices) throws TensorException  {
            if (indices.length > dimension) {
                throw new TensorException("Index depth " + indices.length + " is deeper than tensor dimension " + dimension);
            }
            Tensor result = this;
            for (int idx : indices) {
                Tensor child = result.array[idx];
                if (child == null) {
                    return null; 
                    // throw new TensorSubtreeException("Child not found!!!1!");
                }
                result = child;
            }
            return result;
        }
        
        public boolean hasValue(int[] indices) throws TensorException {
            return hasSubarray(indices); // um this is a dumb method
        }

        public float getValue(int[] indices) throws TensorException {
            if (indices.length > dimension) {
                throw new TensorException("Index depth " + indices.length + " exceeds tensor dimension " + dimension);
            }
            Tensor zeroDim = getSubarray(indices); 
            if (zeroDim == null) {
                return Float.NaN;
            }
            return zeroDim.value;
        }

        public String toString() {
            if (dimension == 0) {return String.valueOf(value);}
            StringBuilder result = new StringBuilder();
            result.append("[");
            for (Tensor t : array) {
                if (t != null) {
                    result.append(t).append(", ");
                } else {
                    result.append("X, "); // marker for empty slot
                }

            }
            result.append("]");
            return result.toString();
        }
        
        public void setValue(int[] indices, float newValue) throws TensorException {
            if (indices.length > dimension) {
                throw new TensorException("Index depth " + indices.length + " exceeds tensor dimension " + dimension);
            }
            Tensor leaf = touchSubarray(indices);
            leaf.value = newValue;
        }

        // Returns the all of the subtrees of dimension `dimension` as an ArrayList
        // DFSes through the tensor
        public ArrayList<Tensor> iterator(int dimension) {
            ArrayList<Tensor> iterator = new ArrayList<Tensor>();
            Stack<Tensor> stack = new Stack<Tensor>();
            stack.push(this);
            while (!stack.isEmpty()) {
                Tensor top = stack.pop();
                if (top.dimension > dimension) {
                    for (Tensor child : top.array) {
                        if (child != null) {
                            stack.push(child);
                        }
                    }
                }
                if (top.dimension == dimension) {
                    iterator.add(top);
                }
            }

            return iterator;
        }
    }

    // Takes a list representing a base-`base` number and increments it. Returns `null` if overflow occurs
    // Note: modifies `input`
    private static int[] increment(int[] input, int base) {
        input[input.length-1]++;
        for (int place = input.length-1; place >= 1; place--) {
          if (input[place] < base) { break; } 
          else {
            input[place] -= base;
            input[place-1]++;
          }
          if (input[0] >= base) {
            return null; // overflow!
          }
        }
        return input;
    }


    public static Tensor populateTensor(char[] input, YummyArray<Character> alphabet, int contextSize) throws Exception {
        Tensor result = new Tensor(contextSize, alphabet.size);
        // Tensor denominators = new Tensor(contextSize-1, alphabet.size); // could just save these as the `values` of the `Tensors`
        // one level up from the leaves

        // tally up all the groups of three characters
        for (int pos = contextSize-1; pos < input.length; pos++) {
            // Find the correct tensor entry for this context
            int[] index = new int[contextSize]; // the list of indices corresponding to this symbol/token given the preceding context
            for (int i = 0; i < contextSize; i++) {
                char symbol = input[pos-(contextSize-1)+i];
                try {
                    index[i] = alphabet.indexOf(symbol);
                } catch (NullPointerException e) {
                    // for data-cleaning purposes: print characters surrounding bad character
                    for (int idx = Math.max(0,pos-10); idx < pos+10; idx++) { // TODO: fix the bounds on this
                        System.err.print(input[idx]);
                    }
                    System.err.println();
                    throw new Exception("Encountered foreign character " + symbol + " at position " + pos);
                }
            }

            // System.out.println(Arrays.toString(index));
            // increment value, denominator
            float oldValue = result.getValue(index);
            oldValue = (Float.isNaN(oldValue) ? 0F : oldValue);
            result.setValue(index, 1 + oldValue);    
            
            // the denominators are stored in the one-up-from leaf nodes
            int[] contextIndex = Arrays.copyOfRange(index, 0, contextSize-1); // just `indices` with the last index ommitted
            float oldDenom = result.getValue(contextIndex);
            oldDenom = (Float.isNaN(oldDenom) ? 0F : oldDenom);
            result.setValue(contextIndex, 1 + oldDenom);
        }

        // System.out.println(result);
        // System.out.println(denominators);
        // normalize so that the sum over k of result[i][j][k] = 1 forall i,j
        // int[] contextIndex = new int[contextSize-1]; // start off with contextIndex = [0, 0, ..., 0, 0]

        ArrayList<Tensor> columns = result.iterator(1);
        for (Tensor column : columns) { // loop through all the possible preceding contexts [0, ..., 0], [0, ..., 1], etc.
            float denom = column.value;
            if (column == null) continue; // no need to normalize non-existent columns. Columns should never be null, though.
            for (int i = 0; i < alphabet.size; i++) {
                // float old_value = colum;n.getValue()
                float new_value;
                if (denom != 0) {
                    new_value = (float) column.getValue(new int[] {i}) / denom;
                } else { // I think this should be unreachable because for any existing column, `denom` shouldn't be zero?
                    System.out.println("This should be unreachable: Normalizing column with denom = 0");
                    new_value = 1f / alphabet.size;
                }
                column.setValue(new int[] {i}, new_value);
            }
        }
        

        // for (int dim = 0; dim < contextSize-1; dim++) { 
            
        // }
        // for (int i = 0; i < alphabet.size; i++) {
        //     for (int j = 0; j < alphabet.size; j++) {
        //         float denominator =
        //         for (int k = 0; k < alphabet.size; k++) {
        //             result [i][j][k] = (denominator[i][j] != 0 ?
        //                 result[i][j][k] /= denominator[i][j] :
        //                 (float) 1 / alphabet.size); // could optimize but nah
        //         } 
        //     }
        // }
        return result;
    }
    // public static int ()

    public static float score(char[] input, Tensor weights, YummyArray<Character> alphabet) throws Exception {
        float score = 0;
        
        for (int pos = 2; pos < input.length; pos++) {
            int[] index = new int[weights.dimension]; // the list of indices corresponding to this symbol/token given the preceding context
            for (int i = 0; i < weights.dimension; i++) {
                char symbol = input[pos-(weights.dimension-1)+i];
                try {
                    index[i] = alphabet.indexOf(symbol);
                } catch (NullPointerException e) {
                    throw new Exception("Encountered foreign character " + symbol + " at position " + pos);
                }
            }
            score += Math.log(weights.getValue(index))/Math.log(2);
        }

        return score;
    }

    public static char nextChar(char[] seed, Tensor weights, YummyArray<Character> alphabet) throws Exception {
        int pos = seed.length-1;
        int contextLength = weights.dimension-1; // Here `contextLength` referse to the number of preceding symbols. 
        int[] contextIndex = new int[contextLength]; // the list of indices corresponding to this symbol/token given the preceding context
        for (int i = 0; i < contextLength; i++) {
            char symbol = seed[pos-(contextLength-1)+i];
            try {
                contextIndex[i] = alphabet.indexOf(symbol);
            } catch (NullPointerException e) {
                throw new Exception("Encountered foreign character " + symbol + " at position " + pos);
            }
        }

        Tensor column = weights.getSubarray(contextIndex);
        if (column != null) {
            float accum = 0;
            double rand = Math.random();
            for (int idx = 0; idx < alphabet.size; idx++) {
                float weight = column.getValue(new int[] {idx});
                accum += (Float.isNaN(weight) ? 0F : weight);
                if (accum >= rand) {
                    return alphabet.get(idx);
                }
            }
            return alphabet.get(alphabet.size-1); // in case everything barely doesn't add up
        } else { // if we have a never-before seen context column
            int idx = (int) (Math.random() * alphabet.size);
            return alphabet.get(idx);
        }
    }
}