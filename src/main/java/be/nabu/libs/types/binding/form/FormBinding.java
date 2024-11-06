/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.types.binding.form;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.converter.api.Converter;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.resources.URIUtils;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.binding.BaseTypeBinding;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;

public class FormBinding extends BaseTypeBinding {

	private Converter converter = ConverterFactory.getInstance().getConverter();
	private Charset charset;
	private ComplexType type;

	public FormBinding(ComplexType type) {
		this(type, Charset.defaultCharset());
	}
	
	public FormBinding(ComplexType type, Charset charset) {
		this.charset = charset;
		this.type = type;
	}
	
	@Override
	public void marshal(OutputStream output, ComplexContent content, Value<?>...values) throws IOException {
		Writer writer = new OutputStreamWriter(output, charset);
		marshal(writer, content, null, true);
		writer.flush();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean marshal(Writer writer, ComplexContent content, String path, boolean first) throws IOException {
		for (Element<?> element : TypeUtils.getAllChildren(path == null ? type : content.getType())) {
			Object object = content.get(element.getName());
			if (object != null) {
				boolean isList = element.getType().isList(element.getProperties());
				CollectionHandlerProvider handler = isList ? CollectionHandlerFactory.getInstance().getHandler().getHandler(object.getClass()) : null;
				String childPath = path == null ? element.getName() : path + "." + element.getName();
				if (element.getType() instanceof SimpleType) {
					if (isList) {
						for (Object single : handler.getAsCollection(object)) {
							if (first) {
								first = false;
							}
							else {
								writer.write("&");
							}
							writer.write(childPath + "=" + URIUtils.encodeURIComponent(converter.convert(single, String.class)));	
						}
					}
					else {
						if (first) {
							first = false;
						}
						else {
							writer.write("&");
						}
						writer.write(childPath + "=" + URIUtils.encodeURIComponent(converter.convert(object, String.class)));
					}
				}
				else {
					if (isList) {
						int counter = 0;
						for (Object single : handler.getAsCollection(object)) {
							if (!(single instanceof ComplexContent)) {
								single = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(single);
							}
							// for complex types we add indexes, we don't do this for simple types
							first &= marshal(writer, (ComplexContent) single, childPath + "[" + counter++ + "]", first);
						}
					}
					else {
						if (!(object instanceof ComplexContent)) {
							object = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(object);
						}
						first &= marshal(writer, (ComplexContent) object, childPath, first);
					}
				}
			}
		}
		return first;
	}

	@Override
	protected ComplexContent unmarshal(ReadableResource resource, Window[] windows, Value<?>...values) throws IOException, ParseException {
		ReadableContainer<ByteBuffer> readable = resource.getReadable();
		try {
			ComplexContent content = type.newInstance();
			String string = new String(IOUtils.toBytes(readable), charset);
			Map<String, Integer> indexes = new HashMap<String, Integer>();
			for (String part : string.split("&(?!amp;)")) {
				int index = part.indexOf('=');
				String name = index > 0 ? part.substring(0, index) : part;
				String value = index > 0 ? URIUtils.decodeURIComponent(part.substring(index + 1)) : "true";
				// the name can contain brackets for lists of complex types
				String typePath = name.replaceAll("\\[[^\\]]+\\]", "").replace(".", "/");
				Element<?> target = type.get(typePath);
				// if the element does not exist, we assume you are not interested
				if (target != null) {
					if (target.getType().isList(target.getProperties())) {
						Integer newIndex = indexes.get(name);
						if (newIndex == null) {
							newIndex = 0;
							indexes.put(name, 1);
						}
						else {
							indexes.put(name, newIndex + 1);
						}
						name += "[" + newIndex + "]";
					}
					content.set(name.replace(".", "/"), value);
				}
			}
			return content;
		}
		finally {
			readable.close();
		}
	}

}
