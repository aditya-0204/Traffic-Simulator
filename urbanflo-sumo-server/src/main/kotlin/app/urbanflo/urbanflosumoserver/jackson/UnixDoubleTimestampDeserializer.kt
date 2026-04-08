package app.urbanflo.urbanflosumoserver.jackson

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.math.roundToLong

/**
 * Jackson deserialization class to parse Unix timestamp encoded as [Double] to [OffsetDateTime].
 *
 * [Source/adapted from](https://stackoverflow.com/a/20638114)
 */
class UnixDoubleTimestampDeserializer : StdDeserializer<OffsetDateTime>(OffsetDateTime::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): OffsetDateTime {
        val number = p?.valueAsString?.toDouble() ?: run { throw JsonParseException("Value is null") }
        val instant = Instant.ofEpochMilli((number * 1000).roundToLong())
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
    }
}