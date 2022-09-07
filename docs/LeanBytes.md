# Lean Bytes: Handling Null Values

How I deal with null values and other junk from the 'outside word'.


- View HTTP requests, SQS messages, cron events, etc. through a _common abstraction_
  - The outside world can't send scala values, so they send bytes
  - No compiler exists, so we need contracts!
  - Any interaction with the outside world starts with a decoding function like: `String => Try[CustomerRequest]`
- Model missing values with `Option`
- View missing and loosely typed values as complexity
  - Review cyclomatic complexity
  - Real world example
- Minimize complexity by aggressively typing contracts
  - Required is the default
  - Ask: do we really need this value?
  - Usually use strictest types possible, e.g. UUID
- Treat decoding/anticorruption layer as holy
- Scala 3 support for 'explicit nulls' will hopefully put the nail in the coffin










case class Foo(bar: Bar, baz: Baz, biz: Option[Biz])

| Bar | Baz | Biz |
| N   | N   | N   |
| N   | N   | Y   |
| N   | Y   | N   |
| N   | Y   | Y   |
| Y   | N   | N   |
| Y   | N   | Y   |
| Y   | Y   | N   |
| Y   | Y   | Y   |


2^N cases

case class CustomerRequest(id: UUID)

case class SessionContext(id: UUID, teamId: Option[UUID], teamName: Option[String], teamCreationTimestamp: Option[Instant])

case class Team(teamId: UUID, teamName: String, teamCreationTimestamp: Instant)

case class SessionContext(id: UUID, team: Option[Team])

























