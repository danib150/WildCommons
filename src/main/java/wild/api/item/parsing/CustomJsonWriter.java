/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package wild.api.item.parsing;
/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.*;
import java.util.Map;

/**
 * Writes a JSON (<a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>)
 * encoded value to a stream, one token at a time. The stream includes both
 * literal values (strings, numbers, booleans and nulls) as well as the begin
 * and end delimiters of objects and arrays.
 *
 * <h3>Encoding JSON</h3>
 * To encode your data as JSON, create a new {@code CustomJsonWriter}. Each JSON
 * document must contain one top-level array or object. Call methods on the
 * writer as you walk the structure's contents, nesting arrays and objects as
 * necessary:
 * <ul>
 *   <li>To write <strong>arrays</strong>, first call {@link #beginArray()}.
 *       Write each of the array's elements with the appropriate {@link #value}
 *       methods or by nesting other arrays and objects. Finally close the array
 *       using {@link #endArray()}.
 *   <li>To write <strong>objects</strong>, first call {@link #beginObject()}.
 *       Write each of the object's properties by alternating calls to
 *       {@link #name} with the property's value. Write property values with the
 *       appropriate {@link #value} method or by nesting other objects or arrays.
 *       Finally close the object using {@link #endObject()}.
 * </ul>
 *
 * <h3>Example</h3>
 * Suppose we'd like to encode a stream of messages such as the following: <pre> {@code
 * [
 *   {
 *     "id": 912345678901,
 *     "text": "How do I stream JSON in Java?",
 *     "geo": null,
 *     "user": {
 *       "name": "json_newb",
 *       "followers_count": 41
 *      }
 *   },
 *   {
 *     "id": 912345678902,
 *     "text": "@json_newb just use CustomJsonWriter!",
 *     "geo": [50.454722, -104.606667],
 *     "user": {
 *       "name": "jesse",
 *       "followers_count": 2
 *     }
 *   }
 * ]}</pre>
 * This code encodes the above structure: <pre>   {@code
 *   public void writeJsonStream(OutputStream out, List<Message> messages) throws IOException {
 *     CustomJsonWriter writer = new CustomJsonWriter(new OutputStreamWriter(out, "UTF-8"));
 *     writer.setIndent("    ");
 *     writeMessagesArray(writer, messages);
 *     writer.close();
 *   }
 *
 *   public void writeMessagesArray(JsonWriter writer, List<Message> messages) throws IOException {
 *     writer.beginArray();
 *     for (Message message : messages) {
 *       writeMessage(writer, message);
 *     }
 *     writer.endArray();
 *   }
 *
 *   public void writeMessage(JsonWriter writer, Message message) throws IOException {
 *     writer.beginObject();
 *     writer.name("id").value(message.getId());
 *     writer.name("text").value(message.getText());
 *     if (message.getGeo() != null) {
 *       writer.name("geo");
 *       writeDoublesArray(writer, message.getGeo());
 *     } else {
 *       writer.name("geo").nullValue();
 *     }
 *     writer.name("user");
 *     writeUser(writer, message.getUser());
 *     writer.endObject();
 *   }
 *
 *   public void writeUser(JsonWriter writer, User user) throws IOException {
 *     writer.beginObject();
 *     writer.name("name").value(user.getName());
 *     writer.name("followers_count").value(user.getFollowersCount());
 *     writer.endObject();
 *   }
 *
 *   public void writeDoublesArray(JsonWriter writer, List<Double> doubles) throws IOException {
 *     writer.beginArray();
 *     for (Double value : doubles) {
 *       writer.value(value);
 *     }
 *     writer.endArray();
 *   }}</pre>
 *
 * <p>Each {@code CustomJsonWriter} may be used to write a single JSON stream.
 * Instances of this class are not thread safe. Calls that would result in a
 * malformed JSON string will fail with an {@link IllegalStateException}.
 *
 * @author Jesse Wilson
 * @since 1.6
 */
public class CustomJsonWriter implements Closeable, Flushable {
	
