# streams-fs2-learning

This is a learning project for my own use.

## Ideas Demonstrated Here

### Streams of Events

When the 'outside world' interacts with this app, via SQS, HTTP or other mechanism, this is modeled as events in a `Stream[F,A]`.  To process these, we map a function(s) over the streams.  Since streams are lazy and potentially infinite they are a good fit for modeling the outside world's interactions.  This means that 90% of the program is ignorant of whether we are using HTTP, SQS, etc.

#### Pipelines

For events that arrive, they flow through a `Pipe[F,A,B]` which encapsulates the ideas of:

- Decoding the outside world's representation of some input type, e.g. `CustomerRequest` which is probably JSON from the outside world
- Invoking the desired function in the domain, e.g. `CustomerRequest => F[Option[Customer]]`
- Encoding the result, e.g. to JSON

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

## TODO

  - Finish refinement usage
  - Get github actions/terraform/aws pipeline working
    - Deploys main to ecs task
      - https://github.com/actions/starter-workflows/blob/main/deployments/aws.yml
      - https://github.com/actions/starter-workflows/blob/main/deployments/terraform.yml
  - Logging / log publishing
  - Error handling/recovery/retry  
  - SQS
  - Manage resources via cats' Resource
  - Observability
    - Where to publish metrics (cloudwatch, probably)
    - Where to visualize metrics (cloudwatch, possibly grafana later)
  - Domain Arbitraries
    - Related values
  - Property-based testing
  - Magnolia
  - Buffering
  - Interruptable
    - Some operations must finish processing
  - Check compile times
  - Use two-level cache
  - Prevent cache stampede
  - Contract (AsyncAPI?)