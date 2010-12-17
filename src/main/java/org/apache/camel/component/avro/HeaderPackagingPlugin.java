package org.apache.camel.component.avro;

import org.apache.avro.ipc.RPCContext;
import org.apache.avro.ipc.RPCPlugin;
import org.apache.camel.TypeConverter;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

class HeaderPackagingPlugin extends RPCPlugin {

    private static ThreadLocal<Map<String, Object>> headerStorage = new ThreadLocal<Map<String, Object>>();
    private TypeConverter typeConverter;

    HeaderPackagingPlugin(TypeConverter typeConverter){
        this.typeConverter = typeConverter;
    }

    public static void setExchangeHeaders(Map<String, Object> headers){
        headerStorage.set(headers);
    }

    public static Map<String, Object>  getExchangeHeaders(){
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

    private void avroHeadersToExchangeHeaders( Map<CharSequence, ByteBuffer> avroHeaders) {
        Map<String, Object> currentHeaders = new HashMap<String, Object>();

        for (CharSequence key : avroHeaders.keySet()) {
            currentHeaders.put(key.toString(), avroHeaders.get(key));
        }
        headerStorage.set(currentHeaders);
    }
}