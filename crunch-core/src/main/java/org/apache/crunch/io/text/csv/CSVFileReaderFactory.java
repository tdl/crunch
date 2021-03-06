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

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.crunch.io.FileReaderFactory;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.google.common.collect.Iterators;

/**
 * The {@code FileReaderFactory} instance that is responsible for building a
 * {@code CSVRecordIterator}
 */
public class CSVFileReaderFactory implements FileReaderFactory<String> {
  private static final Log LOG = LogFactory.getLog(CSVFileReaderFactory.class);
  private int bufferSize;
  private String inputFileEncoding;
  private char openQuoteChar;
  private char closeQuoteChar;
  private char escapeChar;

  /**
   * Creates a new {@code CSVFileReaderFactory} instance with default
   * configuration
   */
  CSVFileReaderFactory() {
    this(CSVLineReader.DEFAULT_BUFFER_SIZE, CSVLineReader.DEFAULT_INPUT_FILE_ENCODING,
        CSVLineReader.DEFAULT_QUOTE_CHARACTER, CSVLineReader.DEFAULT_QUOTE_CHARACTER,
        CSVLineReader.DEFAULT_ESCAPE_CHARACTER);
  }

  /**
   * Creates a new {@code CSVFileReaderFactory} instance with custon
   * configuration
   * 
   * @param bufferSize
   *          The size of the buffer to be used in the underlying
   *          {@code CSVLineReader}
   * @param inputFileEncoding
   *          The the encoding of the input file to be read by the underlying
   *          {@code CSVLineReader}
   * @param openQuoteChar
   *          The character representing the quote character to be used in the
   *          underlying {@code CSVLineReader}
   * @param closeQuoteChar
   *          The character representing the quote character to be used in the
   *          underlying {@code CSVLineReader}
   * @param escapeChar
   *          The character representing the escape character to be used in the
   *          underlying {@code CSVLineReader}
   */
  CSVFileReaderFactory(final int bufferSize, final String inputFileEncoding, final char openQuoteChar,
      final char closeQuoteChar, final char escapeChar) {
    this.bufferSize = bufferSize;
    this.inputFileEncoding = inputFileEncoding;
    this.openQuoteChar = openQuoteChar;
    this.closeQuoteChar = closeQuoteChar;
    this.escapeChar = escapeChar;
  }

  @Override
  public Iterator<String> read(FileSystem fs, Path path) {
    FSDataInputStream is;
    try {
      is = fs.open(path);
      return new CSVRecordIterator(is, bufferSize, inputFileEncoding, openQuoteChar, closeQuoteChar, escapeChar);
    } catch (IOException e) {
      LOG.info("Could not read path: " + path, e);
      return Iterators.emptyIterator();
    }
  }
}