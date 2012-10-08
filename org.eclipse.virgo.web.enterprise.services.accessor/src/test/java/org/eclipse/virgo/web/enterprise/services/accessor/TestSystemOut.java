package org.eclipse.virgo.web.enterprise.services.accessor;

import java.util.ArrayList;
import java.util.List;
import java.io.PrintStream;

public class TestSystemOut extends PrintStream {
	private List<Object> output = new ArrayList<Object>();
	
	public TestSystemOut () {
		super(System.out);
	}
	
	@Override
	public void print(String o) {
		output.add(o);
	}
	
	@Override
	public void print(long o) {
		output.add(o);
	}

	@Override
	public void println(String o) {
		output.add(o);
	}
	
	@Override
	public void println() {
		//no-op
	}
	
	public List<Object> getOutput() {
		return output;
	}

}
