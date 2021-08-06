# Dixa Backend Engineer test 💜

## Implementation 

I haven't worked with gRPC before, so after a bit of Googling it seemed like Akka would fit my needs for this assignment, and it has support for producing/consuming streams as well and a lot of documentation. I used the [Akka gRPC Quickstart with Scala](https://developer.lightbend.com/guides/akka-grpc-quickstart-scala/index.html) as a basis for the project, since I don't have a lot of experience with either gRPC or Akka, and then stitched it together by understanding the codebase and borrowing from previous knowledge and resources online. 

The project has three main components: 

### [PrimesProxy](./src/main/scala/com/dixa/primes/PrimesProxy.scala)

This service is responsible for starting at HTTP server at localhost:8081 which exposes the primes GET endpoint for calculating prime numbers up to a certain number. It handles inputs and streams the resulting primes (in the specified CSV format) which it gets from an RPC call to the PrimesServer. 

### [PrimesServer](./src/main/scala/com/dixa/primes/PrimesServer.scala)

This service is responsible for starting up the gRPC connection which exposes the PrimesService and allows communication with the PrimesProxy to get primes. 

### [PrimesClient](./src/main/scala/com/dixa/primes/PrimesClient.scala)

This is essentially just a script which makes a GET request to the proxy server for a given limit (taken in as a command line argument). The script then reads and decodes the incoming stream and then prints each received prime to the console.

## How to run

The project comes bundled with an sbt distribution.

1. From the project root, compile the project by entering:

```bash
./sbt compile
```

2. Start the PrimesServer: 

```bash
./sbt "runMain com.dixa.primes.PrimesServer"
```

3. Open another terminal in the project root, start the PrimesProxy

```bash
./sbt "runMain com.dixa.primes.PrimesProxy"
```

4. Either
    - a) Open another terminal in the project root, run the PrimesClient with a given number:
    ```bash
    ./sbt "runMain com.dixa.primes.PrimesClient <NUMBER>" 
    ```
    - b) Open a browser and navigate to http://localhost:8080/10


## How to test 

```bash
./sbt test
```

## Explanation of design choices

I decided to keep the calculation of prime numbers quite simple, the algorithms used can be found in [Primes.scala](./src/main/scala/com/dixa/primes/Primes.scala). What's perhaps inefficient about this implementation is that it doesn't keep state of the prime numbers calculated, so new incoming requests will be re-calculating primes. I decided to keep the service stateless for the sake of simplicity. Another improvement would be to perhaps use Akka's Actor Model for implementing a concurrent algorithm for primes, but that seemed a bit out of scope for this assignment.

I wrote some tests to check that the gRPC service worked well and responded with the correct results. This was more unit testing, but I could extend this by adding full integration tests. 

I spent most of my time just getting gRPC and the streaming to work well, which was a bit of a hassle at times. Many of the codebases I've worked in before have a lot of examples and setup already in place, so it was interesting getting to stitch things together myself for this assignment. 

I tried to remove the included security boilerplate, but ended up having too many issues so I just left it even though it bloated the codebase a little.

It's an interesting technology and assignment and I think I learned a lot from touching so many different areas! 

Thanks, hope you guys like my solution! Looking forward to discussing with you 😄