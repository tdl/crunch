/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.crunch.io.text.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;

import com.google.common.io.Closeables;

/**
 * An {@code Iterator} for an internally created {@code CSVLineReader}
 */
public class CSVRecordIterator implements Iterator<String>, Closeable {
  private CSVLineReader csvLineReader;
  private InputStream inputStream;
  private String currentLine;

  /**
   * Creates an instance of {@code CSVRecordIterator} with default configuration
   * 
   * @param inputStream
   *          The {@code InputStream} for the CSV file to iterate over
   * @throws UnsupportedEncodingException
   */
  public CSVRecordIterator(final InputStream inputStream) throws UnsupportedEncodingException {
    this(inputStream, CSVLineReader.DEFAULT_BUFFER_SIZE, CSVLineReader.DEFAULT_INPUT_FILE_ENCODING,
        CSVLineReader.DEFAULT_QUOTE_CHARACTER, CSVLineReader.DEFAULT_QUOTE_CHARACTER,
        CSVLineReader.DEFAULT_ESCAPE_CHARACTER);
  }

  /**
   * Creates an instance of {@code CSVRecordIterator} with custom configuration
   * 
   * @param inputStream
   *          The {@code InputStream} for the CSV file to iterate over
   * @param bufferSize
   *          The size of the buffer used when reading the input stream
   * @param inputFileEncoding
   *          the encoding for the input file
   * @param openQuote
   *          the character to use to open quote blocks
   * @param closeQuote
   *          the character to use to close quote blocks
   * @param escape
   *          the character to use for escaping control characters and quotes
   * @throws UnsupportedEncodingException
   */
  public CSVRecordIterator(final InputStream inputStream, final int bufferSize, final String inputFileEncoding,
      final char openQuoteChar, final char closeQuoteChar, final char escapeChar) throws UnsupportedEncodingException {
    csvLineReader = new CSVLineReader(inputStream, bufferSize, inputFileEncoding, openQuoteChar, closeQuoteChar,
        escapeChar);
    this.inputStream = inputStream;
    incrementValue();
  }

  @Override
  public boolean hasNext() {
    if (!(currentLine == null)) {
      return true;
    }
    Closeables.closeQuietly(this);
    return false;
  }

  @Override
  public String next() {
    String result = currentLine;
    incrementValue();
    return result;
  }

  @Override
  public void remove() {
    incrementValue();
  }

  private void incrementValue() {
    Text tempText = new Text();
    try {
      csvLineReader.readCSVLine(tempText);
    } catch (IOException e) {
      throw new RuntimeException("A problem occurred accessing the underlying CSV file stream.", e);
    }
    String tempTextAsString = tempText.toString();
    if ("".equals(tempTextAsString)) {
      currentLine = null;
    } else {
      currentLine = tempTextAsString;
    }
  }

  @Override
  public void close() throws IOException {
    if (inputStream != null) {
      inputStream.close();
      inputStream = null;
    }
  }
}