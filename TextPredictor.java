import java.lang.Math;

public class TextPredictor {
    public static void main(String[] args) throws Exception {
        YummyArray<Character> alphabet = new YummyArray<Character>();
        char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ ".toCharArray();
        // char[] letters = "cG".toCharArray();
        for (char l : letters) alphabet.add(l);

        char[] input = "JOHN THE CAT WENT TO THE STORE AND BOUGHT EIGHTEEN DOLLARS OF BREAD TO FEED TO HIS DOG NAMED JIM AND JIM ATE THE BREAD AND THEN JOHN THE CAT DECIDED TO REFLECT ON HIS LIFE UP UNTIL THAT POINT AND SO HE WENT ON A LONG JOURNEY TO THE MOUNTAINS AT THE EDGE OF THE WORLD WHERE A GREAT SAGE TOLD JOHN THE CAT THAT HE PROBABLY SHOULD NOT HAVE ABANDONED JIM THE DOG AT HIS HOME AND COME TO THIS LONELY MOUNTAIN THEN JOHN THE CAT WEPT AND SOBBED AND CRIED THE END".toCharArray();
        // char[] input = "ccGGGGGccGGGcGcccGcGGGGGGcGGGcG".toCharArray();
        float[][][] weights = populateTensor(input, alphabet);

        // print tensor
        // for (int i = 0; i < alphabet.size; i++) {
        //     for (int j = 0; j < alphabet.size; j++) {
        //         for (int k = 0; k < alphabet.size; k++) {
        //             System.out.println("" + alphabet.get(i) + alphabet.get(j) + alphabet.get(k) + ": " + weights[i][j][k]);
        //         }
        //     }
        // }

        // System.out.println(score(input, weights, alphabet));

        char[] seed = new char[2];
        seed = "JO".toCharArray();
        System.out.print(seed);
        for (int i = 0; i < 1000; i++) {
            char next = nextChar(seed, weights, alphabet);
            System.out.print(next);
            seed[0] = seed[1];
            seed[1] = next;
        }
    }

    // public static float[][][] populateTensor(Character[] input, Character[] ) {
    //     int al = alphabet.length;
    //     float[][][] result = new Character[al][al][al]
    // }

    public static float[][][] populateTensor(char[] input, YummyArray<Character> alphabet) throws Exception {
        float[][][] result = new float[alphabet.size][alphabet.size][alphabet.size];

        float[][] denominator = new float[alphabet.size][alphabet.size];

        // tally up all the groups of three characters
        for (int pos = 2; pos < input.length; pos++) {
            // System.err.println(pos);
            int i, j, k;
            try {
                i = alphabet.indexOf(input[pos-2]);
                j = alphabet.indexOf(input[pos-1]);
                k = alphabet.indexOf(input[pos]);
            } catch (NullPointerException e) {
                throw new Exception("Encountered foreign letter near character " + pos);
            }

            result[i][j][k]++;
            denominator[i][j]++;
        }

        // normalize so that the sum over k of result[i][j][k] = 1 forall i,j
        for (int i = 0; i < alphabet.size; i++) {
            for (int j = 0; j < alphabet.size; j++) {
                for (int k = 0; k < alphabet.size; k++) {
                    result [i][j][k] = (denominator[i][j] != 0 ?
                        result[i][j][k] /= denominator[i][j] :
                        (float) 1 / alphabet.size); // could optimize but nah
                } 
            }
        }
        return result;
    }
    // public static int ()

    public static float score(char[] input, float[][][] weights, YummyArray<Character> alphabet) throws Exception {
        float score = 0;
        for (int pos = 2; pos < input.length; pos++) {
            int i, j, k;
            try {
                i = alphabet.indexOf(Character.valueOf(input[pos-2]));
                j = alphabet.indexOf(Character.valueOf(input[pos-1]));
                k = alphabet.indexOf(Character.valueOf(input[pos]));
            } catch (NullPointerException e) {
                throw new Exception("Encountered foreign letter near character " + pos);
            }
            score += Math.log(weights[i][j][k])/Math.log(2);
        }

        return score;
    }

    public static char nextChar(char[] seed, float[][][] weights, YummyArray<Character> alphabet) throws Exception {
        int i, j;
        try {
            i = alphabet.indexOf(Character.valueOf(seed[0]));
            j = alphabet.indexOf(Character.valueOf(seed[1]));
        } catch (NullPointerException e) {
            throw new Exception("Seed " +  " contains foreign letter");
        }
        float[] column = weights[i][j];
        float accum = 0;
        double rand = Math.random();
        for (int idx = 0; idx < alphabet.size; idx++) {
            accum += column[idx];
            if (accum >= rand) {
                return alphabet.get(idx);
            }
        }
        return alphabet.get(alphabet.size-1); // in case everything barely doesn't add up
    }
}