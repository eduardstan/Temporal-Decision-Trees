package demos;

import timeseries.functions.*;
import timeseries.functions.interval.*;

public class DemoFunctions {

	public static void main(String[] argv) {
		
		double[] values = {1,2,3,4,5,6,7,8,9,10};
		Functions functions = new Functions();
		functions.add(new Mean());
	}
}
