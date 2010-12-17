package org.apache.camel.component.avro;

import org.apache.avro.ipc.ByteBufferInputStream;
import org.apache.camel.Converter;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Converter
public class AvroTypeConverter {

    @Converter
    public List<ByteBuffer> inputStreamToListOfBuffers(InputStream in) throws IOException {
        // Its a pity we can't get the underlying array from the InputStreamCache
        // as then we could more efficiently populate the Buffers list by wrapping it
        // in a byte buffer and slicing the result.
        DataInputStream din = new DataInputStream(in);
        List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
        while (true) {
            int length = din.readInt();
            if (length == 0) { // end of buffers
                return buffers;
            }
            
            int read = 0;
            byte[] chunk = new byte[length];
            do {
                read += in.read(chunk, read, length - read);
            }while(read != length);
            buffers.add(ByteBuffer.wrap(chunk));       
        }
    }
    
    @Converter
    public InputStream ListOfBuffersToInputStream(List<ByteBuffer> buffers) throws IOException {
        return new ByteBufferInputStream(buffers);
    }
}

