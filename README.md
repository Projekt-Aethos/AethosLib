# AethosLib

## Content:
 - Database Support
   - Actions
   - Pooling
   - Connectors
   - Builders
 - Option Type
   - Some
   - None 
 - Result Type
   - Okay
   - Error  
 - Packages
 - ItemBuilder

### Database Support


### Options
Option is a container type that has exactly two implementations. These implementations are Some and None. Some contains a value while None is empty.

However, the value in Option cannot be accessed directly to prevent NullPointerExceptions. Instead, you must first check whether an option is of type Some in order to extract the value. To be sure, the type Some must be checked and safely cast. 
Then you can assume that no null value exists. 

There are also a number of methods that are defined for all options regardless of the underlying container. These include, for example, the Stream method.
#### Example - Create Option
```java
  Option<String> opt = Option.none();
  Option<String> opt = Option.some("Example");
```
#### Example - Check for Value
```java
  Option<String> opt = getStringOption();
  if (opt instanceof Some<String> some){
      String str = some.value();
  }
  if (opt instanceof None<String>){
      //
  }
```
#### Example - Stream
```java
  Stream<Option<String>> stream = getOptionStringStream();
  List<String> strings = stream.flatMap(Option::stream).toList();
```


### Results
Results may already be known from other languages and are based on the Either type from Scala or the type of the same name from Rust. 
The implementation is of course slightly different. Results is an interface that has two implementations. Okay or Error. 

Results are container classes that contain a value that cannot be accessed directly. To gain direct access, you can "unpack" the container by checking the type and casting it safely. In the case of a result, there are two containers to unpack: Okay and Error, each of which holds the defined type.
#### Example - Create Results
```java
  Result<String,Integer> result = Result.ok("Test");
  Result<String,Integer> result = Result.err(1);
```
#### Example - Check for Value
```java
  Result<String, Integer> result = getStringOrErrorCode();
  if (result instanceof Okay<String, Integer> okay) {
      String str = okay.value();
  }
  if (result instanceof Error<String, Integer> error) {
      int i = error.error();
  }
```
#### Example - Stream
```java
  Stream<Result<String, Integer>> stream = Stream.of(Result.ok("Hello"), Result.err(1), Result.ok("World"));
  List<String> strings = stream.flatMap(Result::streamOkay).toList();
  System.out.println(strings); //["Hello","World"]
```
### 
