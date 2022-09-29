# streams-fs2-learning

This is a learning project for my own use.

See 'Attributions' for information about sources from which I've borrowed ideas or code.

## Ideas Demonstrated Here

### Streams of Events

When the 'outside world' interacts with this app, via SQS, HTTP or other mechanism, this is modeled as events in a `Stream[F,A]`.  To process these, we map a function(s) over the streams.  Since streams are lazy and potentially infinite they are a good fit for modeling the outside world's interactions.  This means that 90% of the program is ignorant of whether we are using HTTP, SQS, etc.

#### Event 'Pipeline'

For all such events, the pattern of handling them is common.

The outside world wants to invoke some `A => F[B]`, so we must:

- Decode their 'message type', i.e. _which function_ do they want to invoke? _This can fail_.
- Choose the appropriate function, determining the types involved - some `A => F[B]`.
- Decode the `A` from their request.  _This can fail_.
  - Past this point we shouldn't see errors related to invalid events and all values are well-typed and appropriate.
- Invoke the function, hopefully getting a `B` (or failure).
- _Encode the B_ for the outside world's consumption.  Usually, this can't fail.

This encoding/decoding is very important because this is where we avoid the 'messiness' of the outside world, e.g. we convert a message to our domain by:

- Using appropriate names and types
- Discarding fields we don't care about
- Deduplicating redundant fields
- Modeling optional fields explicitly via `Option[A]`

#### Codecs

By making the encoding/decoding a central concept, we ensure we give thought to it.  This can reduce a lot of edge cases we otherwise must handle, e.g. without careful decoding we might have `Customer(a: Option[String], b: Option[String])` whereas we really want `Customer(name: Name, email: EmailAddress)` or similar.

### Refinement Types

Instead of allowing unbounded and unrestricted types like `String` in our program, we use the `refined` library so we can write things like `type Name = String Refined AlphaNumeric And NonEmpty And MaxLength[25]`.  Now we can write `Customer(name: Name, ...)`.  Since many attacks involve string injection, we prevent this in the domain.  Why would we want to admit code into the name field?  We'll do similar things for types like `List` and others.  This will also make test data generation more accurate.

Types which represent input from the outside world will have less restrictions.

Unrefined values are refined into these types via functions which represent failure in their return type.

## Attributions

- Terraform ECS/Fargate setup resource: https://engineering.finleap.com/posts/2020-02-20-ecs-fargate-terraform
- fs2 guide: https://fs2.io/#/guide
- fs2 article: https://www.baeldung.com/scala/fs2-functional-streams

## TODO
  
  - Get github actions/terraform/aws pipeline working
    - Create the ECS task    
    - Need IAM policies/roles
  - Logging / log publishing    
  - Observability
    - Where to publish metrics (cloudwatch, probably)
    - Where to visualize metrics (cloudwatch, possibly grafana later)
    - thread observation (jmx?)
  - SQS
  - Enlarge the concept of Processor to be parameterized on request/response, so that it can be used in HTTP4s
  - Contract (AsyncAPI?)
  - Use two-level cache
  - Buffering
  - Domain Arbitraries
    - Related values
  - Magnolia
  - Interruptable
    - Some operations must finish processing
  - Prevent cache stampede
  - Property-based testing

