# gnir-text-predictor

A simple text predictor that grew out of my algebra homework. It looks back at the last `n` characters and randomly selects a character to come next. 
The probability of selecting a given completion is based on the frequency with which this completion occurs in a user-provided training text.
I bet there's a fancier name for this kind of thing, but I don't know it yet!

All of the weights gathered from the training text are stored in a `Tensor` object. 
I first naively implemented this as a multidimensional array but then (inspired by discussion of the page table in my machine architecture class) switched to a sparse array/trie for EXTREME MEMORY SAVINGS.

I may polish/clean this up more later, but this is no commitment.

The main differences between the different versions of the predictor (huh, I wonder if there were some special software I could use to control all the versions of my software...)
are just the implementation of the `Tensor` class. 
1. In `TextPredictor.java`, the tensor is straight up just a 3-dimensional array.
2. In `FancyPredictor.java`, it's a cool recursive type allowing for an arbitrary-dimension array. 
    - Also there's um also the whole text of the Great Gatsby as a string :)
3. In `FancyPredictor2.java`, the trie is implemented! But other stuff is still broken
4. In `FancyPredictor3.java`, everything else should be compatible and work now? Also did some optimizing so stuff runs much quicker now.
