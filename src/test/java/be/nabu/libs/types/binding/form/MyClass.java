package be.nabu.libs.types.binding.form;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "something", "otherThings", "nestedThing" })
public class MyClass {

	private Integer something;
	private List<Long> otherThings = new ArrayList<Long>();
	private MyNestedClass nestedThing;
	
	public MyNestedClass getNestedThing() {
		return nestedThing;
	}
	public void setNestedThing(MyNestedClass nestedThing) {
		this.nestedThing = nestedThing;
	}
	public Integer getSomething() {
		return something;
	}
	public void setSomething(Integer something) {
		this.something = something;
	}
	public List<Long> getOtherThings() {
		return otherThings;
	}
	public void setOtherThings(List<Long> otherThings) {
		this.otherThings = otherThings;
	}
	
	@Override
	public boolean equals(Object object) {
		// we don't care about null safety for this test...
		return object instanceof MyClass
			&& ((MyClass) object).something.equals(something)
			&& ((MyClass) object).otherThings.equals(otherThings)
			&& ((MyClass) object).nestedThing.equals(nestedThing);
	}
	
	@XmlType(propOrder = { "test", "moreTests" })
	public static class MyNestedClass {
		private String test;
		private List<String> moreTests = new ArrayList<String>();
		public String getTest() {
			return test;
		}
		public void setTest(String test) {
			this.test = test;
		}
		public List<String> getMoreTests() {
			return moreTests;
		}
		public void setMoreTests(List<String> moreTests) {
			this.moreTests = moreTests;
		}
		
		@Override
		public boolean equals(Object object) {
			return object instanceof MyNestedClass
				&& ((MyNestedClass) object).test.equals(test)
				&& ((MyNestedClass) object).moreTests.equals(moreTests);
		}
	}
}

