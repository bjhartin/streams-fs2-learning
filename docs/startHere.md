# FS2 Learning

First, we will start with scala's `Stream`, before we learn fs2's `Stream`.

## What is a scala Stream?

A `Stream` is a data structure that at first, looks a lot like a list.  It's _ordered_, like a `List`.  It is also _potentially infinite_, unlike a `List`, so it must be _lazy_.  You can write a Stream that produces _all_ the integers, as follows:
 
```scala
val integers: Stream[Int] = {
  // A recursive function is used to produce integers endlessly.      
  def increment(i: Int): Stream[Int] = {
    val next = i + 1
    next #:: increment(next) // The '#::' operator concatenates an element with the remainder of the stream (not yet computed) 
  }
  
  increment(0)
}
``` 

We used a recursive function to generate the integers.  Notice that this is the opposite of typical recursion.  We often recurse 'down' to a base case, where we stop.  Here, we start at a base case and recurse 'up' infinitely.  Sometimes this is called 'co-recursion'.  It's also sometimes called 'unfolding'.
 
Scala's `Stream` was recently renamed to `LazyList`.

## What is an fs2 Stream?

## What are Streams good for?

Here is where things get interesting.