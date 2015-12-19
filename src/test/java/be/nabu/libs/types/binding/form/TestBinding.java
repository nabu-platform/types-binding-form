package be.nabu.libs.types.binding.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;

import junit.framework.TestCase;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.form.MyClass.MyNestedClass;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.libs.types.java.BeanType;

public class TestBinding extends TestCase {
	
	public void testBinding() throws IOException, ParseException {
		MyClass instance = new MyClass();
		instance.setSomething(5);
		instance.getOtherThings().add(6l);
		instance.getOtherThings().add(7l);
		
		MyNestedClass nestedInstance = new MyNestedClass();
		nestedInstance.setTest("haha& a/test!");
		nestedInstance.getMoreTests().add("ok, another test");
		nestedInstance.getMoreTests().add("how about spécial stuff?#Hmm?");
		
		instance.setNestedThing(nestedInstance);
		
		FormBinding formBinding = new FormBinding(new BeanType<MyClass>(MyClass.class));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		formBinding.marshal(output, new BeanInstance<MyClass>(instance));
		assertEquals(
			"something=5&otherThings=6&otherThings=7&nestedThing.test=haha%26%20a%2Ftest!&nestedThing.moreTests=ok,%20another%20test&nestedThing.moreTests=how%20about%20spécial%20stuff%3F%23Hmm%3F",
			new String(output.toByteArray(), "UTF-8")
		);
		
		ComplexContent unmarshal = formBinding.unmarshal(new ByteArrayInputStream(output.toByteArray()), new Window[0]);
		MyClass unmarshalled = TypeUtils.getAsBean(unmarshal, MyClass.class);
		assertEquals(instance, unmarshalled);
	}
}
