## 1.1.1
* [jadler-junit](https://github.com/jadler-mocking/jadler/blob/42a429ca31ae12c36ed8faf90014c91fa7a1e3c8/jadler-junit/pom.xml) artifact added, contains specific support for jUnit testing
* [net.jadler.junit.rule.JadlerRule](https://github.com/jadler-mocking/jadler/blob/42a429ca31ae12c36ed8faf90014c91fa7a1e3c8/jadler-junit/src/main/java/net/jadler/junit/rule/JadlerRule.java) jUnit rule added for easy Jadler lifecycle management

## 1.1.0
* stub http responses can now be defined dynamically using the [net.jadler.stubbing.Responder](https://github.com/jadler-mocking/jadler/blob/0dff7a0c8cbfd07d5e3f54a5f87f94e1ede021bc/jadler-core/src/main/java/net/jadler/stubbing/Responder.java) interface via the new [net.jadler.stubbing.RequestStubbing#respondUsing(Responder)](https://github.com/jadler-mocking/jadler/blob/0dff7a0c8cbfd07d5e3f54a5f87f94e1ede021bc/jadler-core/src/main/java/net/jadler/stubbing/RequestStubbing.java#L30) method
* a  [reset method](https://github.com/jadler-mocking/jadler/blob/0dff7a0c8cbfd07d5e3f54a5f87f94e1ede021bc/jadler-core/src/main/java/net/jadler/JadlerMocker.java#L356) added for the [net.jadler.JadlerMocker](https://github.com/jadler-mocking/jadler/blob/0dff7a0c8cbfd07d5e3f54a5f87f94e1ede021bc/jadler-core/src/main/java/net/jadler/JadlerMocker.java) class

## 1.0.0
* first stable version
* http mocking (verification) implemented: [net.jadler.Jadler#verifyThatRequest()] (https://github.com/jadler-mocking/jadler/blob/6b5338c8dc6ad64dce71aa4c8e73a424bfd869f6/jadler-core/src/main/java/net/jadler/Jadler.java#L533)
* methods for matching a request by its path renamed from `havingURI` (`havingURIEqualTo`) to more accurate `havingPath` (`havingPathEqualTo`): [net.jadler.RequestMatching#havingPathEqualTo(String)] (https://github.com/jadler-mocking/jadler/blob/6b5338c8dc6ad64dce71aa4c8e73a424bfd869f6/jadler-core/src/main/java/net/jadler/RequestMatching.java#L80)
* the method for setting a stub response delay renamed from `withTimeout` to more accurate `withDelay`: [net.jadler.stubbing.ResponseStubbing#withDelay(long, TimeUnit)] (https://github.com/jadler-mocking/jadler/blob/6b5338c8dc6ad64dce71aa4c8e73a424bfd869f6/jadler-core/src/main/java/net/jadler/stubbing/ResponseStubbing.java#L122)
* a `JadlerMocker` instance can now be disposed using a `close` method instead of a `stop` method. Once Jadler is switched to Java 7 `AutoCloseable` will be retrofitted easily: [net.jadler.JadlerMocker#close()](https://github.com/jadler-mocking/jadler/blob/6b5338c8dc6ad64dce71aa4c8e73a424bfd869f6/jadler-core/src/main/java/net/jadler/JadlerMocker.java#L138)
* a custom request abstraction introduced and used across the whole library: [net.jadler.Request](https://github.com/jadler-mocking/jadler/blob/6b5338c8dc6ad64dce71aa4c8e73a424bfd869f6/jadler-core/src/main/java/net/jadler/Request.java)

## 0.9.5
* first public version
* http stubbing implemented
