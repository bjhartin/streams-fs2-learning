package streams.examples

// Scala Stream is deprecated in favor of LazyList
object ExampleScalaStreams {
  val integers: LazyList[Int] =
    FunctionsThatCreateStreams.integers

  object FunctionsThatCreateStreams {
    def integers: LazyList[Int] = {
      // A recursive function is used to produce integers endlessly.
      // This is called 'co-recursion' or 'unfolding' as it's the opposite in a way.
      // It starts from a base case and produces values, instead of reducing values to a base case.
      def increment(i: Int): LazyList[Int] = {
        val next = i + 1
        next #:: increment(next)
      }

      increment(0)
    }
  }
}
