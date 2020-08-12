### Spring Generation plugin

####Summary
Plugin allows to generate model to dto converts in kotlin. 

This open source plugin can help you to generate a lot of converters code in kotlin.
 
It uses org.springframework.core.convert.converter.Converter to define converters and matches
field names from one model to other.

### Advantages
Here are some of the advantages of this plugin:
* You can generate converters on the way. 
* You don't need to waste time and do monkey coding 
* Generated converters works faster in 5-7 times than converters on reflection


### Usage example
Image you have dto ann model

```kotlin
data class TestDto(
    val param: String,
    val param1: String
)

data class Test(
    val param: String,
    val param1: String
)
```

Next you can generate converters on the fly:
1) Click right button on the TestDto and choose "Transform In Model"
2) Than click right button on Test and choose "Transform From Model"
3) Choose package where to generate in project tree and right click on directory and choose "Generate converters"

Yahoo and you get the result:

```kotlin
package converters.test
import org.springframework.core.convert.converter.Converter
import model.Test
import model.TestDto

object TestDtoConverter: Converter<Test, TestDto> {
    override fun convert(source: Test): TestDto {
        return TestDto(
            param = source.param,
            param1 = source.param1
        )
    }   
}
```
 