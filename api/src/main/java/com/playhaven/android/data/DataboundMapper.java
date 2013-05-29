/**
 * Copyright 2013 Medium Entertainment, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.playhaven.android.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.playhaven.android.PlayHaven;

import java.io.IOException;

/**
 * For Databound JSON
 */
public class DataboundMapper
    extends ObjectMapper
{

    private static final long serialVersionUID = -862133437038975726L;

    /**
     * Default constructor, which will construct the default
     * {@link com.fasterxml.jackson.core.JsonFactory} as necessary, use
     * {@link com.fasterxml.jackson.databind.SerializerProvider} as its
     * {@link com.fasterxml.jackson.databind.SerializerProvider}, and
     * {@link com.fasterxml.jackson.databind.ser.BeanSerializerFactory} as its
     * {@link com.fasterxml.jackson.databind.ser.SerializerFactory}.
     * This means that it
     * can serialize all standard JDK types, as well as regular
     * Java Beans (based on method names and Jackson-specific annotations),
     * but does not support JAXB annotations.
     */
    public DataboundMapper() {
        super();
        addHandler(getDeserializationProblemHandler());
    }

    /**
     * Set a handler for Jackson deserialization
     *
     * @return a handler
     * @see ObjectMapper#addHandler(com.fasterxml.jackson.databind.deser.DeserializationProblemHandler)
     */
    private DeserializationProblemHandler getDeserializationProblemHandler()
    {
        return new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName)
                    throws IOException, JsonProcessingException
            {
                return handleUnknownJsonProperty(ctxt, jp, deserializer, beanOrClass, propertyName);
            }
        };
    }

    /**
     * The handler for Jackson deserialization
     *
     * @return a handler
     * @see ObjectMapper#addHandler(com.fasterxml.jackson.databind.deser.DeserializationProblemHandler)
     */
    protected boolean handleUnknownJsonProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName)
            throws IOException, JsonProcessingException
    {
        if(beanOrClass == null)
            PlayHaven.d("unknown property: %s", propertyName);
        else
            PlayHaven.d("unknown property (%s): %s", beanOrClass.getClass().getSimpleName(), propertyName);

        return true;
    }
}