	protected static final String toString(JsonObject json) {
		try {
			StringWriter stringWriter = new StringWriter();
			wild.api.item.parsing.CustomJsonWriter jsonWriter = new wild.api.item.parsing.CustomJsonWriter(stringWriter);
			jsonWriter.setLenient(true);
			write(jsonWriter, json);
			return stringWriter.toString();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	protected static void write(wild.api.item.parsing.CustomJsonWriter out, JsonElement value) throws IOException {
		if (value == null || value.isJsonNull()) {
			out.nullValue();
		} else if (value.isJsonPrimitive()) {
			JsonPrimitive primitive = value.getAsJsonPrimitive();
			if (primitive.isNumber()) {
				out.value(primitive.getAsNumber());
			} else if (primitive.isBoolean()) {
				out.value(primitive.getAsBoolean());
			} else {
				out.value(primitive.getAsString());
			}

		} else if (value.isJsonArray()) {
			out.beginArray();
			for (JsonElement e : value.getAsJsonArray()) {
				write(out, e);
			}
			out.endArray();

		} else if (value.isJsonObject()) {
			out.beginObject();
			for (Map.Entry<String, JsonElement> e : value.getAsJsonObject().entrySet()) {
				out.name(e.getKey());
				write(out, e.getValue());
			}
			out.endObject();

		} else {
			throw new IllegalArgumentException("Couldn't write " + value.getClass());
		}
	}

	/*
	 * From RFC 4627, "All Unicode characters may be placed within the
	 * quotation marks except for the characters that must be escaped:
	 * quotation mark, reverse solidus, and the control characters
	 * (U+0000 through U+001F)."
	 *
	 * We also escape '\u2028' and '\u2029', which JavaScript interprets as
	 * newline characters. This prevents eval() from failing with a syntax
	 * error. http://code.google.com/p/google-gson/issues/detail?id=341
	 */
	private static final String[] REPLACEMENT_CHARS;
	private static final String[] HTML_SAFE_REPLACEMENT_CHARS;
	static {
		REPLACEMENT_CHARS = new String[128];
		for (int i = 0; i <= 0x1f; i++) {
			REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
		}
		REPLACEMENT_CHARS['"'] = "\\\"";
		REPLACEMENT_CHARS['\\'] = "\\\\";
		REPLACEMENT_CHARS['\t'] = "\\t";
		REPLACEMENT_CHARS['\b'] = "\\b";
		REPLACEMENT_CHARS['\n'] = "\\n";
		REPLACEMENT_CHARS['\r'] = "\\r";
		REPLACEMENT_CHARS['\f'] = "\\f";
		HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
		HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
		HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
		HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
		HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
		HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
	}

	/** The output data, containing at most one top-level array or object. */
	private final Writer out;

	private int[] stack = new int[32];
	private int stackSize = 0;
	{
		push(JsonScope.EMPTY_DOCUMENT);
	}

	/**
	 * A string containing a full set of spaces for a single level of
	 * indentation, or null for no pretty printing.
	 */
	private String indent;

	/**
	 * The name/value separator; either ":" or ": ".
	 */
	private String separator = ":";

	private boolean lenient;

	private boolean htmlSafe;

	private String deferredName;

	private boolean serializeNulls = true;

	/**
	 * Creates a new instance that writes a JSON-encoded stream to {@code out}.
	 * For best performance, ensure {@link Writer} is buffered; wrapping in
	 * {@link java.io.BufferedWriter BufferedWriter} if necessary.
	 */
	protected CustomJsonWriter(Writer out) {
		if (out == null) {
			throw new NullPointerException("out == null");
		}
		this.out = out;
	}

	/**
	 * Sets the indentation string to be repeated for each level of indentation
	 * in the encoded document. If {@code indent.isEmpty()} the encoded document
	 * will be compact. Otherwise the encoded document will be more
	 * human-readable.
	 *
	 * @param indent a string containing only whitespace.
	 */
	public final void setIndent(String indent) {
		if (indent.length() == 0) {
			this.indent = null;
			this.separator = ":";
		} else {
			this.indent = indent;
			this.separator = ": ";
		}
	}

	/**
	 * Configure this writer to relax its syntax rules. By default, this writer
	 * only emits well-formed JSON as specified by <a
	 * href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>. Setting the writer
	 * to lenient permits the following:
	 * <ul>
	 *   <li>Top-level values of any type. With strict writing, the top-level
	 *       value must be an object or an array.
	 *   <li>Numbers may be {@link Double#isNaN() NaNs} or {@link
	 *       Double#isInfinite() infinities}.
	 * </ul>
	 */
	public final void setLenient(boolean lenient) {
		this.lenient = lenient;
	}

	/**
	 * Returns true if this writer has relaxed syntax rules.
	 */
	public boolean isLenient() {
		return lenient;
	}

	/**
	 * Configure this writer to emit JSON that's safe for direct inclusion in HTML
	 * and XML documents. This escapes the HTML characters {@code <}, {@code >},
	 * {@code &} and {@code =} before writing them to the stream. Without this
	 * setting, your XML/HTML encoder should replace these characters with the
	 * corresponding escape sequences.
	 */
	public final void setHtmlSafe(boolean htmlSafe) {
		this.htmlSafe = htmlSafe;
	}

	/**
	 * Returns true if this writer writes JSON that's safe for inclusion in HTML
	 * and XML documents.
	 */
	public final boolean isHtmlSafe() {
		return htmlSafe;
	}

	/**
	 * Sets whether object members are serialized when their value is null.
	 * This has no impact on array elements. The default is true.
	 */
	public final void setSerializeNulls(boolean serializeNulls) {
		this.serializeNulls = serializeNulls;
	}

	/**
	 * Returns true if object members are serialized when their value is null.
	 * This has no impact on array elements. The default is true.
	 */
	public final boolean getSerializeNulls() {
		return serializeNulls;
	}

	/**
	 * Begins encoding a new array. Each call to this method must be paired with
	 * a call to {@link #endArray}.
	 *
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter beginArray() throws IOException {
		writeDeferredName();
		return open(JsonScope.EMPTY_ARRAY, "[");
	}

	/**
	 * Ends encoding the current array.
	 *
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter endArray() throws IOException {
		return close(JsonScope.EMPTY_ARRAY, JsonScope.NONEMPTY_ARRAY, "]");
	}

	/**
	 * Begins encoding a new object. Each call to this method must be paired
	 * with a call to {@link #endObject}.
	 *
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter beginObject() throws IOException {
		writeDeferredName();
		return open(JsonScope.EMPTY_OBJECT, "{");
	}

	/**
	 * Ends encoding the current object.
	 *
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter endObject() throws IOException {
		return close(JsonScope.EMPTY_OBJECT, JsonScope.NONEMPTY_OBJECT, "}");
	}

	/**
	 * Enters a new scope by appending any necessary whitespace and the given
	 * bracket.
	 */
	private wild.api.item.parsing.CustomJsonWriter open(int empty, String openBracket) throws IOException {
		beforeValue(true);
		push(empty);
		out.write(openBracket);
		return this;
	}

	/**
	 * Closes the current scope by appending any necessary whitespace and the
	 * given bracket.
	 */
	private wild.api.item.parsing.CustomJsonWriter close(int empty, int nonempty, String closeBracket) throws IOException {
		int context = peek();
		if (context != nonempty && context != empty) {
			throw new IllegalStateException("Nesting problem.");
		}
		if (deferredName != null) {
			throw new IllegalStateException("Dangling name: " + deferredName);
		}

		stackSize--;
		if (context == nonempty) {
			newline();
		}
		out.write(closeBracket);
		return this;
	}

	private void push(int newTop) {
		if (stackSize == stack.length) {
			int[] newStack = new int[stackSize * 2];
			System.arraycopy(stack, 0, newStack, 0, stackSize);
			stack = newStack;
		}
		stack[stackSize++] = newTop;
	}

	/**
	 * Returns the value on the top of the stack.
	 */
	private int peek() {
		if (stackSize == 0) {
			throw new IllegalStateException("JsonWriter is closed.");
		}
		return stack[stackSize - 1];
	}

	/**
	 * Replace the value on the top of the stack with the given value.
	 */
	private void replaceTop(int topOfStack) {
		stack[stackSize - 1] = topOfStack;
	}

	/**
	 * Encodes the property name.
	 *
	 * @param name the name of the forthcoming value. May not be null.
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter name(String name) throws IOException {
		if (name == null) {
			throw new NullPointerException("name == null");
		}
		if (deferredName != null) {
			throw new IllegalStateException();
		}
		if (stackSize == 0) {
			throw new IllegalStateException("JsonWriter is closed.");
		}
		deferredName = name;
		return this;
	}

	private void writeDeferredName() throws IOException {
		if (deferredName != null) {
			beforeName();
			string(deferredName, true);
			deferredName = null;
		}
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @param value the literal string value, or null to encode a null literal.
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter value(String value) throws IOException {
		if (value == null) {
			return nullValue();
		}
		writeDeferredName();
		beforeValue(false);
		string(value, false);
		return this;
	}

	/**
	 * Writes {@code value} directly to the writer without quoting or
	 * escaping.
	 *
	 * @param value the literal string value, or null to encode a null literal.
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter jsonValue(String value) throws IOException {
		if (value == null) {
			return nullValue();
		}
		writeDeferredName();
		beforeValue(false);
		out.append(value);
		return this;
	}

	/**
	 * Encodes {@code null}.
	 *
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter nullValue() throws IOException {
		if (deferredName != null) {
			if (serializeNulls) {
				writeDeferredName();
			} else {
				deferredName = null;
				return this; // skip the name and the value
			}
		}
		beforeValue(false);
		out.write("null");
		return this;
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter value(boolean value) throws IOException {
		writeDeferredName();
		beforeValue(false);
		out.write(value ? "true" : "false");
		return this;
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
	 *     {@link Double#isInfinite() infinities}.
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter value(double value) throws IOException {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
		}
		writeDeferredName();
		beforeValue(false);
		out.append(Double.toString(value));
		return this;
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter value(long value) throws IOException {
		writeDeferredName();
		beforeValue(false);
		out.write(Long.toString(value));
		return this;
	}

	/**
	 * Encodes {@code value}.
	 *
	 * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
	 *     {@link Double#isInfinite() infinities}.
	 * @return this writer.
	 */
	public wild.api.item.parsing.CustomJsonWriter value(Number value) throws IOException {
		if (value == null) {
			return nullValue();
		}

		writeDeferredName();
		String string = value.toString();
		if (!lenient && (string.equals("-Infinity") || string.equals("Infinity") || string.equals("NaN"))) {
			throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
		}
		beforeValue(false);
		out.append(string);
		return this;
	}

	/**
	 * Ensures all buffered data is written to the underlying {@link Writer}
	 * and flushes that writer.
	 */
	public void flush() throws IOException {
		if (stackSize == 0) {
			throw new IllegalStateException("JsonWriter is closed.");
		}
		out.flush();
	}

	/**
	 * Flushes and closes this writer and the underlying {@link Writer}.
	 *
	 * @throws IOException if the JSON document is incomplete.
	 */
	public void close() throws IOException {
		out.close();

		int size = stackSize;
		if (size > 1 || size == 1 && stack[size - 1] != JsonScope.NONEMPTY_DOCUMENT) {
			throw new IOException("Incomplete document");
		}
		stackSize = 0;
	}

	private void string(String value, boolean omitQuotes) throws IOException {
		String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
		if (!omitQuotes) {
			out.write("\"");
		}
		int last = 0;
		int length = value.length();
		for (int i = 0; i < length; i++) {
			char c = value.charAt(i);
			String replacement;
			if (c < 128) {
				replacement = replacements[c];
				if (replacement == null) {
					continue;
				}
			} else if (c == '\u2028') {
				replacement = "\\u2028";
			} else if (c == '\u2029') {
				replacement = "\\u2029";
			} else {
				continue;
			}
			if (last < i) {
				out.write(value, last, i - last);
			}
			out.write(replacement);
			last = i + 1;
		}
		if (last < length) {
			out.write(value, last, length - last);
		}
		if (!omitQuotes) {
			out.write("\"");
		}
	}

	private void newline() throws IOException {
		if (indent == null) {
			return;
		}

		out.write("\n");
		for (int i = 1, size = stackSize; i < size; i++) {
			out.write(indent);
		}
	}

	/**
	 * Inserts any necessary separators and whitespace before a name. Also
	 * adjusts the stack to expect the name's value.
	 */
	private void beforeName() throws IOException {
		int context = peek();
		if (context == JsonScope.NONEMPTY_OBJECT) { // first in object
			out.write(',');
		} else if (context != JsonScope.EMPTY_OBJECT) { // not in an object!
			throw new IllegalStateException("Nesting problem.");
		}
		newline();
		replaceTop(JsonScope.DANGLING_NAME);
	}

	/**
	 * Inserts any necessary separators and whitespace before a literal value,
	 * inline array, or inline object. Also adjusts the stack to expect either a
	 * closing bracket or another element.
	 *
	 * @param root true if the value is a new array or object, the two values
	 *     permitted as top-level elements.
	 */
	@SuppressWarnings("fallthrough")
	private void beforeValue(boolean root) throws IOException {
		switch (peek()) {
			case JsonScope.NONEMPTY_DOCUMENT:
				if (!lenient) {
					throw new IllegalStateException("JSON must have only one top-level value.");
				}
				// fall-through
			case JsonScope.EMPTY_DOCUMENT: // first in document
				if (!lenient && !root) {
					throw new IllegalStateException("JSON must start with an array or an object.");
				}
				replaceTop(JsonScope.NONEMPTY_DOCUMENT);
				break;

			case JsonScope.EMPTY_ARRAY: // first in array
				replaceTop(JsonScope.NONEMPTY_ARRAY);
				newline();
				break;

			case JsonScope.NONEMPTY_ARRAY: // another in array
				out.append(',');
				newline();
				break;

			case JsonScope.DANGLING_NAME: // value for name
				out.append(separator);
				replaceTop(JsonScope.NONEMPTY_OBJECT);
				break;

			default:
				throw new IllegalStateException("Nesting problem.");
		}
	}

	static final class JsonScope {
		static final int EMPTY_ARRAY = 1;
		static final int NONEMPTY_ARRAY = 2;
		static final int EMPTY_OBJECT = 3;
		static final int DANGLING_NAME = 4;
		static final int NONEMPTY_OBJECT = 5;
		static final int EMPTY_DOCUMENT = 6;
		static final int NONEMPTY_DOCUMENT = 7;
		static final int CLOSED = 8;
	}

}
