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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.util.ByteBufferInputStream;
import org.apache.camel.Converter;

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
            } while (read != length);
            buffers.add(ByteBuffer.wrap(chunk));
        }
    }

    @Converter
    public InputStream listOfBuffersToInputStream(List<ByteBuffer> buffers) throws IOException {
        return new ByteBufferInputStream(buffers);
    }
}
