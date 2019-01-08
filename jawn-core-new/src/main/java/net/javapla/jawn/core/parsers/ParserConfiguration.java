package net.javapla.jawn.core.parsers;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

final class ParserConfiguration {

    public static void config(ObjectMapper mapper) {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        mapper.configure(Feature.ALLOW_COMMENTS, true);
        mapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        
        /*mapper.setDateFormat(new ISO8601DateFormat());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);*/
    }
}
