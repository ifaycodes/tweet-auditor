## TRADEOFFS
### - Architecture choice: 
I decided to go modular using maven/sprinngBoot default setting. This way, each package has one job only. It is easy to navigate and find issues could be coming from.

### Concurrency strategy:
I used concurrent batch processing and the semaphore rate limiter as backpressure. Using a thread pool of 5 threads, I am able to run a parallel processing. Each thread still queues up to fire at at 1 request per second.

### Error handling approach
I used the fail-fast approach by having my methods throw exceptions at the higher level should anything go wrong. That and try-catches in-between to get a more detailed insight as to what exactly is failing

### Performance Vs Safety
I traded performance for safety. Using a rate limit of 1 request per second to not run into the issue of having multiples and an equal multiple fail rate. That way each process has time to complete before the next. Also with the checkpoint, in the case of a crash, it doesn't have to start over, though it has to go through checkpoint to see what is already there.

### Why this language/framework
Because I want to get familiar with enterprise patterns like concurrency, structuring, libraries and in-built services in the language