# streams-fs2-learning

This is a learning project for my own use.

There is documentation.  Start [here](./docs/startHere.md)

## A Different View

1. Our programs consist of functions executing within a type system, e.g. each function is an A => B, A => F[B], or similar.

The compiler ensures that functions can't be called with, or return, the wrong types [1].

1. Most of our program is functions calling other functions, but sometimes the 'outside world' wants to call one of our functions, e.g. 

- POST /customers/123 is really the outside world wanting to call updateCustomer(c: Customer): F[Unit] 
- Likewise for SQS messages and many other things, even the `main(args:String[])` method!
- We often call these 'entry points'

1. The entry points are special, and have something in common:

- Ultimately 



1 - Excluding nulls and exceptions and infinite loops, which functional progamming prohibits precisely because they break these rules.

## TODO:

- Use bracket for resources
- 