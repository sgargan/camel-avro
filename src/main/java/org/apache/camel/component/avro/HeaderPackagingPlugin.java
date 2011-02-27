/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.avro;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.ipc.RPCContext;
import org.apache.avro.ipc.RPCPlugin;
import org.apache.camel.TypeConverter;

class HeaderPackagingPlugin extends RPCPlugin {

    private static ThreadLocal<Map<String, Object>> headerStorage = new ThreadLocal<Map<String, Object>>();
    private TypeConverter typeConverter;

    HeaderPackagingPlugin(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    public static void setExchangeHeaders(Map<String, Object> headers) {
        headerStorage.set(headers);
    }

    public static Map<String, Object> getExchangeHeaders() {
        return headerStorage.get();
    }

    @Override
    public void clientSendRequest(RPCContext context) {
        exchangeHeadersToAvroHeaders(context.requestCallMeta());
    }

    @Override
    public void clientReceiveResponse(RPCContext context) {
        avroHeadersToExchangeHeaders(context.requestCallMeta());
    }

    @Override
    public void serverReceiveRequest(RPCContext context) {
        avroHeadersToExchangeHeaders(context.requestCallMeta());
    }

    @Override
    public void serverSendResponse(RPCContext context) {
        exchangeHeadersToAvroHeaders(context.responseCallMeta());
    }

    private void exchangeHeadersToAvroHeaders(Map<CharSequence, ByteBuffer> avroHeaders) {
        Map<String, Object> fromExchange = headerStorage.get();

        for (String key : fromExchange.keySet()) {
            Object value = fromExchange.get(key);
            if (value != null) {
                avroHeaders.put(key, typeConverter.convertTo(ByteBuffer.class, value));
            }
        }
    }

    private void avroHeadersToExchangeHeaders(Map<CharSequence, ByteBuffer> avroHeaders) {
        Map<String, Object> currentHeaders = new HashMap<String, Object>();

        for (CharSequence key : avroHeaders.keySet()) {
            currentHeaders.put(key.toString(), avroHeaders.get(key));
        }
        headerStorage.set(currentHeaders);
    }
}
